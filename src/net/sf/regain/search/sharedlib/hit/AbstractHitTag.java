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
 */
package net.sf.regain.search.sharedlib.hit;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchConstants;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

import org.apache.lucene.document.Document;

/**
 * Parent class for all tags that show information about a search hit. Provides
 * a template method that gets the current hit.
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractHitTag extends SharedTag
  implements SearchConstants
{

  /**
   * Called when the parser reaches the end tag.
   * <p>
   * Gets the current hit and calls the template method.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  @Override
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    Document hit = (Document) request.getContextAttribute(ATTR_CURRENT_HIT);
    if (hit == null) {
      throw new RegainException("Tag " + getTagName()
          + " must be inside a list tag!");
    }

    Integer hitIndex = (Integer) request.getContextAttribute(ATTR_CURRENT_HIT_INDEX);

    printEndTag(request, response, hit, hitIndex.intValue());
  }


  /**
   * The template method.
   * <p>
   * Must be implemented by subclasses to genereate the actual tag content.
   *  
   * @param request The page request.
   * @param response The page response.
   * @param hit The current search hit.
   * @param hitIndex The index of the hit.
   * @throws RegainException If there was an exception.
   */
  protected abstract void printEndTag(PageRequest request, PageResponse response,
    Document hit, int hitIndex)
    throws RegainException;

}
