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
 *     $Date: 2011-10-18 09:21:09 +0200 (Di, 18 Okt 2011) $
 *   $Author: benjaminpick $
 * $Revision: 540 $
 */
package net.sf.regain.util.sharedtag;

import java.io.OutputStream;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.util.io.HtmlEntities;
import net.sf.regain.util.io.Printer;

/**
 * A page response.
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class PageResponse implements Printer {
  
  enum EscapeType { none, html, xml };
  protected EscapeType escapeType = EscapeType.none;
  
  /**
   * Escape all output.
   * 
   * @param type  One of: none, html, xml (Null/Default : none)
   */
  public void setEscapeType(String type)
  {
    if (type == null)
      escapeType = EscapeType.none;
    else
    {
      escapeType = EscapeType.valueOf(type);
      if (escapeType == null)
        escapeType = EscapeType.none;
    }
  }
  
  public String getEscapeType() { return escapeType.toString(); } 
  
  /**
   * Gets the character encoding of the response.
   * 
   * @return The character encoding of the response.
   * @throws RegainException If getting th encoding failed.
   */
  public abstract String getEncoding() throws RegainException;
  
  /**
   * Sets the header with the given name.
   * 
   * @param name The name of the header.
   * @param value The header value to set.
   * @throws RegainException If getting the header failed.
   */
  public abstract void setHeader(String name, String value) throws RegainException;
  
  /**
   * Sets the header with the given name as date.
   * 
   * @param name The name of the header.
   * @param value The header value to set.
   * @throws RegainException If getting the header failed.
   */
  public abstract void setHeaderAsDate(String name, long value) throws RegainException;

  /**
   * Gets the OutputStream to use for sending binary data.
   * 
   * @return The OutputStream to use for sending binary data.
   * @throws RegainException If getting the OutputStream failed.
   */
  public abstract OutputStream getOutputStream() throws RegainException;
  
  
  private static final String[] xmlToReplace = new String[]{"<",    ">",    "&",     "\"",     "'"};
  private static final String[] xmlReplace = new String[]{  "&lt;", "&gt;", "&amp;", "&quot;", "&#039;"};

  private static final String[] htmlToReplace = new String[]{"<",    ">"};
  private static final String[] htmlReplace = new String[]{  "&lt;", "&gt;"};

  /**
   * Prints text to a page (escaping when necessary).
   * 
   * @param text The text to print.
   * @throws RegainException If printing failed.
   */
  public final void print(String text) throws RegainException
  {
    if (text != null && !escapeType.equals(EscapeType.none))
    {
      text = HtmlEntities.encode(text);
      switch(escapeType)
      {
        case xml:
          RegainToolkit.replace(text, xmlToReplace, xmlReplace);
          break;
        case html:
          RegainToolkit.replace(text, htmlToReplace, htmlReplace);
          break;
      }
    }
    rawPrint(text);
  }
  
  /**
   * Do the real printing
   * @param text The text to print
   * @throws RegainException  If printing failed.
   */
  public abstract void rawPrint(String text) throws RegainException;

  /**
   * Prints text to a page and escapes all HTML tags. 
   * 
   * @param text The text to print.
   * @throws RegainException If printing failed.
   */
  public void printNoHtml(String text) throws RegainException {
    if (text != null) {
      text = RegainToolkit.replace(text, "<", "&lt;");
      text = RegainToolkit.replace(text, ">", "&gt;");
    }
    
    print(text);
  }
  
  /**
   * Redirects the request to another URL.
   * 
   * @param url The URL to redirect to.
   * @throws RegainException If redirecting failed.
   */
  public abstract void sendRedirect(String url) throws RegainException;
  
  /**
   * Sends a HTTP error.
   * 
   * @param errorCode The error code to send.
   * @throws RegainException If sending the error failed.
   */
  public abstract void sendError(int errorCode) throws RegainException;

}
