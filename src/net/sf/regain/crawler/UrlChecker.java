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
 *     $Date: 2006-02-07 17:24:01 +0100 (Di, 07 Feb 2006) $
 *   $Author: til132 $
 * $Revision: 202 $
 */
package net.sf.regain.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.StartUrl;
import net.sf.regain.crawler.config.UrlMatcher;
import net.sf.regain.crawler.config.WhiteListEntry;

import org.apache.log4j.Logger;

/**
 * Decides whether a URL was already accepted or ignored.
 * <p>
 * For this decision we take advantage of a specialty of the processing of file
 * URLs: Since directory are searched by tree traversing, we can be sure that
 * we never find the same file twice in one crawler run. The only thing we have
 * to attend to make this true is that the start URLs are prefix free among each
 * other (Wich is done by {@link #normalizeStartUrls(StartUrl[])}).
 * <p>
 * For http-URLs we have to remember all accepted or ignored URLs, because in
 * http URLs are found by page parsing which can ramdomly find any URL.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class UrlChecker {

  /** The logger for this class. */
  private static Logger mLog = Logger.getLogger(UrlChecker.class);
  
  /** Contains all http-URLs that have been accepted. */
  private HashSet mAcceptedUrlSet;
  /** Contains all http-URLs that have been ignored. */
  private HashSet mIgnoredUrlSet;
  /** The number of URLs that have been ignored. */
  private int mIgnoredCount;

  /**
   * The white list.
   * <p>
   * The white list is an array of WhiteListEntry, a URLs <i>must</i> match to,
   * in order to be processed.
   */
  private WhiteListEntry[] mWhiteListEntryArr;
  /**
   * The black list.
   * <p>
   * The black list is an array of UrlMatchers, a URL <i>must not</i> match to,
   * in order to be processed.
   */
  private UrlMatcher[] mBlackListArr;


  /**
   * Creates a new instance of UrlChecker.
   * 
   * @param whiteList The white list. The white list is an array of
   *        WhiteListEntry, a URL <i>must</i> match to, in order to be processed.
   * @param blackList The black list. The black list is an array of UrlMatchers,
   *        a URL <i>must not</i> match to, in order to be processed.
   */
  public UrlChecker(WhiteListEntry[] whiteList, UrlMatcher[] blackList) {
    mAcceptedUrlSet = new HashSet();
    mIgnoredUrlSet = new HashSet();
    mIgnoredCount = 0;

    mWhiteListEntryArr = whiteList;
    mBlackListArr = blackList;
  }


  /**
   * Normalizes the start URLs
   * 
   * @param urlArr The start URLs to normalize.
   * @return The normalized start URLs.
   */
  public StartUrl[] normalizeStartUrls(StartUrl[] urlArr) {
    // Check whether the file-URLs are prefix free.
    boolean foundPrefix = false;
    for (int i = 0; i < urlArr.length; i++) {
      String currUrl = urlArr[i].getUrl();
      if (currUrl.startsWith("file://")) {
        // Check whether another URL is a prefix of this URL
        for (int j = 0; j < urlArr.length; j++) {
          if ((i != j) && (urlArr[j] != null)) {
            if (currUrl.startsWith(urlArr[j].getUrl())) {
              // URL i is already covered by searching URL j
              // (Example: i = "file:///home/til/docs/abc", j = "file:///home/til")
              // -> Remove URL i to avoid finding the same file twice
              mLog.info("Ignoring start URL '" + currUrl + "', because it is " +
                  "covered by start URL '" + urlArr[j].getUrl() + "'");

              urlArr[i] = null;
              foundPrefix = true;

              break;
            }
          }
        }
      }
    }
    
    if (foundPrefix) {
      // There were entries removed -> We have to build a new array
      ArrayList list = new ArrayList(urlArr.length);
      for (int i = 0; i < urlArr.length; i++) {
        if (urlArr[i] != null) {
          list.add(urlArr[i]);
        }
      }
      urlArr = new StartUrl[list.size()];
      list.toArray(urlArr);
    }
    
    return urlArr;
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
  public boolean isUrlAccepted(String url) {
    // check whether this URL matches to a white list prefix
    boolean matchesToWhiteList = false;
    for (int i = 0; i < mWhiteListEntryArr.length; i++) {
      if (mWhiteListEntryArr[i].shouldBeUpdated()) {
        UrlMatcher matcher = mWhiteListEntryArr[i].getUrlMatcher();
        if (matcher.matches(url)) {
          matchesToWhiteList = true;
          break;
        }
      }
    }
    if (! matchesToWhiteList) {
      return false;
    }

    // check whether this URL matches to a black list prefix
    for (int i = 0; i < mBlackListArr.length; i++) {
      if (mBlackListArr[i].matches(url)) {
        return false;
      }
    }

    // All tests passed -> URL is accepted
    return true;
  }


  /**
   * Creates an array of UrlMatchers that identify URLs that should not be
   * deleted from the search index.
   * <p>
   * This list is according to the white list entries whichs
   * <code>shouldBeUpdated</code> flag is <code>false</code>.
   *
   * @return An array of UrlMatchers that identify URLs that should not be
   *         deleted from the search index.
   * @see WhiteListEntry#shouldBeUpdated()
   */
  public UrlMatcher[] createPreserveUrlMatcherArr() {
    ArrayList list = new ArrayList();
    for (int i = 0; i < mWhiteListEntryArr.length; i++) {
      if (! mWhiteListEntryArr[i].shouldBeUpdated()) {
        list.add(mWhiteListEntryArr[i].getUrlMatcher());
      }
    }

    UrlMatcher[] asArr = new UrlMatcher[list.size()];
    list.toArray(asArr);
    return asArr;
  }


  /**
   * Decides whether the given URL was already accepted in a crawler run.
   * 
   * @param url The URL to check.
   * @return Whether the URL was already accepted.
   */
  public boolean wasAlreadyAccepted(String url) {
    if (url.startsWith("file://")) {
      // This is a file URL -> We haven't found it yet (Why? See class javadoc)
      return false;
    } else {
      return mAcceptedUrlSet.contains(url);
    }
  }


  /**
   * Decides whether the given URL was already ignored in a crawler run.
   * 
   * @param url The URL to check.
   * @return Whether the URL was already ignored.
   */
  public boolean wasAlreadyIgnored(String url) {
    if (url.startsWith("file://")) {
      // This is a file URL -> We haven't found it yet (Why? See class javadoc)
      return false;
    } else {
      return mIgnoredUrlSet.contains(url);
    }
  }


  /**
   * Decides whether the given URL should be kept in the search index.
   * 
   * @param url The URL to check.
   * @return Whether the URL should be kept in the search index.
   * @throws RegainException If the url is invalid.
   */
  public boolean shouldBeKeptInIndex(String url) throws RegainException {
    if (url.startsWith("file://")) {
      // This is a file URL -> We have no information whether this file exists
      // since we didn't remember whether it was accepted or not.
      
      // Check whether the url is accepted by the white and black list
      if (! isUrlAccepted(url)) {
        // This file is not accepted -> Remove it from the index
        return false;
      }
      
      // Check whether the file exists
      File file = RegainToolkit.urlToFile(url);
      if (! file.exists()) {
        // This file does not exist -> Remove it from the index
        return false;
      }
      
      // All tests passed -> Keep the file
      return true;
    } else {
      return mAcceptedUrlSet.contains(url);
    }
  }


  /**
   * Used by the crawler to set the accepted state for a certain URL.
   * 
   * @param url The URL that was accepted by the crawler.
   */
  public void setAccepted(String url) {
    if (url.startsWith("file://")) {
      // This is a file URL -> We haven't to remember it (Why? See class javadoc)
    } else {
      mAcceptedUrlSet.add(url);
    }
  }


  /**
   * Used by the crawler to set the ignored state for a certain URL.
   * 
   * @param url The URL that was ignored by the crawler.
   */
  public void setIgnored(String url) {
    mIgnoredCount++;
    
    if (url.startsWith("file://")) {
      // This is a file URL -> We haven't to remember it (Why? See class javadoc)
    } else {
      mIgnoredUrlSet.add(url);
    }
  }


  /**
   * Gets the number of URLs that have been ignored.
   * 
   * @return The number of URLs that have been ignored.
   */
  public int getIgnoredCount() {
    return mIgnoredCount;
  }

}
