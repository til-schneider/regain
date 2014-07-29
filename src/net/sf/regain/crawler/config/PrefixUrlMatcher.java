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
package net.sf.regain.crawler.config;

import org.apache.log4j.Logger;

/**
 * A UrlMatcher that matches URLs that start with a certain prefix.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class PrefixUrlMatcher extends UrlMatcherResult {

  /** The logger for this class. */
  private static Logger mLog = Logger.getLogger(PrefixUrlMatcher.class);
  /** The prefix a URL must start with in order to be matched by this matcher. */
  private String mUrlPrefix;


  /**
   * Creates a new instance of PrefixUrlMatcher.
   *
   * @param urlPrefix The prefix a URL must start with to be matched by this
   *        matcher.
   */
  public PrefixUrlMatcher(String urlPrefix, boolean shouldBeParsed, boolean shouldBeIndexed) {
    super(shouldBeParsed, shouldBeIndexed);
    mUrlPrefix = urlPrefix;
  }


  /**
   * Checks whether a URL matches to the rules of this matcher.
   *
   * @param url The URL to check.
   * @return Whether the given URL matches to the rules of this matcher.
   */
  @Override
  public boolean matches(String url) {
    mLog.debug("Matches with prefix: " + url + ", " + mUrlPrefix);
    return url.startsWith(mUrlPrefix);
  }


  /**
   * Gets a String representation of this UrlMatcher.
   */
  @Override
  public String toString() {
    return mUrlPrefix;
  }

}
