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
 *  $RCSfile: XmlPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/XmlPreparator.java,v $
 *     $Date: 2005/03/14 15:03:38 $
 *   $Author: til132 $
 * $Revision: 1.4 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Präpariert ein XML-Dokument für die Indizierung.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit.
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of XmlPreparator.
   */
  public XmlPreparator() {
    super("xml");
  }


  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    String contentAsString = rawDocument.getContentAsString();

    // Clean the content from tags
    String cleanedContent = CrawlerToolkit.cleanFromHtmlTags(contentAsString);
    setCleanedContent(cleanedContent);
  }

}
