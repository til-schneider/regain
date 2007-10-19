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
 *     $Date: 2006-01-03 12:25:03 +0100 (Di, 03 Jan 2006) $
 *   $Author: til132 $
 * $Revision: 187 $
 */
package net.sf.regain.util.sharedtag.taglib;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.util.sharedtag.PageResponse;

/**
 * Adapter from a ServletResponse to a SharedTag PageResponse.
 *
 * @author Til Schneider, www.murfman.de
 */
public class JspPageResponse extends PageResponse {

  /** The page context to adapt. */
  private PageContext mPageContext;
  
  /** The ServletResponse to adapt. */
  private HttpServletResponse mServletResponse;
  
  /** The JSP writer. Is <code>null</code> until the first time requested. */
  private JspWriter mJspWriter;


  /**
   * Creates a new instance of JspPageWriter.
   * 
   * @param pageContext The page context to adapt.
   */
  public JspPageResponse(PageContext pageContext) {
    mPageContext = pageContext;
    mServletResponse = (HttpServletResponse) pageContext.getResponse();
  }


  /**
   * Gets the character encoding of the response.
   * 
   * @return The character encoding of the response.
   * @throws RegainException If getting th encoding failed.
   */
  public String getEncoding() throws RegainException {
  	String encoding = mServletResponse.getCharacterEncoding();
    return (encoding != null) ? encoding : RegainToolkit.getSystemDefaultEncoding();
  }


  /**
   * Sets the header with the given name.
   * 
   * @param name The name of the header.
   * @param value The header value to set.
   * @throws RegainException If getting the header failed.
   */
  public void setHeader(String name, String value) throws RegainException {
    mServletResponse.setHeader(name, value);
  }


  /**
   * Sets the header with the given name as date.
   * 
   * @param name The name of the header.
   * @param value The header value to set.
   * @throws RegainException If getting the header failed.
   */
  public void setHeaderAsDate(String name, long value) throws RegainException {
    mServletResponse.setDateHeader(name, value);
  }
  
  
  /**
   * Gets the OutputStream to use for sending binary data.
   * 
   * @return The OutputStream to use for sending binary data.
   * @throws RegainException If getting the OutputStream failed.
   */
  public OutputStream getOutputStream() throws RegainException {
    try {
      return mServletResponse.getOutputStream();
    }
    catch (IOException exc) {
      throw new RegainException("Getting the response OutputStream failed", exc);
    }
  }
  

  /**
   * Prints text to a page.
   * 
   * @param text The text to print.
   * @throws RegainException If printing failed.
   */
  public void print(String text) throws RegainException {
    try {
      if (mJspWriter == null) {
        mJspWriter = mPageContext.getOut();
      }

      mJspWriter.print(text);
    }
    catch (IOException exc) {
      throw new RegainException("Writing results failed", exc);
    }
  }


  /**
   * Redirects the request to another URL.
   * 
   * @param url The URL to redirect to.
   * @throws RegainException If redirecting failed.
   */
  public void sendRedirect(String url) throws RegainException {
    try {
      mServletResponse.sendRedirect(url);
    }
    catch (IOException exc) {
      throw new RegainException("Sending redirect to '" + url + "' failed", exc);
    }
  }


  /**
   * Sends a HTTP error.
   * 
   * @param errorCode The error code to send.
   * @throws RegainException If sending the error failed.
   */
  public void sendError(int errorCode) throws RegainException {
    try {
      mServletResponse.sendError(errorCode);
    }
    catch (IOException exc) {
      throw new RegainException("Sending error code " + errorCode + " failed", exc);
    }
  }

}
