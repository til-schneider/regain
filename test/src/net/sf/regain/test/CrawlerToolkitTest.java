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
 *     $Date: 2008-11-16 22:23:54 +0100 (So, 16 Nov 2008) $
 *   $Author: thtesche $
 * $Revision: 360 $
 */
package net.sf.regain.test;

import net.sf.regain.crawler.CrawlerToolkit;
import junit.framework.TestCase;

/**
 * A JUnit test for the class {@link net.sf.regain.crawler.CrawlerToolkit}.
 *
 * @author Til Schneider, www.murfman.de
 * @author Thomas Tesche, www.thtesche.com
 */
public class CrawlerToolkitTest extends TestCase {

  public void testCreateURLFromProps() {
    assertEquals("", CrawlerToolkit.createURLFromProps(new String[]{"imap", "test"}));
    assertEquals("imap://sld.tld/", CrawlerToolkit.createURLFromProps(new String[]{"imap", "sld", "tld", "account"}));
    assertEquals("imap://tld.sld.fld/", CrawlerToolkit.createURLFromProps(new String[]{"imap", "tld", "sld", "fld", "account"}));
    assertEquals("imap://tld.sld.fld:993/", CrawlerToolkit.createURLFromProps(new String[]{"imap", "tld", "sld", "fld", "993", "account"}));
  }

  /**
   * Tests {@link CrawlerToolkit#toAbsoluteUrl(String, String)}.
   */
  public void testToAbsoluteUrl() {
    // Test absolute
    assertEquals("http://murfman.de/abc/f/g",
      CrawlerToolkit.toAbsoluteUrl("http://murfman.de/abc/f/g", "http://murfman.de/abc/d/e/bla.html"));

    // Test relative
    assertEquals("http://murfman.de/abc/d/e/bla/test.doc",
      CrawlerToolkit.toAbsoluteUrl("bla/test.doc", "http://murfman.de/abc/d/e/bla.html"));

    // Test absolute within domain
    assertEquals("http://murfman.de/bla/test.doc",
      CrawlerToolkit.toAbsoluteUrl("/bla/test.doc", "http://murfman.de/abc/d/e/bla.html"));

    // Test ..
    assertEquals("file://abc/d/test.doc",
      CrawlerToolkit.toAbsoluteUrl("../test.doc", "file://abc/d/e/"));
    assertEquals("file://abc/d/e/test.doc",
      CrawlerToolkit.toAbsoluteUrl("f/../test.doc", "file://abc/d/e/"));
    assertEquals("file://abc/d/e/f",
      CrawlerToolkit.toAbsoluteUrl("f/g/..", "file://abc/d/e/"));
    assertEquals("file://abc/d/e/..otto/karl../test.doc",
      CrawlerToolkit.toAbsoluteUrl("..otto/karl../test.doc", "file://abc/d/e/"));
    assertEquals("http://xyz/shg/abc/def/test.html",
      CrawlerToolkit.toAbsoluteUrl("abc/ash/khjasd/../../asjdhg/ghj/fght/../.././../def/test.html", "http://xyz/shg/main.html"));

    // Test .
    assertEquals("file://abc/d/e/f/g",
      CrawlerToolkit.toAbsoluteUrl("./f/./g", "file://abc/d/e/"));
    assertEquals("file://abc/d/e/f/g",
      CrawlerToolkit.toAbsoluteUrl("f/g/.", "file://abc/d/e/"));
    assertEquals("file://abc/d/e/f/.protected",
      CrawlerToolkit.toAbsoluteUrl("f/.protected", "file://abc/d/e/"));
  }
}
