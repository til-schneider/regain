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
 *     $Date: 2009-11-26 18:14:25 +0100 (Do, 26 Nov 2009) $
 *   $Author: thtesche $
 * $Revision: 430 $
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
    IndexConfig[] indexConfigArr = SearchToolkit.getIndexConfigArr(request);
    for (int i = 0; i < indexConfigArr.length; i++) {
      File indexdir = new File(indexConfigArr[i].getDirectory());
      File newFile = new File(indexdir, "new");
      File indexFile = new File(indexdir, "index");
      
      if (indexdir.exists() && ! indexFile.exists() && ! newFile.exists()) {
        // There is no index -> Forward to the noIndexUrl
        String noIndexUrl = getParameter("noIndexUrl", true);
        response.sendRedirect(noIndexUrl);
        return;
      }
    }
    
    // Check whether there is a query
    String query = SearchToolkit.getSearchQuery(request);
    if ((query == null) || (query.length() == 0)) {
      // There was no query specified -> Forward to the noQueryUrl
      String noQueryUrl = getParameter("noQueryUrl", false);
      if (noQueryUrl != null) {
        response.sendRedirect(noQueryUrl);
        return;
      }
    }
  }

}
