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
 *  $RCSfile: LinkTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/sharedlib/hit/LinkTag.java,v $
 *     $Date: 2005/04/01 07:43:22 $
 *   $Author: til132 $
 * $Revision: 1.9 $
 */
package net.sf.regain.search.sharedlib.hit;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchContext;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.lucene.document.Document;

/**
 * Generates a link to the current hit's document. For the link text the title
 * is used.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>class</code>: The style sheet class to use for the link.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class LinkTag extends AbstractHitTag {

  /**
   * Generates the tag.
   *
   * @param request The page request.
   * @param response The page response.
   * @param hit The current search hit.
   * @throws RegainException If there was an exception.
   */
  protected void printEndTag(PageRequest request, PageResponse response,
    Document hit)
    throws RegainException
  {
    // Get the search context
    SearchContext search = SearchToolkit.getSearchContext(request);

    String url   = search.rewriteUrl(hit.get("url"));
    String title = hit.get("title");
    boolean openInNewWindow = search.getOpenUrlInNewWindow(url);
    
    // Trim the title
    if (title != null) {
      title = title.trim();
    }

    // Use the URL as title if there is no title.
    if ((title == null) || (title.length() == 0)) {
      int lastSlash = url.lastIndexOf("/");
      if (lastSlash == -1) {
        title = url;
      } else {
        title = RegainToolkit.urlDecode(url.substring(lastSlash + 1));
      }
    }

    // Pass file URLs to the file servlet
    String href = url;
    if (url.startsWith("file://")) {
      // URL encode the URL
      String filename = RegainToolkit.urlToFileName(url);
      String urlEncoded = RegainToolkit.urlEncode(filename);
      urlEncoded = RegainToolkit.replace(urlEncoded, "+", "%20");
      
      // Restore the slashes...
      urlEncoded = RegainToolkit.replace(urlEncoded, "%2F", "/");

      // Create a URL
      href = "file/" + urlEncoded;

      // ...but encode double slashes
      href = RegainToolkit.replace(href, "//", "/\\");
      
      String indexName = request.getParameter("index");
      if (indexName != null) {
        String encodedIndexName = RegainToolkit.urlEncode(indexName);
        href += "?index=" + encodedIndexName;
      }
    }
    
    // Generate the link
    response.print("<a href=\"" + href + "\"");
    if (openInNewWindow) {
      response.print(" target=\"_blank\"");
    }
    String styleSheetClass = getParameter("class");
    if (styleSheetClass != null) {
      response.print(" class=\"" + styleSheetClass + "\"");
    }
    response.print(">");
    response.printNoHtml(title);
    response.print("</a>");
  }

}
