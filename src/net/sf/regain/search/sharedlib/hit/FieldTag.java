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
 *  $RCSfile: FieldTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/sharedlib/hit/FieldTag.java,v $
 *     $Date: 2005/08/07 10:51:09 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.search.sharedlib.hit;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.lucene.document.Document;

/**
 * Generates an index field value of the current hit's document.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>field</code>: The name of the index field to generate.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class FieldTag extends AbstractHitTag {

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
    String field = getParameter("field", true);
    String value = hit.get(field);
    if (value != null) {
      response.printNoHtml(value);
    }
  }

}
