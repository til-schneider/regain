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
 *  $RCSfile: CrawlerAccessController.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/access/CrawlerAccessController.java,v $
 *     $Date: 2005/04/14 08:15:34 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.crawler.access;

import java.util.Properties;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Identifies the groups that are are allowed to read a document. These groups
 * will be stored in the index and will be used by the
 * {@link net.sf.regain.search.access.SearchAccessController} which is the
 * counter-part to this class.
 * <p>
 * This interface is a part of the access control system that ensures that only
 * those documents are shown in the search results that the user is allowed to
 * read.
 * 
 * @see net.sf.regain.search.access.SearchAccessController
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public interface CrawlerAccessController {

  /**
   * Initializes the CrawlerAccessController.
   * <p>
   * This method is called once right after the CrawlerAccessController instance
   * was created.
   *  
   * @param config The configuration.
   * 
   * @throws RegainException If loading the config failed.
   */
  public void init(Properties config) throws RegainException;

  /**
   * Gets the names of the groups that are allowed to read the given document.
   * <p>
   * Note: The group array must not be <code>null</code> and the group names
   * must not contain whitespace.
   * 
   * @param document The document to get the groups for.
   * @return The groups that are allowed to read the given document.
   * 
   * @throws RegainException If getting the groups failed.
   */
  public String[] getDocumentGroups(RawDocument document)
    throws RegainException;

}
