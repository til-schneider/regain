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
 *  $RCSfile: IndexWriterManager.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/IndexWriterManager.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.Configuration;
import net.sf.regain.crawler.document.DocumentFactory;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.log4j.Category;
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
 * Rufen Sie für jedes Dokument {@link #addToIndex(RawDocument)} auf. Rufen Sie
 * am Ende {@link #close(boolean)} auf, um den Index zu schließen. Danach sind
 * keine weiteren Aufrufe von {@link #addToIndex(RawDocument)} erlaubt.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class IndexWriterManager {

  /** Die Kategorie, die zum Loggen genutzt werden soll. */
  private static Category mCat = Category.getInstance(IndexWriterManager.class);

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
   * Gibt an, ob die Terme sortiert in die Terme-Datei geschrieben werden soll.
   *
   * @see #writeTermFile(File, File)
   */
  private static final boolean WRITE_TERMS_SORTED = true;

  /**
   * Workaround: Unter Windows klappt das Umbenennen unmittelbar nach Schließen
   * des Index nicht. Wahrscheinlich sind die Filepointer auf die gerade
   * geschlossenen Dateien noch nicht richtig aufgeräumt, so dass ein Umbenennen
   * des Indexverzeichnisses fehl schlägt. Das Umbenennen wird daher regelmäßig
   * probiert, bis es entweder funktioniert oder bis der Timeout abgelaufen ist.
   */
  private static final long RENAME_TIMEOUT = 60000; // 1 min
  
  /**
   * Der Hinzufüge-Modus.
   * @see #setIndexMode(int)
   */
  private static final int ADDING_MODE = 1;
  /**
   * Der Lösch-Modus.
   * @see #setIndexMode(int)
   */
  private static final int DELETING_MODE = 2;
  /**
   * Der Beendet-Modus.
   * @see #setIndexMode(int)
   */
  private static final int FINISHED_MODE = 3;

  /** Der Analyzer, der vom IndexWriter genutzt werden soll. */
  private Analyzer mAnalyzer;

  /** Der gekapselte IndexWriter, der den eigentlichen Index erstellt. */
  private IndexWriter mIndexWriter;
  
  /**
   * Der gekapselte IndexReader. Wird zum Löschen von Dokumenten aus dem Index
   * benötigt.
   * <p>
   * Ist <code>null</code>, wenn der Index nicht aktualisiert werden soll.
   */
  private IndexReader mIndexReader;
  
  /**
   * Der gekapselte IndexSearcher. Wird zum Finden von Dokumenten benötigt.
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

  /** Die DocumentFactory, die die Inhalte für die Indizierung aufbereitet. */
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

  /** Das Verzeichnis, in dem die Analyse-Dateien erstellt werden soll. */
  private File mAnalysisDir;

  /** Der Profiler der das Hinzufügen zum Index mißt. */
  private Profiler mAddToIndexProfiler = new Profiler("Indexed documents", "docs");
  
  /**
   * Enthält die URL und den LastUpdated-String aller Dokumente, deren Einträge
   * beim Abschließen des Index entfernt werden müssen.
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
   *
   * @throws RegainException Wenn der neue Index nicht vorbereitet werden konnte.
   */
  public IndexWriterManager(Configuration config, boolean updateIndex)
    throws RegainException
  {
    mUpdateIndex = updateIndex;
    
    File indexDir = new File(config.getIndexDir());
    
    if (! indexDir.exists()) {
      // NOTE: The index directory does not exist.
      //       We could just create it, but it's more savely to throw an
      //       exception. We don't wan't to destroy anything.
      throw new RegainException("The index directory " + indexDir + " does not exist");
    }
    
    mNewIndexDir        = new File(indexDir, NEW_INDEX_SUBDIR);
    mQuarantineIndexDir = new File(indexDir, QUARANTINE_INDEX_SUBDIR);
    mTempIndexDir       = new File(indexDir, TEMP_INDEX_SUBDIR);

    // Delete the old temp index directory if it should still exist
    if (mTempIndexDir.exists()) {
      RegainToolkit.deleteDirectory(mTempIndexDir);
    }
    // and create a new, empty one
    if (! mTempIndexDir.mkdir()) {
      throw new RegainException("Creating working directory failed: "
                                + mTempIndexDir.getAbsolutePath());
    }
    
    // Create the Analyzer
    // NOTE: Make shure you use the same Analyzer in the SearchContext too!
    String analyzerType = config.getAnalyzerType();
    String[] stopWordList = config.getStopWordList();
    String[] exclusionList = config.getExclusionList();
    mAnalyzer = RegainToolkit.createAnalyzer(analyzerType, stopWordList,
        exclusionList);
    
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
        mIndexWriter = new IndexWriter(mTempIndexDir, mAnalyzer, true);
      } catch (IOException exc) {
        throw new RegainException("Creating new index failed", exc);
      }
    }
    
    // Check whether we need a IndexSearcher
    if (updateIndex) {
      try {
        mIndexSearcher = new IndexSearcher(mTempIndexDir.getAbsolutePath());
      } catch (IOException exc) {
        throw new RegainException("Creating IndexSearcher failed", exc);
      }
    }
    
    // Write the stopWordList and the exclusionList in a file so it can be found
    // by the search mask
    String[] analyzerTypeAsList = new String[] { analyzerType };
    writeListToFile(analyzerTypeAsList, new File(mTempIndexDir, "analyzerType.txt"));
    writeListToFile(stopWordList, new File(mTempIndexDir, "stopWordList.txt"));
    writeListToFile(exclusionList, new File(mTempIndexDir, "exclusionList.txt"));
    
    // Prepare the analysis directory if wanted
    if (config.getWriteAnalysisFiles()) {
      mAnalysisDir = new File(mTempIndexDir.getAbsolutePath() + File.separator
                              + "analysis");

      if (! mAnalysisDir.mkdir()) {
        throw new RegainException("Creating analysis directory failed: "
                                  + mAnalysisDir.getAbsolutePath());
      }
    }

    mDocumentFactory = new DocumentFactory(mAnalysisDir,
                                           config.getPreparatorSettingsList(),
                                           config.getHtmlContentExtractors(),
                                           config.getHtmlPathExtractors(),
                                           config.getUseLinkTextAsTitleRegexList());
  }

  
  /**
   * Gibt zurück, ob ein bestehender Index aktualisiert wird.
   * <p>
   * Anderenfalls wird ein komplett neuer Index angelegt.
   * 
   * @return Ob ein bestehender Index aktualisiert wird.
   */
  public boolean getUpdateIndex() {
    return mUpdateIndex;
  }
  

  /**
   * Setzt den aktuellen Modus.
   * <p>
   * Es gibt folgende Modi:
   * <ul>
   *   <li>Hinzufüge-Modus: Hier ist der mIndexWriter und mIndexSearcher
   *     geöffnet, der mIndexReader ist ausgeschalten. In diesem Modus können
   *     Dokumente zum Index hinzugefügt und gesucht werden.</li>
   *   <li>Lösch-Modus: Hier sind mIndexReader und mIndexSearcher geöffnet, der
   *     mIndexWriter ist ausgeschalten. In diesem Modus können Dokumente aus
   *     dem Index gelöscht und gesucht werden.</li>
   *   <li>Beendet-Modus: Hier sind alle Zugriffe auf den Index ausgeschalten:
   *     mIndexWriter, mIndexReader und mIndexSearcher. In diesem Modus kann
   *     gar nicht auf den Index zugegriffen werden.
   * </ul>
   * <p>
   * Falls der Index bereits im entsprechenden Modus ist, dann passiert nichts.
   * Diese Methode ist in diesem Fall sehr schnell.
   * 
   * @param mode Der Modus, in den der Index versetzt werden soll. Muss entweder
   *        {@link #ADDING_MODE}, {@link #DELETING_MODE} oder
   *        {@link #FINISHED_MODE} sein.
   * @throws RegainException Wenn beim schließen oder öffnen etwas schief ging.
   */
  private void setIndexMode(int mode) throws RegainException {
    // Close the mIndexReader in ADDING_MODE and FINISHED_MODE
    if ((mode == ADDING_MODE) || (mode == FINISHED_MODE)) {
      if (mIndexReader != null) {
        try {
          mIndexReader.close();
          mIndexReader = null;
        } catch (IOException exc) {
          throw new RegainException("Closing IndexReader failed", exc);
        }
      }
    }

    // Close the mIndexWriter in DELETING_MODE and FINISHED_MODE
    if ((mode == DELETING_MODE) || (mode == FINISHED_MODE)) {
      if (mIndexWriter != null) {
        try {
          mIndexWriter.close();
          mIndexWriter = null;
        } catch (IOException exc) {
          throw new RegainException("Closing IndexWriter failed", exc);
        }
      }
    }
    
    // Close the mIndexSearcher in FINISHED_MODE
    if ((mode == FINISHED_MODE) && (mIndexSearcher != null)) {
      try {
        mIndexSearcher.close();
        mIndexSearcher = null;
      } catch (IOException exc) {
        throw new RegainException("Closing IndexSearcher failed", exc);
      }
    }
    
    // Open the mIndexWriter in ADDING_MODE
    if ((mode == ADDING_MODE) && (mIndexWriter == null)) {
      mCat.info("Switching to index mode: adding mode");
      try {
        mIndexWriter = new IndexWriter(mTempIndexDir, mAnalyzer, false);
      } catch (IOException exc) {
        throw new RegainException("Creating IndexWriter failed", exc);
      }
    }
    
    // Open the mIndexReader in DELETING_MODE
    if ((mode == DELETING_MODE) && (mIndexReader == null)) {
      mCat.info("Switching to index mode: deleting mode");
      try {
        mIndexReader = IndexReader.open(mTempIndexDir);
      } catch (IOException exc) {
        throw new RegainException("Creating IndexReader failed", exc);
      }
    }
    
    // Tell the user, when the index is finished
    if (mode == FINISHED_MODE) {
      mCat.info("Switching to index mode: finished mode");
    }
  }


  /**
   * Kopiert den zuletzt erstellten Index in das Arbeitsverzeichnis.
   * 
   * @param indexDir Das Verzeichnis, in dem der Index liegt.
   * @param analyzerType Der Analyzer-Typ, den der alte Index haben muss, um
   *        übernommen zu werden.
   * @return Ob ein alter Index gefunden wurde.
   * @throws RegainException Wenn das Kopieren fehl schlug.
   */
  private boolean copyExistingIndex(File indexDir, String analyzerType)
    throws RegainException
  {
    // Neuesten, kompletten Index finden
    File oldIndexDir;
    if (mNewIndexDir.exists()) {
      oldIndexDir = mNewIndexDir;
    } else {
      // Es gibt keinen neuen Index -> Wir müssen den Index nehmen, der gerade
      // verwendet wird
      oldIndexDir = new File(indexDir, WORKING_INDEX_SUBDIR);
    }
    if (! oldIndexDir.exists()) {
      mCat.warn("Can't update index, because there was no old index. " +
        "A complete new index will be created...");
      return false;
    }
    
    // Analyzer-Typ des alten Index prüfen
    File analyzerTypeFile = new File(oldIndexDir, "analyzerType.txt");
    String analyzerTypeOfIndex = RegainToolkit.readStringFromFile(analyzerTypeFile);
    if ((analyzerTypeOfIndex == null)
      || (! analyzerType.equals(analyzerTypeOfIndex.trim())))
    {
      mCat.warn("Can't update index, because the index was created using " +
        "another analyzer type (index type: '" + analyzerTypeOfIndex.trim() +
        "', configured type '" + analyzerType + "'). " +
        "A complete new index will be created...");
      return false;
    }
    
    // Index in Arbeitsverzeichnis kopieren
    mCat.info("Updating index from " + oldIndexDir.getAbsolutePath());
    File[] indexFiles = oldIndexDir.listFiles();
    for (int i = 0; i < indexFiles.length; i++) {
      String fileName = indexFiles[i].getName();
      if ((! indexFiles[i].isDirectory()) && (! fileName.endsWith(".txt"))) {
        // Datei ist weder Verzeichnis, noch Textdatei -> kopieren
        File target = new File(mTempIndexDir, fileName);
        CrawlerToolkit.copyFile(indexFiles[i], target);
      }
    }
    
    return true;
  }
  
  
  /**
   * Schreibt eine Wortliste in eine Datei.
   *
   * @param wordList Die Wortliste.
   * @param file Die Datei, in die geschrieben werden soll.
   *
   * @throws RegainException Wenn die Erstellung der Liste fehl schlug.
   */
  private void writeListToFile(String[] wordList, File file)
    throws RegainException
  {
    if ((wordList == null) || (wordList.length == 0)) {
      // Nothing to do
      return;
    }

    FileOutputStream stream = null;
    PrintStream printer = null;
    try {
      stream = new FileOutputStream(file);
      printer = new PrintStream(stream);

      for (int i = 0; i < wordList.length; i++) {
        printer.println(wordList[i]);
      }
    }
    catch (IOException exc) {
      throw new RegainException("Writing word list to " + file.getAbsolutePath()
        + " failed", exc);
    }
    finally {
      if (printer != null) {
        printer.close();
      }
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }


  /**
   * Fügt ein Dokument dem Index hinzu.
   * <p>
   * Anhand der URL wird der Typ des Dokuments erkannt.
   *
   * @param rawDocument Das zu indizierende Dokument.
   *
   * @throws RegainException Wenn das Hinzufügen zum Index scheiterte.
   */
  public void addToIndex(RawDocument rawDocument) throws RegainException {
    // Prüfen, ob es einen aktuellen Indexeintrag gibt
    if (mUpdateIndex) {
      boolean removeOldEntry = false;
      
      // Alten Eintrag suchen
      Term urlTerm = new Term("url", rawDocument.getUrl());
      Query query = new TermQuery(urlTerm);
      Document doc;
      try {
        Hits hits = mIndexSearcher.search(query);
        if (hits.length() > 0) {
          if (hits.length() > 1) {
            mCat.warn("There are duplicate entries (" + hits.length() + " in " +
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
      
      // Wenn ein Dokument gefunden wurde, dann prüfen, ob Indexeintrag aktuell ist 
      if (doc != null) {
        Date docLastModified = rawDocument.getLastModified();
        if (docLastModified == null) {
          // Wir können nicht feststellen, wann das Dokument zuletzt geändert
          // wurde (Das ist bei http-URLs der Fall)
          // -> Alten Eintrag löschen und Dokument neu indizieren
          mCat.info("Don't know when the document was last modified. " +
            "Creating a new index entry...");
          removeOldEntry = true;
        } else {
          // Änderungsdatum mit dem Datum des Indexeintrages vergleichen
          String asString = doc.get("last-modified");
          if (asString != null) {
            Date indexLastModified = RegainToolkit.stringToLastModified(asString);
            
            long diff = docLastModified.getTime() - indexLastModified.getTime();
            if (diff > 60000L) {
              // Das Dokument ist mehr als eine Minute neuer
              // -> Der Eintrag ist nicht aktuell -> Alten Eintrag löschen
              mCat.info("Index entry is outdated. Creating a new one... ("
                + docLastModified + " > " + indexLastModified + ")");
              removeOldEntry = true;
            } else {
              // Der Indexeintrag ist aktuell -> Wir sind fertig
              mCat.info("Index entry is already up to date");
              return;
            }
          } else {
            // Wir kennen das Änderungsdatum nicht -> Alten Eintrag löschen
            mCat.info("Index entry has no last-modified field. Creating a new one...");
            removeOldEntry = true;
          }
        }
      }

      // Evtl. alten Eintrag löschen
      if (removeOldEntry) {
        // Eintrag nicht sofort löschen, sondern nur zum Löschen vormerken.
        // Siehe markForDeletion(Document)
        markForDeletion(doc);
      }
    }
    
    // Neuen Eintrag erzeugen
    createNewIndexEntry(rawDocument);
  }


  /**
   * Erzeugt für ein Dokument einen neuen Indexeintrag.
   * 
   * @param rawDocument Das Dokument für das der Eintrag erzeugt werden soll
   * @throws RegainException Wenn die Erzeugung fehl schlug.
   */
  private void createNewIndexEntry(RawDocument rawDocument)
    throws RegainException
  {
    // Dokument erzeugen
    if (mCat.isDebugEnabled()) {
      mCat.debug("Creating document");
    }
    Document doc = mDocumentFactory.createDocument(rawDocument);

    // Dokument in den Index aufnehmen
    mAddToIndexProfiler.startMeasuring();
    try {
      setIndexMode(ADDING_MODE);
      if (mCat.isDebugEnabled()) {
        mCat.debug("Adding document to index");
      }
      mIndexWriter.addDocument(doc);
      mAddToIndexProfiler.stopMeasuring(rawDocument.getContent().length);
    }
    catch (IOException exc) {
      mAddToIndexProfiler.abortMeasuring();
      throw new RegainException("Adding document to index failed", exc);
    }
  }


  /**
   * Geht durch den Index und löscht alle veralteten Einträge.
   * <p>
   * Veraltet sind alle Einträge, die entweder vom IndexWriterManager fürs Löschen
   * vorgemerkt wurden Siehe {@link #mUrlsToDeleteHash} oder die weder im
   * urlToKeepSet stehen noch zu einem Eintrag des prefixesToKeepArr passen.
   * 
   * @param urlToKeepSet Die zu verschonenden URLs
   * @param prefixesToKeepArr URL-Präfixe für zu verschonende URLs. Wenn eine
   *        URL einem dieser Präfixe entspricht, dann soll sie auch verschont
   *        werden.
   * @throws RegainException Wenn ein Indexeintrag entweder nicht gelesen oder
   *         nicht gelöscht werden konnte.
   */
  public void removeObsoleteEntires(HashSet urlToKeepSet,
    String[] prefixesToKeepArr)
    throws RegainException
  {
    if (! mUpdateIndex) {
      // Wir haben einen komplett neuen Index erstellt
      // -> Es kann keine Einträge zu nicht vorhandenen Dokumenten geben
      // -> Wir sind fertig
      return;
    }
    
    setIndexMode(DELETING_MODE);
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
        
        // Prüfen, ob die URL gelöscht werden soll
        boolean shouldBeDeleted;
        if (url != null) {
          // Prüfen, ob dieser Eintrag zum Löschen vorgesehen ist
          if (isMarkedForDeletion(doc)) {
            shouldBeDeleted = true;
          }
          // Prüfen, ob dieser Eintrag zu verschonen ist
          else if (urlToKeepSet.contains(url)) {
            shouldBeDeleted = false;
          }
          // Prüfen, ob die URL zu einem zu-verschonen-Präfix passt
          else {
            shouldBeDeleted = true;
            for (int i = 0; i < prefixesToKeepArr.length; i++) {
              if (url.startsWith(prefixesToKeepArr[i])) {
                shouldBeDeleted = false;
                break;
              }
            }
          }
          
          if (shouldBeDeleted) {
            try {
              mCat.info("Deleting from index: " + url + " from " + lastModified);
              mIndexReader.delete(docIdx);
            }
            catch (IOException exc) {
              throw new RegainException("Deleting document #" + docIdx
                + " from index failed: " + url + " from " + lastModified, exc);
            }
          }
        }
      }
    }
    
    // Merkliste der zu löschenden Einträge löschen
    mUrlsToDeleteHash = null;
  }

  
  /**
   * Merkt ein Dokument für die spätere Löschung vor.
   * <p>
   * Diese Methode ist Teil eines Workaround: Ein alter Eintrag, der durch einen
   * neuen ersetzt wird, wird nicht sofort gelöscht, sondern nur zur Löschung
   * vorgemerkt. Auf diese Weise wird ein seltener Fehler umgangen, der das
   * Schließen des IndexWriter verhindert, wenn häufig zwischen InderWriter und
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
      mCat.info("Marking old entry for a later deletion: " + url + " from "
        + lastModified);
      mUrlsToDeleteHash.put(url, lastModified);
    }
  }
  
  
  /**
   * Gibt zurück, ob ein Dokument für die Löschung vorgemerkt wurde.
   * 
   * @param doc Das zu prüfende Dokument.
   * @return Ob das Dokument für die Löschung vorgemerkt wurde.
   */
  private boolean isMarkedForDeletion(Document doc) {
    String url = doc.get("url");
    String lastModified = doc.get("last-modified");
    
    if ((url == null) || (lastModified == null)) {
      // url und last-modified sind Mussfelder
      // Da eines fehlt -> Dokument löschen
      return true;
    }
    
    if (mUrlsToDeleteHash == null) {
      // Es sind gar keine Dokumente zum Löschen vorgemerkt
      return false;
    }
    
    // Prüfen, ob es einen Eintrag für diese URL gibt und ob er dem
    // last-modified des Dokuments entspricht
    String lastModifiedToDelete = (String) mUrlsToDeleteHash.get(url);
    return lastModified.equals(lastModifiedToDelete);
  }
  
  
  /**
   * Gibt die Anzahl der Einträge im Index zurück.
   * 
   * @return Die Anzahl der Einträge im Index.
   * @throws RegainException Wenn die Anzahl nicht ermittelt werden konnte.
   */
  public int getIndexEntryCount() throws RegainException {
    if (mIndexReader != null) {
      return mIndexReader.numDocs();
    } else {
      setIndexMode(ADDING_MODE);
      return mIndexWriter.docCount();
    }
  }


  /**
   * Optimiert und schließt den Index
   *
   * @param putIntoQuarantine Gibt an, ob der Index in Quarantäne soll.
   * @throws RegainException Wenn der Index nicht geschlossen werden konnte.
   */
  public void close(boolean putIntoQuarantine) throws RegainException {
    // Ressourcen der DocumentFactory freigeben
    mDocumentFactory.close();
    
    // Testen, ob noch Einträge für die Löschung vorgesehen sind
    if (mUrlsToDeleteHash != null) {
      throw new RegainException("There are still documents marked for deletion."
        + " The method removeObsoleteEntires(...) has to be called first.");
    }
    
    // Index optimieren und schließen
    try {
      setIndexMode(ADDING_MODE);
      mIndexWriter.optimize();
    }
    catch (IOException exc) {
      throw new RegainException("Finishing IndexWriter failed", exc);
    }

    // Switch to FINISHED_MODE
    setIndexMode(FINISHED_MODE);

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
    if (targetDir.exists()) {
      // We rename it before deletion so there will be no problems when the
      // search mask tries not to switch to the new index during deletion. This
      // case is very unlikely but it may happen once in 100.000 years...
      File secureDir = new File(targetDir.getAbsolutePath() + "_del");
      if (targetDir.renameTo(secureDir)) {
        RegainToolkit.deleteDirectory(secureDir);
      } else {
        // It really happend: The search mask tries to get the new index right now.
        // -> In this case we do nothing (The new index will stay in the temp dir
        //    and the next operation (renaming temp to new) will fail).
      }
    }

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
    
    if (! renameSucceed) {
      throw new RegainException("Renaming " + mTempIndexDir + " to " + targetDir
        + " failed after " + (RENAME_TIMEOUT / 1000) + " seconds!");
    }
  }


  /**
   * Erzeugt eine Datei, die alle Terme (also alle erlaubten Suchtexte) enthält.
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
      writer.println("This file was generated by the spider and contains all "
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

      mCat.info("Wrote " + termCount + " terms into " + termFile.getAbsolutePath());
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
   * @param termEnum Die Aufzählung mit allen Termen.
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
   * Um die Terme sortieren zu können, müssen sie zwischengespeichert werden. Falls
   * es zu viele sind, könnte das schief gehen. In diesem Fall sollte man auf simples
   * Schreiben umstellen.
   *
   * @param termEnum Die Aufzählung mit allen Termen.
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
