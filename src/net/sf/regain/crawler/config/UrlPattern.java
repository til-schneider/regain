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
 *  $RCSfile: UrlPattern.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/UrlPattern.java,v $
 *     $Date: 2004/11/10 15:08:51 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.crawler.config;

/**
 * Enthält alle Daten eines URL-Pattern.
 * <p>
 * Ein URL-Pattern wird beim Durchsuchen von Dokumenten nach URLs dazu verwendet,
 * URLs eines bestimmten Typs zu identifizieren.
 *
 * @author Til Schneider, www.murfman.de
 */
public class UrlPattern {

  /** Der Reguläre Ausdruck, die eine URL findet. */
  private String mRegexPattern;
  /** Die Gruppe des Regulären Ausdrucks, die die URL enthält. */
  private int mRegexUrlGroup;
  /** Gibt an, ob eine gefundene URL nach weiteren URLs durchsucht werden soll. */
  private boolean mShouldBeParsed;
  /** Gibt an, ob eine gefundene URL indiziert werden soll. */
  private boolean mShouldBeIndexed;



  /**
   * Erzeugt eine neue UrlPattern-Instanz.
   *
   * @param regexPattern Der Reguläre Ausdruck, die eine URL findet.
   * @param regexUrlGroup Die Gruppe des Regulären Ausdrucks, die die URL
   *        enthält.
   * @param shouldBeParsed Gibt an, ob eine gefundene URL nach weiteren URLs
   *        durchsucht werden soll.
   * @param shouldBeIndexed Gibt an, ob eine gefundene URL indiziert werden soll.
   */
  public UrlPattern(String regexPattern, int regexUrlGroup, boolean shouldBeParsed,
    boolean shouldBeIndexed)
  {
    mRegexPattern = regexPattern;
    mRegexUrlGroup = regexUrlGroup;
    mShouldBeParsed = shouldBeParsed;
    mShouldBeIndexed = shouldBeIndexed;
  }



  /**
   * Gibt den Reguläre Ausdruck zurück, die eine URL findet.
   *
   * @return Der Reguläre Ausdruck, die eine URL findet.
   */
  public String getRegexPattern() {
    return mRegexPattern;
  }



  /**
   * Gibt die Gruppe des Regulären Ausdrucks zurück, die die URL enthält.
   *
   * @return Die Gruppe des Regulären Ausdrucks, die die URL enthält.
   */
  public int getRegexUrlGroup() {
    return mRegexUrlGroup;
  }



  /**
   * Gibt zurück, ob eine gefundene URL nach weiteren URLs durchsucht werden soll.
   *
   * @return Ob eine gefundene URL nach weiteren URLs durchsucht werden soll.
   */
  public boolean getShouldBeParsed() {
    return mShouldBeParsed;
  }



  /**
   * Gibt zurück, ob eine gefundene URL indiziert werden soll.
   *
   * @return Ob eine gefundene URL indiziert werden soll.
   */
  public boolean getShouldBeIndexed() {
    return mShouldBeIndexed;
  }

}
