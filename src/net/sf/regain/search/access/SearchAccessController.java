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
 *  $RCSfile: SearchAccessController.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/access/SearchAccessController.java,v $
 *     $Date: 2005/03/30 10:30:03 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.access;

import java.util.Properties;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;

/**
 * Identifies the groups a user has reading rights for.
 * <p>
 * This interface is a part of the access control system that ensures that only
 * those documents are shown in the search results that the user is allowed to
 * read.
 * 
 * @see net.sf.regain.crawler.access.CrawlerAccessController
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public interface SearchAccessController {

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
   * Gets the groups the current user has reading rights for.
   * 
   * @param request The page request to use for identifying the user.
   * @return The groups of the current user.
   * 
   * @throws RegainException If getting the groups failed.
   */
  public String[] getUserGroups(PageRequest request)
    throws RegainException;

}
