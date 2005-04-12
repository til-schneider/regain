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
 *  $RCSfile: Crawler.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/Crawler.java,v $
 *     $Date: 2005/03/17 12:57:49 $
 *   $Author: til132 $
 * $Revision: 1.17 $
 */
package net.sf.regain.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.CrawlerConfig;
import net.sf.regain.crawler.config.StartUrl;
import net.sf.regain.crawler.config.UrlPattern;
import net.sf.regain.crawler.config.WhiteListEntry;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Durchsucht alle konfigurierten Startseiten nach URLs. Die gefundenen Seiten
 * werden je nach Einstellung nur geladen, in den Suchindex aufgenommen oder
 * wiederum nach URLs durchsucht.
 * <p>
 * F�r jede URL wird Anhand der Schwarzen und der Wei�en Liste entschieden, ob sie
 * ignoriert oder bearbeitet wird. Wenn <CODE>loadUnparsedUrls</CODE> auf
 * <CODE>false</CODE> gesetzt wurde, dann werden auch URLs ignoriert, die weder
 * durchsucht noch indiziert werden.
 *
 * @author Til Schneider, www.murfman.de
 */
public class Crawler implements ErrorLogger {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(Crawler.class);

  /** Die Konfiguration mit den Einstellungen. */
  private CrawlerConfig mConfiguration;

  /** Enth�lt alle bereits gefundenen URLs, die nicht ignoriert wurden. */
  private HashSet mFoundUrlSet;
  /** Enth�lt alle bereits gefundenen URLs, die ignoriert wurden. */
  private HashSet mIgnoredUrlSet;
  /** Die Liste der noch zu bearbeitenden Jobs. */
  private LinkedList mJobList;

  /** The number of occured errors. */
  private int mErrorCount;
  
  /**
   * Die Anzahl der fatalen Fehler, die aufgetreten sind.
   * <p>
   * Fatale Fehler sind Fehler, durch die eine Erstellung oder Aktualisierung
   * des Index verhindert wurde.
   */
  private int mFatalErrorCount;

  /**
   * Enth�lt alle bisher gefundenen Dead-Links.
   * <p>
   * Es werden Object[]s gespeichert, wobei das erste Element die URL enth�lt, die
   * nicht gefunden werden konnte und die zweite, das Dokument, in dem diese URL
   * gefunden wurde.
   */
  private LinkedList mDeadlinkList;

  /**
   * Enth�lt die Pr�fixe, die eine URL <i>nicht</i> haben darf, um bearbeitet zu
   * werden.
   */
  private String[] mUrlPrefixBlackListArr;
  /**
   * Die Wei�e Liste.
   *
   * @see WhiteListEntry
   */
  private WhiteListEntry[] mWhiteListEntryArr;

  /** Die UrlPattern, die der HTML-Parser nutzen soll, um URLs zu identifizieren. */
  private UrlPattern[] mHtmlParserUrlPatternArr;
  /**
   * Die Regul�ren Ausdr�cke, die zu den jeweiligen UrlPattern f�r den
   * HTML-Parser geh�ren.
   *
   * @see #mHtmlParserUrlPatternArr
   */
  private RE[] mHtmlParserPatternReArr;

  /** Der Profiler der die gesamten Crawler-Jobs mi�t. */
  private Profiler mCrawlerJobProfiler;
  /** Der Profiler der das Durchsuchen von HTML-Dokumenten mi�t. */
  private Profiler mHtmlParsingProfiler;
  
  /** The IndexWriterManager to use for adding documents to the index. */
  private IndexWriterManager mIndexWriterManager;
  
  /** Specifies whether the crawler should pause as soon as possible, */
  private boolean mShouldPause;


  /**
   * Erzeugt eine neue Crawler-Instanz.
   *
   * @param config Die Konfiguration
   *
   * @throws RegainException Wenn die regul�ren Ausdr�cke fehlerhaft sind.
   */
  public Crawler(CrawlerConfig config) throws RegainException {
    Profiler.clearRegisteredProfilers();
    
    mCrawlerJobProfiler = new Profiler("Whole crawler jobs", "jobs");
    mHtmlParsingProfiler = new Profiler("Parsed HTML documents", "docs");
    
    mConfiguration = config;

    mFoundUrlSet = new HashSet();
    mIgnoredUrlSet = new HashSet();
    mJobList = new LinkedList();
    mDeadlinkList = new LinkedList();

    mFatalErrorCount = 0;

    RawDocument.setHttpTimeoutSecs(config.getHttpTimeoutSecs());

    mUrlPrefixBlackListArr = config.getUrlPrefixBlackList();
    mWhiteListEntryArr = config.getWhiteList();

    mHtmlParserUrlPatternArr = config.getHtmlParserUrlPatterns();
    mHtmlParserPatternReArr = new RE[mHtmlParserUrlPatternArr.length];
    for (int i = 0; i < mHtmlParserPatternReArr.length; i++) {
      String regex = mHtmlParserUrlPatternArr[i].getRegexPattern();
      try {
        mHtmlParserPatternReArr[i] = new RE(regex);
      }
      catch (RESyntaxException exc) {
        throw new RegainException("Regular exception of HTML parser pattern #"
          + (i + 1) + " has a wrong syntax: '" + regex + "'", exc);
      }
    }
  }
  
  
  /**
   * Gets the number of processed documents.
   * 
   * @return The number of processed documents.
   */
  public int getFinishedJobCount() {
    return mCrawlerJobProfiler.getMeasureCount();
  }


  /**
   * Gets the number of documents that were in the (old) index when the
   * IndexWriterManager was created.
   * 
   * @return The initial number of documents in the index.
   */
  public int getInitialDocCount() {
    IndexWriterManager mng = mIndexWriterManager;
    return (mng == null) ? -1 : mng.getInitialDocCount();
  }


  /**
   * Gets the number of documents that were added to the index.
   * 
   * @return The number of documents added to the index.
   */
  public int getAddedDocCount() {
    IndexWriterManager mng = mIndexWriterManager;
    return (mng == null) ? -1 : mng.getAddedDocCount();
  }


  /**
   * Gets the number of documents that will be removed from the index.
   * 
   * @return The number of documents removed from the index.
   */
  public int getRemovedDocCount() {
    IndexWriterManager mng = mIndexWriterManager;
    return (mng == null) ? -1 : mng.getRemovedDocCount();
  }


  /**
   * Sets whether the crawler should pause.
   *  
   * @param shouldPause Whether the crawler should pause.
   */
  public void setShouldPause(boolean shouldPause) {
    mShouldPause = shouldPause;
  }


  /**
   * Gets whether the crawler is currently pausing or is pausing soon.
   * 
   * @return Whether the crawler is currently pausing.
   */
  public boolean getShouldPause() {
    return mShouldPause;
  }
  

  /**
   * Analysiert die URL und entscheidet, ob sie bearbeitet werden soll oder nicht.
   * <p>
   * Wenn ja, dann wird ein neuer Job erzeugt und der Job-Liste hinzugef�gt.
   *
   * @param url Die URL des zu pr�fenden Jobs.
   * @param sourceUrl Die URL des Dokuments in der die URL des zu pr�fenden Jobs
   *        gefunden wurde.
   * @param shouldBeParsed Gibt an, ob die URL geparst werden soll.
   * @param shouldBeIndexed Gibt an, ob die URL indiziert werden soll.
   * @param sourceLinkText Der Text des Links in dem die URL gefunden wurde. Ist
   *        <code>null</code>, falls die URL nicht in einem Link (also einem
   *        a-Tag) gefunden wurde oder wenn aus sonstigen Gr�nden kein Link-Text
   *        vorhanden ist.
   */
  private void addJob(String url, String sourceUrl, boolean shouldBeParsed,
    boolean shouldBeIndexed, String sourceLinkText)
  {
    if (! mConfiguration.getBuildIndex()) {
      // Indexing is disabled
      shouldBeIndexed = false;
    }

    // Change all blanks to %20, since blanks are not allowed in URLs
    url = RegainToolkit.replace(url, " ", "%20");

    boolean alreadyFound = mFoundUrlSet.contains(url);
    boolean alreadyIgnored = mIgnoredUrlSet.contains(url);

    if ((! alreadyFound) && (! alreadyIgnored)) {
      boolean accepted = isUrlAccepted(url);

      // Check whether this page has to be loaded at all
      if (! mConfiguration.getLoadUnparsedUrls()) {
        // Pages that are neither parsed nor indexed can be skipped
        if ((! shouldBeParsed) && (! shouldBeIndexed)) {
          accepted = false;
        }
      }

      if (accepted) {
        mFoundUrlSet.add(url);
        if (mLog.isDebugEnabled()) {
          mLog.debug("Found new URL: " + url);
        }

        CrawlerJob job = new CrawlerJob(url, sourceUrl, sourceLinkText,
                                      shouldBeParsed, shouldBeIndexed);
        mJobList.add(job);
      } else {
        mIgnoredUrlSet.add(url);
        if (mLog.isDebugEnabled()) {
          mLog.debug("Ignoring URL: " + url);
        }
      }
    }
  }



  /**
   * Pr�ft ob die URL von der Schwarzen und Wei�en Liste akzeptiert wird.
   * <p>
   * Dies ist der Fall, wenn sie keinem Pr�fix aus der Schwarzen Liste und
   * mindestens einem aus der Wei�en Liste entspricht.
   *
   * @param url Die zu pr�fende URL.
   * @return Ob die URL von der Schwarzen und Wei�en Liste akzeptiert wird.
   */
  private boolean isUrlAccepted(String url) {
    // check whether this URL matches to a white list prefix
    boolean matchesToWhiteList = false;
    for (int i = 0; i < mWhiteListEntryArr.length; i++) {
      if (mWhiteListEntryArr[i].shouldBeUpdated()) {
        String whiteListPrefix = mWhiteListEntryArr[i].getPrefix();
        if (url.startsWith(whiteListPrefix)) {
          matchesToWhiteList = true;
          break;
        }
      }
    }
    if (! matchesToWhiteList) {
      return false;
    }

    // check whether this URL matches to a black list prefix
    for (int i = 0; i < mUrlPrefixBlackListArr.length; i++) {
      if (url.startsWith(mUrlPrefixBlackListArr[i])) {
        return false;
      }
    }

    // All tests passed -> URL is accepted
    return true;
  }



  /**
   * F�hrt den Crawler-Proze� aus und gibt am Ende die Statistik, die
   * Dead-Link-Liste und die Fehler-Liste aus.
   *
   * @param updateIndex Gibt an, ob ein bereits bestehender Index aktualisiert
   *        werden soll.
   * @param onlyEntriesArr Die Namen der Eintr�ge in der Wei�en Liste, die
   *        bearbeitet werden sollen. Wenn <code>null</code> oder leer, dann
   *        werden alle Eintr�ge bearbeitet.
   */
  public void run(boolean updateIndex, String[] onlyEntriesArr) {
    mLog.info("Starting crawling...");
    mShouldPause = false;

    // Initialize the IndexWriterManager if building the index is wanted
    mIndexWriterManager = null;
    if (mConfiguration.getBuildIndex()) {
      mLog.info("Preparing the index");
      try {
        mIndexWriterManager = new IndexWriterManager(mConfiguration, updateIndex);
        updateIndex = mIndexWriterManager.getUpdateIndex();
      }
      catch (RegainException exc) {
        logError("Preparing the index failed!", exc, true);
        return;
      }
    }

    // Check whether some white list entries should be ignored
    useOnlyWhiteListEntries(onlyEntriesArr, updateIndex);

    // Add the start URLs
    addStartUrls();
    
    // Remember the last time when a breakpoint was created
    long lastBreakpointTime = System.currentTimeMillis();
    
    // Work in the job list
    while (! mJobList.isEmpty()) {
      mCrawlerJobProfiler.startMeasuring();

      CrawlerJob job = (CrawlerJob) mJobList.removeFirst();
      String url = job.getUrl();

      boolean shouldBeParsed = job.shouldBeParsed();
      boolean shouldBeIndexed = job.shouldBeIndexed();

      // Check whether this is a directory
      if (url.startsWith("file://")) {
        try {
          File file = RegainToolkit.urlToFile(url);
          if (file.isDirectory()) {
            // This IS a directory -> Add all child files as Jobs
            if (shouldBeParsed) {
              parseDirectory(file);
            }
            
            // A directory can't be parsed or indexed -> continue
            mCrawlerJobProfiler.stopMeasuring(0);
            continue;
          }
        }
        catch (Throwable thr) {
          logError("Invalid URL: '" + url + "'", thr, false);
        }
      }

      // Create a raw document
      RawDocument rawDocument;
      try {
        rawDocument = new RawDocument(url, job.getSourceUrl(),
                                      job.getSourceLinkText());
      }
      catch (RegainException exc) {
        // Check whether the exception was caused by a dead link
        handleDocumentLoadingException(exc, job);

        // This document does not exist -> We can't parse or index anything
        // -> continue
        mCrawlerJobProfiler.abortMeasuring();
        continue;
      }

      // Parse the content
      if (shouldBeParsed) {
        mLog.info("Parsing " + rawDocument.getUrl());
        mHtmlParsingProfiler.startMeasuring();
        try {
          parseHtmlDocument(rawDocument);
          mHtmlParsingProfiler.stopMeasuring(rawDocument.getLength());
        }
        catch (RegainException exc) {
          logError("Parsing HTML failed: " + rawDocument.getUrl(), exc, false);
        }
      }

      // Index the content
      if (shouldBeIndexed) {
        if (mLog.isDebugEnabled()) {
          mLog.debug("Indexing " + rawDocument.getUrl());
        }
        try {
          mIndexWriterManager.addToIndex(rawDocument, this);
        }
        catch (RegainException exc) {
          logError("Indexing failed: " + rawDocument.getUrl(), exc, false);
        }
      }

      // System-Ressourcen des RawDocument wieder frei geben.
      rawDocument.dispose();

      // Zeitmessung stoppen
      mCrawlerJobProfiler.stopMeasuring(rawDocument.getLength());
      
      // Check whether to create a breakpoint
      if (mShouldPause || (System.currentTimeMillis() > lastBreakpointTime + 10 * 60 * 1000)) {
        try {
          mIndexWriterManager.createBreakpoint();
        }
        catch (RegainException exc) {
          logError("Creating breakpoint failed", exc, false);
        }
        
        // Pause
        while (mShouldPause) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException exc) {}
        }
        
        lastBreakpointTime = System.currentTimeMillis();
      }
    }

    // Nicht mehr vorhandene Dokumente aus dem Index l�schen
    if (mConfiguration.getBuildIndex()) {
      mLog.info("Removing index entries of documents that do not exist any more...");
      try {
        String[] prefixesToKeepArr = createPrefixesToKeep();
        mIndexWriterManager.removeObsoleteEntries(mFoundUrlSet, prefixesToKeepArr);
      }
      catch (Throwable thr) {
        logError("Removing non-existing documents from index failed", thr, true);
      }
    }

    // Pr�fen, ob Index leer ist
    int entryCount = 0;
    try {
      entryCount = mIndexWriterManager.getIndexEntryCount();
    }
    catch (Throwable thr) {
      logError("Counting index entries failed", thr, true);
    }
    double failedPercent;
    if (entryCount == 0) {
      logError("The index is empty.", null, true);
      failedPercent = 1;
    } else {
      // Pr�fen, ob die Anzahl der abgebrochenen Dokumente �ber der Toleranzgranze
      // ist.
      double failedDocCount = mDeadlinkList.size() + mErrorCount;
      double totalDocCount = failedDocCount + entryCount;
      failedPercent = failedDocCount / totalDocCount;
      double maxAbortedPercent = mConfiguration.getMaxFailedDocuments();
      if (failedPercent > maxAbortedPercent) {
        logError("There are more failed documents than allowed (Failed: " +
          RegainToolkit.toPercentString(failedPercent) + ", allowed: " +
          RegainToolkit.toPercentString(maxAbortedPercent) + ").",
          null, true);
      }
    }

    // Fehler und Deadlink-Liste schreiben
    writeDeadlinkAndErrorList();

    // Index abschlie�en
    if (mIndexWriterManager != null) {
      boolean thereWereFatalErrors = (mFatalErrorCount > 0);
      if (thereWereFatalErrors) {
        mLog.warn("There were " + mFatalErrorCount + " fatal errors. " +
          "The index will be finished but put into quarantine.");
      } else {
        mLog.info("Finishing the index and providing it to the search mask");
      }
      try {
        mIndexWriterManager.close(thereWereFatalErrors);
      } catch (RegainException exc) {
        logError("Finishing index failed!", exc, true);
      }
      mIndexWriterManager = null;
    }

    mLog.info("... Finished crawling\n");

    mLog.info(Profiler.getProfilerResults());

    // Systemspeziefischen Zeilenumbruch holen
    String lineSeparator = RegainToolkit.getLineSeparator();

    mLog.info("Statistics:" + lineSeparator
      + "  Ignored URLs:       " + mIgnoredUrlSet.size() + lineSeparator
      + "  Documents in index: " + entryCount + lineSeparator
      + "  Dead links:         " + mDeadlinkList.size() + lineSeparator
      + "  Errors:             " + mErrorCount + lineSeparator
      + "  Error ratio:        " + RegainToolkit.toPercentString(failedPercent));
  }


  /**
   * Handles an exception caused by a failed document loadung. Checks whether
   * the exception was caused by a dead link and puts it to the dead link list
   * if necessary.
   * 
   * @param exc The exception to check.
   * @param job The job of the document.
   */
  private void handleDocumentLoadingException(RegainException exc, CrawlerJob job) {
    if (isExceptionFromDeadLink(exc)) {
      // Don't put this exception in the error list, because it's already in
      // the dead link list. (Use mCat.error() directly)
      mLog.error("Dead link: '" + job.getUrl() + "'. Found in '" + job.getSourceUrl()
                 + "'", exc);
      mDeadlinkList.add(new Object[] { job.getUrl(), job.getSourceUrl() });
    } else {
      logError("Loading " + job.getUrl() + " failed!", exc, false);
    }
  }


  /**
   * Adds all start URL to the job list.
   */
  private void addStartUrls() {
    StartUrl[] startUrlArr = mConfiguration.getStartUrls();
    for (int i = 0; i < startUrlArr.length; i++) {
      String url = startUrlArr[i].getUrl();
      boolean shouldBeParsed = startUrlArr[i].getShouldBeParsed();
      boolean shouldBeIndexed = startUrlArr[i].getShouldBeIndexed();

      addJob(url, "Start URL from configuration", shouldBeParsed,
          shouldBeIndexed, null);
    }
  }


  /**
   * Erzeugt ein Array von URL-Pr�fixen, die von der L�schung aus dem Index
   * verschont bleiben sollen.
   * <p>
   * Diese Liste entspricht den Eintr�gen der Wei�en Liste, deren
   * <code>shouldBeUpdated</code>-Flag auf <code>false</code> gesetzt ist.
   *
   * @return Die URL-Pr�fixen, die nicht aus dem Index gel�scht werden sollen.
   * @see WhiteListEntry#shouldBeUpdated()
   */
  private String[] createPrefixesToKeep() {
    ArrayList list = new ArrayList();
    for (int i = 0; i < mWhiteListEntryArr.length; i++) {
      if (! mWhiteListEntryArr[i].shouldBeUpdated()) {
        list.add(mWhiteListEntryArr[i].getPrefix());
      }
    }

    String[] asArr = new String[list.size()];
    list.toArray(asArr);
    return asArr;
  }


  /**
   * Pr�ft, ob Eintr�ge der Wei�en Liste ignoriert werden sollen und �ndert
   * die Wei�e Liste entsprechend.
   *
   * @param onlyEntriesArr Die Namen der Eintr�ge in der Wei�en Liste, die
   *        bearbeitet werden sollen. Wenn <code>null</code> oder leer, dann
   *        werden alle Eintr�ge bearbeitet.
   * @param updateIndex Gibt an, ob ein bereits bestehender Index aktualisiert
   *        werden soll.
   */
  private void useOnlyWhiteListEntries(String[] onlyEntriesArr,
    boolean updateIndex)
  {
    // NOTE: At that moment all white list entries are set to "should be updated"

    if ((onlyEntriesArr != null) && (onlyEntriesArr.length != 0)) {
      if (updateIndex) {
        // First set all white list entries to "should NOT be updated".
        for (int i = 0; i < mWhiteListEntryArr.length; i++) {
          mWhiteListEntryArr[i].setShouldBeUpdated(false);
        }

        // Now set those entries to "should be updated" that are in the list
        for (int i = 0; i < onlyEntriesArr.length; i++) {
          // Find the matching white list entry
          WhiteListEntry entry = null;
          for (int j = 0; j < mWhiteListEntryArr.length; j++) {
            if (onlyEntriesArr[i].equals(mWhiteListEntryArr[j].getName())) {
              entry = mWhiteListEntryArr[j];
              break;
            }
          }

          if (entry == null) {
            // No matching white list entry found
            logError("There is no white list entry named '" + onlyEntriesArr[i]
                     + "'", null, true);
          } else {
            entry.setShouldBeUpdated(true);
          }
        }

        // Log all ignored entries
        for (int i = 0; i < mWhiteListEntryArr.length; i++) {
          if (! mWhiteListEntryArr[i].shouldBeUpdated()) {
            mLog.info("Ignoring white list entry: "
              + mWhiteListEntryArr[i].getPrefix());
          }
        }
      } else {
        mLog.warn("Unable to ignore white list entries, because a new index " +
                  "will be created");
      }
    }
  }



  /**
   * Schreibt die Deadlink- und Fehlerliste ins Logfile und nochmal in eine
   * eigene Datei. Diese stehen in einem Unterverzeichnis namens 'log'.
   * Bei eingeschalteter Indizierung steht dieses Unterverzeichnis im Index, bei
   * ausgeschalteter Indizierung im aktuellen Verzeichnis.
   */
  private void writeDeadlinkAndErrorList() {
    if (mDeadlinkList.isEmpty() && (mErrorCount == 0)) {
      // Nothing to do
      return;
    }

    // Get the directory where the files should be put in
    File listDir;
    if (mConfiguration.getBuildIndex()) {
      listDir = new File(mConfiguration.getIndexDir() + File.separator + "temp"
        + File.separator + "log");
    } else {
      listDir = new File("log");
    }

    String msg;
    FileOutputStream stream = null;
    PrintStream printer = null;
    try {
      // Create the directory if doesn't exist
      if (! listDir.exists()) {
        if (! listDir.mkdir()) {
          throw new IOException("Creating directory failed: " + listDir.getAbsolutePath());
        }
      }

      // Write the deadlink list
      if (! mDeadlinkList.isEmpty()) {
        stream = new FileOutputStream(new File(listDir, "deadlinks.txt"));
        printer = new PrintStream(stream);

        msg = "There were " + mDeadlinkList.size() + " dead links:";
        System.out.println(msg);
        printer.println(msg);

        Iterator iter = mDeadlinkList.iterator();
        for (int i = 0; iter.hasNext(); i++) {
          Object[] tupel = (Object[]) iter.next();
          String url = (String) tupel[0];
          String sourceUrl = (String) tupel[1];

          msg = "  Dead link #" + (i + 1) + ": '" + url + "' found in '" + sourceUrl + "'";
          System.out.println(msg);
          printer.println(msg);
        }

        printer.close();
        stream.close();
      }

      // Write the error list
      if (mErrorCount > 0) {
        mLog.warn("There were " + mErrorCount + " errors");
      }
    }
    catch (IOException exc) {
      logError("Writing deadlink list and error list failed", exc, false);
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
   * Pr�ft, ob die Exception von einem Dead-Link herr�hrt.
   *
   * @param thr Die zu pr�fende Exception
   * @return Ob die Exception von einem Dead-Link herr�hrt.
   */
  private boolean isExceptionFromDeadLink(Throwable thr) {
    if (thr instanceof HttpStreamException) {
      HttpStreamException exc = (HttpStreamException) thr;
      return exc.isHttpReturnCodeFromDeadLink();
    }
    else if (thr instanceof RegainException) {
      RegainException exc = (RegainException) thr;
      return isExceptionFromDeadLink(exc.getCause());
    }
    else {
      return false;
    }
  }


  /**
   * Durchsucht ein Verzeichnis nach URLs, also Dateien und Unterverzeichnissen,
   * und erzeugt f�r jeden Treffer einen neuen Job.
   *
   * @param dir Das zu durchsuchende Verzeichnis.
   */
  private void parseDirectory(File dir) {
    // Get the URL for the directory
    String sourceUrl = RegainToolkit.fileToUrl(dir);

    // Parse the directory
    File[] childArr = dir.listFiles();
    for (int childIdx = 0; childIdx < childArr.length; childIdx++) {
      // Get the URL for the current child file
      String url = RegainToolkit.fileToUrl(childArr[childIdx]);

      // Check wether this is a directory
      if (childArr[childIdx].isDirectory()) {
        // It's a directory -> Add a parse job
        addJob(url, sourceUrl, true, false, null);
      } else {
        // It's a file -> Add a index job
        addJob(url, sourceUrl, false, true, null);
      }
    }
  }


  /**
   * Durchsucht den Inhalt eines HTML-Dokuments nach URLs und erzeugt f�r jeden
   * Treffer einen neuen Job.
   *
   * @param rawDocument Das zu durchsuchende Dokument.
   * @throws RegainException Wenn das Dokument nicht gelesen werden konnte.
   */
  private void parseHtmlDocument(RawDocument rawDocument) throws RegainException {
    for (int i = 0; i < mHtmlParserPatternReArr.length; i++) {
      RE re = mHtmlParserPatternReArr[i];
      int urlGroup = mHtmlParserUrlPatternArr[i].getRegexUrlGroup();
      boolean shouldBeParsed = mHtmlParserUrlPatternArr[i].getShouldBeParsed();
      boolean shouldBeIndexed = mHtmlParserUrlPatternArr[i].getShouldBeIndexed();

      int offset = 0;
      String contentAsString = rawDocument.getContentAsString();
      while (re.match(contentAsString, offset)) {
        offset = re.getParenEnd(0);

        String parentUrl = rawDocument.getUrl();
        String url = re.getParen(urlGroup);

        // Convert the URL to an absolute URL
        url = CrawlerToolkit.toAbsoluteUrl(url, parentUrl);

        // Try to get a link text
        String linkText = getLinkText(contentAsString, offset);

        // Add the job
        addJob(url, parentUrl, shouldBeParsed, shouldBeIndexed, linkText);
      }
    }
  }



  /**
   * Tries to extract a link text from a position where a URL was found.
   *
   * @param content The content to extract the link text from
   * @param offset The offset where o start looking
   * @return A link text or <code>null</code> if there was no link text found.
   */
  private String getLinkText(String content, int offset) {
    // NOTE: if there is a link text the following code must be something
    //       like: ' someParam="someValue">The link text</a>'
    //       Assumed that the tag started with '<a href="aDocument.doc"'

    // Find the end of the current tag
    int tagEnd = content.indexOf('>', offset);
    if (tagEnd == -1) {
      // No tag end found
      return null;
    }

    // If there is a link text the next part must be: 'The link text</a>'
    // -> Find the start of the next tag
    int tagStart = content.indexOf('<', tagEnd);
    if (tagStart == -1) {
      // No starting tag found
      return null;
    }

    // Check whether the starting tag is a '</a>' tag.
    if ((content.length() > tagStart + 3)
      && (content.charAt(tagStart + 1) == '/')
      && (content.charAt(tagStart + 2) == 'a')
      && (content.charAt(tagStart + 3) == '>'))
    {
      // We have a link text
      String linkText = content.substring(tagEnd + 1, tagStart);
      linkText = linkText.trim();
      if (linkText.length() == 0) {
        linkText = null;
      }

      return linkText;
    } else {
      // The tag was no </a> tag, so the text was no link text
      return null;
    }
  }


  /**
   * Gibt die Anzahl der Fehler zur�ck (das beinhaltet fatale und nicht fatale
   * Fehler).
   *
   * @return Die Anzahl der Fehler.
   * @see #getFatalErrorCount()
   */
  public int getErrorCount() {
    return mErrorCount;
  }


  /**
   * Gibt Die Anzahl der fatalen Fehler zur�ck.
   * <p>
   * Fatale Fehler sind Fehler, durch die eine Erstellung oder Aktualisierung
   * des Index verhindert wurde.
   *
   * @return Die Anzahl der fatalen Fehler.
   * @see #getErrorCount()
   */
  public int getFatalErrorCount() {
    return mFatalErrorCount;
  }


  /**
   * Loggs an error.
   *
   * @param msg The error message.
   * @param thr The error. May be <code>null</code>.
   * @param fatal Specifies whether the error was fatal. An error is fatal if
   *        it caused that the index could not be created.
   */
  public void logError(String msg, Throwable thr, boolean fatal) {
    if (fatal) {
      msg = "Fatal: " + msg;
    }
    mLog.error(msg, thr);
    try {
      if (mIndexWriterManager != null) {
        mIndexWriterManager.logError(msg, thr);
      }
    }
    catch (RegainException exc) {
      mLog.error("Logging error in error log of index failed", exc);
    }
    
    mErrorCount ++;
    if (fatal) {
      mFatalErrorCount++;
    }
  }

}
