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
 *     $Date: 2008-10-25 18:35:21 +0200 (Sa, 25 Okt 2008) $
 *   $Author: thtesche $
 * $Revision: 349 $
 */
package net.sf.regain.crawler;

import java.net.URLConnection;
import java.net.HttpURLConnection;

import net.sf.regain.RegainException;


/**
 * Eine spezielle Exception, die nur von
 * {@link CrawlerToolkit#getHttpStream(java.net.URL)} genutzt wird. Sie stellt
 * auch den HTTP Antwortcode bereit, woraus sich z.B. erkennen läßt, ob die
 * Exception auf Grund eines Dead-Links geworfen wurde.
 *
 * @author  tschneider
 */
public class HttpStreamException extends RegainException {
  
  /** Der HTTP Antwortcode. */
  private int mHttpReturnCode;
  
  
  
  /**
   * Erzeugt eine neue HttpStreamException-Instanz.
   * 
   * @param message Die Fehlermeldung.
   * @param cause Der Fehler, der diese Ausnahme ausgel�st hat. Ist
   *        <code>null</code> wenn dies der urspr�ngliche Fehler ist.
   * @param httpReturnCode Der HTTP-Return-Code
   */
  private HttpStreamException(String message, Throwable cause, int httpReturnCode) {
    super(message, cause);
    
    mHttpReturnCode = httpReturnCode;
  }

    
    
  /**
   * Erzeugt eine neue HttpStreamException-Instanz.
   * 
   * @param message Die Fehlermeldung.
   * @param cause Der Fehler, der diese Ausnahme ausgel�st hat. Ist
   *        <code>null</code> wenn dies der urspr�ngliche Fehler ist.
   * @param conn Die HTTP-Connection, bei der der Fehler auftrat.
   * @return Eine Ausnahme, die neben des urspr�nglichen Fehlers auch den
   *         HTTP-Return-Code enthält.
   */
  public static HttpStreamException createInstance(String message,
    Throwable cause, URLConnection conn)
  {
    int httpReturnCode = -1;
    
    // Try to provide the HTTP response code
    if (conn instanceof HttpURLConnection) {
      HttpURLConnection hconn = (HttpURLConnection) conn;
      try {
        httpReturnCode = hconn.getResponseCode();
        message += " (HTTP response code: " + httpReturnCode + ")";
        
      }
      catch (Exception exc) {}
    }
    
    return new HttpStreamException(message, cause, httpReturnCode);
  }
  
  
  
  /**
   * Gibt den HTTP-Return-Code zurück.
   * 
   * @return Der HTTP-Return-Code.
   */
  public int getHttpReturnCode() {
    return mHttpReturnCode;
  }
  
  
  
  /**
   * Gibt zurück, ob der HTTP-Code con einem dead link stammt.
   * 
   * @return Ob der HTTP-Code con einem dead link stammt.
   */
  public boolean isHttpReturnCodeFromDeadLink() {
    return (mHttpReturnCode == 404 || mHttpReturnCode == 400);
  }
  
}
