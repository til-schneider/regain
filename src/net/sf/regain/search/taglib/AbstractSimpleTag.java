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
 *  $RCSfile: AbstractSimpleTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/taglib/AbstractSimpleTag.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.sf.regain.search.ExtendedJspException;
import net.sf.regain.search.SearchConstants;


/**
 * Provides a template method for easy writing simple tag class.
 * <p>
 * Using the template method subclasses don't have to deal with getting the
 * JspWriter or catching IOExceptions.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public abstract class AbstractSimpleTag
  extends TagSupport implements SearchConstants
{

  /**
   * Called when the JSP parser reaches the end tag.
   * <p>
   * Gets the JspWriter and calls the template method.
   *
   * @see #printEndTag(JspWriter)
   * @return {@link #EVAL_PAGE}
   * @throws ExtendedJspException Wenn das Ergebnis des Tags nicht geschrieben
   *         werden konnte.
   */
  public int doEndTag() throws ExtendedJspException {
    try {
      JspWriter out = pageContext.getOut();

      printEndTag(out);
    }
    catch (IOException exc) {
      throw new ExtendedJspException("Error writing results", exc);
    }

    return EVAL_PAGE;
  }



  /**
   * The Template method.
   * <p>
   * Must be implemented by subclasses for generating the tag.
   *
   * @param out The JspWriter to write the tag code to.
   *
   * @throws IOException Wenn das Ergebnis nicht in den JspWriter geschrieben
   *         werden konnte.
   * @throws ExtendedJspException Wenn bei der Erstellung des Tagergebnisses
   *         etwas fehl schlug.
   */
  protected abstract void printEndTag(JspWriter out)
    throws IOException, ExtendedJspException;

}
