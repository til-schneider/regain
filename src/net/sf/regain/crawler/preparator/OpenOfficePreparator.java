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
 *  $RCSfile: OpenOfficePreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/OpenOfficePreparator.java,v $
 *     $Date: 2005/03/14 15:03:38 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.crawler.preparator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Preparates OpenOffice, StarOffice and OpenDocument documents.
 *
 * @author Til Schneider, www.murfman.de
 */
public class OpenOfficePreparator extends AbstractPreparator {

  /**
   * Creates a new instance of OpenOfficePreparator.
   */
  public OpenOfficePreparator() {
    super(new String[] { "sds", "sdc", "sdw", "sgl", "sda", "sdd", "sdf", "sxw",
           "stw", "sxg", "sxc", "stc", "sxi", "sti", "sxd", "std", "sxm", "odt",
           "ott", "oth", "odm", "odg", "otg", "odp", "otp", "ods", "ots", "odc",
           "odf", "odb", "odi" });
  }


  /**
   * Prepares a document for indexing.
   *
   * @param rawDocument The document to prepare.
   *
   * @throws RegainException If preparing the document failed.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    File file = rawDocument.getContentAsFile();
    
    ZipFile zipFile;
    try {
      zipFile = new ZipFile(file);
    }
    catch (IOException exc) {
      throw new RegainException("Opening OpenOffice file failed: " +
          file.getAbsolutePath(), exc);
    }

    // Read the content.xml
    ZipEntry entry = zipFile.getEntry("content.xml");
    InputStream xmlStream = null;
    String content;
    try {
      xmlStream = zipFile.getInputStream(entry);
      content = RegainToolkit.readStringFromStream(xmlStream, "UTF-8");
    }
    catch (IOException exc) {
      throw new RegainException("Reading text from OpenOffice file failed: " +
          file.getAbsolutePath(), exc);
    }
    finally {
      if (xmlStream != null) {
        try { xmlStream.close(); } catch (IOException exc) {}
      }
      try { zipFile.close(); } catch (IOException exc) {}
    }

    // Clean the content from tags
    String cleanedContent = CrawlerToolkit.cleanFromHtmlTags(content);
    setCleanedContent(cleanedContent);
  }

}
