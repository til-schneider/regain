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
 *     $Date: 2008-08-06 16:04:27 +0200 (Mi, 06 Aug 2008) $
 *   $Author: thtesche $
 * $Revision: 325 $
 */
package net.sf.regain.crawler.preparator;

import java.io.IOException;
import java.io.InputStream;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.poi.hslf.extractor.PowerPointExtractor;

/**
 * Prepare a Microsoft PowerPoint document for indexing with help from
 * <a href="http://jakarta.apache.org/poi/">POI-API</a>.
 * <p>
 * The raw data (both slides and notes) and a title is extracted.
 *
 * @author Gerhard Olsson, gerhard.nospam@gmail.com
 */
public class PoiMsPowerPointPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of PoiMsPowerPointPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public PoiMsPowerPointPreparator() throws RegainException {
    super(new String[] {"application/mspowerpoint", "application/vnd.ms-powerpoint"});
  }


  /**
   * Prepares a document for indexing.
   *
   * @param rawDocument The document to prepare.
   *
   * @throws RegainException When the document cannot be prepared.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    InputStream stream = null;
    try {
      stream = rawDocument.getContentAsStream();
      PowerPointExtractor ppe = new PowerPointExtractor(stream);

      setCleanedContent(ppe.getText(true, true));
    }
    catch (IOException exc) {
      throw new RegainException("Reading MS PowerPoint document failed: "
        + rawDocument.getUrl(), exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
  }

}
