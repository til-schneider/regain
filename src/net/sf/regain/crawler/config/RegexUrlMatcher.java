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
 *  $RCSfile: RegexUrlMatcher.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/RegexUrlMatcher.java,v $
 *     $Date: 2005/05/11 09:21:40 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.config;

import net.sf.regain.RegainException;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * An UrlMatcher that matches URLs that match to a regular expression.
 *  
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class RegexUrlMatcher implements UrlMatcher {

  /** The regex as String. */
  private String mUrlRegexAsString;
  
  /** The regex a URL must match to in order to be matched by this matcher. */
  private RE mUrlRegex;


  /**
   * Creates a new instance of RegexUrlMatcher.
   * 
   * @param regex The regular expression a URL must match to in order to be
   *        matched by this matcher.
   * @throws RegainException
   */
  public RegexUrlMatcher(String regex)
    throws RegainException
  {
    mUrlRegexAsString = regex;
    
    try {
      mUrlRegex = new RE(regex);
    }
    catch (RESyntaxException exc) {
      throw new RegainException("Regular expression of URL matcher has a " +
          "wrong syntax: '" + regex + "'", exc);
    }
  }


  /**
   * Checks whether a URL matches to the rules of this matcher.
   * 
   * @param url The URL to check.
   * @return Whether the given URL matches to the rules of this matcher.
   */
  public boolean matches(String url) {
    return mUrlRegex.match(url);
  }


  /**
   * Gets a String representation of this UrlMatcher.
   */
  public String toString() {
    return mUrlRegexAsString;
  }

}
