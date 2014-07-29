/*
 * regain/2 - A file search engine providing plenty of formats
 * Copyright (C) 2004, 2088  Til Schneider, Thomas Tesche
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
 * Author: Thomas Tesche, cluster:Consult, Gletscherstr.13, 16341 Panketal
 *         +49 30 946 300 34, thomas.tesche@thtesche.com
 *
 *
 */
package net.sf.regain.crawler.config;

/**
 * An UrlMatcherResult that implements the UrlMatcher interface.
 * This URLMatcher holds only the result of another URLMatcher.
 *
 * @author Thomas Tesche, cluster:Consult, http://www.thtesche.com/
 */
public class UrlMatcherResult implements UrlMatcher {

  /** The link extraction flag */
  private boolean mShouldBeParsed;
  /** The content indexing flag */
  private boolean mShouldBeIndexed;
  /** The result of the matching. */
  private boolean mMatches;

  /**
   * Creates a new instance of UrlMatcher.
   *
   * @param shouldBeParsed, whether from a match for matches(url) possibly inclosed links should be extracted.
   * @param shouldBeIndexed, whether from a match for matches(url) the content from the URL should be indexed.
   */
  public UrlMatcherResult(boolean shouldBeParsed, boolean shouldBeIndexed) {
    mShouldBeParsed = shouldBeParsed;
    mShouldBeIndexed = shouldBeIndexed;
    mMatches = false;
  }

  /**
   * Checks whether a URL matches to the rules of this matcher.
   *
   * @param url The URL to check.
   * @return Whether the given URL matches to the rules of this matcher.
   */
  //@Override
  public boolean matches(String url) {
    return mMatches;
  }

  /**
   * Gets a flag whether links should be extracted from the content
   *
   * @return Whether from a match for matches(url) possibly inclosed links should be extracted.
   */
  //@Override
  public boolean getShouldBeParsed() {
    return mShouldBeParsed;
  }

  /**
   * Gets a flag whether the content should be indexed.
   *
   * @return Whether from a match for matches(url) the content from the URL should be indexed.
   */
  //@Override
  public boolean getShouldBeIndexed() {
    return mShouldBeIndexed;
  }

  /**
   * Sets a flag whether links should be extracted from the content
   */
  //@Override
  public void setShouldBeParsed(boolean shouldBeParsed) {
    this.mShouldBeParsed = shouldBeParsed;
  }

  /**
   * Sets a flag whether the content should be indexed.
   */
  //@Override
  public void setShouldBeIndexed(boolean shouldBeIndexed) {
    this.mShouldBeIndexed = shouldBeIndexed;
  }
}
