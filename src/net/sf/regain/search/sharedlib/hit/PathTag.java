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
 *  $RCSfile: PathTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/sharedlib/hit/PathTag.java,v $
 *     $Date: 2005/08/07 10:51:09 $
 *   $Author: til132 $
 * $Revision: 1.3 $
 */
package net.sf.regain.search.sharedlib.hit;

import java.util.StringTokenizer;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.lucene.document.Document;

/**
 * Generates the path to the current hit's document.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>before</code>: The HTML code that should be inserted before
 *     the path if the document has one.</li>
 * <li><code>after</code>: The HTML code that should be inserted after
 *     the path if the document has one.</li>
 * <li><code>class</code>: The style sheet class to use for the link.</li>
 * <li><code>createLinks</code>: Specifies whether links (a tags) should be
 *     created.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class PathTag extends AbstractHitTag {

  /**
   * Generates the tag.
   *
   * @param request The page request.
   * @param response The page response.
   * @param hit The current search hit.
   * @param hitIndex The index of the hit.
   * @throws RegainException If there was an exception.
   */
  protected void printEndTag(PageRequest request, PageResponse response,
    Document hit, int hitIndex)
    throws RegainException
  {
    String path = hit.get("path");

    if (path != null) {
      String htmlBefore = getParameter("before");
      if (htmlBefore != null) {
        response.print(htmlBefore);
      }
      
      boolean createLinks = getParameterAsBoolean("createLinks", true);
      String styleSheetClass = getParameter("class");

      // NOTE: The path is formatted as follows:
      //       For each path element there is a line ending with \n
      //       A line constists of the URL, a blank and the title
      
      StringTokenizer tokenizer = new StringTokenizer(path, "\n");
      boolean firstPathElement = true;
      while (tokenizer.hasMoreTokens()) {
        String line = tokenizer.nextToken();

        int blankPos = line.indexOf(' ');
        if (blankPos != -1) {
          String url = line.substring(0, blankPos);
          String title = line.substring(blankPos + 1, line.length());

          if (! firstPathElement) {
            response.print(" &gt; "); // >
          }
          if (createLinks) {
            response.print("<a href=\"" + url + "\"");
            if (styleSheetClass != null) {
              response.print(" class=\"" + styleSheetClass + "\"");
            }
            response.print(">" + title + "</a>");
          } else {
            response.print(title);
          }

          firstPathElement = false;
        }
      }

      String htmlAfter = getParameter("after");
      if (htmlAfter != null) {
        response.print(htmlAfter);
      }
    }
  }

}
