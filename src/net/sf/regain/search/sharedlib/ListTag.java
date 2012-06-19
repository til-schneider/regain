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
 *     $Date: 2012-05-21 18:04:19 +0200 (Mo, 21 Mai 2012) $
 *   $Author: benjaminpick $
 * $Revision: 598 $
 */
package net.sf.regain.search.sharedlib;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchConstants;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.results.SearchResults;
import net.sf.regain.search.results.SortingOption;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

/**
 * The list tag encloses the JSP code that should be repeated for every shown
 * search hit.
 * <p>
 * Request Parameters:
 * <ul>
 * <li><code>fromresult</code>: The index of the first result to show.</li>
 * <li><code>maxresults</code>: The maximum number of results to show.</li>
 * </ul>
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>msgNoResults</code>: The message to generate if the were no
 *     results.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class ListTag extends SharedTag implements SearchConstants {

  /** The index of the currently generated result. */
  private int mCurrentResult;

  /** The index of the last generated index on this page. */
  private int mToResult;

  
  /**
   * Called when the parser reaches the start tag.
   * <p>
   * Initializes the list generation.
   *  
   * @param request The page request.
   * @param response The page response.
   * @return {@link #EVAL_TAG_BODY} if you want the tag body to be evaluated or
   *         {@link #SKIP_TAG_BODY} if you want the tag body to be skipped.
   * @throws RegainException If there was an exception.
   */
  @Override
  public int printStartTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    SearchResults results = SearchToolkit.getSearchResults(request);

    int fromResult = request.getParameterAsInt(PARAM_FROM_RESULT, 0);
    int maxResults = request.getParameterAsInt(PARAM_MAX_RESULTS, SearchConstants.DEFAULT_MAX_RESULTS);

    if (results.getHitCount() == 0) {
      String msgNoResults = getParameter("msgNoResults");
      if (msgNoResults != null) {
        response.print(msgNoResults);
      }
            
      return SKIP_TAG_BODY;
    } else {
      mCurrentResult = fromResult;

      mToResult = fromResult + maxResults - 1;
      if (mToResult >= results.getHitCount()) {
        mToResult = results.getHitCount() - 1;
      }

      writeHitToAttributes(mCurrentResult, results, request);

      return EVAL_TAG_BODY;
    }
  }


  /**
   * Writes a hit to the page context, so it may be read by the hit tags.
   *
   * @param hitIndex The index of the hit.
   * @param results The SearchResults to read the hit from.
   * @param request The page request to write the hit to.
   * @throws RegainException If the hit could not be read.
   */
  private void writeHitToAttributes(int hitIndex, SearchResults results,
    PageRequest request)
    throws RegainException
  {
    boolean shouldHighlight = results.getShouldHighlight(hitIndex);
    
    try {
      Document hit = results.getHitDocument(hitIndex);
      if (shouldHighlight) {
        results.highlightHitDocument(hitIndex);
      } else {
        results.shortenSummary(hitIndex);
      }
      request.setContextAttribute(ATTR_CURRENT_HIT, hit);
      float score = results.getHitScore(hitIndex);
      request.setContextAttribute(ATTR_CURRENT_HIT_SCORE, score);
      request.setContextAttribute(ATTR_CURRENT_HIT_INDEX, hitIndex);

      String order = request.getParameter("order");
      //System.out.println("order: " + order);
      if (!(order == null || order.length() == 0 || order.startsWith(SortingOption.RELEVANCE))) {
        String fieldName = order.substring(0, order.lastIndexOf("_"));
        //System.out.println("none standard order. fieldname: " + fieldName);
        Fieldable field = hit.getFieldable(fieldName);
        String fieldContent = null;
        if (field != null) {
          fieldContent = field.stringValue();
        }
        if (fieldContent == null) {
          fieldContent = "not set";
        }
        request.setContextAttribute(ATTR_CURRENT_HIT_SORT_CONTENT, fieldContent);
      }

    }
    catch (RegainException exc) {
      throw new RegainException("Getting hit #" + hitIndex + " failed", exc);
    }
  }


  /**
   * Called after the body content was evaluated.
   * <p>
   * Decides whether there are more hits to generate HTML for. If yes the next
   * hit is put to the page attributes.
   *  
   * @param request The page request.
   * @param response The page response.
   * @return {@link #EVAL_TAG_BODY} if you want the tag body to be evaluated
   *         once again or {@link #SKIP_TAG_BODY} if you want to print the
   *         end tag.
   * @throws RegainException If there was an exception.
   */
  @Override
  public int printAfterBody(PageRequest request, PageResponse response)
    throws RegainException
  {
    mCurrentResult++;

    if (mCurrentResult <= mToResult) {
      SearchResults results = SearchToolkit.getSearchResults(request);
      writeHitToAttributes(mCurrentResult, results, request);

      return EVAL_TAG_BODY;
    } else {
      return SKIP_TAG_BODY;
    }
  }

}
