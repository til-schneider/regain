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
 *     $Date: 2006/08/11 09:22:53 $
 *   $Author: til132 $
 * $Revision: 1.29 $
 */
package net.sf.regain.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

import au.id.jericho.lib.html.*;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Durchsucht alle konfigurierten Startseiten nach URLs. Die gefundenen Seiten
 * werden je nach Einstellung nur geladen, in den Suchindex aufgenommen oder
 * wiederum nach URLs durchsucht.
 * <p>
 * Fï¿½r jede URL wird Anhand der Schwarzen und der Weiï¿½en Liste entschieden, ob sie
 * ignoriert oder bearbeitet wird. Wenn <CODE>loadUnparsedUrls</CODE> auf
 * <CODE>false</CODE> gesetzt wurde, dann werden auch URLs ignoriert, die weder
 * durchsucht noch indiziert werden.
 *
 * @author Til Schneider, www.murfman.de
 */
public class Crawler implements ErrorLogger {

  /** The logger for this class. */
  private static Logger mLog = Logger.getLogger(Crawler.class);

  /** The configuration with the preferences. */
  private CrawlerConfig mConfiguration;
  
  /** The URL checker. */
  private UrlChecker mUrlChecker;

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

  /** The current crawler job. May be null. */
  private CrawlerJob mCurrentJob;

  /**
   * Contains all found dead links.
   * <p>
   * Contains Object[]s with two elements: The first is the URL that couldn't be
   * found (a String), the second the URL of the document where the dead link
   * was found (a String).
   */
  private LinkedList mDeadlinkList;

  /** The UrlPattern the HTML-Parser should use to identify URLs. */
  private UrlPattern[] mHtmlParserUrlPatternArr;
  /**
   * The regular expressions that belong to the respective UrlPattern for the
   * HTML-Parser.
   *
   * @see #mHtmlParserUrlPatternArr
   */
  private RE[] mHtmlParserPatternReArr;

  /** The profiler that measures the whole crawler jobs. */
  private Profiler mCrawlerJobProfiler;
  /** The profiler that measures the HTML-Parser. */
  private Profiler mHtmlParsingProfiler;
  
  /** The IndexWriterManager to use for adding documents to the index. */
  private IndexWriterManager mIndexWriterManager;
  
  /** Specifies whether the crawler should pause as soon as possible, */
  private boolean mShouldPause;


  /**
   * Creates a new instance of Crawler.
   * 
   * @param config The Configuration
   *
   * @throws RegainException If the regular expressions have errors.
   */
  public Crawler(CrawlerConfig config) throws RegainException {
    Profiler.clearRegisteredProfilers();
    
    mCrawlerJobProfiler = new Profiler("Whole crawler jobs", "jobs");
    mHtmlParsingProfiler = new Profiler("Parsed HTML documents", "docs");
    
    mConfiguration = config;

    mJobList = new LinkedList();
    mDeadlinkList = new LinkedList();

    mFatalErrorCount = 0;

    RawDocument.setHttpTimeoutSecs(config.getHttpTimeoutSecs());

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
   * Gets the URL of the current job. Returns null, if the crawler has currently
   * no job.
   * 
   * @return The URL of the current job.
   */
  public String getCurrentJobUrl() {
    // NOTE: We put the current job in a local variable to avoid it is set to
    //       null while this method is executed.
    CrawlerJob job = mCurrentJob;
    if (job == null) {
      return null;
    } else {
      return job.getUrl();
    }
  }


  /**
   * Get the time the crawler is already working on the current job.
   * 
   * @return The current working time in milli seconds. Returns -1 if the
   *         crawler has currently no job.
   */
  public long getCurrentJobTime() {
    return mCrawlerJobProfiler.getCurrentMeasuringTime();
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
   * Wenn ja, dann wird ein neuer Job erzeugt und der Job-Liste hinzugefï¿½gt.
   *
   * @param url Die URL des zu prï¿½fenden Jobs.
   * @param sourceUrl Die URL des Dokuments in der die URL des zu prï¿½fenden Jobs
   *        gefunden wurde.
   * @param shouldBeParsed Gibt an, ob die URL geparst werden soll.
   * @param shouldBeIndexed Gibt an, ob die URL indiziert werden soll.
   * @param sourceLinkText Der Text des Links in dem die URL gefunden wurde. Ist
   *        <code>null</code>, falls die URL nicht in einem Link (also einem
   *        a-Tag) gefunden wurde oder wenn aus sonstigen Grï¿½nden kein Link-Text
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

    boolean alreadyAccepted = mUrlChecker.wasAlreadyAccepted(url);
    boolean alreadyIgnored = mUrlChecker.wasAlreadyIgnored(url);

    if ((! alreadyAccepted) && (! alreadyIgnored)) {
      boolean accepted = mUrlChecker.isUrlAccepted(url);

      // Check whether this page has to be loaded at all
      if (! mConfiguration.getLoadUnparsedUrls()) {
        // Pages that are neither parsed nor indexed can be skipped
        if ((! shouldBeParsed) && (! shouldBeIndexed)) {
          accepted = false;
        }
      }

      if (accepted) {
        mUrlChecker.setAccepted(url);
        if (mLog.isDebugEnabled()) {
          mLog.debug("Found new URL: " + url);
        }

        CrawlerJob job = new CrawlerJob(url, sourceUrl, sourceLinkText,
                                      shouldBeParsed, shouldBeIndexed);
        
        // NOTE: This is a little trick: We put documents that aren't parsed at
        //       the beginning of the job list and documents that are parsed at
        //       the end. This keeps the job list small as first all documents
        //       are processed, before new documents are added.
        if (shouldBeParsed) {
          mJobList.addLast(job);
        } else {
          mJobList.addFirst(job);
        }
      } else {
        mUrlChecker.setIgnored(url);
        if (mLog.isDebugEnabled()) {
          mLog.debug("Ignoring URL: " + url);
        }
      }
    }
  }


  /**
   * Executes the crawler process and prints out a statistik, the dead-link-list
   * and the error-list at the end.
   *
   * @param updateIndex Specifies whether an already existing index should be
   *        updated.
   * @param retryFailedDocs Specifies whether a document that couldn't be
   *        prepared the last time should be retried.
   * @param onlyEntriesArr The names of the white list entries, that should be
   *        updated. If <code>null</code> or empty, all entries will be updated.
   */
  public void run(boolean updateIndex, boolean retryFailedDocs,
    String[] onlyEntriesArr)
  {
    mLog.info("Starting crawling...");
    mShouldPause = false;

    // Init the HTTP client
    CrawlerToolkit.initHttpClient(mConfiguration);

    // Initialize the IndexWriterManager if building the index is wanted
    mIndexWriterManager = null;
    if (mConfiguration.getBuildIndex()) {
      mLog.info("Preparing the index");
      try {
        mIndexWriterManager = new IndexWriterManager(mConfiguration, updateIndex, retryFailedDocs);
        updateIndex = mIndexWriterManager.getUpdateIndex();
      }
      catch (RegainException exc) {
        logError("Preparing the index failed!", exc, true);
        return;
      }
    }

    // Get the white list and set the "should be updated"-flags
    WhiteListEntry[] whiteList = mConfiguration.getWhiteList();
    whiteList = useOnlyWhiteListEntries(whiteList, onlyEntriesArr, updateIndex);

    // Create the UrlChecker
    mUrlChecker = new UrlChecker(whiteList, mConfiguration.getBlackList());

    // Add the start URLs
    addStartUrls();
    
    // Remember the last time when a breakpoint was created
    long lastBreakpointTime = System.currentTimeMillis();
    
    // Work in the job list
    while (! mJobList.isEmpty()) {
      mCrawlerJobProfiler.startMeasuring();

      mCurrentJob = (CrawlerJob) mJobList.removeFirst();
      String url = mCurrentJob.getUrl();

      boolean shouldBeParsed = mCurrentJob.shouldBeParsed();
      boolean shouldBeIndexed = mCurrentJob.shouldBeIndexed();

      // Check whether this is a directory
      if (url.startsWith("file://")) {
        try {
          File file = RegainToolkit.urlToFile(url);
          // Check whether the file is readable.
          if (! file.canRead()) {
            mCrawlerJobProfiler.abortMeasuring();
            logError("File is not readable: '" + url + "'", null, false);
            continue;
          } else if (file.isDirectory()) {
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
        rawDocument = new RawDocument(url, mCurrentJob.getSourceUrl(),
                                      mCurrentJob.getSourceLinkText());
      }
      catch (RedirectException exc) {
        String redirectUrl = exc.getRedirectUrl();
        mLog.info("Redirect '" + url +  "' -> '" + redirectUrl + "'");
        mUrlChecker.setIgnored(url);
        addJob(redirectUrl, mCurrentJob.getSourceUrl(), shouldBeParsed,
               shouldBeIndexed, mCurrentJob.getSourceLinkText());
        mCrawlerJobProfiler.stopMeasuring(0);
        continue;
      }
      catch (RegainException exc) {
        // Check whether the exception was caused by a dead link
        handleDocumentLoadingException(exc, mCurrentJob);

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
        } catch (MalformedURLException e) {
        	 logError("Parsing HTML MalformedURLException: " + rawDocument.getUrl(), e, false);
			e.printStackTrace();
		} catch (IOException e) {
			 logError("Parsing HTML IOExcpetion: " + rawDocument.getUrl(), e, false);
			
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
      mCurrentJob = null;
      
      // Check whether to create a breakpoint
      int breakpointInterval = mConfiguration.getBreakpointInterval();
      boolean breakpointIntervalIsOver = (breakpointInterval > 0)
        && (System.currentTimeMillis() > lastBreakpointTime + breakpointInterval * 60 * 1000);
      if (mShouldPause || breakpointIntervalIsOver) {
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
    } // while (! mJobList.isEmpty())

    // Nicht mehr vorhandene Dokumente aus dem Index lï¿½schen
    if (mConfiguration.getBuildIndex()) {
      mLog.info("Removing index entries of documents that do not exist any more...");
      try {
        mIndexWriterManager.removeObsoleteEntries(mUrlChecker);
      }
      catch (Throwable thr) {
        logError("Removing non-existing documents from index failed", thr, true);
      }
    }

    // Prï¿½fen, ob Index leer ist
    int entryCount = 0;
    try {
      entryCount = mIndexWriterManager.getIndexEntryCount();
      // NOTE: We've got to substract the errors, because for each failed
      //       document a substitude document is added to the index
      //       (which should not be counted).
      entryCount -= mErrorCount;
      if (entryCount < 0) {
        entryCount = 0;
      }
    }
    catch (Throwable thr) {
      logError("Counting index entries failed", thr, true);
    }
    double failedPercent;
    if (entryCount == 0) {
      logError("The index is empty.", null, true);
      failedPercent = 1;
    } else {
      // Prï¿½fen, ob die Anzahl der abgebrochenen Dokumente ï¿½ber der Toleranzgranze
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

    // Index abschlieï¿½en
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
      + "  Ignored URLs:       " + mUrlChecker.getIgnoredCount() + lineSeparator
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
    // Get the start URLs from the config
    StartUrl[] startUrlArr = mConfiguration.getStartUrls();
    
    // Normalize the start URLs
    startUrlArr = mUrlChecker.normalizeStartUrls(startUrlArr);
    
    // Add the start URLs as jobs
    for (int i = 0; i < startUrlArr.length; i++) {
      String url = startUrlArr[i].getUrl();
      boolean shouldBeParsed = startUrlArr[i].getShouldBeParsed();
      boolean shouldBeIndexed = startUrlArr[i].getShouldBeIndexed();

      addJob(url, "Start URL from configuration", shouldBeParsed,
          shouldBeIndexed, null);
    }
  }


  /**
   * Sets the "should be updated"-flag for each entry in the white list.
   * 
   * @param whiteList The white list to process.
   * @param onlyEntriesArr The names of the white list entries, that should be
   *        updated. If <code>null</code> or empty, all entries will be updated.
   * @param updateIndex Specifies whether an already existing index will be
   *        updated in this crawler run.
   * @return The processed white list.
   */
  private WhiteListEntry[] useOnlyWhiteListEntries(WhiteListEntry[] whiteList,
    String[] onlyEntriesArr, boolean updateIndex)
  {
    // NOTE: At that moment all white list entries are set to "should be updated"

    if ((onlyEntriesArr != null) && (onlyEntriesArr.length != 0)) {
      if (updateIndex) {
        // First set all white list entries to "should NOT be updated".
        for (int i = 0; i < whiteList.length; i++) {
          whiteList[i].setShouldBeUpdated(false);
        }

        // Now set those entries to "should be updated" that are in the list
        for (int i = 0; i < onlyEntriesArr.length; i++) {
          // Find the matching white list entry
          WhiteListEntry entry = null;
          for (int j = 0; j < whiteList.length; j++) {
            if (onlyEntriesArr[i].equals(whiteList[j].getName())) {
              entry = whiteList[j];
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
        for (int i = 0; i < whiteList.length; i++) {
          if (! whiteList[i].shouldBeUpdated()) {
            mLog.info("Ignoring white list entry: " + whiteList[i].getUrlMatcher());
          }
        }
      } else {
        mLog.warn("Unable to ignore white list entries, because a new index " +
                  "will be created");
      }
    }
    
    return whiteList;
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
   * Prï¿½ft, ob die Exception von einem Dead-Link herrï¿½hrt.
   *
   * @param thr Die zu prï¿½fende Exception
   * @return Ob die Exception von einem Dead-Link herrï¿½hrt.
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
   * und erzeugt fï¿½r jeden Treffer einen neuen Job.
   *
   * @param dir Das zu durchsuchende Verzeichnis.
   * @throws RegainException If encoding the found URLs failed. 
   */
  private void parseDirectory(File dir) throws RegainException {
    // Get the URL for the directory
    String sourceUrl = RegainToolkit.fileToUrl(dir);

    // Parse the directory
    File[] childArr = dir.listFiles();
    for (int childIdx = 0; childIdx < childArr.length; childIdx++) {
      // Get the URL for the current child file
      String url = RegainToolkit.fileToUrl(childArr[childIdx]);

      // Check whether this is a directory
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
   * Durchsucht den Inhalt eines HTML-Dokuments nach URLs und erzeugt fuer jeden
   * Treffer einen neuen Job.
   * 
   * Christian Horn, 28.9.2006
   *
   * @param rawDocument Das zu durchsuchende Dokument.
   * @throws RegainException Wenn das Dokument nicht gelesen werden konnte.
   * @throws IOException 
   * @throws MalformedURLException 
   */
//  private void parseHtmlDocument(RawDocument rawDocument) throws RegainException {
//    for (int i = 0; i < mHtmlParserPatternReArr.length; i++) {
//      RE re = mHtmlParserPatternReArr[i];
//      int urlGroup = mHtmlParserUrlPatternArr[i].getRegexUrlGroup();
//      boolean shouldBeParsed = mHtmlParserUrlPatternArr[i].getShouldBeParsed();
//      boolean shouldBeIndexed = mHtmlParserUrlPatternArr[i].getShouldBeIndexed();
//
//      int offset = 0;
//      String contentAsString = rawDocument.getContentAsString();
//      while (re.match(contentAsString, offset)) {
//        offset = re.getParenEnd(0);
//
//        String parentUrl = rawDocument.getUrl();
//        String url = re.getParen(urlGroup);
//
//        if (url != null) {
//          // Convert the URL to an absolute URL
//          url = CrawlerToolkit.toAbsoluteUrl(url, parentUrl);
//
//          // Try to get a link text
//          String linkText = getLinkText(contentAsString, offset);
//
//          // Add the job
//          addJob(url, parentUrl, shouldBeParsed, shouldBeIndexed, linkText);
//        }
//      }
//     }
//    }
  
  private void parseHtmlDocument(RawDocument rawDocument) throws RegainException, MalformedURLException, IOException {
	  boolean shouldBeParsed = false;
	  boolean shouldBeIndexed = false;
	  boolean isStartUrl=false;
	  String parentUrl = rawDocument.getUrl();
	
	  // Start-URL muss immer vom HTML-Preparator geparst werden!
	  StartUrl[] startUrlArr = mConfiguration.getStartUrls();
	  for (int i = 0; i < startUrlArr.length; i++) {
	      String url = startUrlArr[i].getUrl();
	      isStartUrl=url.equals(parentUrl);
	      if (isStartUrl)
	      {
	    	    shouldBeParsed = startUrlArr[i].getShouldBeParsed();
	    	    shouldBeIndexed = startUrlArr[i].getShouldBeIndexed();
	    	    rawDocument.setDocType(1);//HTML-Typ setzen
	    	    break;
	      }
	    }
	  
	  // Prueft, ob das Dokument ein HTML-Dokument ist. Nur HTML-Dokumente dŸrfen mit Status 1 fŸr den Preparator gekennzeichnet werden. siehe RawDocument.
	  if (!isStartUrl)
	  {
	  	  for (int i = 0; i < mHtmlParserPatternReArr.length; i++) {
	  		
	  		  if(this.isHTMLSite(parentUrl))
	  		  {
	  			  shouldBeParsed = mHtmlParserUrlPatternArr[i].getShouldBeParsed();
	  			  shouldBeIndexed = mHtmlParserUrlPatternArr[i].getShouldBeIndexed();
	  			  rawDocument.setDocType(1); //HTML-Typ setzen
	  			  break;
	  		  }
	  	  }
	  }
	  // Parst eine HTML-Seite und extrahiert alle href`s und erzeugt dann neue Job`s!
	  if (rawDocument.isHTMLDoc())
	  {
	  	  Source source=new Source(new URL(parentUrl));
	  	  source.setLogWriter(new OutputStreamWriter(System.err)); // send log messages to stderr
	  	  source.fullSequentialParse();	
	  	  List linkElements=source.findAllElements(HTMLElementName.A);
	  	  //paro: neu
	  	  for (Iterator i=linkElements.iterator(); i.hasNext();) {
			Element linkElement=(Element)i.next();
			String href=linkElement.getAttributeValue("href");
			//paro: alt--> if (href==null) href=new String("");
			if (href==null) continue;
			// A element can contain other tags so need to extract the text from it:
			String label=linkElement.getContent().extractText();
			//Wichtig um relative Links zu erkennen
			href = CrawlerToolkit.toAbsoluteUrl(href, parentUrl);
			addJob(href, parentUrl, shouldBeParsed, shouldBeIndexed, label);
		  }
	  }
  }

  /**
   * prueft, ob eine URL ein Link auf eine HTML-Seite ist.
   * @param url
   * @return
   */
  
  private boolean isHTMLSite(String url)
  {
	  return ((url.indexOf(".php")>0)||(url.indexOf(".htm")>0)||(url.indexOf(".html")>0)||(url.indexOf(".jsp")>0)||(url.indexOf(".asp")>0)||(url.indexOf(".pl")>0));
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
   * Gibt die Anzahl der Fehler zurï¿½ck (das beinhaltet fatale und nicht fatale
   * Fehler).
   *
   * @return Die Anzahl der Fehler.
   * @see #getFatalErrorCount()
   */
  public int getErrorCount() {
    return mErrorCount;
  }


  /**
   * Gibt Die Anzahl der fatalen Fehler zurï¿½ck.
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