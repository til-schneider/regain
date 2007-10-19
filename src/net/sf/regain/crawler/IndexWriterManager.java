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
 *     $Date: 2007-10-20 18:22:56 +0200 (Sa, 20 Okt 2007) $
 *   $Author: til132 $
 * $Revision: 247 $
 */
package net.sf.regain.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.AuxiliaryField;
import net.sf.regain.crawler.config.CrawlerConfig;
import net.sf.regain.crawler.config.UrlMatcher;
import net.sf.regain.crawler.document.DocumentFactory;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Kontrolliert und kapselt die Erstellung des Suchindex.
 * <p>
 * <b>Anwendung:</b><br>
 * Rufen Sie f�r jedes Dokument {@link #addToIndex(RawDocument, ErrorLogger)}
 * auf. Rufen Sie am Ende {@link #close(boolean)} auf, um den Index zu
 * schlie�en. Danach sind keine weiteren Aufrufe von
 * {@link #addToIndex(RawDocument, ErrorLogger)} erlaubt.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IndexWriterManager {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(IndexWriterManager.class);

  /**
   * Der Name des Index-Unterverzeichnisses, in das der neue Index gestellt
   * werden soll, sobald er fertig ist ohne dass fatale Fehler aufgetreten sind.
   * <p>
   * Die Suchmaske wird, sobald es diese Verzeichnis gibt seine Suche darauf
   * umstellen. Dabei wird es in "index" umbenannt.
   */
  private static final String NEW_INDEX_SUBDIR = "new";

  /**
   * Der Name des Index-Unterverzeichnisses, in das der neue Index gestellt
   * werden soll, sobald er fertig ist, wobei fatale Fehler sufgetreten sind.
   */
  private static final String QUARANTINE_INDEX_SUBDIR = "quarantine";

  /** Der Name des Index-Unterverzeichnisses, in dem der genutzte Index steht. */
  private static final String WORKING_INDEX_SUBDIR = "index";
  /**
   * Der Name des Index-Unterverzeichnisses, in dem der neue Index aufgebaut
   * werden soll.
   */
  private static final String TEMP_INDEX_SUBDIR = "temp";
  
  /**
   * The name of the index sub directory that contains a breakpoint.
   * <p>
   * NOTE: The crawler creates periodically so called breakpoints. If the
   * crawler should be stopped before it is finished it can use a breakpoint
   * to go on the next time. Besides the search mask can use a breakpoint if no
   * other index exists. So the user can already search before the first index
   * was fully created.
   */
  private static final String BREAKPOINT_INDEX_SUBDIR = "breakpoint";

  /**
   * Gibt an, ob die Terme sortiert in die Terme-Datei geschrieben werden soll.
   *
   * @see #writeTermFile(File, File)
   */
  private static final boolean WRITE_TERMS_SORTED = true;

  /**
   * Workaround: Unter Windows klappt das Umbenennen unmittelbar nach Schlie�en
   * des Index nicht. Wahrscheinlich sind die Filepointer auf die gerade
   * geschlossenen Dateien noch nicht richtig aufger�umt, so dass ein Umbenennen
   * des Indexverzeichnisses fehl schl�gt. Das Umbenennen wird daher regelm��ig
   * probiert, bis es entweder funktioniert oder bis der Timeout abgelaufen ist.
   */
  private static final long RENAME_TIMEOUT = 60000; // 1 min

  /**
   * The writing mode.
   * @see #setIndexMode(int)
   */
  private static final int WRITING_MODE = 1;
  /**
   * The reading mode.
   * @see #setIndexMode(int)
   */
  private static final int READING_MODE = 2;
  /**
   * The searching mode.
   * @see #setIndexMode(int)
   */
  private static final int SEARCHING_MODE = 3;
  /**
   * The all closed mode.
   * @see #setIndexMode(int)
   */
  private static final int ALL_CLOSED_MODE = 4;

  /** The crawler configuration. */
  private CrawlerConfig mConfig;

  /** Der Analyzer, der vom IndexWriter genutzt werden soll. */
  private Analyzer mAnalyzer;

  /** Der gekapselte IndexWriter, der den eigentlichen Index erstellt. */
  private IndexWriter mIndexWriter;

  /**
   * Der gekapselte IndexReader. Wird zum L�schen von Dokumenten aus dem Index
   * ben�tigt.
   * <p>
   * Ist <code>null</code>, wenn der Index nicht aktualisiert werden soll.
   */
  private IndexReader mIndexReader;

  /**
   * Der gekapselte IndexSearcher. Wird zum Finden von Dokumenten ben�tigt.
   * <p>
   * Ist <code>null</code>, wenn der Index nicht aktualisiert werden soll.
   */
  private IndexSearcher mIndexSearcher;

  /**
   * Gibt an, ob ein bestehender Index aktualisiert wird.
   * <p>
   * Anderenfalls wird ein komplett neuer Index angelegt.
   */
  private boolean mUpdateIndex;

  /**
   * Specifies whether a document that couldn't be prepared the last time should be retried.
   */
  private boolean mRetryFailedDocs;
  
  /** Die DocumentFactory, die die Inhalte f�r die Indizierung aufbereitet. */
  private DocumentFactory mDocumentFactory;

  /**
   * Das Verzeichnis, in dem der Suchindex am Ende stehen soll, wenn es keine
   * fatalen Fehler gab.
   */
  private File mNewIndexDir;

  /**
   * Das Verzeichnis, in dem der Suchindex am Ende stehen soll, wenn es
   * fatale Fehler gab.
   */
  private File mQuarantineIndexDir;

  /** Das Verzeichnis, in dem der neue Suchindex aufgebaut werden soll. */
  private File mTempIndexDir;

  /** The directory to create breakpoint indices. */
  private File mBreakpointIndexDir;

  /** Das Verzeichnis, in dem die Analyse-Dateien erstellt werden soll. */
  private File mAnalysisDir;
  
  /** The file where the error log should be stored. */
  private File mErrorLogFile;
  
  /**
   * The stream used for writing errors to the error log of the index.
   * May be <code>null</code>.
   */
  private FileOutputStream mErrorLogStream;
  
  /**
   * The print writer used for writing errors to the error log of the index.
   * May be <code>null</code>.
   */
  private PrintWriter mErrorLogWriter;
  
  /**
   * The number of documents that were in the (old) index when the
   * IndexWriterManager was created.
   */
  private int mInitialDocCount;

  /** Der Profiler der das Hinzuf�gen zum Index mi�t. */
  private Profiler mAddToIndexProfiler = new Profiler("Indexed documents", "docs");

  /** The profiler for the breakpoint creation. */
  private Profiler mBreakpointProfiler = new Profiler("Created breakpoints", "breakpoints");

  /**
   * Enth�lt die URL und den LastUpdated-String aller Dokumente, deren Eintr�ge
   * beim Abschlie�en des Index entfernt werden m�ssen.
   * <p>
   * Die URL bildet den key, der LastUpdated-String die value.
   */
  private HashMap mUrlsToDeleteHash;



  /**
   * Erzeugt eine neue IndexWriterManager-Instanz.
   *
   * @param config Die zu verwendende Konfiguration.
   * @param updateIndex Gibt an, ob ein bereits bestehender Index aktualisiert
   *        werden soll.
   * @param retryFailedDocs Specifies whether a document that couldn't be
   *        prepared the last time should be retried.
   *
   * @throws RegainException Wenn der neue Index nicht vorbereitet werden konnte.
   */
  public IndexWriterManager(CrawlerConfig config, boolean updateIndex,
    boolean retryFailedDocs)
    throws RegainException
  {
    mConfig = config;
    mUpdateIndex = updateIndex;
    mRetryFailedDocs = retryFailedDocs;
    
    mInitialDocCount = 0;

    File indexDir = new File(config.getIndexDir());

    if (! indexDir.exists()) {
      // The index directory does not exist -> Create it
      mLog.info("Creating index directory " + indexDir.getAbsolutePath());
      indexDir.mkdirs();
    }

    mNewIndexDir        = new File(indexDir, NEW_INDEX_SUBDIR);
    mQuarantineIndexDir = new File(indexDir, QUARANTINE_INDEX_SUBDIR);
    mTempIndexDir       = new File(indexDir, TEMP_INDEX_SUBDIR);
    mBreakpointIndexDir = new File(indexDir, BREAKPOINT_INDEX_SUBDIR);
    
    mErrorLogFile = new File(mTempIndexDir, "log/error.log");

    // Delete the old temp index directory if it should still exist
    if (mTempIndexDir.exists()) {
      RegainToolkit.deleteDirectory(mTempIndexDir);
    }
    // and create a new, empty one
    if (! mTempIndexDir.mkdir()) {
      throw new RegainException("Creating working directory failed: "
                                + mTempIndexDir.getAbsolutePath());
    }

    // Get the untokenized field names
    String[] untokenizedFieldNames = getUntokenizedFieldNames(config);

    // Create the Analyzer
    // NOTE: Make shure you use the same Analyzer in the SearchContext too!
    String analyzerType = config.getAnalyzerType();
    String[] stopWordList = config.getStopWordList();
    String[] exclusionList = config.getExclusionList();
    mAnalyzer = RegainToolkit.createAnalyzer(analyzerType, stopWordList,
        exclusionList, untokenizedFieldNames);

    // Alten Index kopieren, wenn Index aktualisiert werden soll
    if (updateIndex) {
      if (! copyExistingIndex(indexDir, analyzerType)) {
        mUpdateIndex = updateIndex = false;
      }
    }

    // Check whether we have to create a new index
    boolean createNewIndex = ! updateIndex;
    if (createNewIndex) {
      // Create a new index
      try {
        mIndexWriter = createIndexWriter(true); 
      } catch (IOException exc) {
        throw new RegainException("Creating new index failed", exc);
      }
    }

    if (updateIndex) {
      // Force an unlock of the index (we just created a copy so this is save)
      setIndexMode(READING_MODE);
      try {
        IndexReader.unlock(mIndexReader.directory());
        mInitialDocCount = mIndexReader.numDocs();
      } catch (IOException exc) {
        throw new RegainException("Forcing unlock failed", exc);
      }
    }

    // Write the stopWordList and the exclusionList in a file so it can be found
    // by the search mask
    RegainToolkit.writeToFile(analyzerType, new File(mTempIndexDir, "analyzerType.txt"));
    RegainToolkit.writeListToFile(stopWordList, new File(mTempIndexDir, "stopWordList.txt"));
    RegainToolkit.writeListToFile(exclusionList, new File(mTempIndexDir, "exclusionList.txt"));
    if (untokenizedFieldNames.length != 0) {
      RegainToolkit.writeListToFile(untokenizedFieldNames, new File(mTempIndexDir, "untokenizedFieldNames.txt"));
    }

    // Prepare the analysis directory if wanted
    if (config.getWriteAnalysisFiles()) {
      mAnalysisDir = new File(mTempIndexDir.getAbsolutePath() + File.separator
                              + "analysis");

      if (! mAnalysisDir.mkdir()) {
        throw new RegainException("Creating analysis directory failed: "
                                  + mAnalysisDir.getAbsolutePath());
      }
    }

    mDocumentFactory = new DocumentFactory(config, mAnalysisDir);
  }

  
  /**
   * Returns the names of the fields that shouldn't be tokenized.
   * 
   * @param config The crawler configuration.
   * @return The names of the fields that shouldn't be tokenized.
   */
  private String[] getUntokenizedFieldNames(CrawlerConfig config) {
    AuxiliaryField[] auxFieldArr = config.getAuxiliaryFieldList();
    ArrayList list = new ArrayList();
    for (int i = 0; i < auxFieldArr.length; i++) {
      if (! auxFieldArr[i].isTokenized()) {
        list.add(auxFieldArr[i].getFieldName());
      }
    }

    String[] asArr = new String[list.size()];
    list.toArray(asArr);
    return asArr;
  }
  

  /**
   * Gibt zur�ck, ob ein bestehender Index aktualisiert wird.
   * <p>
   * Anderenfalls wird ein komplett neuer Index angelegt.
   *
   * @return Ob ein bestehender Index aktualisiert wird.
   */
  public boolean getUpdateIndex() {
    return mUpdateIndex;
  }
  
  
  /**
   * Gets the number of documents that were in the (old) index when the
   * IndexWriterManager was created.
   * 
   * @return The initial number of documents in the index.
   */
  public int getInitialDocCount() {
    return mInitialDocCount;
  }


  /**
   * Gets the number of documents that were added to the index.
   * 
   * @return The number of documents added to the index.
   */
  public int getAddedDocCount() {
    return mAddToIndexProfiler.getMeasureCount();
  }


  /**
   * Gets the number of documents that will be removed from the index.
   * 
   * @return The number of documents removed from the index.
   */
  public int getRemovedDocCount() {
    // NOTE: We get a local pointer to the mUrlsToDeleteHash, if the hash should
    //       be set to null in the same time.
    HashMap hash = mUrlsToDeleteHash;
    return (hash == null) ? 0 : hash.size();
  }


  /**
   * Logs an error at the error log of the index.
   * 
   * @param msg The error message.
   * @param thr The error to log. May be <code>null</code>.
   * @throws RegainException If writing to the error log failed.
   */
  public void logError(String msg, Throwable thr) throws RegainException {
    if (mErrorLogStream == null) {
      try {
        new File(mTempIndexDir, "log").mkdir();
        mErrorLogStream = new FileOutputStream(mErrorLogFile, true);
        mErrorLogWriter = new PrintWriter(mErrorLogStream);
      }
      catch (IOException exc) {
        throw new RegainException("Opening error log file of the index failed");
      }
    }
    
    if (thr == null) {
      mErrorLogWriter.println(msg);
    } else {
      mErrorLogWriter.println(msg + ":");
      thr.printStackTrace(mErrorLogWriter);
      mErrorLogWriter.println();
    }
    mErrorLogWriter.flush();
  }


  /**
   * Sets the current mode
   * <p>
   * The are the following modes:
   * <ul>
   *   <li>Writing mode: The mIndexWriter is opened, the mIndexSearcher may be
   *     opened, the mIndexReader is closed. In this mode documents may be added
   *     to the index.</li>
   *   <li>Reading mode: The mIndexReader is opened, the mIndexSearcher may be
   *     opened, the mIndexWriter is closed. In this mode documents may be
   *     read or removed from the index.</li>
   *   <li>Searching mode: The mIndexSearcher is opened, the mIndexWriter or
   *     mIndexReader may be opened. In this mode documents may be searched.
   *   <li>All closed mode: All access to the index ist closed:
   *     mIndexWriter, mIndexReader and mIndexSearcher. In this mode the index
   *     can't be accessed at all.
   * </ul>
   * <p>
   * If the index already is in the wanted mode nothing happens. This method is
   * very fast in this case.
   *
   * @param mode The mode the index should have. Must be one of
   *        {@link #WRITING_MODE}, {@link #READING_MODE}, {@link #SEARCHING_MODE}
   *        or {@link #ALL_CLOSED_MODE}.
   * @throws RegainException If closing or opening failed.
   */
  private void setIndexMode(int mode) throws RegainException {
    // Close the mIndexReader in WRITING_MODE and ALL_CLOSED_MODE
    if ((mode == WRITING_MODE) || (mode == ALL_CLOSED_MODE)) {
      if (mIndexReader != null) {
        try {
          mIndexReader.close();
          mIndexReader = null;
        } catch (IOException exc) {
          throw new RegainException("Closing IndexReader failed", exc);
        }
      }
    }

    // Close the mIndexWriter in READING_MODE and ALL_CLOSED_MODE
    if ((mode == READING_MODE) || (mode == ALL_CLOSED_MODE)) {
      if (mIndexWriter != null) {
        try {
          mIndexWriter.close();
          mIndexWriter = null;
        } catch (IOException exc) {
          throw new RegainException("Closing IndexWriter failed", exc);
        }
      }
    }

    // Close the mIndexSearcher in ALL_CLOSED_MODE
    if ((mode == ALL_CLOSED_MODE) && (mIndexSearcher != null)) {
      try {
        mIndexSearcher.close();
        mIndexSearcher = null;
      } catch (IOException exc) {
        throw new RegainException("Closing IndexSearcher failed", exc);
      }
    }

    // Open the mIndexWriter in WRITING_MODE
    if ((mode == WRITING_MODE) && (mIndexWriter == null)) {
      mLog.info("Switching to index mode: adding mode");
      try {
        mIndexWriter = createIndexWriter(false);
      } catch (IOException exc) {
        throw new RegainException("Creating IndexWriter failed", exc);
      }
    }

    // Open the mIndexReader in READING_MODE
    if ((mode == READING_MODE) && (mIndexReader == null)) {
      mLog.info("Switching to index mode: deleting mode");
      try {
        mIndexReader = IndexReader.open(mTempIndexDir);
      } catch (IOException exc) {
        throw new RegainException("Creating IndexReader failed", exc);
      }
    }
    
    // Open the mIndexSearcher in SEARCHING_MODE
    if ((mode == SEARCHING_MODE) && (mIndexSearcher == null)) {
      mLog.info("Switching to index mode: searching mode");
      try {
        mIndexSearcher = new IndexSearcher(mTempIndexDir.getAbsolutePath());
      } catch (IOException exc) {
        throw new RegainException("Creating IndexSearcher failed", exc);
      }
    }

    // Tell the user, when switching to ALL_CLOSED_MODE
    if (mode == ALL_CLOSED_MODE) {
      mLog.info("Switching to index mode: all closed mode");
    }
  }


  private IndexWriter createIndexWriter(boolean createNewIndex)
    throws IOException
  { 
    IndexWriter indexWriter = new IndexWriter(mTempIndexDir, mAnalyzer, createNewIndex);

    int maxFieldLength = mConfig.getMaxFieldLength();
    if (maxFieldLength > 0) {
      indexWriter.setMaxFieldLength(maxFieldLength);
    }

    return indexWriter;
  }


  /**
   * Kopiert den zuletzt erstellten Index in das Arbeitsverzeichnis.
   *
   * @param indexDir Das Verzeichnis, in dem der Index liegt.
   * @param analyzerType Der Analyzer-Typ, den der alte Index haben muss, um
   *        �bernommen zu werden.
   * @return Ob ein alter Index gefunden wurde.
   * @throws RegainException Wenn das Kopieren fehl schlug.
   */
  private boolean copyExistingIndex(File indexDir, String analyzerType)
    throws RegainException
  {
    // Find the newest index
    File oldIndexDir;
    if (mBreakpointIndexDir.exists()) {
      oldIndexDir = mBreakpointIndexDir;
    } else if (mNewIndexDir.exists()) {
      oldIndexDir = mNewIndexDir;
    } else {
      // Es gibt keinen neuen Index -> Wir m�ssen den Index nehmen, der gerade
      // verwendet wird
      oldIndexDir = new File(indexDir, WORKING_INDEX_SUBDIR);
    }
    if (! oldIndexDir.exists()) {
      mLog.warn("Can't update index, because there was no old index. " +
        "A complete new index will be created...");
      return false;
    }

    // Analyzer-Typ des alten Index pr�fen
    File analyzerTypeFile = new File(oldIndexDir, "analyzerType.txt");
    String analyzerTypeOfIndex = RegainToolkit.readStringFromFile(analyzerTypeFile);
    if ((analyzerTypeOfIndex == null)
      || (! analyzerType.equals(analyzerTypeOfIndex.trim())))
    {
      mLog.warn("Can't update index, because the index was created using " +
        "another analyzer type (index type: '" + analyzerTypeOfIndex.trim() +
        "', configured type '" + analyzerType + "'). " +
        "A complete new index will be created...");
      return false;
    }

    // Index in Arbeitsverzeichnis kopieren
    mLog.info("Updating index from " + oldIndexDir.getAbsolutePath());
    RegainToolkit.copyDirectory(oldIndexDir, mTempIndexDir, false, ".txt");

    return true;
  }


  /**
   * F�gt ein Dokument dem Index hinzu.
   * <p>
   * Anhand der URL wird der Typ des Dokuments erkannt.
   *
   * @param rawDocument Das zu indizierende Dokument.
   * @param errorLogger The error logger to use for logging errors.
   *
   * @throws RegainException Wenn das Hinzuf�gen zum Index scheiterte.
   */
  public void addToIndex(RawDocument rawDocument, ErrorLogger errorLogger)
    throws RegainException
  {
    // Check whether there already is an up-to-date entry in the index
    if (mUpdateIndex) {
      boolean removeOldEntry = false;

      // Search the entry for this URL
      Term urlTerm = new Term("url", rawDocument.getUrl());
      Query query = new TermQuery(urlTerm);
      Document doc;
      try {
        setIndexMode(SEARCHING_MODE);
        Hits hits = mIndexSearcher.search(query);
        if (hits.length() > 0) {
          if (hits.length() > 1) {
            mLog.warn("There are duplicate entries (" + hits.length() + " in " +
              "total) for " + rawDocument.getUrl() + ". They will be removed.");
            removeOldEntry = true;
          }
          doc = hits.doc(0);
        }
        else {
          doc = null;
        }
      }
      catch (IOException exc) {
        throw new RegainException("Searching old index entry failed for "
          + rawDocument.getUrl(), exc);
      }

      // If we found an entry, check whether it is up-to-date
      if (doc != null) {
        // Get the last modification date from the document
        Date docLastModified = rawDocument.getLastModified();
        
        if (docLastModified == null) {
          // We are not able to get the last modification date from the
          // document (this happens with all http-URLs)
          // -> Delete the old entry and create a new one
          mLog.info("Don't know when the document was last modified. " +
            "Creating a new index entry...");
          removeOldEntry = true;
        } else {
          // Compare the modification date with the one from the index entry
          String asString = doc.get("last-modified");
          if (asString != null) {
            Date indexLastModified = RegainToolkit.stringToLastModified(asString);

            long diff = docLastModified.getTime() - indexLastModified.getTime();
            if (diff > 60000L) {
              // The document is at least one minute newer
              // -> The index entry is not up-to-date -> Delete the old entry
              mLog.info("Index entry is outdated. Creating a new one (" +
                  docLastModified + " > " + indexLastModified + "): " +
                  rawDocument.getUrl());
              removeOldEntry = true;
            } else {
              // The index entry is up-to-date

              // Check whether the preparation failed the last time
              boolean failedLastTime = doc.get("preparation-error") != null;
              if (failedLastTime) {
                if (mRetryFailedDocs) {
                  // The entry failed the last time, the user want's a retry
                  // -> We do a retry
                  mLog.info("Retrying preparation of: " + rawDocument.getUrl());
                  removeOldEntry = true;
                } else {
                  // The entry failed the last time, the user want's no retry
                  // -> We are done
                  mLog.info("Ignoring " + rawDocument.getUrl() + ", because " +
                      "preparation already failed the last time and no retry is wanted.");
                  return;
                }
              } else {
                // The entry is up-to-date and contains text -> We are done
                mLog.info("Index entry is already up to date: " + rawDocument.getUrl());
                return;
              }
            }
          } else {
            // We don't know the last modification date from the index entry
            // -> Delete the entry
            mLog.info("Index entry has no last-modified field. " +
                "Creating a new one: " + rawDocument.getUrl());
            removeOldEntry = true;
          }
        }
      }

      // Check whether we have to delete the old entry
      if (removeOldEntry) {
        // We don't delete the entry immediately, but we remember it.
        // See javadoc of markForDeletion(Document)
        markForDeletion(doc);
      }
    }

    // Create a new entry
    createNewIndexEntry(rawDocument, errorLogger);
  }


  /**
   * Erzeugt f�r ein Dokument einen neuen Indexeintrag.
   *
   * @param rawDocument Das Dokument f�r das der Eintrag erzeugt werden soll
   * @param errorLogger The error logger to use for logging errors.
   * @throws RegainException Wenn die Erzeugung fehl schlug.
   */
  private void createNewIndexEntry(RawDocument rawDocument, ErrorLogger errorLogger)
    throws RegainException
  {
    // Dokument erzeugen
    if (mLog.isDebugEnabled()) {
      mLog.debug("Creating document: " + rawDocument.getUrl());
    }
    Document doc = mDocumentFactory.createDocument(rawDocument, errorLogger);

    // Dokument in den Index aufnehmen
    if (doc != null) {
      mAddToIndexProfiler.startMeasuring();
      try {
        setIndexMode(WRITING_MODE);
        mIndexWriter.addDocument(doc);
        mAddToIndexProfiler.stopMeasuring(rawDocument.getLength());
      }
      catch (IOException exc) {
        mAddToIndexProfiler.abortMeasuring();
        throw new RegainException("Adding document to index failed", exc);
      }
    }
  }


  /**
   * Goes through the index and deletes all obsolete entries.
   * <p>
   * Entries are obsolete if they are marked for deletion by the
   * IndexWriterManager (see {@link #mUrlsToDeleteHash}) or if the don't neither
   * match an entry of the urlToKeepSet nor of the prefixesToKeepArr.
   *
   * @param urlChecker The UrlChecker to use for deciding whether an index entry
   *        should be kept in the index or not. If null only the documents in
   *        the {@link #mUrlsToDeleteHash} will be deleted.
   * @throws RegainException If an index entry could either not be read or
   *         deleted.
   */
  public void removeObsoleteEntries(UrlChecker urlChecker)
    throws RegainException
  {
    if (! mUpdateIndex) {
      // Wir haben einen komplett neuen Index erstellt
      // -> Es kann keine Eintr�ge zu nicht vorhandenen Dokumenten geben
      // -> Wir sind fertig
      return;
    }
    
    if ((mUrlsToDeleteHash == null) && (urlChecker == null)) {
      // There is nothing to delete -> Fast return
      return;
    }

    // Get the UrlMatchers that identify URLs that should not be deleted
    UrlMatcher[] preserveUrlMatcherArr = null;
    if (urlChecker != null) {
      preserveUrlMatcherArr = urlChecker.createPreserveUrlMatcherArr();
    }

    // Go through the index
    setIndexMode(READING_MODE);
    int docCount = mIndexReader.numDocs();
    for (int docIdx = 0; docIdx < docCount; docIdx++) {
      if (! mIndexReader.isDeleted(docIdx)) {
        // Document lesen
        Document doc;
        try {
          doc = mIndexReader.document(docIdx);
        }
        catch (Throwable thr) {
          throw new RegainException("Getting document #" + docIdx
            + " from index failed.", thr);
        }

        // URL und last-modified holen
        String url = doc.get("url");
        String lastModified = doc.get("last-modified");

        // Pr�fen, ob die URL gel�scht werden soll
        boolean shouldBeDeleted;
        if (url != null) {
          // Pr�fen, ob dieser Eintrag zum L�schen vorgesehen ist
          if (isMarkedForDeletion(doc)) {
            shouldBeDeleted = true;
          }
          // Check whether all other documents should NOT be deleted
          else if ((urlChecker == null)) {
            shouldBeDeleted = false;
          }
          // Pr�fen, ob dieser Eintrag zu verschonen ist
          else if (urlChecker.shouldBeKeptInIndex(url)) {
            shouldBeDeleted = false;
          }
          // Pr�fen, ob die URL zu einem zu-verschonen-Pr�fix passt
          else {
            shouldBeDeleted = true;
            for (int i = 0; i < preserveUrlMatcherArr.length; i++) {
              if (preserveUrlMatcherArr[i].matches(url)) {
                shouldBeDeleted = false;
                break;
              }
            }
          }

          if (shouldBeDeleted) {
            try {
              mLog.info("Deleting from index: " + url + " from " + lastModified);
              mIndexReader.deleteDocument(docIdx);
            }
            catch (IOException exc) {
              throw new RegainException("Deleting document #" + docIdx
                + " from index failed: " + url + " from " + lastModified, exc);
            }
          }
        }
      }
    }

    // Merkliste der zu l�schenden Eintr�ge l�schen
    mUrlsToDeleteHash = null;
  }

  
  /**
   * Goes through the index and deletes all obsolete entries.
   * <p>
   * Entries are obsolete if they are marked for deletion by the
   * IndexWriterManager (see {@link #mUrlsToDeleteHash}).
   *
   * @throws RegainException If an index entry could either not be read or
   *         deleted.
   */
  private void removeObsoleteEntries() throws RegainException {
    removeObsoleteEntries(null);
  }
  

  /**
   * Merkt ein Dokument f�r die sp�tere L�schung vor.
   * <p>
   * Diese Methode ist Teil eines Workaround: Ein alter Eintrag, der durch einen
   * neuen ersetzt wird, wird nicht sofort gel�scht, sondern nur zur L�schung
   * vorgemerkt. Auf diese Weise wird ein seltener Fehler umgangen, der das
   * Schlie�en des IndexWriter verhindert, wenn h�ufig zwischen InderWriter und
   * IndexReader gewechselt wird.
   *
   * @param doc Das vorzumerkende Dokument.
   */
  private void markForDeletion(Document doc) {
    if (mUrlsToDeleteHash == null) {
      mUrlsToDeleteHash = new HashMap();
    }

    String url = doc.get("url");
    String lastModified = doc.get("last-modified");
    if ((url != null) || (lastModified != null)) {
      mLog.info("Marking old entry for a later deletion: " + url + " from "
        + lastModified);
      mUrlsToDeleteHash.put(url, lastModified);
    }
  }


  /**
   * Gibt zur�ck, ob ein Dokument f�r die L�schung vorgemerkt wurde.
   *
   * @param doc Das zu pr�fende Dokument.
   * @return Ob das Dokument f�r die L�schung vorgemerkt wurde.
   */
  private boolean isMarkedForDeletion(Document doc) {
    String url = doc.get("url");
    String lastModified = doc.get("last-modified");

    if ((url == null) || (lastModified == null)) {
      // url und last-modified sind Mussfelder
      // Da eines fehlt -> Dokument l�schen
      return true;
    }

    if (mUrlsToDeleteHash == null) {
      // Es sind gar keine Dokumente zum L�schen vorgemerkt
      return false;
    }

    // Pr�fen, ob es einen Eintrag f�r diese URL gibt und ob er dem
    // last-modified des Dokuments entspricht
    String lastModifiedToDelete = (String) mUrlsToDeleteHash.get(url);
    return lastModified.equals(lastModifiedToDelete);
  }


  /**
   * Gibt die Anzahl der Eintr�ge im Index zur�ck.
   *
   * @return Die Anzahl der Eintr�ge im Index.
   * @throws RegainException Wenn die Anzahl nicht ermittelt werden konnte.
   */
  public int getIndexEntryCount() throws RegainException {
    if (mIndexReader != null) {
      return mIndexReader.numDocs();
    } else {
      setIndexMode(WRITING_MODE);
      return mIndexWriter.docCount();
    }
  }

  
  /**
   * Prepares a breakpoint.
   * 
   * @throws RegainException If preparing the breakpoint failed.
   */
  private void prepareBreakpoint() throws RegainException {
    // Testen, ob noch Eintr�ge f�r die L�schung vorgesehen sind
    if (mUrlsToDeleteHash != null) {
      throw new RegainException("There are still documents marked for deletion."
        + " The method removeObsoleteEntires(...) has to be called first.");
    }

    // Switch to ALL_CLOSED_MODE
    setIndexMode(ALL_CLOSED_MODE);
    
    // Close the error log of the index
    if (mErrorLogStream != null) {
      mErrorLogWriter.close();
      try {
        mErrorLogStream.close();
      }
      catch (IOException exc) {
        throw new RegainException("Closing error log file failed", exc);
      }
      
      mErrorLogWriter = null;
      mErrorLogStream = null;
    }
  }
  
  
  /**
   * Creates a breakpoint.
   * 
   * @throws RegainException If creating the breakpoint failed.
   */
  public void createBreakpoint() throws RegainException {
    mLog.info("Creating a breakpoint...");
    try {
      mBreakpointProfiler.startMeasuring();
      
      // Remove the entries that were marked for deletion 
      removeObsoleteEntries();
      
      // Prepare the breakpoint
      prepareBreakpoint();

      // Create a temp directory
      // NOTE: We copy to a temp directory and rename it when we are finished.
      File tempDir = new File(mBreakpointIndexDir.getAbsolutePath() + "_tmp");
      RegainToolkit.deleteDirectory(tempDir);
      tempDir.mkdir();

      // Copy the current working index to the breakpoint directory
      RegainToolkit.copyDirectory(mTempIndexDir, tempDir, false);

      // Delete the old breakpoint if it exists
      deleteOldIndex(mBreakpointIndexDir);
      
      // Rename the temp directory and let it become the new breakpoint
      if (! tempDir.renameTo(mBreakpointIndexDir)) {
        throw new RegainException("Renaming temporary copy directory failed: " +
            tempDir.getAbsolutePath());
      }

      // Stop measuring
      long breakpointSize = RegainToolkit.getDirectorySize(mBreakpointIndexDir);
      mBreakpointProfiler.stopMeasuring(breakpointSize);
    }
    catch (RegainException exc) {
      mBreakpointProfiler.abortMeasuring();
      throw exc;
    }
  }


  /**
   * Optimiert und schlie�t den Index
   *
   * @param putIntoQuarantine Gibt an, ob der Index in Quarant�ne soll.
   * @throws RegainException Wenn der Index nicht geschlossen werden konnte.
   */
  public void close(boolean putIntoQuarantine) throws RegainException {
    // Index optimieren
    try {
      setIndexMode(WRITING_MODE);
      mIndexWriter.optimize();
    }
    catch (IOException exc) {
      throw new RegainException("Finishing IndexWriter failed", exc);
    }

    // Prefetch destinct field values
    String[] prefetchFields = mConfig.getValuePrefetchFields();
    if (prefetchFields != null && prefetchFields.length != 0) {
      String msg = "Prefetching destinct field values for: ";
      for (int i = 0; i < prefetchFields.length; i++) {
        msg += (i != 0 ? ", " : "") + prefetchFields[i];
      }
      mLog.info(msg);

      setIndexMode(READING_MODE);
      RegainToolkit.readFieldValues(mIndexReader, prefetchFields, mTempIndexDir);
    }

    // Prepare the final 'breakpoint'
    // NOTE: This will set the ALL_CLOSED_MODE
    prepareBreakpoint();

    // Ressourcen der DocumentFactory freigeben
    mDocumentFactory.close();

    // Write all terms in the index into a file
    if (mAnalysisDir != null) {
      File termFile = new File(mAnalysisDir.getAbsolutePath() + File.separator
                               + "AllTerms.txt");
      writeTermFile(mTempIndexDir, termFile);
    }

    // Verzeichnis bestimmen, in das der Index kommen soll
    File targetDir;
    if (putIntoQuarantine) {
      targetDir = mQuarantineIndexDir;
    } else {
      targetDir = mNewIndexDir;
    }
    
    // If there is already the target directory -> delete it
    deleteOldIndex(targetDir);

    // Let the new index become the working index
    // Workaround: Siehe Javadoc von RENAME_TIMEOUT
    long deadline = System.currentTimeMillis() + RENAME_TIMEOUT;
    boolean renameSucceed = false;
    while ((! renameSucceed) && (System.currentTimeMillis() < deadline)) {
      renameSucceed = mTempIndexDir.renameTo(targetDir);
      try {
        Thread.sleep(100);
      }
      catch (Exception exc) {}
    }

    if (renameSucceed) {
      // Delete the last breakpoint if there should be one
      deleteOldIndex(mBreakpointIndexDir);
    } else {
      throw new RegainException("Renaming " + mTempIndexDir + " to " + targetDir
        + " failed after " + (RENAME_TIMEOUT / 1000) + " seconds!");
    }
  }


  /**
   * Delets an old index directory.
   * 
   * @param oldIndexDir The old index directory.
   * @throws RegainException If deleting failed.
   */
  private void deleteOldIndex(File oldIndexDir) throws RegainException {
    if (oldIndexDir.exists()) {
      // We rename it before deletion so there will be no problems when the
      // search mask tries not to switch to the new index during deletion. This
      // case is very unlikely but it may happen once in 100.000 years...
      File secureDir = new File(oldIndexDir.getAbsolutePath() + "_del");
      if (oldIndexDir.renameTo(secureDir)) {
        RegainToolkit.deleteDirectory(secureDir);
      } else {
        throw new RegainException("Deleting old index failed: " +
            oldIndexDir.getAbsolutePath());
      }
    }
  }


  /**
   * Erzeugt eine Datei, die alle Terme (also alle erlaubten Suchtexte) enth�lt.
   *
   * @param indexDir Das Verzeichnis, in dem der Index steht.
   * @param termFile Der Ort, wo die Datei erstellt werden soll.
   *
   * @throws RegainException Wenn die Erstellung fehlgeschlagen ist.
   */
  private void writeTermFile(File indexDir, File termFile) throws RegainException {
    IndexReader reader = null;
    FileOutputStream stream = null;
    PrintWriter writer = null;
    try {
      reader = IndexReader.open(indexDir);

      stream = new FileOutputStream(termFile);
      writer = new PrintWriter(stream);
      writer.println("This file was generated by the crawler and contains all "
                     + "terms in the index.");
      writer.println("It's no error when endings like 'e', 'en', and so on "
                     + "are missing.");
      writer.println("They have been cuttet by the GermanAnalyzer and will be "
                     + "cuttet from a search query too.");
      writer.println();

      // Write the terms
      TermEnum termEnum = reader.terms();
      int termCount;
      if (WRITE_TERMS_SORTED) {
        termCount = writeTermsSorted(termEnum, writer);
      } else {
        termCount = writeTermsSimply(termEnum, writer);
      }

      mLog.info("Wrote " + termCount + " terms into " + termFile.getAbsolutePath());
    }
    catch (IOException exc) {
      throw new RegainException("Writing term file failed", exc);
    }
    finally {
      if (reader != null) {
        try { reader.close(); } catch (IOException exc) {}
      }
      if (writer != null) {
        writer.close();
      }
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }



  /**
   * Schreibt die Terme so wie sie vom IndexReader kommen in den Writer.
   * <p>
   * Diese Methode braucht minimale Ressourcen.
   *
   * @param termEnum Die Aufz�hlung mit allen Termen.
   * @param writer Der Writer auf den geschrieben werden soll.
   *
   * @return Die Anzahl der Terme.
   * @throws IOException Wenn das Schreiben fehl schlug.
   */
  private int writeTermsSimply(TermEnum termEnum, PrintWriter writer)
    throws IOException
  {
    int termCount = 0;
    while (termEnum.next()) {
      Term term = termEnum.term();
      writer.println(term.text());
      termCount++;
    }

    return termCount;
  }



  /**
   * Schreibt die Terme vom IndexReader sortiert in den Writer.
   * <p>
   * Um die Terme sortieren zu k�nnen, m�ssen sie zwischengespeichert werden. Falls
   * es zu viele sind, k�nnte das schief gehen. In diesem Fall sollte man auf simples
   * Schreiben umstellen.
   *
   * @param termEnum Die Aufz�hlung mit allen Termen.
   * @param writer Der Writer auf den geschrieben werden soll.
   *
   * @return Die Anzahl der Terme.
   * @throws IOException Wenn das Schreiben fehl schlug.
   */
  private int writeTermsSorted(TermEnum termEnum, PrintWriter writer)
    throws IOException
  {
    // Put all terms in a list for a later sorting
    ArrayList list = new ArrayList();
    while (termEnum.next()) {
      Term term = termEnum.term();
      list.add(term.text());
    }

    String[] asArr = new String[list.size()];
    list.toArray(asArr);

    // Sort the terms
    Arrays.sort(asArr);

    // Write them to the writer
    for (int i = 0; i < asArr.length; i++) {
      writer.println(asArr[i]);
    }

    return asArr.length;
  }

}
