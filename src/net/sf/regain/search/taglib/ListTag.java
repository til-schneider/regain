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
 *  $RCSfile: ListTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/taglib/ListTag.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.taglib;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.sf.regain.RegainException;
import net.sf.regain.search.*;

import org.apache.lucene.document.Document;


/**
 * The list tag encloses the JSP code that should be repeated for every shown
 * search hit.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class ListTag extends BodyTagSupport implements SearchConstants {

  /** The index of the currently generated result. */
  private int mCurrentResult;

  /** The index of the last generated index on this page. */
  private int mToResult;

  /** The seach context of the current search. */
  private SearchContext mSearch;

  /** The message to generate if the were no results. */
  private String mMsgNoResults;



  /**
   * Sets the message to generate if the were no results.
   *
   * @param msgNoResults The message to generate if the were no results.
   */
  public void setMsgNoResults(String msgNoResults) {
    mMsgNoResults = msgNoResults;
  }



  /**
   * Creates a new instance of ListTag.
   */
  public ListTag() {
  }



  /**
   * Called when the JSP parser reaches the start tag.
   * <p>
   * Initializes the list generation.
   * 
   * @return Wie die Servlet-Engine weiter verfahren soll.
   * @throws ExtendedJspException Wenn die Erzeugung des Tagergebnisses fehl
   *         schlug.
   */
  public int doStartTag() throws ExtendedJspException {
    ServletRequest request = pageContext.getRequest();

    mSearch = null;
    try {
      mSearch = SearchToolkit.getSearchContextFromPageContext(pageContext);
    } catch (RegainException exc) {
      throw new ExtendedJspException("Error creating search context", exc);
    }

    int fromResult = SearchToolkit.getIntParameter(request, PARAM_FROM_RESULT, 0);
    int maxResults = SearchToolkit.getIntParameter(request, PARAM_MAX_RESULTS, 25);

    if (mSearch.getHitCount() == 0) {
      try {
        JspWriter out = pageContext.getOut();
        out.print(mMsgNoResults);
      }
      catch (IOException exc) {
        throw new ExtendedJspException("Error writing results", exc);
      }

      return SKIP_BODY;
    } else {
      mCurrentResult = fromResult;

      mToResult = fromResult + maxResults - 1;
      if (mToResult >= mSearch.getHitCount()) {
        mToResult = mSearch.getHitCount() - 1;
      }

      writeHitToAttributes(mCurrentResult);

      return EVAL_BODY_TAG;
    }
  }


  /**
   * Schreibt einen Treffer in die Seitenattribute, so dass er von den hit-Tags
   * gelesen werden kann.
   * 
   * @param hitIndex Der Index des Treffers.
   * @throws ExtendedJspException Wenn der Treffer nicht gelesen werden konnte
   */
  private void writeHitToAttributes(int hitIndex) throws ExtendedJspException {
    try {
      Document hit = mSearch.getHitDocument(hitIndex);
      pageContext.setAttribute(ATTR_CURRENT_HIT, hit);
      float score = mSearch.getHitScore(hitIndex);
      pageContext.setAttribute(ATTR_CURRENT_HIT_SCORE, new Float(score));
      pageContext.setAttribute(ATTR_CURRENT_HIT_INDEX, new Integer(hitIndex));
    }
    catch (RegainException exc) {
      throw new ExtendedJspException("Getting hit #" + hitIndex
        + " failed", exc);
    }
  }



  /**
   * Called when the JSP parser finished parsing the tag body.
   * <p>
   * Decides whether there are more hits to generate HTML for. If yes the next
   * hit is put to the page attributes.
   * 
   * @return Wie die Servlet-Engine weiter verfahren soll.
   * @throws ExtendedJspException Wenn die Erzeugung des Tagergebnisses fehl
   *         schlug.
   */
  public final int doAfterBody() throws ExtendedJspException {
    mCurrentResult++;

    if (mCurrentResult <= mToResult) {
      writeHitToAttributes(mCurrentResult);
      
      return EVAL_BODY_TAG;
    } else {
      return SKIP_BODY;
    }
  }



  /**
   * Called when the JSP parser reaches the end tag.
   * <p>
   * Writes the content of the tag to the enclosing JspWriter.
   * 
   * @return Wie die Servlet-Engine weiter verfahren soll.
   * @throws ExtendedJspException Wenn die Erzeugung des Tagergebnisses fehl
   *         schlug.
   */
  public int doEndTag() throws ExtendedJspException {
    try {
      if (bodyContent != null) {
        bodyContent.writeOut(bodyContent.getEnclosingWriter());
      }
    }
    catch(java.io.IOException exc) {
      throw new ExtendedJspException("Writing end tag failed", exc);
    }
    return EVAL_PAGE;
  }



  /**
   * Releases all resources reserved by this tag.
   */
  public void release() {
    super.release();

    mMsgNoResults = null;
  }

}
