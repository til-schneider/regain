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
 *     $Date: 2010-11-07 16:26:47 +0100 (So, 07 Nov 2010) $
 *   $Author: thtesche $
 * $Revision: 466 $
 */
package net.sf.regain.crawler.preparator;

import java.io.InputStream;
import java.io.IOException;
import org.apache.log4j.Logger;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Präpariert ein PDF-Dokument für die Indizierung.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PdfBoxPreparator extends AbstractPreparator {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(PdfBoxPreparator.class);

  /**
   * Creates a new instance of PdfBoxPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public PdfBoxPreparator() throws RegainException {
    super("application/pdf");
  }

  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu pr�pariernde Dokument.
   *
   * @throws RegainException Wenn die Pr�paration fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    String url = rawDocument.getUrl();

    InputStream stream = null;
    PDDocument pdfDocument = null;

    try {
      // Create a InputStream that reads the content.
      stream = rawDocument.getContentAsStream();

      // Parse the content
      PDFParser parser = new PDFParser(stream);
      parser.parse();
      pdfDocument = parser.getPDDocument();

      // Decrypt the PDF-Dokument
      if (pdfDocument.isEncrypted()) {
        pdfDocument.decrypt("");
      }

      // Extract the text with a utility class
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setSuppressDuplicateOverlappingText(false);
      stripper.setSortByPosition(true);
      stripper.setStartPage(1);
      stripper.setEndPage(Integer.MAX_VALUE);

      setCleanedContent(stripper.getText(pdfDocument).replaceAll("visiblespace", " "));

      // Get the meta data
      PDDocumentInformation info = pdfDocument.getDocumentInformation();
      StringBuilder metaData = new StringBuilder();
      metaData.append("p.");
      metaData.append(Integer.toString(pdfDocument.getNumberOfPages()));
      metaData.append(" ");

      // Check if fields are null
      if (info.getAuthor() != null) {
        metaData.append(info.getAuthor());
        metaData.append(" ");
      }
      if (info.getSubject() != null) {
        metaData.append(info.getSubject());
        metaData.append(" ");
      }
      if (info.getKeywords() != null) {
        metaData.append(info.getKeywords());
        metaData.append(" ");
      }

      if (info.getTitle() != null) {
        setTitle(info.getTitle());
      }

      setCleanedMetaData(metaData.toString());
      if (mLog.isDebugEnabled()) {
        mLog.debug("Extracted meta data ::" + getCleanedMetaData()
                + ":: from " + rawDocument.getUrl());
      }

    } catch (CryptographyException exc) {
      throw new RegainException("Error decrypting document: " + url, exc);
    } catch (InvalidPasswordException exc) {
      // They didn't supply a password and the default of "" was wrong.
      throw new RegainException("Document is encrypted: " + url, exc);
    } catch (IOException exc) {
      throw new RegainException("Error reading document: " + url, exc);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (Exception exc) {
        }
      }
      if (pdfDocument != null) {
        try {
          pdfDocument.close();
        } catch (Exception exc) {
        }
      }
    }
  }
}
