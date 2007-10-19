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
 *     $Date: 2005-03-16 14:50:04 +0100 (Mi, 16 Mrz 2005) $
 *   $Author: til132 $
 * $Revision: 91 $
 */
package net.sf.regain.ui.desktop.status.sharedlib.autoupdate;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates a form the enables or disables the autoupdate.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>url</code>: The URL of the page that should be autoupdated.</li>
 * <li><code>msgAutoupdate</code>: The message to show before the button. When
 *     the message contains <code>{0}</code> it will be replaced with the update
 *     time.</li>
 * <li><code>msgEnable</code>: The message for the enable button.</li>
 * <li><code>msgDisable</code>: The message for the disable button.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class FormTag extends SharedTag {

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
    String url = getParameter("url", true);
    int time = getParameterAsInt("time", 5);
    
    String msgAutoupdate = getParameter("msgAutoupdate", true);
    msgAutoupdate = RegainToolkit.replace(msgAutoupdate, "{0}", Integer.toString(time));
    
    String autoupdate = request.getParameter("autoupdate");
    response.print("<form name=\"autoupdate\" action=\"" + url + "\" " +
        "style=\"display:inline;\" method=\"get\">" + msgAutoupdate + " ");
    if (autoupdate == null) {
      response.print("<input type=\"hidden\" name=\"autoupdate\" value=\"" + time + "\"/>");
      String msgEnable = getParameter("msgEnable", true);
      response.print("<input type=\"submit\" value=\"" + msgEnable + "\"/>");
    } else {
      String msgDisable = getParameter("msgDisable", true);
      response.print("<input type=\"submit\" value=\"" + msgDisable + "\"/>");
    }
    response.print("</form>");
  }
  
}
