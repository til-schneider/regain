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
 *  $RCSfile: PlainTextPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/PlainTextPreparator.java,v $
 *     $Date: 2005/11/21 10:19:29 $
 *   $Author: til132 $
 * $Revision: 1.4 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Präpariert ein Plain-Text-Dokument für die Indizierung.
 * <p>
 * Das Dokument wird dabei unverändert übernommen befreit.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PlainTextPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of PlainTextPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public PlainTextPreparator() throws RegainException {
    super("txt");
  }


  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    setCleanedContent(rawDocument.getContentAsString());
  }

}
