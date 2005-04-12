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
 *  $RCSfile: RawDocument.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/document/RawDocument.java,v $
 *     $Date: 2005/03/30 17:23:14 $
 *   $Author: til132 $
 * $Revision: 1.8 $
 */
package net.sf.regain.crawler.document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.Profiler;

import org.apache.log4j.Logger;


/**
 * Enth�lt alle Rohdaten eines Dokuments.
 * <p>
 * Falls der Inhalt des Dokuments zur besseren Bearbeitung in Form eines Strings
 * gebraucht wird, dann wird dieser zum sp�test m�glichen Zeitpunkt erstellt.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RawDocument {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(RawDocument.class);

  /** Der Profiler der das Laden via HTTP mi�t. */
  private static final Profiler HTTP_LOADING_PROFILER
    = new Profiler("Documents loaded with HTTP", "docs");

  /** Der Profiler der das Laden vom Dateisystem mi�t. */
  private static final Profiler FILE_LOADING_PROFILER
    = new Profiler("Documents loaded from the file system", "docs");

  /**
   * Der Timeout f�r HTTP-Downloads. Dieser Wert bestimmt die maximale Zeit
   * in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   */
  private static int mHttpTimeoutSecs = 180;

  /** Die URL des Dokuments. */
  private String mUrl;

  /** Die URL jenen Dokuments, in dem die URL dieses Dokuments gefunden wurde. */
  private String mSourceUrl;

  /**
   * Der Text des Links in dem die URL gefunden wurde. Ist <code>null</code>,
   * falls die URL nicht in einem Link (also einem a-Tag) gefunden wurde oder
   * wenn aus sonstigen Gr�nden kein Link-Text vorhanden ist.
   */
  private String mSourceLinkText;

  /**
   * Der Inhalt des Dokuments.
   * Ist <CODE>null</CODE>, wenn sich das Dokument in einer Datei befindet und
   * noch nicht angefragt wurde.
   */
  private byte[] mContent;

  /**
   * Der Inhalt des Dokuments als String. Ist <CODE>null</CODE>, bis er zum
   * ersten mal angefragt wurde.
   */
  private String mContentAsString;

  /**
   * Die Datei, in der sich der Inhalt des Dokuments befindet.
   * Ist <CODE>null</CODE>, wenn das Dokument �ber HTTP bezogen wird und noch
   * nicht angefragt wurde.
   */
  private File mContentAsFile;

  /**
   * Ist die Datei {@link #mContentAsFile} tempor�r. Wenn <code>true</code>,
   * dann wird die Datei am Ende gel�scht.
   *
   * @see #dispose()
   */
  private boolean mContentAsFileIsTemporary;



  /**
   * Erzeugt eine neue RawDocument-Instanz.
   *
   * @param url Die URL des Dokuments.
   * @param sourceUrl Die URL jenen Dokuments, in dem die URL dieses Dokuments
   *        gefunden wurde.
   * @param sourceLinkText Der Text des Links in dem die URL gefunden wurde. Ist
   *        <code>null</code>, falls die URL nicht in einem Link (also einem
   *        a-Tag) gefunden wurde oder wenn aus sonstigen Gr�nden kein Link-Text
   *        vorhanden ist.
   * @throws RegainException Wenn das Dokument nicht geladen werden konnte.
   */
  public RawDocument(String url, String sourceUrl, String sourceLinkText)
    throws RegainException
  {
    mUrl = url;
    mSourceUrl = sourceUrl;
    mSourceLinkText = sourceLinkText;

    if (url.startsWith("file://")) {
      mContentAsFile = RegainToolkit.urlToFile(url);
    } else {
      mContent = loadContent(url);
    }
  }


  /**
   * Setzt den Timeout f�r HTTP-Downloads.
   * <p>
   * Dieser Wert bestimmt die maximale Zeit, die ein HTTP-Download insgesamt
   * dauern darf.
   *
   * @param httpTimeoutSecs Der neue Timeout.
   */
  public static void setHttpTimeoutSecs(int httpTimeoutSecs) {
    mHttpTimeoutSecs = httpTimeoutSecs;
  }


  /**
   * L�dt Daten von einer URL.
   *
   * @param url Die URL.
   * @return Die Daten des Dokuments.
   * @throws RegainException Wenn das Laden fehl schlug.
   */
  private byte[] loadContent(String url) throws RegainException {
    HTTP_LOADING_PROFILER.startMeasuring();
    HttpDownloadThread loaderThread
      = new HttpDownloadThread(url, Thread.currentThread());
    loaderThread.start();

    // Warten bis entweder der Timeout abl�uft, oder bis dieser Thread vom
    // HttpContentLoaderThread unterbrochen wird.
    try {
      Thread.sleep(mHttpTimeoutSecs * 1000);
    }
    catch (InterruptedException exc) {}

    // Pr�fen, ob wir mittlerweile den Inhalt haben
    byte[] content = loaderThread.getContent();
    if (content != null) {
      HTTP_LOADING_PROFILER.stopMeasuring(content.length);
      return content;
    } else {
      // Wir haben keinen Inhalt
      HTTP_LOADING_PROFILER.abortMeasuring();

      // Pr�fen, ob der Download fehl schlug
      if (loaderThread.getError() != null) {
        throw new RegainException("Loading Document by HTTP failed: " + url,
                                  loaderThread.getError());
      }

      // Wir haben weder einen Inhalt noch einen Fehler
      // -> Der Download l�uft noch
      // -> Da jedoch mittlerweile der Timeout abgelaufen ist -> Exception werfen
      throw new RegainException("Loading Document by HTTP timed out after " +
          mHttpTimeoutSecs + " seconds: " + url);
    }
  }


  /**
   * Gibt die L�nge des Dokuments zur�ck (in Bytes).
   *
   * @return Die L�nge des Dokuments.
   */
  public int getLength() {
    if (mContent != null) {
      return mContent.length;
    } else {
      // Das Dokument befindet sich in einer Datei -> L�nge der Datei nutzen
      return (int) mContentAsFile.length();
    }
  }


  /**
   * Gibt zur�ck, wann das Dokument zuletzt ge�ndert wurde.
   * <p>
   * Wenn die letzte �nderung nicht ermittelt werden kann (z.B. bei
   * HTTP-Dokumenten), dann wird <code>null</code> zur�ckgegeben.
   *
   * @return Wann das Dokument zuletzt ge�ndert wurde.
   */
  public Date getLastModified() {
    if (mUrl.startsWith("file://")) {
      return new Date(mContentAsFile.lastModified());
    } else {
      // We don't know when it was last modified
      return null;
    }
  }


  /**
   * Gibt die URL des Dokuments zur�ck.
   *
   * @return Die URL des Dokuments.
   */
  public String getUrl() {
    return mUrl;
  }



  /**
   * Gibt die URL jenen Dokuments zur�ck, in dem die URL dieses Dokuments
   * gefunden wurde.
   *
   * @return Die URL jenen Dokuments, in dem die URL dieses Dokuments gefunden
   *         wurde.
   */
  public String getSourceUrl() {
    return mSourceUrl;
  }



  /**
   * Gibt den Text des Links zur�ck in dem die URL gefunden wurde.
   * <p>
   * Ist <code>null</code>, falls die URL nicht in einem Link (also einem a-Tag)
   * gefunden wurde oder wenn aus sonstigen Gr�nden kein Link-Text vorhanden ist.
   *
   * @return Der Text des Links zur�ck in dem die URL gefunden wurde.
   */
  public String getSourceLinkText() {
    return mSourceLinkText;
  }



  /**
   * Gibt den Inhalt des Dokuments zur�ck.
   *
   * @return Der Inhalt des Dokuments.
   * @throws RegainException Wenn das Dokument nicht geladen werden konnte.
   */
  public byte[] getContent() throws RegainException {
    if (mContent == null) {
      // Das Dokument befindet sich in einer Datei -> Diese laden
      FILE_LOADING_PROFILER.startMeasuring();
      try {
        byte[] content = CrawlerToolkit.loadFile(mContentAsFile);
        FILE_LOADING_PROFILER.stopMeasuring(content.length);
        return content;
      }
      catch (RegainException exc) {
        FILE_LOADING_PROFILER.abortMeasuring();
        throw new RegainException("Loading Document from file failed: "
          + mContentAsFile, exc);
      }
    }

    return mContent;
  }



  /**
   * Gibt den Inhalt des Dokuments als String zur�ck.
   * <p>
   * Dieser String wird erst bei der ersten Abfrage erzeugt und dann gecached.
   *
   * @return Der Inhalt des Dokuments als String.
   * @throws RegainException Wenn das Dokument nicht geladen werden konnte.
   */
  public String getContentAsString() throws RegainException {
    if (mContentAsString == null) {
      mContentAsString = new String(getContent());
    }

    return mContentAsString;
  }


  /**
   * Gets the content of the document as stream. The stream must be closed by
   * the caller.
   * 
   * @return The content of the document as stream.
   * @throws RegainException If creating the stream failed.
   */
  public InputStream getContentAsStream()
    throws RegainException
  {
    if (mContent != null) {
      return new ByteArrayInputStream(mContent);
    } else {
      // This document must be a file
      try {
        return new FileInputStream(mContentAsFile);
      }
      catch (Throwable thr) {
        throw new RegainException("Creating stream for file failed: " +
            mContentAsFile, thr);
      }
    }
  }


  /**
   * Schreibt den Inhalt des Dokuments in eine Datei.
   *
   * @param file Die Datei in die geschrieben werden soll.
   * @throws RegainException Wenn das Schreiben fehl schlug.
   */
  public void writeToFile(File file) throws RegainException {
    try {
      CrawlerToolkit.writeToFile(getContent(), file);

      if (mContentAsFile == null) {
        // Falls das Dokument in Dateiform ben�tigt wird, dann diese Datei
        // nutzen.
        mContentAsFile = file;
      }
    }
    catch (RegainException exc) {
      throw new RegainException("Creating file that contains the "
        + "document from '" + mUrl + "' failed", exc);
    }
  }


  /**
   * Gibt den Datei des Dokuments zur�ck. Falls das Dokument nicht als Datei
   * existiert, wird eine tempor�re Datei erzeugt.
   *
   * @return Die Datei des Dokuments.
   * @throws RegainException Wenn entweder keine tempor�re Datei erstellt werden
   *         konnte oder wenn nicht in die tempor�re Datei geschrieben werden
   *         konnte.
   */
  public File getContentAsFile() throws RegainException {
    return getContentAsFile(false);
  }


  /**
   * Gibt den Datei des Dokuments zur�ck. Falls das Dokument nicht als Datei
   * existiert, wird eine tempor�re Datei erzeugt.
   *
   * @param forceTempFile Bigt an, ob erzwungen werden soll, dass eine
   *        tempor�re Datei erzeugt wird. Auf diese Weise kann man sicher sein,
   *        dass die Datei von niemandem ge�ffnet ist.
   * @return Die Datei des Dokuments.
   * @throws RegainException Wenn entweder keine tempor�re Datei erstellt werden
   *         konnte oder wenn nicht in die tempor�re Datei geschrieben werden
   *         konnte.
   */
  public File getContentAsFile(boolean forceTempFile) throws RegainException {
    if ((mContentAsFile == null)
      || (forceTempFile && ! mContentAsFileIsTemporary))
    {
      // Das Dokument wurde via HTTP geladen
      // -> Inhalt in eine Datei schreiben

      // Get the file extension
      String extension;
      int lastDot = mUrl.lastIndexOf('.');
      if (lastDot == -1) {
        extension = ".tmp";
      } else {
        extension = mUrl.substring(lastDot);
      }

      // Get an unused file
      File tmpFile;
      try {
        tmpFile = File.createTempFile("lucenesearch_", extension);
      }
      catch (IOException exc) {
        throw new RegainException("Getting temporary File failed", exc);
      }

      // Inhalt in tempor�re Datei schreiben
      writeToFile(tmpFile);

      mContentAsFile = tmpFile;
      mContentAsFileIsTemporary = true;
    }

    return mContentAsFile;
  }


  /**
   * Gibt alle genutzten System-Ressourcen, wie tempor�re Dateien, wieder frei.
   * <p>
   * Ressourcen der VM, wie z.B. Arrays, werden nicht freigegeben. Das soll der
   * GarbageCollector erledigen.
   */
  public void dispose() {
    if ((mContentAsFile != null) && mContentAsFileIsTemporary) {
      if (mLog.isDebugEnabled()) {
        mLog.debug("Deleting temporary file: " + mContentAsFile.getAbsolutePath());
      }
      if (! mContentAsFile.delete()) {
        mLog.warn("Deleting temporary file failed: " + mContentAsFile.getAbsolutePath());
      }
    }
  }


  /**
   * Gets the String representation of this class.
   *
   * @return The String representation of this class.
   */
  public String toString() {
    return getUrl();
  }

}
