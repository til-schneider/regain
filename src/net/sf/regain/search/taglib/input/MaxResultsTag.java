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
 *  $RCSfile: MaxResultsTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/taglib/input/MaxResultsTag.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.taglib.input;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspWriter;

import net.sf.regain.search.ExtendedJspException;
import net.sf.regain.search.taglib.AbstractSimpleTag;


/**
 * Generiert ein verstecktes Feld, das die maximale Anzahl von Treffern
 * (<code>maxresults</code>) enthält.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class MaxResultsTag extends AbstractSimpleTag {

  /** Die Standardanzahl der maximal zu zeigenden direkten Links zu Trefferseiten. */
  private static final int DEFAULT_MAX_RESULTS = 15;



  /**
   * Generiert den Tag.
   *
   * @param out Der JspWriter auf den der Taginhalt geschrieben werden soll.
   */
  public void printEndTag(JspWriter out)
    throws IOException, ExtendedJspException
  {
    ServletRequest request = pageContext.getRequest();
    
    //  <input name="maxresults" type="hidden" value="<%= (request.getParameter("maxresults") == null) ? "15" : request.getParameter("maxresults")%>"/>

    String maxresults = request.getParameter("maxresults");
    if (maxresults == null) {
      maxresults = Integer.toString(DEFAULT_MAX_RESULTS);
    }

    out.print("<input name=\"maxresults\" type=\"hidden\" "
      + "value=\"" + maxresults + "\"/>");
  }

}
