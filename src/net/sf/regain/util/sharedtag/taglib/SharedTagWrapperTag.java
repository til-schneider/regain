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
package net.sf.regain.util.sharedtag.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchConstants;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Provides a template method for easy writing simple tag class.
 * <p>
 * Using the template method subclasses don't have to deal with getting the
 * JspWriter or catching IOExceptions.
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class SharedTagWrapperTag
  extends BodyTagSupport implements SearchConstants
{
  
  /** The nested shared tag. */
  private SharedTag mNestedTag;
  
  
  /**
   * Creates a new instance of SharedTagWrapperTag.
   * 
   * @param nestedTag The tag that should be wrapped by this tag.
   */
  public SharedTagWrapperTag(SharedTag nestedTag) {
    mNestedTag = nestedTag;
  }
  
  
  /**
   * Gets the nested shared tag.
   * 
   * @return The nested shared tag.
   */
  protected SharedTag getNestedTag() {
    return mNestedTag;
  }
  
  
  /**
   * Called when the JSP parser reaches the start tag.
   * <p>
   * Calls the start tag method of the nested tag.
   *
   * @see SharedTag#printStartTag(PageRequest, PageResponse)
   * @return {@link #EVAL_PAGE}
   * @throws JspException If the tag could not be executed
   */
  public int doStartTag() throws JspException {
    PageRequest request = getPageRequest();
    JspPageResponse response = new JspPageResponse(pageContext);
    try {
      // Set the context
      mNestedTag.setContext(request);
      
      // Set Escaping
      response.setEscapeType(mNestedTag.getParameter("escape"));

      // Print the start tag
      int result = mNestedTag.printStartTag(request, response);
      
      // response.close();
      
      switch (result) {
        case SharedTag.EVAL_TAG_BODY: return EVAL_BODY_TAG;
        case SharedTag.SKIP_TAG_BODY: return SKIP_BODY;
        default: throw new RegainException("printStartTag must return either " +
            "EVAL_TAG_BODY or SKIP_TAG_BODY");
      }
    }
    catch (RegainException exc) {
      throw new ExtendedJspException("Writing results failed", exc);
    }
  }


  /**
   * Called after the body content was evaluated.
   * <p>
   * Calls the after body method of the nested tag.
   *
   * @see SharedTag#printAfterBody(PageRequest, PageResponse)
   * @return {@link #EVAL_PAGE}
   * @throws JspException If the tag could not be executed
   */
  public int doAfterBody() throws JspException {
    PageRequest request = getPageRequest();
    PageResponse response = new JspPageResponse(pageContext);
    try {
      int result = mNestedTag.printAfterBody(request, response);
      switch (result) {
        case SharedTag.EVAL_TAG_BODY: return EVAL_BODY_TAG;
        case SharedTag.SKIP_TAG_BODY: return SKIP_BODY;
        default: throw new RegainException("printAfterBody must return either " +
            "EVAL_TAG_BODY or SKIP_TAG_BODY");
      }
    }
    catch (RegainException exc) {
      throw new ExtendedJspException("Writing results failed", exc);
    }
  }


  /**
   * Called when the JSP parser reaches the end tag.
   * <p>
   * Calls the end tag method of the nested tag.
   *
   * @see SharedTag#printEndTag(PageRequest, PageResponse)
   * @return {@link #EVAL_PAGE}
   * @throws JspException If the tag could not be executed
   */
  public int doEndTag() throws JspException {
    // Print the body content
    try {
      if (bodyContent != null) {
        bodyContent.writeOut(bodyContent.getEnclosingWriter());

        // Reset the body content
        bodyContent = null;
      }
    }
    catch(IOException exc) {
      throw new ExtendedJspException("Writing end tag failed", exc);
    }

    // Print the end tag
    PageRequest request = getPageRequest();
    PageResponse response = new JspPageResponse(pageContext);
    
    // Set Escaping
    response.setEscapeType(mNestedTag.getParameter("escape"));

    try {
      // Print the end tag
      mNestedTag.printEndTag(request, response);
      
      // Unset the context
      mNestedTag.unsetContext();
    }
    catch (RegainException exc) {
      throw new ExtendedJspException("Writing results failed", exc);
    }
    
    return EVAL_PAGE;
  }


  /**
   * Gets the PageRequest adapter from the JSP page context.
   * <p>
   * If the adapter does not yet exist, it is created.
   * 
   * @return The PageRequest adapter.
   */
  private PageRequest getPageRequest() {
    PageRequest request = (PageRequest) pageContext.getAttribute("SharedTagPageRequest");
    if (request == null) {
      request = new JspPageRequest(pageContext);
      pageContext.setAttribute("SharedTagPageRequest", request);
      
      // Add the error to the page attributes
      Throwable error = (Throwable) pageContext.getRequest().getAttribute("javax.servlet.jsp.jspException");
      if (error != null) {
        request.setContextAttribute("page.exception", error);
      }
    }
    
    return request;
  }
  
  /**
   * All Tags may have an optional escape attribute.
   * @param escapeType
   */
  public void setEscape(String escapeType)
  {
    getNestedTag().setParameter("escape", escapeType);
  }

}
