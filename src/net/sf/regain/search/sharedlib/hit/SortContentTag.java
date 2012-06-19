/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2010  Til Schneider, Thomas Tesche
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
 * Contact: Til Schneider, info@murfman.de, Thomas Tesche, www.thtesche.com
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-03-01 17:04:30 +0100 (Tue, 01 Mar 2005) $
 *   $Author: til132 $
 * $Revision: 46 $
 */
package net.sf.regain.search.sharedlib.hit;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchConstants;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.util.io.Localizer;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates the SortContent of the current hit.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SortContentTag extends SharedTag implements SearchConstants {

  /**
   * Called when the parser reaches the end tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  @Override
  public void printEndTag(PageRequest request, PageResponse response)
          throws RegainException {
    String sortContent = (String) request.getContextAttribute(ATTR_CURRENT_HIT_SORT_CONTENT);
    // Get the IndexConfig
    IndexConfig[] configArr = SearchToolkit.getIndexConfigArr(request);
    boolean showSortFieldContent = false;
    if (configArr.length >= 1) {
      // We take the first index config
      IndexConfig config = configArr[0];
      showSortFieldContent = config.getShowSortFieldContent();
    }
    if (sortContent != null && showSortFieldContent) {
      Localizer localizer = new Localizer(request.getResourceBaseUrl(), "msg", request.getLocale());
      response.print("&nbsp;");
      response.print(localizer.msg("sortContent", "sort field content"));
      response.print(":&nbsp;");
      response.printNoHtml(sortContent);
    }
  }
}
