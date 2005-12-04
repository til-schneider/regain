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
 *  $RCSfile: NavigationTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/sharedlib/NavigationTag.java,v $
 *     $Date: 2005/11/02 21:58:52 $
 *   $Author: til132 $
 * $Revision: 1.10 $
 */
package net.sf.regain.search.sharedlib;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchConstants;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.results.SearchResults;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * This Tag creates hyperlinks to navigate through the search result pages.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>msgBack</code>: The message to use for labeling the back link.</li>
 * <li><code>msgForward</code>: The message to use for labeling the forward link.</li>
 * <li><code>targetPage</code>: The URL of the page where the links should point to.</li>
 * <li><code>class</code>: The style sheet class to use for the link tags.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class NavigationTag extends SharedTag implements SearchConstants {

  /** The maximum number of links to create. */
  private static final int MAX_BUTTONS = 15;

  /** Die Default-Seite, auf die die Weiter-Links zeigen sollen. */
  private static final String DEFAULT_TARGET_PAGE = "SearchOutput.jsp";


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
    String query = SearchToolkit.getSearchQuery(request);
    SearchResults results = SearchToolkit.getSearchResults(request);

    int fromResult = request.getParameterAsInt(PARAM_FROM_RESULT, 0);
    int maxResults = request.getParameterAsInt(PARAM_MAX_RESULTS, SearchConstants.DEFAULT_MAX_RESULTS);
    int totalResults = results.getHitCount();

    int buttonCount = (int) Math.ceil((double) totalResults / (double) maxResults);
    int currButton = fromResult / maxResults;

    // The first and the last button to show
    int fromButton = 0;
    int toButton = buttonCount - 1;

    if (buttonCount > MAX_BUTTONS) {
      if (currButton < (MAX_BUTTONS / 2)) {
        // The button range starts at the first button (---X------.....)
        toButton = fromButton + MAX_BUTTONS - 1;
      }
      else if (currButton > (buttonCount - ((MAX_BUTTONS + 1) / 2))) {
        // The button range ends at the last button (.....-------X--)
        fromButton = toButton - MAX_BUTTONS + 1;
      }
      else {
        // The button range is somewhere in the middle (...----X-----..)
        toButton = currButton + (MAX_BUTTONS / 2);
        fromButton = toButton - MAX_BUTTONS + 1;
      }
    }

    String[] indexNameArr = request.getParameters("index");
    if (currButton > 0) {
      String msgBack = getParameter("msgBack", true);
      msgBack = RegainToolkit.replace(msgBack, "&quot;", "\"");
      printLink(response, currButton - 1, query, maxResults, indexNameArr, msgBack);
    }
    for (int i = fromButton; i <= toButton; i++) {
      if (i == currButton) {
        // This is the current button
        response.print("<b>" + (i + 1) + "</b> ");
      } else {
        String linkText = Integer.toString(i + 1);
        printLink(response, i, query, maxResults, indexNameArr, linkText);
      }
    }
    if (currButton < (buttonCount -1)) {
      String msgForward = getParameter("msgForward", true);
      msgForward = RegainToolkit.replace(msgForward, "'", "\"");
      printLink(response, currButton + 1, query, maxResults, indexNameArr, msgForward);
    }
  }


  /**
   * Prints the HTML for a hyperlink.
   *
   * @param response The page response.
   * @param button The index of the button to create the HTML for.
   * @param query The search query.
   * @param maxResults The maximum results.
   * @param indexNameArr The names of the search indexes.
   * @param linkText The link text.
   * @throws RegainException If printing failed.
   */
  private void printLink(PageResponse response, int button, String query,
    int maxResults, String[] indexNameArr, String linkText)
    throws RegainException
  {
    String targetPage = getParameter("targetPage",  DEFAULT_TARGET_PAGE);

    String encoding = response.getEncoding();
    String encodedQuery = RegainToolkit.urlEncode(query, encoding);

    response.print("<a href=\"" + targetPage + "?query=" + encodedQuery);
    if (indexNameArr != null) {
      for (int i = 0; i < indexNameArr.length; i++) {
        String encodedIndexName = RegainToolkit.urlEncode(indexNameArr[i], encoding);
        response.print("&index=" + encodedIndexName);
      }
    }
    if (maxResults != SearchConstants.DEFAULT_MAX_RESULTS) {
      response.print("&maxresults=" + maxResults);
    }
    if (button != 0) {
      response.print("&fromresult=" + (button * maxResults));
    }
    response.print("\"");
    String styleSheetClass = getParameter("class");
    if (styleSheetClass != null) {
      response.print(" class=\"" + styleSheetClass + "\"");
    }
    response.print(">" + linkText + "</a> ");
  }

}
