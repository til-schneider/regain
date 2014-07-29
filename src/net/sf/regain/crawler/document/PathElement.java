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
package net.sf.regain.crawler.document;

/**
 * Ein Element eines Pfades. Besteht aus einer URL und einem Titel
 *
 * @author Til Schneider, www.murfman.de
 */
public class PathElement {

  /** Die URL. */
  private String mUrl;

  /** Der Titel. */
  private String mTitle;



  /**
   * Erzeugt eine neue PathElement-Instanz.
   *
   * @param url Die URL
   * @param title Der Titel
   */
  public PathElement(String url, String title) {
    super();
    mUrl = url;
    mTitle = title;
  }



  /**
   * Gibt die URL zurück.
   *
   * @return Die URL.
   */
  public String getUrl() {
    return mUrl;
  }



  /**
   * Gibt den Titel zurück.
   *
   * @return Der Titel.
   */
  public String getTitle() {
    return mTitle;
  }

}
