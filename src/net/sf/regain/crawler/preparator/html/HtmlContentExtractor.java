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
 *  $RCSfile: HtmlContentExtractor.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/html/HtmlContentExtractor.java,v $
 *     $Date: 2004/11/10 15:08:51 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.crawler.preparator.html;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Extrahiert aus einem HTML-Dokument den eigentlichen Inhalt.
 * <p>
 * Dazu werden zwei reguläre Ausdrücke verwendet, die jeweils den Anfang und das
 * Ende des Inhalts erkennen. Alles was dazwischen liegt wird ausgeschnitten.
 *
 * @author Til Schneider, www.murfman.de
 */
public class HtmlContentExtractor extends AbstractExtractor {

  /**
   * Der Reguläre Ausdruck, der eine Überschrift findet.
   * <p>
   * Ist <code>null</code>, wenn das HTML-Dokuments nicht auf Überschriften
   * durchsucht werden soll.
   */
  private RE mHeadlineRE;

  /**
   * Die Gruppe des Reguläre Ausdrucks, der eine Überschrift findet.
   */
  private int mHeadlineRegexGroup = -1;



  /**
   * Erzeugt eine neue HtmlContentExtractor-Instanz.
   *
   * @param prefix Der Präfix den eine URL haben muss, damit das zugehörige
   *        Dokument von diesem HtmlContentExtractor bearbeitet wird.
   * @param contentStartRegex Der Reguläre Ausdruck, der die Stelle findet,
   *        wo der zu indizierende Inhalt von HTML-Dokumenten beginnt.
   *        <p>
   *        Ist <code>null</code> oder Leerstring, wenn der gesamte Anfang des
   *        HTML-Dokuments indiziert werden soll.
   * @param contentEndRegex Der Reguläre Ausdruck, der die Stelle findet,
   *        wo der zu indizierende Inhalt von HTML-Dokumenten endet.
   *        <p>
   *        Ist <code>null</code> oder Leerstring, wenn das gesamte Ende des
   *        HTML-Dokuments indiziert werden soll.
   * @param headlineRegex Der Reguläre Ausdruck, der eine Überschrift findet.
   *        <p>
   *        Ist <code>null</code>, wenn das HTML-Dokuments nicht auf
   *        Überschriften durchsucht werden soll.
   * @param headlineRegexGroup Die Gruppe des Reguläre Ausdrucks, der eine
   *        Überschrift findet.
   * @throws RegainException Wenn ein Regulärer Ausdruck einen Syntaxfehler
   *         enthält.
   */
  public HtmlContentExtractor(String prefix, String contentStartRegex,
    String contentEndRegex, String headlineRegex, int headlineRegexGroup)
    throws RegainException
  {
    super(prefix, contentStartRegex, contentEndRegex);

    try {
      if ((headlineRegex != null) && (headlineRegex.length() != 0)) {
        mHeadlineRE = new RE(headlineRegex, RE.MATCH_CASEINDEPENDENT | RE.MATCH_MULTILINE);
        mHeadlineRegexGroup = headlineRegexGroup;
      }
    }
    catch (RESyntaxException exc) {
      throw new RegainException("Syntax error in regular expression", exc);
    }
  }



  /**
   * Extrahiert den eigentlichen HTML-Inhalt aus dem gegebenen Dokument.
   *
   * @param rawDocument Das Dokument, dessen Inhalt extrahiert werden soll.
   * @return Der eigentliche HTML-Inhalt.
   * @throws RegainException Wenn das Dokument nicht gelesen werden konnte.
   */
  public String extractContent(RawDocument rawDocument) throws RegainException {
    return extractFragment(rawDocument);
  }



  /**
   * Extrahiert die Überschrifen aus einem HTML-Dokuments.
   * <p>
   * Es handelt sich dabei nicht um die Überschrift des Dokuments selbst,
   * sondern lediglich um Unter-Überschriften, die in dem Dokument verwendendet
   * werden. Mit Hilfe dieser Überschriften läßt sich eine bessere Relevanz
   * berechnen.
   *
   * @param content Der Inhalt, aus dem die Überschriften extrahiert werden
   *        sollen.
   * @return Die Überschriften, die im Dokument gefunden wurden, durch \n
   *         getrennt, oder <code>null</code>, wenn keine Überschrift gefunden
   *         wurde oder wenn gar nicht nach Überschriften gesucht werden soll.
   * @see #extractContent(RawDocument)
   */
  public String extractHeadlines(String content) {
    if (mHeadlineRE == null) {
      return null;
    }

    int offset = 0;
    StringBuffer buffer = null;
    while (mHeadlineRE.match(content, offset)) {
      String headline = mHeadlineRE.getParen(mHeadlineRegexGroup);
      headline = headline.trim();

      // Append the headline if it is not empty
      if (headline.length() != 0) {
        if (buffer == null) {
          buffer = new StringBuffer();
        }

        buffer.append(headline);
        buffer.append("\n");
      }

      offset = mHeadlineRE.getParenEnd(0);
    }

    if (buffer == null) {
      return null;
    } else {
      return buffer.toString();
    }
  }

}
