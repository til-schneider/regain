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
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/taglib/NavigationTag.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.taglib;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspWriter;

import net.sf.regain.RegainException;
import net.sf.regain.search.*;


/**
 * This Tag creates hyperlinks to navigate through the search result pages.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class NavigationTag extends AbstractSimpleTag {

  /** The maximum number of links to create. */
  private static final int MAX_BUTTONS = 15;

  /** Die Default-Seite, auf die die Weiter-Links zeigen sollen. */
  private static final String DEFAULT_TARGET_PAGE = "SearchOutput.jsp";

  /** The message to use for labeling the back link. */
  private String mMsgBack;

  /** The message to use for labeling the forward link. */
  private String mMsgForward;
  
  /** Die Seite, auf die die Weiter-Links zeigen sollen. */ 
  private String mTargetPage;

  /** Die zu verwendende Stylesheet-Klasse. (Kann null sein) */
  private String mStyleSheetClass;


  /**
   * Sets the message to use for labeling the back link.
   *
   * @param msgBack the message to use for labeling the back link.
   */
  public void setMsgBack(String msgBack) {
    mMsgBack = msgBack;
  }



  /**
   * Sets the message to use for labeling the forward link.
   *
   * @param msgForward the message to use for labeling the forward link.
   */
  public void setMsgForward(String msgForward) {
    mMsgForward = msgForward;
  }
  
  
  
  /**
   * Setzt die Seite, auf die die Weiter-Links zeigen sollen.
   *   
   * @param targetPage Die Seite, auf die die Weiter-Links zeigen sollen.
   */
  public void setTargetPage(String targetPage) {
    mTargetPage = targetPage;
  }


  /**
   * Setzt die zu verwendende Stylesheet-Klasse.
   *   
   * @param styleSheetClass Die zu verwendende Stylesheet-Klasse.
   */
  public void setClass(String styleSheetClass) {
    mStyleSheetClass = styleSheetClass;
  }


  /**
   * Prints the tag.
   *
   * @param out The writer to print to.
   *
   * @throws IOException If printing failed.
   * @throws ExtendedJspException If printing failed.
   */
  public void printEndTag(JspWriter out)
    throws IOException, ExtendedJspException
  {
    ServletRequest request = pageContext.getRequest();

    String query = request.getParameter("query");
    if (query == null) {
      // Nothing to do
      return;
    }
    String indexName = request.getParameter("index");
    if (indexName == null) {
      throw new ExtendedJspException("Request parameter 'index' not specified");
    }

    SearchContext search;
    try {
      search = SearchToolkit.getSearchContextFromPageContext(pageContext);
    } catch (RegainException exc) {
      throw new ExtendedJspException("Error creating search context", exc);
    }

    int fromResult = SearchToolkit.getIntParameter(request, PARAM_FROM_RESULT, 0);
    int maxResults = SearchToolkit.getIntParameter(request, PARAM_MAX_RESULTS, 25);
    int totalResults = search.getHitCount();

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

    if (currButton > 0) {
      printLink(out, currButton - 1, query, maxResults, indexName, mMsgBack);
    }
    for (int i = fromButton; i <= toButton; i++) {
      if (i == currButton) {
        // This is the current button
        out.print("<b>" + (i + 1) + "</b> ");
      } else {
        String linkText = Integer.toString(i + 1);
        printLink(out, i, query, maxResults, indexName, linkText);
      }
    }
    if (currButton < (buttonCount -1)) {
      printLink(out, currButton + 1, query, maxResults, indexName, mMsgForward);
    }
  }


  /**
   * Prints the HTML for a hyperlink.
   *
   * @param out The writer to print to.
   * @param button The index of the button to create the HTML for.
   * @param query The search query.
   * @param maxResults The maximum results.
   * @param indexName The name of the search index.
   * @param linkText The link text.
   * @throws IOException If printing failed.
   */
  private void printLink(JspWriter out, int button, String query, int maxResults,
    String indexName, String linkText)
    throws IOException
  {
    String targetPage = mTargetPage;
    if (targetPage == null) {
      targetPage = DEFAULT_TARGET_PAGE;
    }

    // Für Java 1.2.2
    String encodedQuery = URLEncoder.encode(query);
    String encodedIndexName = URLEncoder.encode(indexName);

    // Ab Java 1.3
    /*
    String encoding = pageContext.getResponse().getCharacterEncoding();
    String encodedQuery = URLEncoder.encode(query, encoding);
    String encodedIndexName = URLEncoder.encode(indexName, encoding);
    */
    
    out.print("<a href=\"" + targetPage + "?query=" + encodedQuery
      + "&index=" + encodedIndexName + "&maxresults=" + maxResults
      + "&fromresult=" + (button * maxResults) + "\"");
    if (mStyleSheetClass != null) {
      out.print(" class=\"" + mStyleSheetClass + "\"");
    }
    out.print(">" + linkText + "</a> ");
  }



  /**
   * Releases the resources used by this tag.
   */
  public void release() {
    super.release();

    mMsgBack = null;
    mMsgForward = null;
    mTargetPage = null;
    mStyleSheetClass = null;
  }

}
