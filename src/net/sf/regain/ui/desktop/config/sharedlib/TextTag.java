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
 *  $RCSfile: TextTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/desktop/config/sharedlib/TextTag.java,v $
 *     $Date: 2005/03/10 13:47:33 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.ui.desktop.config.sharedlib;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates a textfield for a certain setting.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>name</code>: The name of the setting.</li>
 * <li><code>size</code>: The size of the textfield.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class TextTag extends SharedTag {

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
    int size = getParameterAsInt("size", 15);

    String name = getParameter("name", true);
    String currValue = (String) request.getContextAttribute("settings." + name);
    
    response.print("<input type=\"text\" size=\"" + size + "\" name=\"" +
        name + "\" value=\"" + currValue + "\"/>");
  }
  
}
