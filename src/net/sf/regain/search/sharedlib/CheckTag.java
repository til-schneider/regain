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
 *     $Date: 2011-08-13 15:15:43 +0200 (Sa, 13 Aug 2011) $
 *   $Author: benjaminpick $
 * $Revision: 525 $
 */
package net.sf.regain.search.sharedlib;

import java.io.File;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Checks whether there is a index and whether a query was given. If one of
 * these conditions is not fulfilled, a redirect to another page is sent.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>noIndexUrl</code>: The URL to redirect to if there is no index.</li>
 * <li><code>noQueryUrl</code>: The URL to redirect to if there is no query.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class CheckTag extends SharedTag {

  /**
   * Called when the parser reaches the end tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  @Override
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    // Check whether the indexes exist
    boolean indexFound = false;
    String noIndexUrl = getParameter("noIndexUrl", true);

    for (IndexConfig config : SearchToolkit.getIndexConfigArr(request)) {
      File indexDir = new File(config.getDirectory());
      File newFile = new File(indexDir, "new");
      File indexFile = new File(indexDir, "index");
      
      if (indexDir.exists()) {
        if (indexFile.exists() || newFile.exists()) {
          indexFound = true;
        } else {        
          // There is no index -> Forward to the noIndexUrl
          response.sendRedirect(noIndexUrl);
          return;
        }
      }
    }
    if (!indexFound)
    {
      // There is no index -> Forward to the noIndexUrl
      response.sendRedirect(noIndexUrl);
      return;
    }
    
    // Check whether there is a query
    String noQueryUrl = getParameter("noQueryUrl", false);
    if (noQueryUrl != null)
    {
      String query = SearchToolkit.getSearchQuery(request);
      if ((query == null) || (query.length() == 0)) {
        // There was no query specified -> Forward to the noQueryUrl
        response.sendRedirect(noQueryUrl);
        return;
      }
    }
  }

}
