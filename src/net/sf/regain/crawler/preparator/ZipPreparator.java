/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2012  Thomas Tesche
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
 * Contact: Thomas Tesche, info@clustersystems.de
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-03-16 20:50:37 +0100 (So, 16 Mär 2008) $
 *   $Author: thtesche $
 * $Revision: 281 $
 */
package net.sf.regain.crawler.preparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Prepares  archive files (zipped content) for indexing
 * <p>
 * The following information will be extracted:
 * filename (toLowerCase)
 *
 * @author Thomas Tesche, cluster:Systems CSG GmbH, http://www.clustersystems.info
 */
public class ZipPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of ZipPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public ZipPreparator() throws RegainException {
    super(new String[]{"application/zip"});
  }

  /**
   * Prepares the document for indexing
   *
   * @param rawDocument the document
   *
   * @throws RegainException if preparation goes wrong
   */
  @Override
  public void prepare(RawDocument rawDocument) throws RegainException {

    File rawFile = rawDocument.getContentAsFile(false);
    ArrayList<String> contentParts = new ArrayList<String>();

    try {
      ZipFile zipFile = new ZipFile(rawFile, ZipFile.OPEN_READ);
      for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
        ZipEntry entry = (ZipEntry) entries.nextElement();
        if (!entry.isDirectory()) {
          String filenameWithVariants =
                  RegainToolkit.urlToWhitespacedFileName(entry.getName());
          if (!filenameWithVariants.startsWith(
                  entry.getName().substring(0, entry.getName().length() - 1))) {
            contentParts.add(entry.getName());
          }
          contentParts.add(filenameWithVariants);
        }
      }

      setCleanedContent(concatenateStringParts(contentParts, Integer.MAX_VALUE));

    } catch (Exception ex) {
      throw new RegainException("Error parsing archive (zipped) file: "
              + rawDocument.getUrl(), ex);
    }

  }
}
