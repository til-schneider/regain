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
 *  $RCSfile: SearchToolkit.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/SearchToolkit.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;


/**
 * A toolkit for the search JSPs containing helper methods.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class SearchToolkit {

  /** Der Name des PageContext-Attributs, unter dem der SearchContext abgelegt ist. */
  private static final String SEARCH_CONTEXT_ATTR_NAME = "SearchContext";
  
  /** Die Standard-Liste der Felder in denen gesucht wird. */
  private static final String DEFAULT_SEARCH_FIELD_LIST = "content title headlines";



  /**
   * Gets the SearchContext from the PageContext.
   * <p>
   * If there is no SearchContext in the PageContext it is created and put in the
   * PageContext, so the next call will find it.
   *
   * @param pageContext The page context where the SearchContext will be taken
   *        from or put to.
   * @return The SearchContext for the page the context is for.
   *
   * @throws RegainException When the SearchContext could not be created.
   * @see SearchContext
   */
  public static SearchContext getSearchContextFromPageContext(PageContext pageContext)
    throws RegainException
  {
    SearchContext context = (SearchContext) pageContext.getAttribute(SEARCH_CONTEXT_ATTR_NAME);
    if (context == null) {
      ServletContext ctx = pageContext.getServletContext();

      // Namen des Index holen      
      String indexName = pageContext.getRequest().getParameter("index");
      if (indexName == null) {
        throw new RegainException("Request parameter 'index' not specified");
      }

      // Verzeichnis für diesen Namen erfragen      
      String indexDir = ctx.getInitParameter("indexDir." + indexName);
      if (indexDir == null) {
        throw new RegainException("Context parameter 'indexDir." + indexName
          + "' not set in the web.xml!");
      }
      
      // Regex holen, zu der eine URL passen muss, damit sie in einem neuen
      // Fenster geöffnet wird.
      String param = "openInNewWindowRegex." + indexName;
      String openInNewWindowRegex = ctx.getInitParameter(param);
      if (openInNewWindowRegex == null) {
        // Globalen Eintrag probieren
        openInNewWindowRegex = ctx.getInitParameter("openInNewWindowRegex");
      }
      
      // Liste der Felder holen, in denen gesucht werden soll
      String searchFieldList = ctx.getInitParameter("searchFieldList." + indexName);
      if (searchFieldList == null) {
        // Globalen Eintrag probieren
        searchFieldList = ctx.getInitParameter("searchFieldList");

        if (searchFieldList == null) {
          searchFieldList = DEFAULT_SEARCH_FIELD_LIST;
        }
      }
      String[] searchFieldArr = RegainToolkit.splitString(searchFieldList, " ");

      // Suchkontext erstellen und im pageContext speichern
      String query = pageContext.getRequest().getParameter("query");
      context = new SearchContext(indexDir, openInNewWindowRegex,
                                  searchFieldArr, query);
      pageContext.setAttribute(SEARCH_CONTEXT_ATTR_NAME, context);
    }

    return context;
  }


  /**
   * Gets a request parameter and converts it to an int.
   *
   * @param request The request to read the parameter from.
   * @param paramName The name of the parameter
   * @param defaultValue The value to return if the parameter is not set.
   * @throws ExtendedJspException When the parameter value is not a number.
   * @return The int value of the parameter.
   */
  public static int getIntParameter(ServletRequest request, String paramName,
    int defaultValue) throws ExtendedJspException
  {
    String asString = request.getParameter(paramName);
    if (asString == null) {
      return defaultValue;
    } else {
      try {
        return Integer.parseInt(asString);
      }
      catch (NumberFormatException exc) {
        throw new ExtendedJspException("Parameter '" + paramName
                                       + "' must be a number: " + asString);
      }
    }
  }

}
