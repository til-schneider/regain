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
package net.sf.regain.crawler.config;

/**
 * enthält die Daten einer Start-URL.
 * <p>
 * Eine Start-URL ist ein Startpunkt an dem der Crawler-Proze� beginnt.
 *
 * @author Til Schneider, www.murfman.de
 */
public class StartUrl {

  /** Die URL des zu bearbeitenden Dokuments. */
  private String mUrl;
  /** Gibt an, ob das Dokument nach weiteren URLs durchsucht werden soll. */
  private boolean mShouldBeParsed;
  /** Gibt an, ob das Dokument indiziert werden soll. */
  private boolean mShouldBeIndexed;



  /**
   * Creates a new instance of StartUrl.
   *
   * @param url Die URL des zu bearbeitenden Dokuments.
   * @param shouldBeParsed Gibt an, ob das Dokument nach weiteren URLs
   *        durchsucht werden soll.
   * @param shouldBeIndexed Gibt an, ob das Dokument indiziert werden soll.
   */
  public StartUrl(String url, boolean shouldBeParsed, boolean shouldBeIndexed)
  {
    mUrl = url;
    mShouldBeParsed = shouldBeParsed;
    mShouldBeIndexed = shouldBeIndexed;
  }



  /**
   * Gibt die URL des zu bearbeitenden Dokuments zurück.
   *
   * @return Die URL des zu bearbeitenden Dokuments.
   */
  public String getUrl() {
    return mUrl;
  }



  /**
   * Gibt zurück, ob das Dokument nach weiteren URLs durchsucht werden soll.
   *
   * @return Ob das Dokument nach weiteren URLs durchsucht werden soll.
   */
  public boolean getShouldBeParsed() {
    return mShouldBeParsed;
  }



  /**
   * Gibt zurück, ob das Dokument indiziert werden soll.
   *
   * @return Ob das Dokument indiziert werden soll.
   */
  public boolean getShouldBeIndexed() {
    return mShouldBeIndexed;
  }

}

