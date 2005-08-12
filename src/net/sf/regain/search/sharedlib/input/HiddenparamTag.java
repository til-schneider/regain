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
 *  $RCSfile: HiddenparamTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/sharedlib/input/HiddenparamTag.java,v $
 *     $Date: 2005/08/07 10:51:09 $
 *   $Author: til132 $
 * $Revision: 1.3 $
 */
package net.sf.regain.search.sharedlib.input;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates the value of a HTML request parameter as a hidden field. 
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>name</code>: The name of the request parameter whichs value should
 *     be generated.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class HiddenparamTag extends SharedTag {

  /**
   * Called when the parser reaches the end tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    String name = getParameter("name", true);
    String[] valueArr = request.getParameters(name);
    if (valueArr != null) {
      for (int i = 0; i < valueArr.length; i++) {
        response.print("<input name=\"" + name + "\" type=\"hidden\" value=\""
            + valueArr[i] + "\"/>");
      }
    }
  }
  
}
