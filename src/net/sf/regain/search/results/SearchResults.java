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
 *     $Date: 2005-08-20 13:20:15 +0200 (Sa, 20 Aug 2005) $
 *   $Author: til132 $
 * $Revision: 174 $
 */
package net.sf.regain.search.results;

import net.sf.regain.RegainException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

/**
 * Holds the results of one search.
 *
 * @see net.sf.regain.search.SearchToolkit#getSearchResults(net.sf.regain.util.sharedtag.PageRequest)
 * @author Til Schneider, www.murfman.de
 */
public interface SearchResults {

  /**
   * Gets the number of hits the search had.
   *
   * @return the number of hits the search had.
   */
  public int getHitCount();

  /**
   * Gets the document of one hit.
   *
   * @param index The index of the hit.
   * @return the document of one hit.
   *
   * @throws RegainException If getting the document failed.
   * @see Document
   */
  public Document getHitDocument(int index) throws RegainException;

  /**
   * Gets the score of one hit.
   *
   * @param index The index of the hit.
   * @return the score of one hit.
   *
   * @throws RegainException If getting the score failed.
   * @see Hits#score(int)
   */
  public float getHitScore(int index) throws RegainException;

  /**
   * Gets the url from a hit and rewrites it according to the rewrite rules
   * specified in the index config.
   * 
   * @param index The index of the hit to get the URL for.
   * @return The url of the wanted hit.
   * @throws RegainException If getting the hit document failed.
   */
  public String getHitUrl(int index) throws RegainException;

  /**
   * Gets the name of the index a hit comes from.
   * 
   * @param index The index of the hit to get the index name for.
   * @return The name of the index a hit comes from.
   * @throws RegainException If getting the index name failed.
   */
  public String getHitIndexName(int index) throws RegainException;

  /**
   * Gets whether a hit should be opened in a new window.
   *
   * @param index The index of the hit.
   * @return Whether the hit should be opened in a new window.
   * @throws RegainException If the hit could not be read.
   */
  public boolean getOpenHitInNewWindow(int index) throws RegainException;

  /**
   * Gets whether the file-to-http-bridge should be used for a certain hit.
   * <p>
   * Mozilla browsers have a security mechanism that blocks loading file-URLs
   * from pages loaded via http. To be able to load files from the search
   * results, regain offers the file-to-http-bridge that provides all files that
   * are listed in the index via http.
   *
   * @param index The index of the hit. 
   * @return Whether the file-to-http-bridge should be used.
   * @throws RegainException If the hit could not be read.
   */
  public boolean getUseFileToHttpBridgeForHit(int index) throws RegainException;

  /**
   * Gets the time the search took in milliseconds.
   *
   * @return The search time.
   */
  public int getSearchTime();

}
