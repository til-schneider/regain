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
 *     $Date: 2005-11-21 11:20:09 +0100 (Mo, 21 Nov 2005) $
 *   $Author: til132 $
 * $Revision: 180 $
 */
package net.sf.regain.crawler.preparator;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.pdfbox.encryption.DocumentEncryption;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.PDFTextStripper;

/**
 * Präpariert ein PDF-Dokument für die Indizierung.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PdfBoxPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of PdfBoxPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public PdfBoxPreparator() throws RegainException {
    super("pdf");
  }


  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
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
        // code for PDFBox before 0.6.7
        // DecryptDocument decryptor = new DecryptDocument(pdfDocument);
        
        // code for PDFBox 0.6.7 or higher
        DocumentEncryption decryptor = new DocumentEncryption(pdfDocument);
        
        // Just try using the default password and move on
        decryptor.decryptDocument("");
      }

      // WORKAROUND: The PDFTextStripper has a bug: it does not append a space
      //             after every word to the writer. That's why word a
      //             concatinated.
      StringWriter writer = new StringWriter();

      // Clean the content and write it to a String
      // FixedPdfTextStripper stripper = new FixedPdfTextStripper();
      PDFTextStripper stripper = new PDFTextStripper();

      // code for PDFBox before 0.6.4
      // stripper.writeText(pdfDocument.getDocument(), writer);

      // code for PDFBox 0.6.4 or higher
      stripper.writeText(pdfDocument, writer);

      writer.close();
      setCleanedContent(writer.toString());

      // Get the title
      // NOTE: There is more information that could be read from a PFD-Dokument.
      //       See org.pdfbox.searchengine.lucene.LucenePDFDocument for details.
      PDDocumentInformation info = pdfDocument.getDocumentInformation();

      if( info.getTitle() != null ) {
        setTitle(info.getTitle());
      }
    }
    catch (CryptographyException exc) {
      throw new RegainException("Error decrypting document: " + url, exc);
    }
    catch (InvalidPasswordException exc) {
      // They didn't supply a password and the default of "" was wrong.
      throw new RegainException("Document is encrypted: " + url, exc);
    }
    catch (IOException exc) {
      throw new RegainException("Error reading document: " + url, exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
      if (pdfDocument != null) {
        try { pdfDocument.close(); } catch (Exception exc) {}
      }
    }
  }

}
