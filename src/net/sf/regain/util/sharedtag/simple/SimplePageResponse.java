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
package net.sf.regain.util.sharedtag.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import simple.http.Request;
import simple.http.Response;
import simple.http.serve.Resource;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageResponse;

/**
 * Adapter from a simpleweb Response to a SharedTag PageResponse.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SimplePageResponse extends PageResponse {
  
  /** The resource that uses this response. */
  private Resource mResource;
  
  /** The request to adapt. */
  private Request mRequest;
  
  /** The response to adapt. */
  private Response mResponse;
  
  /** The PrintStream to write the results to. */
  private PrintStream mPrintStream;
  
  /** The character encoding of the response. */
  private String mEncoding;

  
  /**
   * Creates a new instance of SimplePageWriter.
   * 
   * @param resource The resource that uses this response.
   * @param request The request to adapt.
   * @param response The response to adapt.
   * @param printStream The PrintStream to write the results to.
   * @param encoding The character encoding of the response.
   */
  public SimplePageResponse(Resource resource,
    Request request, Response response, PrintStream printStream, String encoding)
  {
    mResource = resource;
    mRequest = request;
    mResponse = response;
    mPrintStream = printStream;
    mEncoding = encoding;
  }


  /**
   * Gets the character encoding of the response.
   * 
   * @return The character encoding of the response.
   * @throws RegainException If getting th encoding failed.
   */
  public String getEncoding() throws RegainException {
    return mEncoding;
  }


  /**
   * Sets the header with the given name.
   * 
   * @param name The name of the header.
   * @param value The header value to set.
   * @throws RegainException If getting the header failed.
   */
  public void setHeader(String name, String value) throws RegainException {
    mResponse.set(name, value);
  }


  /**
   * Sets the header with the given name as date.
   * 
   * @param name The name of the header.
   * @param value The header value to set.
   * @throws RegainException If getting the header failed.
   */
  public void setHeaderAsDate(String name, long value) throws RegainException {
    mResponse.setDate(name, value);
  }


  /**
   * Gets the OutputStream to use for sending binary data.
   * 
   * @return The OutputStream to use for sending binary data.
   * @throws RegainException If getting the OutputStream failed.
   */
  public OutputStream getOutputStream() throws RegainException {
    try {
      return mResponse.getOutputStream();
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
  public void rawPrint(String text) throws RegainException {
    if (mPrintStream == null) {
      try {
        mPrintStream = mResponse.getPrintStream();
      }
      catch (IOException exc) {
        throw new RegainException("Getting response PrintStream failed", exc);
      }
    }
    
    mPrintStream.print(text);
  }


  /**
   * Redirects the request to another URL.
   * 
   * @param url The URL to redirect to.
   * @throws RegainException If redirecting failed.
   */
  public void sendRedirect(String url) throws RegainException {
    throw new RedirectException(url);
  }


  /**
   * Sends a HTTP error.
   * 
   * @param errorCode The error code to send.
   * @throws RegainException If sending the error failed.
   */
  public void sendError(int errorCode) throws RegainException {
    mResource.handle(mRequest, mResponse, errorCode);
  }

}
