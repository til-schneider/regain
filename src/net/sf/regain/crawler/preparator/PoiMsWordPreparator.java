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
 *     $Date: 2008-09-19 20:29:55 +0200 (Fr, 19 Sep 2008) $
 *   $Author: thtesche $
 * $Revision: 340 $
 */
package net.sf.regain.crawler.preparator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.poi.hdf.extractor.WordDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;


/**
 * Pr�pariert ein Microsoft-Word-Dokument f�r die Indizierung mit Hilfe der
 * <a href="http://jakarta.apache.org/poi/">POI-API</a>.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PoiMsWordPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of PoiMsWordPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public PoiMsWordPreparator() throws RegainException {
    super( new String[] {"application/msword", "application/vnd.ms-word" });
  }


  /**
   * Pr�pariert ein Dokument f�r die Indizierung.
   *
   * @param rawDocument Das zu pr�pariernde Dokument.
   *
   * @throws RegainException Wenn die Pr�paration fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    InputStream stream = null;
    try {
      stream = rawDocument.getContentAsStream();
      WordExtractor extractor = new WordExtractor(stream);
      setCleanedContent(extractor.getText());
    }
    catch (IOException exc) {
      throw new RegainException("Reading MS Word dokument failed: "
        + rawDocument.getUrl(), exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
  }

  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   * @deprecated
   */
  public void prepareWordDocument(RawDocument rawDocument) throws RegainException {
    InputStream stream = null;
    try {
      stream = rawDocument.getContentAsStream();
      WordDocument doc = new WordDocument(stream);

      StringWriter cleanWriter = new StringWriter();
      doc.writeAllText(cleanWriter);

      cleanWriter.close();
      setCleanedContent(cleanWriter.toString());
    }
    catch (IOException exc) {
      throw new RegainException("Reading MS Word dokument failed: "
        + rawDocument.getUrl(), exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
  }
 

}
