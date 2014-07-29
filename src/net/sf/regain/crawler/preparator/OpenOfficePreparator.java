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
   *
   * @throws RegainException If creating the preparator failed.
   */
  public OpenOfficePreparator() throws RegainException {
    super( new String[] {
      "application/vnd.sun.xml.writer",
      "application/vnd.sun.xml.writer.template",
      "application/vnd.sun.xml.writer.global",
      "application/vnd.sun.xml.calc",
      "application/vnd.sun.xml.calc.template",
      "application/vnd.stardivision.calc",
      "application/vnd.sun.xml.impress",
      "application/vnd.sun.xml.impress.template ",
      "application/vnd.stardivision.impress sdd",
      "application/vnd.sun.xml.draw",
      "application/vnd.sun.xml.draw.template",
      "application/vnd.stardivision.draw",
      "application/vnd.sun.xml.math",
      "application/vnd.stardivision.math",
      "application/vnd.oasis.opendocument.text",
      "application/vnd.oasis.opendocument.text-template",
      "application/vnd.oasis.opendocument.text-web",
      "application/vnd.oasis.opendocument.text-master",
      "application/vnd.oasis.opendocument.graphics",
      "application/vnd.oasis.opendocument.graphics-template",
      "application/vnd.oasis.opendocument.presentation",
      "application/vnd.oasis.opendocument.presentation-template",
      "application/vnd.oasis.opendocument.spreadsheet",
      "application/vnd.oasis.opendocument.spreadsheet-template",
      "application/vnd.oasis.opendocument.chart",
      "application/vnd.oasis.opendocument.formula",
      "application/vnd.oasis.opendocument.database",
      "application/vnd.oasis.opendocument.image"});

    /*super(new String[] {
        // Writer
        "odt", "oth", "ott", "sdw", "stw", "sxw",

        // Calc
        "ods", "ots", "sdc", "stc", "sxc",

        // Draw
        "otg", "sda", "std",

        // Impress
        "odg", "odp", "otp", "sdd", "sti", "sxd", "sxi",

        // Base
        "odb",

        // Math
        "odf", "sxm",

        // Chart
        "odc", "sds",

        // Image
        "odi",

        // Master
        "odm", "sgl", "sxg",
      });*/
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
    if (entry == null)
        throw new RegainException("Opening OpenOffice file failed (no content.xml found inside): " +
                file.getAbsolutePath());

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
