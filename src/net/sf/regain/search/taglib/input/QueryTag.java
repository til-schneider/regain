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
 *  $RCSfile: QueryTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/taglib/input/QueryTag.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.taglib.input;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspWriter;

import net.sf.regain.RegainToolkit;
import net.sf.regain.search.ExtendedJspException;
import net.sf.regain.search.taglib.AbstractSimpleTag;


/**
 * Generiert ein Eingabefeld, das die aktuelle Suchanfrage als Text beinhaltet.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class QueryTag extends AbstractSimpleTag {
  
  /** Die Größe des Eingabefeldes. */
  private static final int INPUTFIELD_SIZE = 25;
  
  

  /**
   * Generiert den Tag.
   *
   * @param out Der JspWriter auf den der Taginhalt geschrieben werden soll.
   */
  public void printEndTag(JspWriter out)
    throws IOException, ExtendedJspException
  {
    ServletRequest request = pageContext.getRequest();
    
    // <input name="query" size="25" value="<%= (request.getParameter("query") == null) ? "" : net.sf.regain.crawler.CrawlerToolkit.replace(request.getParameter("query"), "\"", "&quot;")%>"/>

    out.print("<input name=\"query\" size=\"" + INPUTFIELD_SIZE + "\" value=\"");
    
    String query = request.getParameter("query");
    if (query != null) {
      out.print(RegainToolkit.replace(query, "\"", "&quot;"));
    }

    out.print("\"/>");
  }

}
