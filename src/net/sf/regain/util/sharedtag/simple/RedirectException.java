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

import net.sf.regain.RegainException;

/**
 * An exception used to abort a current page request and redirect the request to
 * another URL.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RedirectException extends RegainException {

  /** The URL to redirect the request to. */
  private String mUrl;
  
  
  /**
   * Creates a new instance of ForwardException.
   * 
   * @param url The URL to redirect the request to.
   */
  public RedirectException(String url) {
    super(null);
    
    mUrl = url;
  }
  
  
  /**
   * Gets the URL to redirect the request to.
   * 
   * @return The URL to redirect the request to.
   */
  public String getUrl() {
    return mUrl;
  }
  
}
