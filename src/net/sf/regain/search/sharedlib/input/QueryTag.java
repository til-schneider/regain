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
 *     $Date: 2012-05-29 09:59:28 +0200 (Di, 29 Mai 2012) $
 *   $Author: benjaminpick $
 * $Revision: 602 $
 */
package net.sf.regain.search.sharedlib.input;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates an input field that contains the current search query.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>size</code>: The size of the input field.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class QueryTag extends SharedTag {

  /** The default size of the input field. */
  private static final int INPUTFIELD_SIZE = 25;


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
    int size = getParameterAsInt("size", INPUTFIELD_SIZE);
    response.print("<input name=\"query\" size=\"" + size + "\" value=\"");

    String query = SearchToolkit.getSearchQuery(request);
    if (query != null) {
      query = RegainToolkit.replace(query, "\"", "&quot;");
      response.printNoHtml(query);
    }

    response.print("\"/>");
  }

}
