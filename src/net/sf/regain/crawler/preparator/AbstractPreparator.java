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
 *  $RCSfile: AbstractPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/AbstractPreparator.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.PathElement;
import net.sf.regain.crawler.document.Preparator;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Abstrakte Implementierung eines Präperators.
 * <p>
 * Implementiert die Getter-Methoden und übernimmt das Aufräumen zwischen zwei
 * Präparationen (Siehe {@link #cleanUp()}).
 * <p>
 * Kindklassen können die Werte über die geschützten (protected) Setter-Methoden
 * setzen. 
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public abstract class AbstractPreparator implements Preparator {

  /**
   * Der Reguläre Ausdruck, dem eine URL entsprechen muss, damit sie von
   * diesem Präperator bearbeitet wird.
   */
  private RE mUrlRegex;
  /** Der gefundene Titel. */
  private String mTitle;
  /** Der gesäuberte Inhalt. */
  private String mCleanedContent;
  /** Die Zusammenfassung des Dokuments. */
  private String mSummary;
  /** Die extrahierten Überschriften. Kann <code>null</code> sein */
  private String mHeadlines;
  /** Der Pfad, über den das Dokument zu erreichen ist. */
  private PathElement[] mPath;
  
  
  
  /**
   * Erzeugt eine neue Instanz.
   */
  public AbstractPreparator() {
  }



  /**
   * Setzt den Regulären Ausdruck, dem eine URL entsprechen muss, damit sie von
   * diesem Präperator bearbeitet wird.
   * 
   * @param regex Der Regulären Ausdruck, dem eine URL entsprechen muss, damit
   *        sie von diesem Präperator bearbeitet wird.
   * @throws RegainException Wenn der Reguläre Ausdruck fehlerhaft ist.
   */
  public void setUrlRegex(String regex) throws RegainException {
    try {
      mUrlRegex = new RE(regex);
    }
    catch (RESyntaxException exc) {
      throw new RegainException("URL-Regex for preparator " + getClass().getName()
        + " has wrong syntax: '" + regex + "'", exc);
    }
  }



  /**
   * Gibt zurück, ob der Präperator das gegebene Dokument bearbeiten kann.
   * Das ist der Fall, wenn seine URL der URL-Regex entspricht.
   *
   * @param rawDocument Das zu prüfenden Dokuments.
   * @return Ob der Präperator das gegebene Dokument bearbeiten kann.
   * @see #setUrlRegex(String)
   */  
  public boolean accepts(RawDocument rawDocument) {
    return mUrlRegex.match(rawDocument.getUrl());
  }



  /**
   * Gibt den Titel des Dokuments zurück.
   * <p>
   * Falls kein Titel extrahiert werden konnte, wird <CODE>null</CODE>
   * zurückgegeben.
   *
   * @return Der Titel des Dokuments.
   */  
  public String getTitle() {
    return mTitle;
  }
  
  
  
  /**
   * Setzt den Titel des Dokuments, das gerade präpariert wird.
   * 
   * @param title Der Titel.
   */
  protected void setTitle(String title) {
    mTitle = title;
  }



  /**
   * Gibt den von Formatierungsinformation befreiten Inhalt des Dokuments zurück.
   *
   * @return Der gesäuberte Inhalt.
   */  
  public String getCleanedContent() {
    return mCleanedContent;
  }



  /**
   * Setzt von Formatierungsinformation befreiten Inhalt des Dokuments, das
   * gerade präpariert wird.
   * 
   * @param cleanedContent Der gesäuberte Inhalt.
   */
  protected void setCleanedContent(String cleanedContent) {
    mCleanedContent = cleanedContent;
  }



  /**
   * Gibt eine Zusammenfassung für das Dokument zurück.
   * <p>
   * Da eine Zusammenfassung nicht einfach möglich ist, wird <CODE>null</CODE>
   * zurückgegeben.
   *
   * @return Eine Zusammenfassung für das Dokument
   */  
  public String getSummary() {
    return mSummary;
  }



  /**
   * Setzt die Zusammenfassung des Dokuments, das gerade präpariert wird.
   * 
   * @param summary Die Zusammenfassung
   */
  protected void setSummary(String summary) {
    mSummary = summary;
  }



  /**
   * Gibt die Überschriften des Dokuments zurück.
   * <p>
   * Es handelt sich dabei nicht um die Überschrift des Dokuments selbst,
   * sondern lediglich um Unter-Überschriften, die in dem Dokument verwendendet
   * werden. Mit Hilfe dieser Überschriften läßt sich eine bessere Relevanz
   * berechnen. 
   * <p>
   * Wenn keine Überschriften gefunden wurden, dann wird <code>null</code>
   * zurückgegeben.
   *
   * @return Die Überschriften des Dokuments.
   */  
  public String getHeadlines() {
    return mHeadlines;
  }



  /**
   * Setzt die Überschriften, in im Dokument, das gerade präpariert wird,
   * gefunden wurden.
   * 
   * @param headlines Die Zusammenfassung
   */
  protected void setHeadlines(String headlines) {
    mHeadlines = headlines;
  }



  /**
   * Gibt den Pfad zurück, über den das Dokument zu erreichen ist.
   * <p>
   * Falls kein Pfad verfügbar ist, wird <code>null</code> zurückgegeben.
   * 
   * @return Der Pfad, über den das Dokument zu erreichen ist.
   */
  public PathElement[] getPath() {
    return mPath;
  }



  /**
   * Setzt den Pfad, über den das Dokument zu erreichen ist.
   * 
   * @param path Der Pfad, über den das Dokument zu erreichen ist.
   */
  public void setPath(PathElement[] path) {
    mPath = path;
  }



  /**
   * Gibt alle Ressourcen frei, die für die Informationen über das Dokument
   * reserviert wurden.
   */
  public void cleanUp() {
    mTitle = null;
    mCleanedContent = null;
    mSummary = null;
    mHeadlines = null;
    mPath = null;
  }


  /**
   * Gibt alle Ressourcen frei, die von diesem Präparator genutzt wurden.
   * <p>
   * Wird ganz am Ende des Crawler-Prozesses aufgerufen, nachdem alle Dokumente
   * bearbeitet wurden.
   */
  public void close() throws RegainException {
  }

}
