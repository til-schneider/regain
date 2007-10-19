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

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Präpariert ein RTF-Dokument für die Indizierung. Dazu wird der RTF-Parser
 * von Swing genutzt.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SwingRtfPreparator extends AbstractPreparator {

  /** Das RTFEditorKit, das zum laden von RTF-Dokumenten verwendet wird. */
  private RTFEditorKit mRTFEditorKit;


  /**
   * Creates a new instance of SwingRtfPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public SwingRtfPreparator() throws RegainException {
    super("rtf");
  }


  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    if (mRTFEditorKit == null) {
      mRTFEditorKit = new RTFEditorKit();
    }

    InputStream stream = null;
    try {
      stream = rawDocument.getContentAsStream();
      Document doc = mRTFEditorKit.createDefaultDocument();
      mRTFEditorKit.read(stream, doc, 0);

      String cleanedContent = doc.getText(0, doc.getLength());
      setCleanedContent(cleanedContent);
    }
    catch (Exception exc) {
      throw new RegainException("Reading RTF dokument failed: "
        + rawDocument.getUrl(), exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
  }


  /**
   * Frees all resources reserved by the preparator.
   * <p>
   * Is called at the end of the crawler process after all documents were
   * processed.
   * 
   * @throws RegainException If freeing the resources failed.
   */
  public void close() throws RegainException {
    mRTFEditorKit = null;
  }

}
