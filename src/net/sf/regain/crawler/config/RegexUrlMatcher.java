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
 *     $Date: 2008-11-23 23:46:59 +0100 (So, 23 Nov 2008) $
 *   $Author: thtesche $
 * $Revision: 364 $
 */
package net.sf.regain.crawler.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.regain.RegainException;
import org.apache.log4j.Logger;


/**
 * An UrlMatcher that matches URLs that match to a regular expression.
 *  
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class RegexUrlMatcher extends UrlMatcherImpl {
  
  /** The logger for this class. */
  private static Logger mLog = Logger.getLogger(RegexUrlMatcher.class);
  /** The regex as String. */
  private String mUrlRegexAsString;
  
  /** The regex a URL must match to in order to be matched by this matcher. */
  private Pattern mUrlRegex;


  /**
   * Creates a new instance of RegexUrlMatcher.
   * 
   * @param regex The regular expression a URL must match to in order to be
   *        matched by this matcher.
   * @throws RegainException
   */
  public RegexUrlMatcher(String regex, boolean shouldBeParsed, boolean shouldBeIndexed)
    throws RegainException
  {
    super(shouldBeParsed, shouldBeIndexed);
    mUrlRegexAsString = regex;

    try {
      mUrlRegex = Pattern.compile(regex);
    } catch (Exception ex) {
      throw new RegainException("Regular expression of URL matcher has a " +
          "wrong syntax: '" + regex + "'", ex);
    }
  }

  /**
   * Checks whether a URL matches to the rules of this matcher.
   * 
   * @param url The URL to check.
   * @return Whether the given URL matches to the rules of this matcher.
   */
  @Override
  public boolean matches(String url) {
    Matcher matcher = mUrlRegex.matcher(url);
    mLog.debug("Matches with pattern: " + mUrlRegexAsString + ", " + mUrlRegex.pattern());
    
    return matcher.matches();
  }


  /**
   * Gets a String representation of this UrlMatcher.
   */
  @Override
  public String toString() {
    return mUrlRegexAsString;
  }

}
