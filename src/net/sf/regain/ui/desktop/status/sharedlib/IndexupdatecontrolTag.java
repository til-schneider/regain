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
 *  $RCSfile: IndexupdatecontrolTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/desktop/status/sharedlib/IndexupdatecontrolTag.java,v $
 *     $Date: 2005/03/16 13:57:16 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.ui.desktop.status.sharedlib;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.Crawler;
import net.sf.regain.ui.desktop.IndexUpdateManager;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates a form to control the index update.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>url</code>: The URL of the page that should be autoupdated.</li>
 * <li><code>msgBefore</code>: The message to print before the button.</li>
 * <li><code>msgStart</code>: The message for the start button.</li>
 * <li><code>msgResume</code>: The message for the resume button.</li>
 * <li><code>msgPause</code>: The message for the pause button.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class IndexupdatecontrolTag extends SharedTag {

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
    Crawler crawler = IndexUpdateManager.getInstance().getCurrentCrawler();

    // Check whether there was an indexaction
    String indexaction = request.getParameter("indexaction");
    if ("start".equals(indexaction)) {
      IndexUpdateManager.getInstance().startIndexUpdate();
      crawler = IndexUpdateManager.getInstance().getCurrentCrawler();
    }
    else if ("resume".equals(indexaction)) {
      IndexUpdateManager.getInstance().setShouldPause(false);
    }
    else if ("pause".equals(indexaction)) {
      IndexUpdateManager.getInstance().setShouldPause(true);
    } 
    
    // Generate the form
    String url = getParameter("url", true);
    String msgBefore = getParameter("msgBefore", "");
    response.print("<form name=\"control\" action=\"" + url + "\" " +
        "style=\"display:inline;\" method=\"post\">" + msgBefore + " ");
    if (crawler == null) {
      // There is currently no index update running -> Provide a start button
      String msgStart = getParameter("msgStart", true);
      response.print("<input type=\"hidden\" name=\"indexaction\" value=\"start\"/>");
      response.print("<input type=\"submit\" value=\"" + msgStart + "\"/>");
    } else {
      if (crawler.getShouldPause()) {
        // The crawler is pausing -> Provide a resume button
        String msgResume = getParameter("msgResume", true);
        response.print("<input type=\"hidden\" name=\"indexaction\" value=\"resume\"/>");
        response.print("<input type=\"submit\" value=\"" + msgResume + "\"/>");
      } else {
        // The crawler is running -> Provide a pause button
        String msgPause = getParameter("msgPause", true);
        response.print("<input type=\"hidden\" name=\"indexaction\" value=\"pause\"/>");
        response.print("<input type=\"submit\" value=\"" + msgPause + "\"/>");
      }
    }
    response.print("</form>");
  }

}
