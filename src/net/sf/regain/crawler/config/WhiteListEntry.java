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

/**
 * Ein Eintrag in der Weißen Liste.
 * <p>
 * Die Wei�e Liste enthält Präfixe, von denen eine URL einen haben <i>mu�</i>,
 * um bearbeitet zu werden.
 * <p>
 * Des weiteren wird durch die Wei�e Liste festgelegt, welche Teile des Index
 * vom Crawler bearbeitet werden sollen.
 *
 * @author Til Schneider, www.murfman.de
 */
public class WhiteListEntry {

  /** The UrlMatcher a URL must match to in order to be processed. */
  private UrlMatcher mUrlMatcher;

  /** The name of the white list entry. May be <code>null</code>. */
  private String mName;

  /** Specifies whether the crawler should update URLs that match to this entry. */
  private boolean mShouldBeUpdated;


  /**
   * Creates a new instance of WhiteListEntry.
   *
   * @param urlMatcher The UrlMatcher a URL must match to in order to be
   *        processed.
   * @param name The name of the white list entry. May be <code>null</code>.
   */
  public WhiteListEntry(UrlMatcher urlMatcher, String name) {
    mUrlMatcher = urlMatcher;
    mName = name;

    mShouldBeUpdated = true;
  }


  /**
   * Gets the UrlMatcher a URL must match to in order to be processed.
   *
   * @return The UrlMatcher a URL must match to in order to be processed..
   */
  public UrlMatcher getUrlMatcher() {
    return mUrlMatcher;
  }


  /**
   * Gets the name of the white list entry. May be <code>null</code>.
   *
   * @return The name. May be <code>null</code>.
   */
  public String getName() {
    return mName;
  }


  /**
   * Gets whether the crawler should update URLs that match to this entry.
   *
   * @return Whether the crawler should update URLs that match to this entry.
   */
  public boolean shouldBeUpdated() {
    return mShouldBeUpdated;
  }


  /**
   * Sets whether the crawler should update URLs that match to this entry.
   *
   * @param shouldBeUpdated Whether the crawler should update URLs that match to
   *        this entry.
   */
  public void setShouldBeUpdated(boolean shouldBeUpdated) {
    mShouldBeUpdated = shouldBeUpdated;
  }

}
