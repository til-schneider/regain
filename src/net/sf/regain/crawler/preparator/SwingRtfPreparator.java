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
 *  $RCSfile: SwingRtfPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/SwingRtfPreparator.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator;

import java.io.ByteArrayInputStream;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.RawDocument;


/**
 * Präpariert ein RTF-Dokument für die Indizierung. Dazu wird der RTF-Parser
 * von Swing genutzt.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class SwingRtfPreparator extends AbstractPreparator {

  /** Das RTFEditorKit, das zum laden von RTF-Dokumenten verwendet wird. */
  private RTFEditorKit mRTFEditorKit;

  
  
  /**
   * Erzeugt eine neue RtfPreparator-Instanz.
   */
  public SwingRtfPreparator() {
    mRTFEditorKit = new RTFEditorKit();
  }


  
  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    ByteArrayInputStream stream = null;
    try {
      stream = new ByteArrayInputStream(rawDocument.getContent());
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

}
