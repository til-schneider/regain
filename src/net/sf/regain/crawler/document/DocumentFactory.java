/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004  Til Schneider
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Til Schneider, info@murfman.de
 *
 * CVS information:
 *  $RCSfile: DocumentFactory.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/document/DocumentFactory.java,v $
 *     $Date: 2005/03/14 21:08:55 $
 *   $Author: til132 $
 * $Revision: 1.12 $
 */
package net.sf.regain.crawler.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.ErrorLogger;
import net.sf.regain.crawler.Profiler;
import net.sf.regain.crawler.config.AuxiliaryField;
import net.sf.regain.crawler.config.CrawlerConfig;
import net.sf.regain.crawler.config.PreparatorSettings;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Fabrik, die aus der URL und den Rohdaten eines Dokuments ein Lucene-Ducument
 * erzeugt, das nur noch den, von Formatierungen ges�uberten, Text des Dokuments,
 * sowie seine URL und seinen Titel enth�lt.
 *
 * @see Document
 * @author Til Schneider, www.murfman.de
 */
public class DocumentFactory {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(DocumentFactory.class);

  /** Die maximale L�nge der Zusammenfassung. */
  private static final int MAX_SUMMARY_LENGTH = 200;
  
  /** The crawler config. */
  private CrawlerConfig mConfig;

  /**
   * Das Verzeichnis, in dem Analyse-Dateien erzeugt werden sollen. Ist
   * <CODE>null</CODE>, wenn keine Analyse-Dateien erzeugt werden sollen.
   */
  private File mAnalysisDir = null;

  /** The preparators. */
  private Preparator[] mPreparatorArr;

  /** Die Profiler, die die Bearbeitung durch die Pr�paratoren messen. */
  private Profiler[] mPreparatorProfilerArr;

  /**
   * Die regul�ren Ausdr�cke, auf die die URL eines Dokuments passen muss,
   * damit anstatt des wirklichen Dokumententitels der Text des Links, der auf
   * das Dokument gezeigt hat, als Dokumententitel genutzt wird.
   */
  private RE[] mUseLinkTextAsTitleReArr;

  /** Der Profiler der das Hinzuf�gen zum Index mi�t. */
  private Profiler mWriteAnalysisProfiler
    = new Profiler("Writing Analysis files", "files");


  /**
   * Creates a new instance of DocumentFactory.
   * 
   * @param config The crawler configuration.
   * @param analysisDir The directory where to store the analysis files. Is
   *        <code>null</code> if no analysis files should be created.
   *
   * @throws RegainException If a preparator could not be created or if a regex
   *         has a syntax error. 
   */
  public DocumentFactory(CrawlerConfig config, File analysisDir)
    throws RegainException
  {
    mConfig = config;
    mAnalysisDir = analysisDir;

    // Create the preparators
    try {
      PreparatorSettings[] prepConf = config.getPreparatorSettingsList();
      mPreparatorArr = PreparatorFactory.getInstance().createPreparatorArr(prepConf);
    }
    catch (RegainException exc) {
      throw new RegainException("Creating the document preparators failed", exc);
    }

    // Create a profiler for each preparator
    mPreparatorProfilerArr = new Profiler[mPreparatorArr.length];
    for (int i = 0; i < mPreparatorProfilerArr.length; i++) {
      String name = mPreparatorArr[i].getClass().getName();
      mPreparatorProfilerArr[i] = new Profiler("Preparator " + name, "docs");
    }

    String[] useLinkTextAsTitleRegexArr = config.getUseLinkTextAsTitleRegexList();
    if (useLinkTextAsTitleRegexArr == null) {
      mUseLinkTextAsTitleReArr = new RE[0];
    } else {
      mUseLinkTextAsTitleReArr = new RE[useLinkTextAsTitleRegexArr.length];
      for (int i = 0; i < useLinkTextAsTitleRegexArr.length; i++) {
        try {
          mUseLinkTextAsTitleReArr[i] = new RE(useLinkTextAsTitleRegexArr[i]);
        }
        catch (RESyntaxException exc) {
          throw new RegainException("Regular expression of "
            + "use-link-text-as-title-pattern #" + i + " has wrong syntax '"
            + useLinkTextAsTitleRegexArr[i] + "'");
        }
      }
    }
  }


  /**
   * Creates a lucene {@link Document} from a {@link RawDocument}.
   *
   * @param rawDocument The raw document.
   * @param errorLogger The error logger to use for logging errors.
   *
   * @return The lucene document with the prepared data or <code>null</code> if
   *         the document couldn't be created.
   */
  public Document createDocument(RawDocument rawDocument, ErrorLogger errorLogger) {
    // Find the preparator that will prepare this URL
    Document doc = null;
    boolean preparatorFound = false;
    for (int i = 0; i < mPreparatorArr.length; i++) {
      if (mPreparatorArr[i].accepts(rawDocument)) {
        // This preparator can prepare this URL
        preparatorFound = true;
        
        try {
          doc = createDocument(mPreparatorArr[i], mPreparatorProfilerArr[i], rawDocument);
          break;
        }
        catch (RegainException exc) {
          errorLogger.logError("Preparing " + rawDocument.getUrl() +
              " with preparator " + mPreparatorArr[i].getClass().getName() +
              " failed", exc, false);
        }
      }
    }
    
    if (! preparatorFound) {
      mLog.info("No preparator feels responsible for " + rawDocument.getUrl());
    }

    // return the document
    return doc;
  }

  
  /**
   * Creates a lucene {@link Document} from a {@link RawDocument} using a
   * certain Preparator.
   * 
   * @param preparator The preparator to use.
   * @param preparatorProfiler The profile of the preparator.
   * @param rawDocument The raw document.
   * @return The lucene document with the prepared data.
   * @throws RegainException If creating the document failed.
   */
  private Document createDocument(Preparator preparator, Profiler preparatorProfiler,
    RawDocument rawDocument)
    throws RegainException
  {
    String url = rawDocument.getUrl();
    
    // Extract the file type specific information
    String cleanedContent;
    String title;
    String summary;
    String headlines;
    PathElement[] path;
    Map additionalFieldMap;
    if (mLog.isDebugEnabled()) {
      mLog.debug("Using preparator " + preparator.getClass().getName()
        + " for " + rawDocument);
    }
    preparatorProfiler.startMeasuring();
    try {
      preparator.prepare(rawDocument);

      cleanedContent     = preparator.getCleanedContent();
      title              = preparator.getTitle();
      summary            = preparator.getSummary();
      headlines          = preparator.getHeadlines();
      path               = preparator.getPath();
      additionalFieldMap = preparator.getAdditionalFields();

      preparator.cleanUp();

      preparatorProfiler.stopMeasuring(rawDocument.getLength());
    }
    catch (Throwable thr) {
      preparatorProfiler.abortMeasuring();
      throw new RegainException("Preparing " + url
        + " with preparator " + preparator.getClass().getName() + " failed", thr);
    }

    // Check the mandatory information
    if (cleanedContent == null) {
      throw new RegainException("Preparator " + preparator.getClass().getName()
        + " did not extract the content of " + url);
    }

    // Preparing suceed -> Create a new, empty document
    Document doc = new Document();
    
    // Create the auxiliary fields
    // NOTE: We do this at first, because if someone defined an auxiliary field
    //       having the same name as a normal field, then the field will be
    //       overriden by the normal field. This way we can be sure that the
    //       normal fields have the value we expect.
    AuxiliaryField[] auxiliaryFieldArr = mConfig.getAuxiliaryFieldList();
    if (auxiliaryFieldArr != null) {
      for (int i = 0; i < auxiliaryFieldArr.length; i++) {
        RE regex = auxiliaryFieldArr[i].getUrlRegex();
        if (regex.match(url)) {
          String fieldName = auxiliaryFieldArr[i].getFieldName();
          int regexGroup = auxiliaryFieldArr[i].getUrlRegexGroup();
          
          if (mLog.isDebugEnabled()) {
            mLog.debug("Adding auxiliary field: " + fieldName + "=" + regex.getParen(regexGroup));
          }
          doc.add(Field.Keyword(fieldName, regex.getParen(regexGroup)));
        }
      }
    }

    // Add the URL of the document
    doc.add(Field.Keyword("url", url));

    // Add the document's size
    int size = rawDocument.getLength();
    doc.add(Field.UnIndexed("size", Integer.toString(size)));

    // Add last modified
    Date lastModified = rawDocument.getLastModified();
    if (lastModified == null) {
      // We don't know when the document was last modified
      // -> Take the current time
      lastModified = new Date();
    }
    String lastModifiedAsString = RegainToolkit.lastModifiedToString(lastModified);
    doc.add(Field.UnIndexed("last-modified", lastModifiedAsString));

    // Write the raw content to an analysis file
    writeContentAnalysisFile(rawDocument);
    
    // Add the additional fields
    if (additionalFieldMap != null) {
      Iterator iter = additionalFieldMap.keySet().iterator();
      while (iter.hasNext()) {
        String fieldName = (String) iter.next();
        String fieldValue = (String) additionalFieldMap.get(fieldName);
        doc.add(Field.Text(fieldName, fieldValue));
      }
    }

    // Write the clean content to an analysis file
    writeAnalysisFile(url, "clean", cleanedContent);

    // Add the cleaned content of the document
    doc.add(Field.UnStored("content", cleanedContent));

    // Check whether to use the link text as title
    for (int i = 0; i < mUseLinkTextAsTitleReArr.length; i++) {
      if (mUseLinkTextAsTitleReArr[i].match(url)) {
        String linkText = rawDocument.getSourceLinkText();
        if (linkText != null) {
          title = linkText;
        }
        break;
      }
    }

    // Add the document's title
    if (hasContent(title)) {
      doc.add(Field.Text("title", title));
    }

    // Add the document's summary
    if (! hasContent(summary)) {
      summary = createSummaryFromContent(cleanedContent);
    }
    if (hasContent(summary)) {
      doc.add(Field.Text("summary", summary));
    }

    // Add the document's headlines
    if (hasContent(headlines)) {
      doc.add(Field.Text("headlines", headlines));
    }

    // Add the document's path
    if (path != null) {
      String asString = pathToString(path);
      doc.add(Field.UnIndexed("path", asString));

      // Write the path to an analysis file
      writeAnalysisFile(url, "path", asString);
    }

    // return the document
    return doc;
  }



  /**
   * Gibt zur�ck, ob der String einen Inhalt hat. Dies ist der Fall, wenn er
   * weder <code>null</code> noch ein Leerstring ist.
   *
   * @param str Der zu untersuchende String
   * @return Ob der String einen Inhalt hat.
   */
  private boolean hasContent(String str) {
    return (str != null) && (str.length() != 0);
  }



  /**
   * Erzeugt eine Zusammenfassung aus dem Inhalt eines Dokuments.
   * <p>
   * Wenn keine Zusammenfassung m�glich ist, wird <code>null</code>
   * zur�ckgegeben.
   *
   * @param content Der Inhalt f�r den die Zusammenfassung erstellt werden soll.
   * @return Eine Zusammenfassung des Dokuments oder <code>null</code>, wenn
   *         keine erzeugt werden konnte.
   */
  private String createSummaryFromContent(String content) {
    int lastSpacePos = content.lastIndexOf(' ', MAX_SUMMARY_LENGTH);

    if (lastSpacePos == -1) {
      return null;
    } else {
      return content.substring(0, lastSpacePos) + "...";
    }
  }



  /**
   * Wandelt einen Pfad in einen String um.
   *
   * @param path Der Pfad
   * @return Der Pfad als String
   */
  private String pathToString(PathElement[] path) {
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < path.length; i++) {
      buffer.append(path[i].getUrl());
      buffer.append(' ');
      buffer.append(path[i].getTitle());
      buffer.append('\n');
    }

    return buffer.toString();
  }


  /**
   * Schreibt eine Ananlyse-Datei mit dem Inhalt des Roh-Dokuments.
   *
   * @param rawDocument
   */
  private void writeContentAnalysisFile(RawDocument rawDocument) {
    if (mAnalysisDir == null) {
      // Analysis is disabled -> nothing to do
      return;
    }

    File file = getAnalysisFile(rawDocument.getUrl(), null);
    mWriteAnalysisProfiler.startMeasuring();
    try {
      rawDocument.writeToFile(file);
      mWriteAnalysisProfiler.stopMeasuring(rawDocument.getLength());
    }
    catch (RegainException exc) {
      mWriteAnalysisProfiler.abortMeasuring();
      mLog.error("Writing analysis file failed", exc);
    }
  }


  /**
   * Schreibt eine Analyse-Datei.
   * <p>
   * Eine Analyse-Datei enth�lt die Daten des Dokuments bei jedem
   * Zwischenschritt der Aufbereitung. Sie hilft die Qualit�t der
   * Index-Erstellung zu pr�fen und wird in einem Unterverzeichnis im
   * Index-Verzeichnis angelegt.
   *
   * @param url Die URL des Dokuments.
   * @param extension Der Erweiterung, die die Analyse-Datei erhalten soll.
   * @param content Der Inhalt, der in die Datei geschrieben werden soll.
   */
  public void writeAnalysisFile(String url, String extension, String content) {
    if (mAnalysisDir == null) {
      // Analysis is disabled -> nothing to do
      return;
    }

    File file = getAnalysisFile(url, extension);

    if (content == null) {
      throw new NullPointerException("Content for analysis file is null: "
        + file.getAbsolutePath());
    }

    mWriteAnalysisProfiler.startMeasuring();

    FileOutputStream stream = null;
    OutputStreamWriter writer = null;
    try {
      stream = new FileOutputStream(file);
      writer = new OutputStreamWriter(stream);

      writer.write(content);

      mWriteAnalysisProfiler.stopMeasuring(content.length());
    }
    catch (IOException exc) {
      mWriteAnalysisProfiler.abortMeasuring();
      mLog.error("Writing analysis file failed", exc);
    }
    finally {
      if (writer != null) {
        try { writer.close(); } catch (IOException exc) {}
      }
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }



  /**
   * Erzeugt den Dateinamen einer Analyse-Datei.
   *
   * @param url Die URL des Dokuments.
   * @param extension Der Erweiterung, die die Analyse-Datei erhalten soll.
   *
   * @return Den Dateinamen einer Analyse-Datei.
   */
  private File getAnalysisFile(String url, String extension) {
    // Cut the protocol
    if (url.startsWith("http://") || url.startsWith("file://")) {
      url = url.substring(7);
    }

    url = RegainToolkit.replace(url, ":", "_");
    url = RegainToolkit.replace(url, "/", "_");

    if (extension == null) {
      return new File(mAnalysisDir, url);
    } else {
      return new File(mAnalysisDir, url + "." + extension);
    }
  }


  /**
   * Gibt alle Ressourcen frei, die von den Pr�paratoren genutzt wurden.
   * <p>
   * Wird ganz am Ende des Crawler-Prozesses aufgerufen, nachdem alle Dokumente
   * bearbeitet wurden.
   */
  public void close() {
    for (int i = 0; i < mPreparatorArr.length; i++) {
      mLog.info("Closing preparator " + mPreparatorArr[i].getClass().getName());
      try {
        mPreparatorArr[i].close();
      }
      catch (Throwable thr) {
        mLog.error("Closing preparator failed: "
          + mPreparatorArr[i].getClass().getName(), thr);
      }
    }

    // Ensure that no call of createDocument(RawDocument) is possible any more
    mPreparatorArr = null;
  }

}
