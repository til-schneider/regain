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
 *  $RCSfile$
 *   $Source$
 *     $Date: 2011-08-05 11:36:46 +0200 (Fr, 05 Aug 2011) $
 *   $Author: benjaminpick $
 * $Revision: 511 $
 */
package net.sf.regain.search.sharedlib.hit;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.results.SearchResults;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.lucene.document.Document;

/**
 * Generates the value of an index field of the current hit's document.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>field</code>: The name of the index field to write the value of.</li>
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
          throws RegainException {
    SearchResults results = SearchToolkit.getSearchResults(request);
    boolean shouldHighlight = results.getShouldHighlight(hitIndex);

    String fieldname = getParameter("field", true);
    String value = null;
    if (shouldHighlight) {
      value = hit.get(RegainToolkit.createHighlightedFieldIdent(fieldname));
    }

    if (value == null || value.length() == 0) {
      value = hit.get(fieldname);
    }

    // Maybe this is a compressed field
    if (value == null) {
      value = SearchToolkit.getCompressedFieldValue(hit, fieldname);
    }

    if (value != null) {
      if (shouldHighlight) {
        response.print(value);
      } else {
        response.printNoHtml(value);
      }
    }
  }


}
