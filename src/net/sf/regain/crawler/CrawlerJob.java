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
 *  $RCSfile: CrawlerJob.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/CrawlerJob.java,v $
 *     $Date: 2004/11/10 15:08:51 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.crawler;

/**
 * Hilfsklasse für den Crawler.
 * <p>
 * Enthält alle Daten, die für die Bearbeitung einer URL nötig sind.
 * <p>
 * Der Crawler erzeugt für jede akzeptierte URL eine CrawlerJob-Instanz, die dann
 * nacheinander abgearbeitet werden.
 *
 * @author Til Schneider, www.murfman.de
 */
public class CrawlerJob {

  /** Die URL des zu bearbeitenden Dokuments. */
  private String mUrl;
  /**
   * Die URL des Dokuments, in dem die URL des zu bearbeitenden Dokuments
   * gefunden wurde.
   */
  private String mSourceUrl;
  /**
   * Der Text des Links in dem die URL gefunden wurde. Ist <code>null</code>,
   * falls die URL nicht in einem Link (also einem a-Tag) gefunden wurde oder
   * wenn aus sonstigen Gründen kein Link-Text vorhanden ist.
   */
  private String mSourceLinkText;
  /** Gibt an, ob das Dokument nach weiteren URLs durchsucht werden soll. */
  private boolean mShouldBeParsed;
  /** Gibt an, ob das Dokument indiziert werden soll. */
  private boolean mShouldBeIndexed;



  /**
   * Erzeugt eine neue CrawlerJob-Instanz.
   *
   * @param url Die URL des zu bearbeitenden Dokuments.
   * @param sourceUrl Die URL des Dokuments, in dem die URL des zu bearbeitenden
   *        Dokuments gefunden wurde.
   * @param sourceLinkText Der Text des Links in dem die URL gefunden wurde. Ist
   *        <code>null</code>, falls die URL nicht in einem Link (also einem
   *        a-Tag) gefunden wurde oder wenn aus sonstigen Gründen kein Link-Text
   *        vorhanden ist.
   * @param shouldBeParsed Gibt an, ob das Dokument nach weiteren URLs
   *        durchsucht werden soll.
   * @param shouldBeIndexed Gibt an, ob das Dokument indiziert werden soll.
   */
  public CrawlerJob(String url, String sourceUrl, String sourceLinkText,
    boolean shouldBeParsed, boolean shouldBeIndexed)
  {
    mUrl = url;
    mSourceUrl = sourceUrl;
    mSourceLinkText = sourceLinkText;
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
   * Gibt Die URL des Dokuments zurück, in dem die URL des zu bearbeitenden
   * Dokuments gefunden wurde.
   *
   * @return Die URL des Dokuments, in dem die URL des zu bearbeitenden
   *         Dokuments gefunden wurde.
   */
  public String getSourceUrl() {
    return mSourceUrl;
  }



  /**
   * Gibt den Text des Links zurück in dem die URL gefunden wurde.
   * <p>
   * Ist <code>null</code>, falls die URL nicht in einem Link (also einem a-Tag)
   * gefunden wurde oder wenn aus sonstigen Gründen kein Link-Text vorhanden ist.
   *
   * @return Der Text des Links zurück in dem die URL gefunden wurde.
   */
  public String getSourceLinkText() {
    return mSourceLinkText;
  }



  /**
   * Gibt zurück, ob das Dokument nach weiteren URLs durchsucht werden soll.
   *
   * @return Ob das Dokument nach weiteren URLs durchsucht werden soll.
   */
  public boolean shouldBeParsed() {
    return mShouldBeParsed;
  }



  /**
   * Gibt zurück, ob das Dokument indiziert werden soll.
   *
   * @return Ob das Dokument indiziert werden soll.
   */
  public boolean shouldBeIndexed() {
    return mShouldBeIndexed;
  }

}
