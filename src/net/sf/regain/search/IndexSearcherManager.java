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
 *     $Date: 2012-10-03 10:19:29 +0200 (Mi, 03 Okt 2012) $
 *   $Author: benjaminpick $
 * $Revision: 626 $
 */
package net.sf.regain.search;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;

/**
 * Encapsulates the search on the lucene search index.
 * <p>
 * Additionally, every 10 seconds we check if there is a new index available.
 * Search queries cannot be execute during index update.
 * If yes, the new index is used and the old saved in /backup/.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IndexSearcherManager implements Closeable {

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
   * enthält für ein Index-Verzeichnis (key) den zust�ndigen
   * IndexWriterManager (value).
   */
  private static HashMap<String, IndexSearcherManager> mIndexManagerHash;
  
  static {
    mIndexManagerHash = new HashMap<String, IndexSearcherManager>();
  }
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
  
  /** Der Analyzer, der für Suchen verwendet werden soll. */
  private Analyzer mAnalyzer;
  
  /** Der Thread, der alle 10 Sekunden Prüft, ob ein neuer Suchindex vorhanden ist. */
  private IndexUpdateThread mIndexUpdateThread;
  /**
   * Holds for a field name (String) all distinct values the index has for that
   * field (String[]).
   */
  private HashMap<String,String[]> mFieldTermHash;
  
  /**
   * Keeps reference of the current IndexSearcher.
   */
  private SearcherManager mSearcherManager;

  /**
   * Erzeugt eine neue IndexWriterManager-Instanz.
   *
   * @param indexDir Das Verzeichnis, in dem der Index steht.
   * @throws RegainException Wenn kein Index-Verzeichnis existiert.
   */
  private IndexSearcherManager(String indexDir) throws RegainException {
    mNewIndexDir = new File(indexDir + File.separator + NEW_INDEX_SUBDIR);
    mWorkingIndexDir = new File(indexDir + File.separator + WORKING_INDEX_SUBDIR);
    mBackupIndexDir = new File(indexDir + File.separator + BACKUP_INDEX_SUBDIR);

    if (!(new File(indexDir).exists()))
      throw new RegainException("No index folder found at " + indexDir);
    
    checkForIndexUpdate();

    try
    {
      mSearcherManager = new SearcherManager(FSDirectory.open(mWorkingIndexDir), new SearcherFactory());
    }
    catch (IOException e)
    {
      throw new RegainException("Could not open index folder found at " + indexDir, e);
    }
    
    mIndexUpdateThread = new IndexUpdateThread();
    mIndexUpdateThread.setPriority(Thread.MIN_PRIORITY);
    mIndexUpdateThread.start();
  }

  /**
   * Return the index searcher manager of a specific directory.
   *
   * @param indexDir Directory of the index.
   *
   * @return An instance of the corresponding index searcher manager.
   * @throws RegainException If the directory does not exist or is not an index.
   */
  public static IndexSearcherManager getInstance(String indexDir) throws RegainException {
    synchronized (mIndexManagerHash)
    {
      // Retrieve the responsible IndexSearcherManager for this index.
      IndexSearcherManager manager = mIndexManagerHash.get(indexDir);
      if (manager == null) {
        // There is none yet, so create a new one.
        manager = new IndexSearcherManager(indexDir);
        mIndexManagerHash.put(indexDir, manager);
      }
  
      return manager;
    }
  }

  /**
   * Gets all distinct values a index has for a certain field. The values are
   * sorted alphabetically.
   *
   * @param field The field to get the values for.
   * @return All distinct values the index has for the field.
   * @throws RegainException If reading the values failed.
   */
  public String[] getFieldValues(String field) throws RegainException {
    if (mFieldTermHash == null) {
      mFieldTermHash = new HashMap<String,String[]>();
    }

    String[] valueArr = mFieldTermHash.get(field);
    if (valueArr == null) {
      valueArr = readFieldValues(field);
      // Copy the field values to our cache
      mFieldTermHash.put(field, valueArr);
    }

    return valueArr;
  }

  /**
   * Read files from index cache file
   * (synchronised, as this requires an searchindex/index directory)
   * 
   * @param field The field to get the values for.
   * @return All distinct values the index has for the field.
   * @throws RegainException If reading the values failed.
   */
  protected synchronized String[] readFieldValues(String field) throws RegainException
  {
    String[] valueArr;
    
    IndexSearcher searcher = null;
    try {
       searcher = getIndexSearcher();
      // Read the field values
      HashMap<String,String[]> valueMap = RegainToolkit.readFieldValues(searcher.getIndexReader(),
              new String[]{field}, mWorkingIndexDir);
      valueArr = valueMap.get(field);
    } finally {
      releaseIndexSearcher(searcher);
    }
    return valueArr;
  }

  /**
   * Gets the total number of documents in the index.
   *
   * @return The total number of documents in the index.
   * @throws RegainException If getting the document count failed.
   */
  public int getDocumentCount() throws RegainException {
    IndexSearcher searcher = null;
    try {
      searcher = getIndexSearcher();
      return searcher.getIndexReader().numDocs();
    } finally {
      releaseIndexSearcher(searcher);
    }
  }

  /**
   * Gibt den Analyzer zurück, der für die Suche genutzt werden soll.
   *
   * @return Der Analyzer.
   * @throws RegainException Wenn die Erzeugung des Analyzers fehl schlug.
   */
  public Analyzer getAnalyzer() throws RegainException {
    if (mAnalyzer == null) {
      createAnalyzer();
    }

    return mAnalyzer;
  }

  protected synchronized void createAnalyzer() throws RegainException
  {
    if (mAnalyzer != null)
      return;
    
    ensureIndexDirExists();

    // Read the stopWordList and the exclusionList
    File analyzerTypeFile = new File(mWorkingIndexDir, "analyzerType.txt");
    String analyzerType = RegainToolkit.readStringFromFile(analyzerTypeFile);
    File stopWordListFile = new File(mWorkingIndexDir, "stopWordList.txt");
    String[] stopWordList = RegainToolkit.readListFromFile(stopWordListFile);
    File exclusionListFile = new File(mWorkingIndexDir, "exclusionList.txt");
    String[] exclusionList = RegainToolkit.readListFromFile(exclusionListFile);

    File untokenizedFieldNamesFile = new File(mWorkingIndexDir, "untokenizedFieldNames.txt");
    String[] untokenizedFieldNames;
    if (untokenizedFieldNamesFile.exists()) {
      untokenizedFieldNames = RegainToolkit.readListFromFile(untokenizedFieldNamesFile);
    } else {
      untokenizedFieldNames = new String[0];
    }

    // NOTE: Make shure to use the same analyzer as in the crawler
    mAnalyzer = RegainToolkit.createAnalyzer(analyzerType, stopWordList,
            exclusionList, untokenizedFieldNames);
  }

  /**
   * Check if the index directory exists. If not,
   * @throws RegainException
   */
  private synchronized void ensureIndexDirExists() throws RegainException
  {
    if (!mWorkingIndexDir.exists()) {
      // There is no working index -> check whether there is a new one
      checkForIndexUpdate();
    }

    if (!mWorkingIndexDir.exists()) {
      // There is no working and no new index -> throw exception
      throw new RegainException("No index found in "
              + mWorkingIndexDir.getParentFile().getAbsolutePath());
    }
  }

  /**
   * Check if there is a new index available.
   * If so, prepare it by renaming the dirs - it will be opened at the next search.
   * <p>
   * NOTE: We synchronize this index update with all methods that directly
   * access the underlying files.
   *
   * @throws RegainException If error during update of index.
   */
  private void checkForIndexUpdate() throws RegainException {
    if (mNewIndexDir.exists()) {
      synchronized (this)
      {
        if (mNewIndexDir.exists())
        {
          System.out.println("New index found on " + new java.util.Date());
          
          // Recreate analyzer and field term cache on next use
          mAnalyzer = null;
          mFieldTermHash = null;
          
          // ---- Okay, now we can move the directories
          
          // Remove the old backup if it should still exist
          if (mBackupIndexDir.exists()) {
            RegainToolkit.deleteDirectory(mBackupIndexDir);
          }

          // Backup the current index (if there is one)
          if (mWorkingIndexDir.exists()) {
            if (!mWorkingIndexDir.renameTo(mBackupIndexDir)) {
              throw new RegainException("Renaming " + mWorkingIndexDir + " to "
                      + mBackupIndexDir + " failed!");
            }
          }

          // Move the new index
          if (!mNewIndexDir.renameTo(mWorkingIndexDir)) {
            throw new RegainException("Renaming " + mNewIndexDir + " to "
                    + mWorkingIndexDir + " failed!");
          }
          
          try
          {
            if (mSearcherManager != null)
              mSearcherManager.maybeRefresh();
          }
          catch (IOException e)
          {
            throw new RegainException("Refresh of lucene index failed.");
          }
          
          System.out.println("Finished loading new index.");          
        }
      }
    }
  }

  /**
   * Returns the IndexSearcher.
   *
   * Must be released after use:
   * 
   * try
   * {
   *  indexSearcher = getIndexSearcher();
   *  
   *  ...
   * }
   * finally
   * {
   *   indexManager.releaseIndexSearcher(indexSearcher);
   *   indexSearcher = null;
   * }
   *
   * @return the IndexSearcher
   */
  public IndexSearcher getIndexSearcher() throws RegainException {
    ensureIndexDirExists();
    return mSearcherManager.acquire();
  }
  
  /**
   * Release an indexSearcher that was acquired by getIndexSearcher()
   * NOTE: It musn't be used afterwards!
   * 
   * @param searcher  Reference to the index searcher. (Null is silently ignored.)
   * @throws RegainException
   */
  public void releaseIndexSearcher(IndexSearcher searcher) throws RegainException {
    if (searcher == null)
        return;
    
    try {
      mSearcherManager.release(searcher);
    } catch (IOException e) {
      throw new RegainException("Release failed", e);
    }
  }

  @Override
  public void close() throws IOException
  {
    if (mIndexUpdateThread != null)
      mIndexUpdateThread.close();
    mIndexUpdateThread = null;
    
    mSearcherManager.close();
    mSearcherManager = null;
  }

  public static void closeAll() throws IOException
  {
    for (Closeable indexManager : mIndexManagerHash.values())
    {
      indexManager.close();
    }
    mIndexManagerHash.clear();
    
    try
    {
      // Give the threads some time to shut down before returning
      Thread.sleep(100);
    }
    catch (InterruptedException e) { }
  }
  
  /**
   * WARNING: Thread Programming ahead.
   * Every single line may have its importance.
   *
   */
  private class IndexUpdateThread extends Thread implements Closeable
  {
    /**
     * Nb of milliseconds between a check if a new index is available.
     */
    private static final int INDEX_UPDATE_THREAD_SLEEPTIME = 10000;
    
    private volatile boolean quit = false;
    
    @Override
    public void run() {
      // Do while no termination is requested
      while (!quit)
      {
        // Check regularly for new indexes
        try {
          checkForIndexUpdate();
        } catch (RegainException exc) {
          System.out.println("Updating index failed!");
          exc.printStackTrace(System.err);
        }

        // Wait some time (approx. 10 sec, but don't be disappointed if it's more or less)
        try {
          Thread.sleep(INDEX_UPDATE_THREAD_SLEEPTIME);
        } catch (InterruptedException exc) {
        }
      }
    }
    
    /**
     * Request termination of thread.
     * Will terminate as soon as possible.
     */
    public void close()
    {
      quit = true;
      interrupt(); // Wake him up if he's sleeping!
    }
  }
}
