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
 */
package net.sf.regain.ui.desktop.config.sharedlib;

import net.sf.regain.RegainException;
import net.sf.regain.util.io.Localizer;
import net.sf.regain.util.io.MultiLocalizer;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates a editable list.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>name</code>: The name of the setting.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class EditlistTag extends SharedTag {

  /** The MultiLocalizer for this class. */
  private static MultiLocalizer mMultiLocalizer = new MultiLocalizer(EditlistTag.class);


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
    Localizer localizer = mMultiLocalizer.getLocalizer(request.getLocale());

    // Get the name of the edit list
    String name = getParameter("name", true);
    
    // Get the current value
    String[] currValueArr = (String[]) request.getContextAttribute("settings." + name);
    
    response.print("<select id=\"" + name + "-list\" name=\"" + name + "\" " +
        "size=\"5\" onClick=\"showListSelection('" + name + "')\" multiple");
    String styleSheetClass = getParameter("class");
    if (styleSheetClass != null) {
      response.print(" class=\"" + styleSheetClass + "\"");
    }
    response.print(">");
    for (int i = 0; i < currValueArr.length; i++) {
      response.print("<option>" + currValueArr[i] + "</option>");
    }
    response.print("</select><br/>");
    
    response.print("<input type=\"text\" id=\"" + name + "-entry\"");
    if (styleSheetClass != null) {
      response.print(" class=\"" + styleSheetClass + "\"");
    }
    response.print("/>");
    response.print("<button type=\"button\" onClick=\"addToList('" + name + "')\">"
        + localizer.msg("add", "Add") + "</button>");
    response.print("<button type=\"button\" onClick=\"removeFromList('" + name + "')\">"
        + localizer.msg("remove", "Remove") + "</button>");
  }

}
