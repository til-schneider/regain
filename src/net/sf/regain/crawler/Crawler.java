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
 */
package net.sf.regain.crawler;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import jcifs.smb.SmbFile;
import net.sf.regain.ImapToolkit;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.access.AccountPasswordEntry;
import net.sf.regain.crawler.config.CrawlerConfig;
import net.sf.regain.crawler.config.PreparatorSettings;
import net.sf.regain.crawler.config.StartUrl;
import net.sf.regain.crawler.config.UrlMatcher;
import net.sf.regain.crawler.config.UrlPattern;
import net.sf.regain.crawler.config.WhiteListEntry;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.plugin.CrawlerPluginFactory;
import net.sf.regain.crawler.plugin.CrawlerPluginManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Durchsucht alle konfigurierten Startseiten nach URLs. Die gefundenen Seiten
 * werden je nach Einstellung nur geladen, in den Suchindex aufgenommen oder
 * wiederum nach URLs durchsucht.
 * <p>
 * fÃ¼r jede URL wird Anhand der Schwarzen und der WeiÃŸen Liste entschieden, ob sie
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
  private LinkedList<CrawlerJob> mJobList;

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
  private List<Object[]> mDeadlinkList;

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

  /** Username, password Map for configured hostnames. */
  Map <String, AccountPasswordEntry> accountPasswordStore;

  /** Plugin Manager */
  private CrawlerPluginManager pluginManager = CrawlerPluginManager.getInstance();

  /**
   * Creates a new instance of Crawler.
   *
   * @param config The Configuration
   *
   * @throws RegainException If the regular expressions have errors.
   */
  public Crawler(CrawlerConfig config, Properties authProps) throws RegainException {
    Profiler.clearRegisteredProfilers();

    mCrawlerJobProfiler = new Profiler("Whole crawler jobs", "jobs");
    mHtmlParsingProfiler = new Profiler("Parsed documents", "docs");

    mConfiguration = config;

    mJobList = new LinkedList<CrawlerJob>();
    mDeadlinkList = new LinkedList<Object[]>();

    mFatalErrorCount = 0;

    RawDocument.setHttpTimeoutSecs(config.getHttpTimeoutSecs());

    mHtmlParserUrlPatternArr = config.getHtmlParserUrlPatterns();
    if(mHtmlParserUrlPatternArr.length > 0) {
      mLog.error("Entries in <htmlParserPatternList/> are no longer supported. Please remove " +
        "the Tag and it's children from your config file. Use <whitelist><prefix|regex " +
        "parse=true|false index=true|false >http://someULR</prefix|regex> instead.");
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

    accountPasswordStore = new HashMap<String, AccountPasswordEntry>();
    readAuthenticationProperties(authProps);
    mLog.debug(System.getenv().toString());

    // Create the crawler Plugins
    PreparatorSettings[] crawlerPluginConf = config.getCrawlerPluginSettingsList();
    pluginManager.clear();
    CrawlerPluginFactory.getInstance().createPluggables(crawlerPluginConf); // Automatically registers them to CrawlerPluginManager
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
   * Wenn ja, dann wird ein neuer Job erzeugt und der Job-Liste hinzugefÃ¼gt.
   *
   * @param url Die URL des zu prÃ¼fenden Jobs.
   * @param sourceUrl Die URL des Dokuments in der die URL des zu prÃ¼fenden Jobs
   *        gefunden wurde.
   * @param shouldBeParsed Gibt an, ob die URL geparst werden soll.
   * @param shouldBeIndexed Gibt an, ob die URL indiziert werden soll.
   * @param sourceLinkText Der Text des Links in dem die URL gefunden wurde. Ist
   *        <code>null</code>, falls die URL nicht in einem Link (also einem
   *        a-Tag) gefunden wurde oder wenn aus sonstigen GrÃ¼nden kein Link-Text
   *        vorhanden ist.
   */
  private void addJob(String url, String sourceUrl, boolean shouldBeParsed,
    boolean shouldBeIndexed, String sourceLinkText)
  {
    mLog.debug("Try to add " + url + " referer " + sourceUrl + " as a new crawler job.");

    if (! mConfiguration.getBuildIndex()) {
      // Indexing is disabled
      shouldBeIndexed = false;
    }

    // Change all blanks to %20, since blanks are not allowed in URLs
    url = RegainToolkit.replace(url, " ", "%20");
    // Change all &amp; to &
    url = RegainToolkit.replace(url, "&amp;", "&");

    // Replace parts of the URL with an empty string. The patterns are the
    // URLCleaners
    url = CrawlerToolkit.cleanURL(url, mConfiguration.getURLCleaners());

    boolean alreadyAccepted = mUrlChecker.wasAlreadyAccepted(url);
    boolean alreadyIgnored = mUrlChecker.wasAlreadyIgnored(url);

    if ((! alreadyAccepted) && (! alreadyIgnored)) {
      // Check whether the url matches an entry in the whitelist and not an entry in the blacklist
      // We assume that the caller of addJob() detected the correct values for shouldBeParsed
      // and shouldBeIndexed.
      UrlMatcher urlMatch = mUrlChecker.isUrlAccepted(url);
      boolean accepted;
      if (urlMatch.getShouldBeParsed() || urlMatch.getShouldBeIndexed()) {
        if (pluginManager.eventAskDynamicBlacklist(url, sourceUrl, sourceLinkText)) {
          accepted = false;
        } else {
          accepted = true;
        }
      } else {
        accepted = false;
      }

      int mMaxCycleCount = mConfiguration.getMaxCycleCount();

      if (mMaxCycleCount > 0 && accepted) {
        // Check for cycles in the path of an URI
        accepted = mUrlChecker.hasNoCycles(url, mMaxCycleCount);
        if (mLog.isDebugEnabled() && !accepted) {
          mLog.debug("URI seems to have cycles (maxCount=" + mMaxCycleCount + "): " + url);
        }
      }

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
          mLog.debug("Found new URL: " + url + " in page: " + sourceUrl);
        }

        CrawlerJob job = new CrawlerJob(url, sourceUrl, sourceLinkText,
                                      shouldBeParsed, shouldBeIndexed);
        pluginManager.eventAcceptURL(url, job);

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
      	pluginManager.eventDeclineURL(url);
        mUrlChecker.setIgnored(url);
        if (mLog.isDebugEnabled()) {
          mLog.debug("Ignoring URL: " + url + " in page: " + sourceUrl);
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
    mLog.info("Starting crawling ...");
    pluginManager.eventStartCrawling(this);
    mShouldPause = false;

    int entryCount = 0;
    double failedPercent = 0.0;
    try {
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

	    mLog.debug("Read whitelist entries from config");
	    // Get the white list and set the "should be updated"-flags
	    WhiteListEntry[] whiteList = mConfiguration.getWhiteList();
	    whiteList = useOnlyWhiteListEntries(whiteList, onlyEntriesArr, updateIndex);

	    // Create the UrlChecker
	    mUrlChecker = new UrlChecker(whiteList, mConfiguration.getBlackList());


	    // Add the start URLs
	    mLog.info("Read start-URLs from config");
	    addStartUrls();

	    // Remember the last time when a breakpoint was created
	    long lastBreakpointTime = System.currentTimeMillis();

	    // Work on the job list
	    while (! mJobList.isEmpty()) {
	      mCrawlerJobProfiler.startMeasuring();

	      mCurrentJob = mJobList.removeFirst();
	      String url = mCurrentJob.getUrl();

	      boolean shouldBeParsed = mCurrentJob.shouldBeParsed();
	      boolean shouldBeIndexed = mCurrentJob.shouldBeIndexed();

	      if (url.startsWith("file://")) {
	        // file system: Check whether this is a directory
	        try {
	          File file = RegainToolkit.urlToFile(url);
	          // Check whether the file is readable.
	          if (!file.canRead()) {
	            mCrawlerJobProfiler.abortMeasuring();
	            mLog.debug("File rights: canRead: " + file.canRead() +
	                    " canExecute: " + file.canExecute() +
	                    " canWrite: " + file.canWrite() + " exists: " + file.exists() +
	                    " for url: " + url + ", canonical url: " + file.getCanonicalPath());
	            logError("File is not readable: '" + url + "'", null, false);
	            continue;
	          } else if (file.isDirectory()) {
	            // This IS a directory -> Add all child files as Jobs
	            if (shouldBeParsed) {
	              parseDirectory(file);
	            }

	            // A directory can't be indexed -> continue
	            mCrawlerJobProfiler.stopMeasuring(0);
	            continue;
	          }
	        }
	        catch (Throwable thr) {
	          mCrawlerJobProfiler.abortMeasuring();
	          logError("Invalid URL: '" + url + "'", thr, false);
	          continue;
	        }
	      } else if (url.startsWith("smb://")) {
	        // Windows share: Check whether this is a directory
	        try {
	          SmbFile smbFile = RegainToolkit.urlToSmbFile(
	            CrawlerToolkit.replaceAuthenticationValuesInURL(url,
	            CrawlerToolkit.findAuthenticationValuesForURL(url, accountPasswordStore)));
	          // Check whether the file is readable.
	          if (!smbFile.canRead()) {
	            mCrawlerJobProfiler.abortMeasuring();
	            logError("File is not readable: '" + url + "'", null, false);
	            continue;
	          } else if (smbFile.isDirectory()) {
	            // This IS a directory -> Add all child files as Jobs
	            if (shouldBeParsed) {
	              parseSmbDirectory(smbFile);
	            }

	            // A directory can't be indexed -> continue
	            mCrawlerJobProfiler.stopMeasuring(0);
	            continue;
	          }

	        }
	        catch (Throwable thr) {
	          mCrawlerJobProfiler.abortMeasuring();
	          logError("Invalid URL: '" + url + "'", thr, false);
	          continue;
	        }

	      } else if(url.startsWith("imap://") || url.startsWith("imaps://")) {
	        // IMAP mail box: Check whether this is a folder or an e-mail url
	        try {
	          if( ImapToolkit.isMessageURL( url) == true) {
	            // This is an URL wich describes an a-mail like
	            // imap://user:password@mail.mailhost.com/INBOX/message_23(_attachment_1)
	            // Mail are only indexed one times
	            if( mIndexWriterManager.isAlreadyIndexed(url)) {
	              // do not crawl the mail again
	              mCrawlerJobProfiler.stopMeasuring(0);
	              continue;
	            }
	          } else {
	            // If the URL is not an e-mail it have to be folder. Add all subfolders and
	            // messages as jobs
	            if (shouldBeParsed) {
	              parseIMAPFolder(url);
	            }

	            // A folder can't be indexed -> continue
	            mCrawlerJobProfiler.stopMeasuring(0);
	            continue;
	          }

	        }
	        catch (Throwable thr) {
	          mCrawlerJobProfiler.abortMeasuring();
	          logError("Invalid URL: '" + url + "'", thr, false);
	          continue;
	        }
	      }

	      // Create a raw document
	      RawDocument rawDocument;
	      try {
	        rawDocument = new RawDocument(url, mCurrentJob.getSourceUrl(),
	          mCurrentJob.getSourceLinkText(),
	          CrawlerToolkit.findAuthenticationValuesForURL(url, accountPasswordStore));

	      } catch (RedirectException exc) {
	        String redirectUrl = exc.getRedirectUrl();
	        mLog.info("Redirect '" + url +  "' -> '" + redirectUrl + "'");
	        mUrlChecker.setIgnored(url);
	        // the RedirectURL inherit the properties for shouldBeParsed, shouldBeIndexed from the
	        // sourceURL. This is possibly not right according to definitions in the whitelist
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

	      if( shouldBeIndexed || shouldBeParsed ){
	        if (mLog.isDebugEnabled()) {
	          mLog.debug("Parsing and indexing " + rawDocument.getUrl());
	        }
	        mHtmlParsingProfiler.startMeasuring();

	        // Parse and index content and metadata
	        if (shouldBeIndexed) {
	           try {
	            mIndexWriterManager.addToIndex(rawDocument, this);
	          }
	          catch (RegainException exc) {
	            logError("Indexing failed for: " + rawDocument.getUrl(), exc, false);
	          }
	        }

	        // Extract links form the document (parse=true). The real meaning of parse in this context
	        // is link-extraction. The document is parsed anyway (building a html-node tree).
	        if (shouldBeParsed) {
	          if(!shouldBeIndexed){
	            // The document is not parsed so parse it
	            mIndexWriterManager.getDocumentFactory().createDocument(rawDocument, this);
	          }
	          try {
	            //parseHtmlDocument(rawDocument);
	            createCrawlerJobs(rawDocument);
	          }
	          catch (RegainException exc) {
	            logError("CrawlerJob creation failed for: " + rawDocument.getUrl(), exc, false);
	          }
	        }
	        mHtmlParsingProfiler.stopMeasuring(rawDocument.getLength());
	      }
	      // System-Ressourcen des RawDocument wieder frei geben.
	      rawDocument.dispose();

	      // Zeitmessung stoppen
	      mCrawlerJobProfiler.stopMeasuring(rawDocument.getLength());
	      mCurrentJob = null;

	      // Check whether to create a breakpoint
	      int breakpointInterval = mConfiguration.getBreakpointInterval();
	      boolean breakpointIntervalIsOver = (breakpointInterval > 0)
	        && (System.currentTimeMillis() > lastBreakpointTime + breakpointInterval * 60 * 1000L);
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
	            mLog.info("The crawler sleeps for 1 second.");
	          } catch (InterruptedException exc) {}
	        }

	        lastBreakpointTime = System.currentTimeMillis();
	      }
	    } // while (! mJobList.isEmpty())

	    // Remove documents from the index which no longer exists
	    if (mConfiguration.getBuildIndex()) {
	      mLog.info("Removing index entries of documents that do not exist any more...");
	      try {
	        mIndexWriterManager.removeObsoleteEntries(mUrlChecker);
	      }
	      catch (Throwable thr) {
	        logError("Removing non-existing documents from index failed", thr, true);
	      }
	    }

	    // Check wether the index is empty
	    try {
	      entryCount = mIndexWriterManager.getIndexEntryCount();
	      // NOTE: We've got to substract the errors, because for each failed
	      //       document a substitute document is added to the index
	      //       (which should not be counted).
	      entryCount -= mErrorCount;
	      if (entryCount < 0) {
	        entryCount = 0;
	      }
	    }
	    catch (Throwable thr) {
	      logError("Counting index entries failed", thr, true);
	    }
	    if (entryCount == 0) {
	      logError("The index is empty.", null, true);
	      failedPercent = 1;
	    } else {
	      // Check wether the count of dead/errror links reached the limit
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
	    writeCrawledURLsList();

	    // finalize index
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
    }
    finally {
	    pluginManager.eventFinishCrawling(this);
	    mLog.info("... Finished crawling\n");
    }

    mLog.info(Profiler.getProfilerResults());

    // get system specific line break
    String lineSeparator = RegainToolkit.getLineSeparator();

    mLog.info("Statistics:" + lineSeparator
      + "  Ignored URLs:       " + mUrlChecker.getIgnoredCount() + lineSeparator
      + "  Documents in index: " + entryCount + lineSeparator
      + "  Dead links:         " + mDeadlinkList.size() + lineSeparator
      + "  Errors:             " + mErrorCount + lineSeparator
      + "  Error ratio:        " + RegainToolkit.toPercentString(failedPercent));
  }

  private File createTempDir() {
    // Get the directory where the files should be put in
    File listDir;
    if (mConfiguration.getBuildIndex()) {
      listDir = new File(mConfiguration.getIndexDir() + File.separator + "temp" + File.separator + "log");
    } else {
      listDir = new File("log");
    }
    try {
      // Create the directory if doesn't exist
      if (!listDir.exists() && !listDir.mkdir()) {
        throw new IOException("Creating directory failed: " + listDir.getAbsolutePath());
      }
    } catch (IOException exc) {
      logError("Writing deadlink list and error list failed", exc, false);
    }
    return listDir;
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
    mLog.info("Found " + startUrlArr.length + " startURLs.");

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
   * Reads the authentication properties of all entries.
   */
  private void readAuthenticationProperties(Properties authProps) {

    try {
      mLog.info("Read authentication entries from authentication properties.");

      Set<String> keys = authProps.stringPropertyNames();

      Iterator<String> iter = keys.iterator();
      // Iterate over all keys
      while (iter.hasNext()) {
        String key = iter.next();
        String parts[] = key.split("\\.");

        String url = CrawlerToolkit.createURLFromProps(parts);
        // Check for url in HashMap
        AccountPasswordEntry acPassEntry;
        if (accountPasswordStore.containsKey(url)) {
          acPassEntry = accountPasswordStore.get(url);
        } else {
          acPassEntry = new AccountPasswordEntry();
        }

        if (key.indexOf(".account") != -1) {
          acPassEntry.setAccountName(URLEncoder.encode(authProps.getProperty(key), "UTF-8"));
          mLog.debug("Found account name: " + acPassEntry.getAccountName() + " for auth entry: " + url);
        } else if (key.indexOf(".password") != -1) {
          Base64 decoder = new Base64();
          acPassEntry.setPassword(new String(decoder.decode(authProps.getProperty(key))));
          mLog.debug("Found password for auth entry: " + url);
        }
        // write the updated entry back to hashtable
        mLog.debug("write entry for url >" + url + "<" + " with username/password into authentication store.");
        accountPasswordStore.put(url, acPassEntry);

      }

    } catch (Exception e) {
      mLog.error("Error handling authentication.properties. ", e);
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
   * Writes the URLs of all crawl jobs into a file.
   */
  private void writeCrawledURLsList() {
    if (mUrlChecker.getmAcceptedUrlSet() != null) {

      File listDir = createTempDir();
      FileOutputStream stream = null;
      PrintStream printer = null;

      Set<String> crawledURLs = mUrlChecker.getmAcceptedUrlSet();
      try {
        stream = new FileOutputStream(new File(listDir, "crawledURLs.txt"));
        printer = new PrintStream(stream);

        for (String url : crawledURLs) {
          printer.println(url);
        }
        printer.close();
        stream.close();
      } catch (IOException exc) {
        logError("Writing crawled URLs failed", exc, false);
      } finally {
        if (printer != null) {
          printer.close();
        }
        if (stream != null) {
          try {
            stream.close();
          } catch (IOException exc) {
          }
        }
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
    File listDir = createTempDir();

    String msg;
    FileOutputStream stream = null;
    PrintStream printer = null;
    try {

      // Write the deadlink list
      if (! mDeadlinkList.isEmpty()) {
        stream = new FileOutputStream(new File(listDir, "deadlinks.txt"));
        printer = new PrintStream(stream);

        msg = "There were " + mDeadlinkList.size() + " dead links:";
        System.out.println(msg);
        printer.println(msg);

        Iterator<Object[]> iter = mDeadlinkList.iterator();
        for (int i = 0; iter.hasNext(); i++) {
          Object[] tupel = iter.next();
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
   * PrÃ¼ft, ob die Exception von einem Dead-Link herrï¿½hrt.
   *
   * @param thr Die zu prÃ¼fende Exception
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
   * Searches a directory for URLs, that means files and sub-directories.
   * The method creates a new job for every match
   *
   * @param dir the directory to parse
   * @throws RegainException If encoding of the found URLs failed.
   */
  private void parseDirectory(File dir) throws RegainException {
    // Get the URL for the directory
    String sourceUrl = RegainToolkit.fileToUrl(dir);

    // Parse the directory
    File[] childArr = dir.listFiles();

    // dir.listFiles() can return null, because of "(Access denied)" / "(Zugriff verweigert)"
	if (childArr == null) {
		if (dir.canRead() == false) {
			throw new RegainException("canRead() on file returned: false. Maybe no access rights for sourceURL: " + sourceUrl);
		} else {
			throw new RegainException("listFiles() returned: null array. Maybe no access rights for sourceURL: " + sourceUrl);
		}
	}

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
   * Searches a samba directory for URLs, that means files and sub-directories.
   * The method creates a new job for every match
   *
   * @param dir the directory to parse
   * @throws RegainException If encoding of the found URLs failed.
   */
  private void parseSmbDirectory(SmbFile dir) throws RegainException {

    try {
      // Get the URL for the directory
      String sourceUrl = dir.getCanonicalPath();

      // Parse the directory
      SmbFile[] childArr = dir.listFiles();
      for (int childIdx = 0; childIdx < childArr.length; childIdx++) {
        // Get the URL for the current child file
        String url = childArr[childIdx].getCanonicalPath();
        // Remove domain, username, password
        if (url.contains("@")) {
          url = "smb://" + url.substring(url.indexOf("@") + 1);
        }
        // Check whether this is a directory
        if (childArr[childIdx].isDirectory()) {
          // It's a directory -> Add a parse job
          addJob(url, sourceUrl, true, false, null);
        } else {
          // It's a file -> Add a index job
          addJob(url, sourceUrl, false, true, null);
        }
      }
    } catch( Exception ex ) {
      throw new RegainException(ex.getMessage(), ex);
    }

  }

  /**
   * Searches a imap directory for folder an counts the containing messages
   * The method creates a new job for every not empty folder
   *
   * @param folderUrl  the folder to parse
   * @throws RegainException If encoding of the found URLs failed.
   */
  private void parseIMAPFolder(String folderUrl) throws RegainException {

    mLog.debug("Determine IMAP subfolder for: " + folderUrl);
    Session session = Session.getInstance(new Properties());

    URLName originURLName = new URLName(
      CrawlerToolkit.replaceAuthenticationValuesInURL(folderUrl,
      CrawlerToolkit.findAuthenticationValuesForURL(folderUrl, accountPasswordStore)));
    // Replace all %20 with whitespace in folder pathes
    String folder = "";
    if (originURLName.getFile() != null) {
      folder = originURLName.getFile().replaceAll("%20", " ");
    }
    URLName urlName = new URLName(originURLName.getProtocol(), originURLName.getHost(),
      originURLName.getPort(), folder, originURLName.getUsername(), originURLName.getPassword());

    try {
      IMAPSSLStore imapStore = new IMAPSSLStore(session, urlName);

      imapStore.connect();
      IMAPFolder startFolder;

      if (urlName.getFile() == null) {
        // There is no folder given
        startFolder = (IMAPFolder) imapStore.getDefaultFolder();
      } else {
        startFolder = (IMAPFolder) imapStore.getFolder(urlName.getFile());
      }

      // Find messages (if folder exist and could be openend)
      if (startFolder.exists()) {
        try {
          startFolder.open(Folder.READ_ONLY);
          Message[] msgs = startFolder.getMessages();
          for (int i = 0; i < msgs.length; i++) {
            MimeMessage message = (MimeMessage) msgs[i];
            // It's a message -> Add a index job
            addJob(folderUrl + "/message_" + startFolder.getUID(message), folderUrl, false, true, null);
          }
          startFolder.close(false);

        } catch (MessagingException messageEx) {
          mLog.debug("Could not open folder for reading but this is not an errror. Folder URL is " + folderUrl);
        }
      }
      // Find all subfolder
      Map<String, Integer> folderList = ImapToolkit.getAllFolders(startFolder, false);

      // Iterate over all subfolders
      for (Map.Entry<String, Integer> entry : folderList.entrySet()) {
        // It's a directory -> Add a parse job
        String newFolder;
        if( folderUrl == null || folderUrl.length() == 0 || !folderUrl.endsWith("/")) {
          newFolder = "/" + entry.getKey();
        } else {
          newFolder = entry.getKey();
        }
        addJob(folderUrl + newFolder, folderUrl, true, false, null);
      }
      imapStore.close();

    } catch (Exception ex) {
      throw new RegainException("Couldn't determine IMAP entries.", ex);
    }
  }

  /**
   * Creates crawler jobs from inclosed links. Every link is checked against the
   * white-/black list.
   *
   * @param rawDocument A document with or without links
   * @throws net.sf.regain.RegainException if an exception occurrs during job
   * creation
   */
  private void createCrawlerJobs(RawDocument rawDocument) throws RegainException {
    if (rawDocument.hasLinks()) {
      // Iterate over all found links in the document
      for (Map.Entry<String, String> entry : rawDocument.getLinks().entrySet()) {
        // The intention of this call is only to determine the link-extraction and indexing property
        UrlMatcher urlMatch = mUrlChecker.isUrlAccepted(entry.getKey());
        // Add the job
        addJob(entry.getKey(), rawDocument.getUrl(),
                urlMatch.getShouldBeParsed(), urlMatch.getShouldBeIndexed(), entry.getValue());
      }
    }
  }

 /**
   * Gibt die Anzahl der Fehler zurÃ¼ck (das beinhaltet fatale und nicht fatale
   * Fehler).
   *
   * @return Die Anzahl der Fehler.
   * @see #getFatalErrorCount()
   */
  public int getErrorCount() {
    return mErrorCount;
  }


  /**
   * Gibt Die Anzahl der fatalen Fehler zurÃ¼ck.
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
