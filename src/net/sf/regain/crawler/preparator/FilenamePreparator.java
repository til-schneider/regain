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
 *     $Date: 2011-12-09 19:28:00 +0100 (Fr, 09 Dez 2011) $
 *   $Author: thtesche $
 * $Revision: 550 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Like EmptyPreparator, but indexes the filename as content.
 * 
 * @author Benjamin
 */
public class FilenamePreparator extends AbstractPreparator
{
  @Override
  public void prepare(RawDocument rawDocument) throws RegainException {
    String filename = RegainToolkit.urlToFileName(rawDocument.getUrl());
    filename = getName(filename);
    setCleanedContent(filename);
  }

  /**
   * From: Apache Commons IO
   * @param filename
   * @return
   */
  private static String getName(String filename)
  {
    if (filename == null) {
      return null;
    }
    int index = indexOfLastSeparator(filename);
    return filename.substring(index + 1);
  }

  private static int indexOfLastSeparator(String filename) {
    if (filename == null) {
      return -1;
    }
    int lastUnixPos = filename.lastIndexOf('/');
    int lastWindowsPos = filename.lastIndexOf('\\');
    return Math.max(lastUnixPos, lastWindowsPos);
  }
}
