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
 *  $RCSfile$
 *   $Source$
 *     $Date: 2012-06-20 14:33:07 +0200 (Mi, 20 Jun 2012) $
 *   $Author: benjaminpick $
 * $Revision: 607 $
 */
package net.sf.regain.crawler.document;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.ErrorLogger;
import net.sf.regain.crawler.Profiler;
import net.sf.regain.crawler.access.CrawlerAccessController;
import net.sf.regain.crawler.config.AuxiliaryField;
import net.sf.regain.crawler.config.CrawlerConfig;
import net.sf.regain.crawler.config.PreparatorSettings;
import net.sf.regain.crawler.plugin.CrawlerPluginManager;

import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import net.sf.regain.util.io.PathFilenamePair;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.DateTools;
import org.semanticdesktop.aperture.mime.identifier.MimeTypeIdentifier;
import org.semanticdesktop.aperture.mime.identifier.magic.MagicMimeTypeIdentifierFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

/**
 * Fabrik, die aus der URL und den Rohdaten eines Dokuments ein Lucene-Ducument
 * erzeugt, das nur noch den, von Formatierungen gesäuberten, Text des Dokuments,
 * sowie seine URL und seinen Titel enthält.
 *
 * @see Document
 * @author Til Schneider, www.murfman.de
 */
public class DocumentFactory {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(DocumentFactory.class);
  /** The crawler config. */
  private CrawlerConfig mConfig;
  /** The maximum amount of characters which will be copied from content to summary */
  private int mMaxSummaryLength;
  /** should the whole content stored in the index for a preview on the result page */
  private boolean storeContentForPreview;
  /**
   * Das Verzeichnis, in dem Analyse-Dateien erzeugt werden sollen. Ist
   * <CODE>null</CODE>, wenn keine Analyse-Dateien erzeugt werden sollen.
   */
  private File mAnalysisDir = null;
  /** The preparators. */
  private Preparator[] mPreparatorArr;
  /** Die Profiler, die die Bearbeitung durch die Präparatoren messen. */
  private Profiler[] mPreparatorProfilerArr;
  /**
   * The {@link CrawlerAccessController} to use for identifying the groups that
   * are allowed to read a document. May be <code>null</code>.
   */
  private CrawlerAccessController mCrawlerAccessController;
  /**
   * The regular expressions that, when one of them applies, cause
   * that instead of the document title the link to that document
   * is used as title.
   */
  private RE[] mUseLinkTextAsTitleReArr;
  /** The profile that measures the addition to index. */
  private Profiler mWriteAnalysisProfiler = new Profiler("Writing Analysis files", "files");
  /** The mimetype mimeTypeIdentifier */
  MimeTypeIdentifier mimeTypeIdentifier;
  /** Crawler Plugin Manager instance */
  private CrawlerPluginManager pluginManager = CrawlerPluginManager.getInstance();

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
          throws RegainException {
    mConfig = config;
    mAnalysisDir = analysisDir;

    // Create the preparators
    try {
      PreparatorSettings[] prepConf = config.getPreparatorSettingsList();
      mPreparatorArr = PreparatorFactory.getInstance().createPreparatorArr(prepConf);
    } catch (RegainException exc) {
      throw new RegainException("Creating the document preparators failed", exc);
    }

    // Create a profiler for each preparator
    mPreparatorProfilerArr = new Profiler[mPreparatorArr.length];
    for (int i = 0; i < mPreparatorProfilerArr.length; i++) {
      String name = mPreparatorArr[i].getClass().getName();
      mPreparatorProfilerArr[i] = new Profiler("Preparator " + name, "docs");
    }

    // Create the CrawlerAccessController
    String accessClass = config.getCrawlerAccessControllerClass();
    if (accessClass != null) {
      String accessJar = config.getCrawlerAccessControllerJar();
      mCrawlerAccessController = (CrawlerAccessController) RegainToolkit.createClassInstance(accessClass, CrawlerAccessController.class,
              accessJar);

      Properties accessControllerConfig = config.getCrawlerAccessControllerConfig();
      if (accessControllerConfig == null) {
        accessControllerConfig = new Properties();
      }
      mCrawlerAccessController.init(accessControllerConfig);

      mLog.info("Using crawler access controller: " + accessClass);
    }

    // Create the mUseLinkTextAsTitleReArr
    String[] useLinkTextAsTitleRegexArr = config.getUseLinkTextAsTitleRegexList();
    if (useLinkTextAsTitleRegexArr == null) {
      mUseLinkTextAsTitleReArr = new RE[0];
    } else {
      mUseLinkTextAsTitleReArr = new RE[useLinkTextAsTitleRegexArr.length];
      for (int i = 0; i < useLinkTextAsTitleRegexArr.length; i++) {
        try {
          mUseLinkTextAsTitleReArr[i] = new RE(useLinkTextAsTitleRegexArr[i]);
        } catch (RESyntaxException exc) {
          throw new RegainException("Regular expression of "
                  + "use-link-text-as-title-pattern #" + i + " has wrong syntax '"
                  + useLinkTextAsTitleRegexArr[i] + "'", exc);
        }
      }
    }
    // Read some more configuration entries from the config
    this.mMaxSummaryLength = this.mConfig.getMaxSummaryLength();
    this.storeContentForPreview = this.mConfig.getStoreContentForPreview();

    // Set up the MimeTypeIdentifierFactory
    MagicMimeTypeIdentifierFactory factory = new MagicMimeTypeIdentifierFactory();
    mimeTypeIdentifier = factory.get();

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

    // Determine the mime-type 
    String mimeType;
    FileInputStream fis = null;
    try {
      File file = rawDocument.getContentAsFile();
      if (file.canRead() == false) {
        mLog.warn("canRead() on file return: false. Maybe no access rights for sourceURL: "
                + RegainToolkit.fileToUrl(file));
        return null;
      }

      fis = new FileInputStream(file);
      byte[] bytes = new byte[mimeTypeIdentifier.getMinArrayLength()];
      fis.read(bytes);
      mimeType = mimeTypeIdentifier.identify(bytes, file.getPath(),
              new URIImpl(rawDocument.getUrl(), false));
      if (mimeType == null || mimeType.length() == 0) {
        mimeType = "application/x-unknown-mime-type";
      }

      mLog.debug("Detected mimetype cycle 1: " + mimeType + ". " + rawDocument.getUrl());
      if (mimeType.equalsIgnoreCase("application/zip")) {
        // some new files like MS Office documents are zip files
        // so rewrite the URL for the correct mimetype detection
        mimeType = mimeTypeIdentifier.identify(bytes, null,
                new URIImpl("zip:mime:file:" + rawDocument.getUrl()));
        mLog.debug("Detected mimetype cycle 2: " + mimeType + ". " + "zip:mime:file:" + rawDocument.getUrl());
      }
    } catch (Exception exc) {
      errorLogger.logError("Determine mime-type of " + rawDocument.getUrl()
              + " failed", exc, false);
      mimeType = "application/x-unknown-mime-type";
    } finally {
      if (fis != null) {
        try { fis.close(); } catch(IOException e) {}
      }
    }

    rawDocument.setMimeType(mimeType);

    // Find the preparator that will prepare this URL
    Document doc = null;
    boolean preparatorFound = false;
    ArrayList<Integer> matchingPreparators = new ArrayList<Integer>();
    for (int i = 0; i < mPreparatorArr.length; i++) {
      if (mPreparatorArr[i].accepts(rawDocument)) {
        // This preparator can prepare this URL
        preparatorFound = true;
        matchingPreparators.add(i);
        if (mLog.isDebugEnabled()) {
          mLog.debug("Found: " + mPreparatorArr[i].getClass().getSimpleName()
                  + ", Prio: " + mPreparatorArr[i].getPriority());
        }
      }
    }

    // TODO: Try several preperators in order of priority?
    if (preparatorFound) {
      // Find the preparator with the highest priority
      Iterator<Integer> prepIdxIter = matchingPreparators.iterator();
      int highestPriorityIdx = (prepIdxIter.next()).intValue();
      // In case of more than one matching preperator find the one with the highest prio
      while (prepIdxIter.hasNext()) {
        int currI = (prepIdxIter.next()).intValue();
        if (mPreparatorArr[currI].getPriority() > mPreparatorArr[highestPriorityIdx].getPriority()) {
          highestPriorityIdx = currI;
        }
      }

      try {
        doc = createDocument(mPreparatorArr[highestPriorityIdx], mPreparatorProfilerArr[highestPriorityIdx], rawDocument);
        mLog.info("Preparation with " + mPreparatorArr[highestPriorityIdx].getClass().getSimpleName()
                + " done: " + rawDocument.getUrl());
      } catch (RegainException exc) {
        errorLogger.logError("Preparing " + rawDocument.getUrl()
                + " with preparator " + mPreparatorArr[highestPriorityIdx].getClass().getName()
                + " failed", exc, false);
      }
    } else {
      mLog.info("No preparator feels responsible for " + rawDocument.getUrl());
    }

    if (preparatorFound && (doc == null)) {
      // There were preparators that felt responsible for the document, but they
      // weren't able to process it
      // -> Create a substitute document to avoid that the same document is
      // tried to be processed the next time
      try {
        doc = createSubstituteDocument(rawDocument);
        mLog.info("Created substitute document: " + rawDocument.getUrl());
      } catch (RegainException exc) {
        errorLogger.logError("Creating substitute document for "
                + rawDocument.getUrl() + " failed", exc, false);
      }
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
          throws RegainException {
    String url = rawDocument.getUrl();

    // Extract the file type specific information
    String cleanedContent;
    String title;
    String summary;
    String metadata;
    String headlines;
    PathElement[] path;
    Map<String, String> additionalFieldMap;
    if (mLog.isDebugEnabled()) {
      mLog.debug("Using preparator " + preparator.getClass().getName()
              + " for " + rawDocument + ", " + rawDocument.getMimeType());
    }

    preparatorProfiler.startMeasuring();
    pluginManager.eventBeforePrepare(rawDocument, (WriteablePreparator) preparator);

    try {
      preparator.prepare(rawDocument);

      pluginManager.eventAfterPrepare(rawDocument, (WriteablePreparator) preparator);

      cleanedContent = preparator.getCleanedContent();
      title = preparator.getTitle();
      summary = preparator.getSummary();
      metadata = preparator.getCleanedMetaData();
      headlines = preparator.getHeadlines();
      path = preparator.getPath();
      additionalFieldMap = preparator.getAdditionalFields();

      preparator.cleanUp();

      preparatorProfiler.stopMeasuring(rawDocument.getLength());
    } catch (Throwable thr) {
      preparatorProfiler.abortMeasuring();
      throw new RegainException("Preparing " + url
              + " with preparator " + preparator.getClass().getName() + " failed", thr);
    }

    // Check the mandatory information
    if (cleanedContent == null) {
      throw new RegainException("Preparator " + preparator.getClass().getName()
              + " did not extract the content of " + url);
    }

    // Preparing succeed -> Create the document
    Document doc = createDocument(rawDocument, cleanedContent, title,
            summary, metadata, headlines, path, additionalFieldMap);

    // return the document
    return doc;
  }

  /**
   * Creates a substitute lucene {@link Document} for a {@link RawDocument}.
   * <p>
   * Substitute documents have no "content" field, but a "preparation-error"
   * field. They are added to the index if preparation failed. This way at least
   * the URL may be searched and following spider runs are much faster as a
   * previously failed document is not retried.
   * 
   * @param rawDocument The document to create the substitute document for.
   * @return The substitide document.
   * @throws RegainException If the user groups that are allowed to read this
   *         document couldn't be determined. 
   */
  private Document createSubstituteDocument(RawDocument rawDocument)
          throws RegainException {
    return createDocument(rawDocument, null, null, null, null, null, null, null);
  }

  /**
   * Create a lucene {@link Document}.
   * 
   * @param rawDocument The raw document to create the lucene {@link Document}
   *        for.
   * @param cleanedContent The content of the document. (May be null, if the
   *        content couldn't be extracted. In this case a substitute document is
   *        created)
   * @param title The title. May be null.
   * @param summary The summary. May be null.
   * @param metadata The cleaned meta data. May be null.
   * @param headlines The headlines. May be null.
   * @param path The path to the document. May be null.
   * @param additionalFieldMap The additional fields provided by the preparator.
   * @return The lucene {@link Document}.
   * 
   * @throws RegainException If the user groups that are allowed to read this
   *         document couldn't be determined. 
   */
  private Document createDocument(RawDocument rawDocument, String cleanedContent,
          String title, String summary, String metadata, String headlines, PathElement[] path,
          Map<String, String> additionalFieldMap)
          throws RegainException {
    String url = rawDocument.getUrl();

    // Create a new, empty document
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

          String value = auxiliaryFieldArr[i].getValue();
          if (value == null) {
            // We have no value set -> Extract the value from the regex
            value = regex.getParen(auxiliaryFieldArr[i].getUrlRegexGroup());
          }

          if (value != null) {
            if (auxiliaryFieldArr[i].getToLowerCase()) {
              value = value.toLowerCase();
            }

            if (mLog.isDebugEnabled()) {
              mLog.debug("Adding auxiliary field: " + fieldName + "=" + value);
            }
            boolean store = auxiliaryFieldArr[i].isStored();
            boolean index = auxiliaryFieldArr[i].isIndexed();
            boolean token = auxiliaryFieldArr[i].isTokenized();

            doc.add(new Field(fieldName, value,
                    store ? Field.Store.YES : Field.Store.NO,
                    index ? (token ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED) : Field.Index.NO));
          }
        }
      }
    }

    // Add the groups of the document
    if (mCrawlerAccessController != null) {
      String[] groupArr = mCrawlerAccessController.getDocumentGroups(rawDocument);

      // Check the Group array
      RegainToolkit.checkGroupArray(mCrawlerAccessController, groupArr);

      // Add the field
      // NOTE: The field "groups" is tokenized, but not stemmed.
      //       See: RegainToolkit.WrapperAnalyzer
      StringBuilder tokenBuilder = new StringBuilder();
      for (String group : groupArr)
      {
        tokenBuilder.append(group)
                    .append(" ");
      }

      //doc.add(new Field(RegainToolkit.FIELD_ACCESS_CONTROL_GROUPS, new IteratorTokenStream(groupIter)));
      doc.add(new Field(RegainToolkit.FIELD_ACCESS_CONTROL_GROUPS, new WhitespaceTokenizer(RegainToolkit.getLuceneVersion(),
              new StringReader(tokenBuilder.toString()))));
    }

    // Add the URL of the document
    doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));

    // Add the file name (without protocol, drive-letter and path)
    String filenameWithVariants = RegainToolkit.urlToWhitespacedFileName(url);
//    doc.add(new Field("filename", new WhitespaceTokenizer(IndexConfig.getLuceneVersion(),
//            new StringReader(filenameWithVariants))));
    doc.add(new Field("filename", filenameWithVariants, Field.Store.YES, Field.Index.ANALYZED));

    // Add the filename field for sorting
    PathFilenamePair pfPair = RegainToolkit.fragmentUrl(url);
    doc.add(new Field("filename_sort", pfPair.getFilename(), Field.Store.YES, Field.Index.NOT_ANALYZED));

    // Add the document's size
    int size = rawDocument.getLength();
    doc.add(new Field("size", Integer.toString(size), Field.Store.YES, Field.Index.NOT_ANALYZED));

    // Add the mime-type
    String mimeType = rawDocument.getMimeType();
    doc.add(new Field("mimetype", mimeType, Field.Store.YES, Field.Index.NOT_ANALYZED));

    // Add last modified
    Date lastModified = rawDocument.getLastModified();
    if (lastModified == null) {
      // We don't know when the document was last modified
      // -> Take the current time
      lastModified = new Date();
    }
    doc.add(new Field("last-modified",
            DateTools.dateToString(lastModified, DateTools.Resolution.DAY), Field.Store.YES,
            Field.Index.NOT_ANALYZED));

    // Write the raw content to an analysis file
    writeContentAnalysisFile(rawDocument);

    // Add the additional fields
    if (additionalFieldMap != null) {
      for (Entry<String, String> entry : additionalFieldMap.entrySet()) {
        String fieldName = entry.getKey();
        String fieldValue = entry.getValue();
        //doc.add(new Field(fieldName, fieldValue, Field.Store.COMPRESS, Field.Index.ANALYZED));
        // DEBUG doc.add(new Field(fieldName, fieldValue, Field.Store.YES, Field.Index.ANALYZED));

        doc.add(new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field(fieldName, CompressionTools.compressString(fieldValue)));
      }
    }

    if (hasContent(cleanedContent)) {
      // Write the clean content to an analysis file
      writeAnalysisFile(url, "clean", cleanedContent);

      // Add the cleaned content of the document
      doc.add(new Field("content", cleanedContent,
              this.storeContentForPreview ? Field.Store.YES : Field.Store.NO, Field.Index.ANALYZED));
    } else {
      // We have no content! This is a substitute document
      // -> Add a "preparation-error"-field
      doc.add(new Field("preparation-error", "true", Field.Store.YES,
              Field.Index.NO));
    }

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
      doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field("title_sort", title.toLowerCase(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    } else {
      doc.add(new Field("title_sort", "", Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    // Add the document's summary
    if (!hasContent(summary) && hasContent(cleanedContent)) {
      summary = createSummaryFromContent(cleanedContent);
    }
    if (hasContent(summary)) {
      doc.add(new Field("summary", summary, Field.Store.NO, Field.Index.ANALYZED));
      doc.add(new Field("summary", CompressionTools.compressString(summary)));
    }

    // Add the document's metadata
    if (hasContent(metadata)) {
      doc.add(new Field("metadata", metadata, Field.Store.YES, Field.Index.ANALYZED));
    }

    // Add the document's headlines
    if (hasContent(headlines)) {
      doc.add(new Field("headlines", headlines, Field.Store.NO,
              Field.Index.ANALYZED));
    }

    // Add the document's path
    if (pfPair.getPath() != null) {
      //String asString = pathToString(path);
      doc.add(new Field("path", pfPair.getPath(), Field.Store.YES, Field.Index.NO));
      doc.add(new Field("path_sort", pfPair.getPath().toLowerCase(), Field.Store.YES, Field.Index.NOT_ANALYZED));

      // Write the path to an analysis file
      writeAnalysisFile(url, "path", pfPair.getPath());
    } else {
      doc.add(new Field("path_sort", "", Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    return doc;
  }

  /**
   * Gibt zurück, ob der String einen Inhalt hat. Dies ist der Fall, wenn er
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
   * Wenn keine Zusammenfassung möglich ist, wird <code>null</code>
   * zurückgegeben.
   *
   * @param content Der Inhalt für den die Zusammenfassung erstellt werden soll.
   * @return Eine Zusammenfassung des Dokuments oder <code>null</code>, wenn
   *         keine erzeugt werden konnte.
   */
  private String createSummaryFromContent(String content) {

    if (content.length() > mMaxSummaryLength) {
      // cut the content only if it exceeds the max size for the summary
      int lastSpacePos = content.lastIndexOf(' ', mMaxSummaryLength);

      if (lastSpacePos == -1) {
        return null;
      } else {
        return content.substring(0, lastSpacePos) + "...";
      }
    } else {
      return content;
    }
  }

  /**
   * Wandelt einen Pfad in einen String um.
   *
   * @param path Der Pfad
   * @return Der Pfad als String
   */
  private String pathToString(PathElement[] path) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < path.length; i++) {
      builder.append(path[i].getUrl());
      builder.append(' ');
      builder.append(path[i].getTitle());
      builder.append('\n');
    }

    return builder.toString();
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
    } catch (RegainException exc) {
      mWriteAnalysisProfiler.abortMeasuring();
      mLog.error("Writing analysis file failed", exc);
    }
  }

  /**
   * Schreibt eine Analyse-Datei.
   * <p>
   * Eine Analyse-Datei enthält die Daten des Dokuments bei jedem
   * Zwischenschritt der Aufbereitung. Sie hilft die Qualit�t der
   * Index-Erstellung zu Prüfen und wird in einem Unterverzeichnis im
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
    } catch (IOException exc) {
      mWriteAnalysisProfiler.abortMeasuring();
      mLog.error("Writing analysis file failed", exc);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException exc) {
        }
      }
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
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
   * Gibt alle Ressourcen frei, die von den Präparatoren genutzt wurden.
   * <p>
   * Wird ganz am Ende des Crawler-Prozesses aufgerufen, nachdem alle Dokumente
   * bearbeitet wurden.
   */
  public void close() {
    for (int i = 0; i < mPreparatorArr.length; i++) {
      mLog.info("Closing preparator " + mPreparatorArr[i].getClass().getName());
      try {
        mPreparatorArr[i].close();
      } catch (Throwable thr) {
        mLog.error("Closing preparator failed: "
                + mPreparatorArr[i].getClass().getName(), thr);
      }
    }

    // Ensure that no call of createDocument(RawDocument) is possible any more
    mPreparatorArr = null;
    
    // Close CrawlerAccessControl if possible
    if (mCrawlerAccessController != null && mCrawlerAccessController instanceof Closeable)
    {
      Closeable c = (Closeable) mCrawlerAccessController;
      try
      {
        c.close();
      }
      catch (IOException e)
      {
        mLog.error("Closing CrawlerAccessController failed:", e);
      }
    }
  }
}
