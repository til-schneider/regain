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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import simple.http.Request;
import simple.http.session.Session;
import simple.util.net.Parameters;

/**
 * Adapter from a simpleweb Request to a SharedTag PageRequest.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SimplePageRequest extends PageRequest {

  /** The init parameters. May be null. */
  private static HashMap<String, String> mInitParameterHash;

  /** The base URL where the JSP files and resources are located. */
  private static URL mResourceBaseUrl;

  /** The working directory of the web server.*/
  private static File mWorkingDir;

  /** The simpleweb Request to adapt. */
  private Request mRequest;

  /** The page context. May be null. */
  private HashMap<String, Object> mPageContext;

  /** The session. */
  private Session mSession;

  /**
   * Creates a new instance of SimplePageRequest.
   *
   * @param request The simpleweb Request to adapt.
   */
  public SimplePageRequest(Request request) {
    mRequest = request;
    mSession = null;
  }


  /**
   * Gets a request parameter that was given to page via GET or POST.
   *
   * @param name The name of the parameter.
   * @return The given parameter or <code>null</code> if no such parameter was
   *         given.
   * @throws RegainException If getting the parameter failed.
   */
  public String getParameter(String name) throws RegainException {
    try {
      return mRequest.getParameter(name);
    }
    catch (IOException exc) {
      throw new RegainException("Getting parameter " + name + " failed", exc);
    }
  }


  /**
   * Gets all request parameters with the given name that were given to the page
   * via GET or POST.
   *
   * @param name The name of the parameter.
   * @return The parameters or <code>null</code> if no such parameter was
   *         given.
   * @throws RegainException If getting the parameter failed.
   */
  public String[] getParameters(String name) throws RegainException {
    try {
      return mRequest.getParameters().getParameters(name);
    }
    catch (IOException exc) {
      throw new RegainException("Getting parameter " + name + " failed", exc);
    }
  }


  /**
   * Gets the names of the given parameters.
   *
   * @return The names of the given parameters.
   * @throws RegainException If getting the parameter names failed.
   */
  public Enumeration<?> getParameterNames() throws RegainException {
    try {
      Parameters params = mRequest.getParameters();
      return params.getParameterNames();
    }
    catch (IOException exc) {
      throw new RegainException("Getting parameter names failed", exc);
    }
  }


  /**
   * Gets the header with the given name.
   *
   * @param name The name of the header.
   * @return The header or <code>null</code> if no such header exists.
   * @throws RegainException If getting the header failed.
   */
  public String getHeader(String name) throws RegainException {
    return mRequest.getValue(name);
  }


  /**
   * Gets the header with the given name as date.
   *
   * @param name The name of the header.
   * @return The date header or <code>-1</code> if no such header exists.
   * @throws RegainException If getting the header failed.
   */
  public long getHeaderAsDate(String name) throws RegainException {
    return mRequest.getDate(name);
  }


  /**
   * Gets the locale of the client.
   *
   * @return The locale.
   * @throws RegainException If getting the locale failed.
   */
  public Locale getLocale() throws RegainException {
    return mRequest.getLanguage();
  }


  /**
   * Sets an attribute at the page context.
   *
   * @param name The name of the attribute to set.
   * @param value The value of the attribute to set.
   */
  public void setContextAttribute(String name, Object value) {
    if (mPageContext == null) {
      mPageContext = new HashMap<String, Object>();
    }
    mPageContext.put(name, value);
  }


  /**
   * Gets an attribute from the page context.
   *
   * @param name The name of the attribute to get.
   * @return The attribute's value or <code>null</code> if there is no such
   *         attribute.
   */
  public Object getContextAttribute(String name) {
    if (mPageContext == null) {
      return null;
    }

    return mPageContext.get(name);
  }


  /**
   * Sets an attribute at the session.
   *
   * @param name The name of the attribute to set.
   * @param value The value of the attribute to set.
   */
  public void setSessionAttribute(String name, Object value) {
    // TODO: implement
    //throw new IllegalStateException("Not yet implemented");
    if (mSession == null)
      mSession = mRequest.getSession();

    mSession.put(name, value);
  }


  /**
   * Gets an attribute from the session.
   *
   * @param name The name of the attribute to get.
   * @return The attribute's value or <code>null</code> if there is no such
   *         attribute.
   */
  public Object getSessionAttribute(String name) {
    // TODO: implement
    //throw new IllegalStateException("Not yet implemented");
    if (mSession == null)
      mSession = mRequest.getSession();

    return mSession.get(name);
  }


  /**
   * Gets an init parameter.
   *
   * @param name The name of the init parameter.
   * @return The value of the init parameter.
   */
  public String getInitParameter(String name) {
    if (mInitParameterHash == null) {
      return null;
    }

    return mInitParameterHash.get(name);
  }


  /**
   * Sets an init parameter.
   *
   * @param name The name of the init parameter.
   * @param value The value of the init parameter.
   */
  public static void setInitParameter(String name, String value) {
    if (mInitParameterHash == null) {
      mInitParameterHash = new HashMap<String, String>();
    }
    mInitParameterHash.put(name, value);
  }


  /**
   * Gets the base URL where the JSP files and resources are located.
   *
   * @return The base URL where the JSP files and resources are located.
   */
  public URL getResourceBaseUrl() {
    return mResourceBaseUrl;
  }


  /**
   * Sets the base URL where the JSP files and resources are located.
   *
   * @param baseurl The base URL where the JSP files and resources are located.
   */
  public static void setResourceBaseUrl(URL baseurl) {
    mResourceBaseUrl = baseurl;
  }


  /**
   * Gets the working directory of the web server.
   *
   * @return The working directory of the web server.
   */
  public File getWorkingDir() {
    return mWorkingDir;
  }


  /**
   * Sets the working directory of the web server.
   *
   * @param workingDir The working directory of the web server.
   */
  public static void setWorkingDir(File workingDir) {
    mWorkingDir = workingDir;
  }

}
