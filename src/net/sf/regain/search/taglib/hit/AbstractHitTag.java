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
 *  $RCSfile: AbstractHitTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/taglib/hit/AbstractHitTag.java,v $
 *     $Date: 2004/07/28 20:26:02 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.taglib.hit;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import net.sf.regain.search.ExtendedJspException;
import net.sf.regain.search.taglib.AbstractSimpleTag;

import org.apache.lucene.document.Document;


/**
 * Elternklasse für alle Tags, die Teile eines Suchtreffers (hit) anzeigen. Stellt
 * eine Schablonenmethode zur Verfügung, die bereits den JspWriter und den
 * aktuellen Treffer als Parameter übergeben bekommt.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public abstract class AbstractHitTag extends AbstractSimpleTag {

  /**
   * Wird aufgerufen, sobald der JSP-Parser den Endtag erreicht.
   * <p>
   * Holt sich den aktuellen Treffer und ruft die Schablonenmethode auf.
   *
   * @param out Der JspWriter auf den der Taginhalt geschrieben werden soll.
   * @throws IOException Wenn das Ergebnis nicht in den JspWriter geschrieben
   *         werden konnte.
   * @throws ExtendedJspException Wenn bei der Erstellung des Tagergebnisses
   *         etwas fehl schlug.
   */
  public void printEndTag(JspWriter out) throws IOException, ExtendedJspException {
    Document hit = (Document) pageContext.getAttribute(ATTR_CURRENT_HIT);
    if (hit == null) {
      throw new ExtendedJspException(getClass().getName()
        + " must be inside a ListTag!");
    }

    printEndTag(out, hit);
  }



  /**
   * Die Schablonenmethode.
   * <p>
   * Muss von Kindklassen überschrieben werden, um den eigentlichen Taginhalt zu
   * generieren.
   *
   * @param out Der JspWriter auf den der Taginhalt geschrieben werden soll.
   * @param hit Der aktuelle Suchtreffer.
   * @throws IOException Wenn das Ergebnis nicht in den JspWriter geschrieben
   *         werden konnte.
   * @throws ExtendedJspException Wenn bei der Erstellung des Tagergebnisses
   *         etwas fehl schlug.
   */
  protected abstract void printEndTag(JspWriter out, Document hit)
    throws IOException, ExtendedJspException;

}
