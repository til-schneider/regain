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
 *  $RCSfile: Preparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/document/Preparator.java,v $
 *     $Date: 2004/07/28 20:26:03 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.document;

import net.sf.regain.RegainException;

/**
 * Präpariert ein Dokument für die Indizierung.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert und eine Zusammenfassung erstellt.
 * <p>
 * Der Ablauf der Indizierung ist dabei der folgende:
 * <ul>
 *   <li>Es wird {@link #accepts(RawDocument)} aufgerufen.</li>
 *   <li>Wenn <code>true</code> zurückgegeben wurde, dann wird
 *     {@link #prepare(RawDocument)} aufgerufen. Dabei extrahiert der Preparator
 *     alle wichtigen Informationen</li>
 *   <li>Diese werden nun durch beliebiges Aufrufen von {@link #getTitle()},
 *     {@link #getCleanedContent()} und {@link #getSummary()} abgefragt.</li>
 *   <li>Schließlich wird {@link #cleanUp()} aufgerufen und der Preparator
 *     vergisst die Informationen über das Dokument.</li>
 * </ul>
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public interface Preparator {

  /**
   * Setzt den Regulären Ausdruck, dem eine URL entsprechen muss, damit sie von
   * diesem Präperator bearbeitet wird.
   * 
   * @param regex Der Regulären Ausdruck, dem eine URL entsprechen muss, damit
   *        sie von diesem Präperator bearbeitet wird.
   * @throws RegainException Wenn der Reguläre Ausdruck fehlerhaft ist.
   */
  public void setUrlRegex(String regex) throws RegainException;

  /**
   * Gibt zurück, ob der Präperator das gegebene Dokument bearbeiten kann.
   * Das ist der Fall, wenn seine URL der URL-Regex entspricht.
   *
   * @param rawDocument Das zu prüfenden Dokuments.
   * @return Ob der Präperator das gegebene Dokument bearbeiten kann.
   * @see #setUrlRegex(String)
   */  
  public boolean accepts(RawDocument rawDocument);

  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */  
  public void prepare(RawDocument rawDocument) throws RegainException;

  /**
   * Gibt den Titel des Dokuments zurück.
   * <p>
   * Falls kein Titel extrahiert werden konnte, wird <CODE>null</CODE>
   * zurückgegeben.
   *
   * @return Der Titel des Dokuments.
   */  
  public String getTitle();

  /**
   * Gibt den von Formatierungsinformation befreiten Inhalt des Dokuments zurück.
   *
   * @return Der gesäuberte Inhalt.
   */  
  public String getCleanedContent();

  /**
   * Gibt eine Zusammenfassung für das Dokument zurück.
   * <p>
   * Falls es keine Zusammenfassung möglich ist, wird <CODE>null</CODE>
   * zurückgegeben.
   *
   * @return Eine Zusammenfassung für das Dokument zurück.
   */  
  public String getSummary();

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
  public String getHeadlines();
  
  /**
   * Gibt den Pfad zurück, über den das Dokument zu erreichen ist.
   * <p>
   * Falls kein Pfad verfügbar ist, wird <code>null</code> zurückgegeben.
   * 
   * @return Der Pfad, über den das Dokument zu erreichen ist.
   */
  public PathElement[] getPath();
  
  /**
   * Gibt alle Ressourcen frei, die für die Informationen über das Dokument
   * reserviert wurden.
   * <p>
   * Wird am Ende der Bearbeitung eines Dokumebts aufgerufen, also nachdem die
   * Getter abgefragt wurden.
   */
  public void cleanUp();
  
  /**
   * Gibt alle Ressourcen frei, die von diesem Präparator genutzt wurden.
   * <p>
   * Wird ganz am Ende des Crawler-Prozesses aufgerufen, nachdem alle Dokumente
   * bearbeitet wurden.
   * 
   * @throws RegainException Wenn der Präparator nicht geschlossen werden konnte.
   */
  public void close() throws RegainException;

}
