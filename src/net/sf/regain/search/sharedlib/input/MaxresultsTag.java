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
package net.sf.regain.search.sharedlib.input;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchConstants;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates a hidden field that contains the maximum number of results.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MaxresultsTag extends SharedTag implements SearchConstants {

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
    String maxresults = request.getParameter(PARAM_MAX_RESULTS);
    if (maxresults != null) {
      response.print("<input name=\"maxresults\" type=\"hidden\" "
        + "value=\"" + maxresults + "\"/>");
    }
  }

}
