/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2010  Til Schneider, Thomas Tesche
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
 * Contact: Til Schneider, info@murfman.de, Thomas Tesche, regain@thtesche.com
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2009-05-21 16:29:23 +0200 (Do, 21 Mai 2009) $
 *   $Author: thtesche $
 * $Revision: 392 $
 */
package net.sf.regain.test;

import junit.framework.TestCase;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.util.io.PathFilenamePair;

/**
 *
 * @author thtesche
 */
public class RegainToolkitTest extends TestCase {

  public RegainToolkitTest(String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testExtractPathAndFilename() throws RegainException {

    PathFilenamePair pfPair = RegainToolkit.fragmentUrl("file:///dir1/dir2/nsis_license.txt");

    System.out.println(pfPair.getPath());
    System.out.println(pfPair.getFilename());
    assertEquals("nsis_license.txt", pfPair.getFilename());
    assertEquals("/dir1/dir2/", pfPair.getPath());

  }

  public void testWhiteSpacedFileName() throws RegainException {

    assertEquals("file.doc file", RegainToolkit.urlToWhitespacedFileName("file:///dir1/dir2/file.doc"));
    assertEquals("file", RegainToolkit.urlToWhitespacedFileName("file:///dir1/dir2/file"));
    assertEquals(".file", RegainToolkit.urlToWhitespacedFileName("file:///dir1/dir2/.file"));
    assertEquals("file my dok.dic file my dok", RegainToolkit.urlToWhitespacedFileName("file:///dir1/dir2/file my dok.dic"));
    assertEquals("file_my.dok.dic file_my.dok file my dok", RegainToolkit.urlToWhitespacedFileName("file:///dir1/dir2/file_my.dok.dic"));
    assertEquals("nsis_license.txt nsis_license nsis license", RegainToolkit.urlToWhitespacedFileName("file:///dir1/dir2/nsis_license.txt"));

  }
}
