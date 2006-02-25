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
 *  $RCSfile: IndexSearcherManager.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/IndexSearcherManager.java,v $
 *     $Date: 2006/01/17 10:50:42 $
 *   $Author: til132 $
 * $Revision: 1.9 $
 */
package net.sf.regain.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

/**
 * Kapselt die Suche auf dem Suchindex.
 * <p>
 * Alle Suchanfragen werden synchronisiert. Außerdem wird im 10-Sekunden-Takt
 * geprüft, ob ein neuer Index verfügbar ist. Wenn ja, dann wird der neue Index
 * übernommen und der alte in einem Backup gesichert.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IndexSearcherManager {

  /**
   * Die Zeit in Millisekunden in der der Update-Thread zwischen zwei
   * Update-Prüfungen schlafen soll.
   */
  private static final int INDEX_UPDATE_THREAD_SLEEPTIME = 10000;

  /**
   * Der Name des Index-Unterverzeichnisses, in das der neue Index gestellt
   * wird, sobald er fertig ist.
   * <p>
   * Wenn dieses Verzeichnis existiert, dann wird die Suche auf den darin
   * enthaltenen Index umgestellt.
   */
  private static final String NEW_INDEX_SUBDIR = "new";
  /** Der Name des Index-Unterverzeichnisses, in dem der genutzte Index steht. */
  private static final String WORKING_INDEX_SUBDIR = "index";
  /**
   * Der Name des Index-Unterverzeichnisses, in das der letzte Index gesichert
   * werden soll.
   */
  private static final String BACKUP_INDEX_SUBDIR = "backup";

  /**
   * Enthält für ein Index-Verzeichnis (key) den zuständigen
   * IndexWriterManager (value).
   */
  private static HashMap mIndexManagerHash;

  /**
   * Das Verzeichnis, in das der neue Index gestellt wird, sobald er fertig ist.
   * <p>
   * Wenn dieses Verzeichnis existiert, dann wird die Suche auf den darin
   * enthaltenen Index umgestellt.
   */
  private File mNewIndexDir;
  /** Das Verzeichnis, in dem der genutzte Index steht. */
  private File mWorkingIndexDir;
  /** Das Verzeichnis, in das der letzte Index gesichert werden soll. */
  private File mBackupIndexDir;

  /**
   * Der IndexSearcher auf dem die Suchen erfolgen.
   * <p>
   * Die Klasse IndexSearcher ist zwar thread-sicher, aber wir synchronisieren
   * trotzdem alle Suchanfragen selbst, damit ein Austausch des Suchindex im
   * laufenden Betrieb möglich ist.
   */
  private IndexSearcher mIndexSearcher;

  /** The IndexReader to use for reading information from an index. */
  private IndexReader mIndexReader;
  
  /** Der Analyzer, der für Suchen verwendet werden soll. */
  private Analyzer mAnalyzer;

  /** Der Thread, der alle 10 Sekunden prüft, ob ein neuer Suchindex vorhanden ist. */
  private Thread mIndexUpdateThread;
  
  /**
   * Holds for a field name (String) all distinct values the index has for that
   * field (String[]).
   */
  private HashMap mFieldTermHash;


  /**
   * Erzeugt eine neue IndexWriterManager-Instanz.
   *
   * @param indexDir Das Verzeichnis, in dem der Index steht.
   */
  private IndexSearcherManager(String indexDir) {
    mNewIndexDir     = new File(indexDir + File.separator + NEW_INDEX_SUBDIR);
    mWorkingIndexDir = new File(indexDir + File.separator + WORKING_INDEX_SUBDIR);
    mBackupIndexDir  = new File(indexDir + File.separator + BACKUP_INDEX_SUBDIR);

    mIndexUpdateThread = new Thread() {
      public void run() {
        indexUpdateThreadRun();
      }
    };
    mIndexUpdateThread.setPriority(Thread.MIN_PRIORITY);
    mIndexUpdateThread.start();
  }



  /**
   * Gibt den IndexWriterManager für das gegebene Index-Verzeichnis zurück.
   *
   * @param indexDir Das Verzeichnis, in dem der Index steht.
   *
   * @return Der IndexWriterManager für das Index-Verzeichnis.
   */
  public static synchronized IndexSearcherManager getInstance(String indexDir) {
    if (mIndexManagerHash == null) {
      mIndexManagerHash = new HashMap();
    }

    // Zuständigen IndexWriterManager aus der Hash zu holen
    IndexSearcherManager manager = (IndexSearcherManager) mIndexManagerHash.get(indexDir);
    if (manager == null) {
      // Für diesen Index gibt es noch keinen Manager -> einen anlegen
      manager = new IndexSearcherManager(indexDir);
      mIndexManagerHash.put(indexDir, manager);
    }

    return manager;
  }



  /**
   * Sucht im Suchindex.
   * <p>
   * Hinweis: Suchen und Update-Checks laufen synchronisiert ab (also niemals
   * gleichzeitig).
   *
   * @param query Die Suchanfrage.
   *
   * @return Die gefundenen Treffer.
   * @throws RegainException Wenn die Suche fehl schlug.
   */
  public synchronized Hits search(Query query) throws RegainException {
    if (mIndexSearcher == null) {
      if (! mWorkingIndexDir.exists()) {
        checkForIndexUpdate();
      }

      try {
        mIndexSearcher = new IndexSearcher(mWorkingIndexDir.getAbsolutePath());
      }
      catch (IOException exc) {
        throw new RegainException("Creating index searcher failed", exc);
      }
    }

    try {
      return mIndexSearcher.search(query);
    }
    catch (IOException exc) {
      throw new RegainException("Searching query failed", exc);
    }
  }

  
  /**
   * Gets an IndexReader for the index.
   * <p>
   * NOTE: Must be called in a synchronized block.
   * 
   * @return An IndexReader for the index.
   * @throws RegainException If creating the IndexReader failed.
   */
  private IndexReader getIndexReader() throws RegainException {
    if (mIndexReader == null) {
      if (! mWorkingIndexDir.exists()) {
        checkForIndexUpdate();
      }
  
      try {
        mIndexReader = IndexReader.open(mWorkingIndexDir.getAbsolutePath());
      }
      catch (IOException exc) {
        throw new RegainException("Creating index reader failed", exc);
      }
    }
    
    return mIndexReader;
  }


  /**
   * Gets all distinct values a index has for a certain field. The values are
   * sorted alphabetically.
   * 
   * @param field The field to get the values for.
   * @return All distinct values the index has for the field.
   * @throws RegainException If reading the values failed.
   */
  public synchronized String[] getFieldValues(String field) throws RegainException {
    if (mFieldTermHash == null) {
      mFieldTermHash = new HashMap();
    }
    
    String[] valueArr = (String[]) mFieldTermHash.get(field);
    if (valueArr == null) {
      // Read the terms
      try {
        TermEnum termEnum = getIndexReader().terms();

        ArrayList valueList = new ArrayList();
        while(termEnum.next()) {
          Term term = termEnum.term();
          if (term.field().equals(field)) {
            valueList.add(term.text());
          }
        }
        
        // Convert the list into an array.
        valueArr = new String[valueList.size()];
        valueList.toArray(valueArr);
        
        // Sort the array
        Arrays.sort(valueArr);
      }
      catch (IOException exc) {
        throw new RegainException("Reading terms from index failed", exc);
      }
      
      // Cache the values
      mFieldTermHash.put(field, valueArr);
    }
    
    return valueArr;
  }


  /**
   * Gets the total number of documents in the index. 
   *  
   * @return The total number of documents in the index.
   * @throws RegainException If getting the document count failed.
   */
  public synchronized int getDocumentCount() throws RegainException {
    return getIndexReader().numDocs();
  }


  /**
   * Gibt den Analyzer zurück, der für die Suche genutzt werden soll.
   *
   * @return Der Analyzer.
   * @throws RegainException Wenn die Erzeugung des Analyzers fehl schlug.
   */
  public synchronized Analyzer getAnalyzer() throws RegainException {
    if (mAnalyzer == null) {
      if (! mWorkingIndexDir.exists()) {
        // There is no working index -> check whether there is a new one
        checkForIndexUpdate();
      }

      if (! mWorkingIndexDir.exists()) {
        // There is no working and no new index -> throw exception
        throw new RegainException("No index found in "
            + mWorkingIndexDir.getParentFile().getAbsolutePath());
      }
      
      // Read the stopWordList and the exclusionList
      File analyzerTypeFile = new File(mWorkingIndexDir, "analyzerType.txt");
      String analyzerType = RegainToolkit.readStringFromFile(analyzerTypeFile);
      File stopWordListFile = new File(mWorkingIndexDir, "stopWordList.txt");
      String[] stopWordList = readWordListFromFile(stopWordListFile);
      File exclusionListFile = new File(mWorkingIndexDir, "exclusionList.txt");
      String[] exclusionList = readWordListFromFile(exclusionListFile);

      File untokenizedFieldNamesFile = new File(mWorkingIndexDir, "untokenizedFieldNames.txt");
      String[] untokenizedFieldNames;
      if (untokenizedFieldNamesFile.exists()) {
          untokenizedFieldNames = readWordListFromFile(untokenizedFieldNamesFile); 
      } else {
          untokenizedFieldNames = new String[0];
      }

      // NOTE: Make shure to use the same analyzer in the crawler
      mAnalyzer = RegainToolkit.createAnalyzer(analyzerType, stopWordList,
                                               exclusionList, untokenizedFieldNames);
    }

    return mAnalyzer;
  }


  /**
   * Liest eine Wortliste aus einer Datei.
   *
   * @param file Die Datei aus der die Liste gelesen werden soll.
   *
   * @return Die Zeilen der Datei.
   * @throws RegainException Wenn das Lesen fehl schlug.
   */
  private String[] readWordListFromFile(File file) throws RegainException {
    if (! file.exists()) {
      return null;
    }

    FileReader reader = null;
    BufferedReader buffReader = null;
    try {
      reader = new FileReader(file);
      buffReader = new BufferedReader(reader);

      ArrayList list = new ArrayList();
      String line;
      while ((line = buffReader.readLine()) != null) {
        list.add(line);
      }

      String[] asArr = new String[list.size()];
      list.toArray(asArr);

      return asArr;
    }
    catch (IOException exc) {
      throw new RegainException("Reading word list from " + file.getAbsolutePath()
        + "failed", exc);
    }
    finally {
      if (buffReader != null) {
        try { buffReader.close(); } catch (IOException exc) {}
      }
      if (reader != null) {
        try { reader.close(); } catch (IOException exc) {}
      }
    }
  }



  /**
   * Die run()-Methode des Index-Update-Thread.
   *
   * @see #mIndexUpdateThread
   */
  void indexUpdateThreadRun() {
    while (true) {
      try {
        checkForIndexUpdate();
      }
      catch (RegainException exc) {
        System.out.println("Updating index failed!");
        exc.printStackTrace();
      }

      try {
        Thread.sleep(INDEX_UPDATE_THREAD_SLEEPTIME);
      }
      catch (InterruptedException exc) {}
    }
  }


  /**
   * Prüft, ob ein neuer Index vorhanden ist. Wenn ja, dann wird die Suche auf den
   * neuen Index umgestellt.
   * <p>
   * Hinweis: Suchen und Update-Checks laufen synchronisiert ab (also niemals
   * gleichzeitig).
   *
   * @throws RegainException Falls die Umstellung auf den neuen Index fehl schlug.
   */
  private synchronized void checkForIndexUpdate() throws RegainException {
    if (mNewIndexDir.exists()) {
      System.out.println("New index found on " + new java.util.Date());

      // Close the IndexSearcher
      if (mIndexSearcher != null) {
        try {
          mIndexSearcher.close();
        }
        catch (IOException exc) {
          throw new RegainException("Closing index searcher failed", exc);
        }

        // Force the creation of a new IndexSearcher and Analyzer next time it
        // will be needed
        mIndexSearcher = null;
        mAnalyzer = null;
      }
      
      // Close the IndexReader
      if (mIndexReader != null) {
        try {
          mIndexReader.close();
        }
        catch (IOException exc) {
          throw new RegainException("Closing index reader failed", exc);
        }

        // Force the creation of a new IndexReader and mFieldTermHash next time
        // it will be needed
        mIndexReader = null;
        mFieldTermHash = null;
      }

      // Remove the old backup if it should still exist
      if (mBackupIndexDir.exists()) {
        RegainToolkit.deleteDirectory(mBackupIndexDir);
      }

      // Backup the current index (if there is one)
      if (mWorkingIndexDir.exists()) {
        if (! mWorkingIndexDir.renameTo(mBackupIndexDir)) {
          throw new RegainException("Renaming " + mWorkingIndexDir + " to "
            + mBackupIndexDir + " failed!");
        }
      }

      // Move the new index
      if (! mNewIndexDir.renameTo(mWorkingIndexDir)) {
        throw new RegainException("Renaming " + mNewIndexDir + " to "
          + mWorkingIndexDir + " failed!");
      }
    }
  }

}
