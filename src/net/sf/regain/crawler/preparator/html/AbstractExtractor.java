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
 *  $RCSfile: AbstractExtractor.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/html/AbstractExtractor.java,v $
 *     $Date: 2004/11/25 15:37:39 $
 *   $Author: til132 $
 * $Revision: 1.3 $
 */
package net.sf.regain.crawler.preparator.html;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Extrahiert mit Hilfe von Regulären Ausdrücken ein Fragment eines Dokuments.
 * <p>
 * Mit Hilfe eines URL-Präfixes wird bestimmt, ob dieser Extrahierer ein
 * konkretes Dokument bearbeiten kann oder nicht.
 *
 * @author Til Schneider, www.murfman.de
 */
public class AbstractExtractor {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(AbstractExtractor.class);

  /**
   * Der Präfix, den eine URL haben muss, um von diesem Extrahierer bearbeitet
   * zu werden.
   */
  private String mPrefix;

  /**
   * Der compilierte Reguläre Ausdruck, der die Stelle findet, wo das zu
   * extrahierende Fragment eines Dokuments beginnt.
   * <p>
   * Ist <code>null</code>, wenn der gesamte Anfang des Dokuments extrahiert
   * werden soll.
   */
  private RE mFragmentStartRE;

  /**
   * Der Reguläre Ausdruck, der die Stelle findet, wo das zu extrahierende
   * Fragment eines Dokuments beginnt.
   * <p>
   * Ist <code>null</code>, wenn der gesamte Anfang des Dokuments extrahiert
   * werden soll.
   */
  private String mFragmentStartRegex;

  /**
   * Der compilierte Reguläre Ausdruck, der die Stelle findet, wo das zu
   * extrahierende Fragment eines Dokuments endet.
   * <p>
   * Ist <code>null</code>, wenn das gesamte Ende des Dokuments extrahiert
   * werden soll.
   */
  private RE mFragmentEndRE;

  /**
   * Der Reguläre Ausdruck, der die Stelle findet, wo das zu extrahierende
   * Fragment eines Dokuments endet.
   * <p>
   * Ist <code>null</code>, wenn das gesamte Ende des Dokuments extrahiert
   * werden soll.
   */
  private String mFragmentEndRegex;



  /**
   * Erzeugt eine neue AbstractExtractor-Instanz.
   *
   * @param prefix Der Präfix den eine URL haben muss, damit das zugehörige
   *        Dokument von diesem HtmlContentExtractor bearbeitet wird.
   * @param fragmentStartRegex Der Reguläre Ausdruck, der die Stelle findet, wo
   *        das zu extrahierende Fragment eines Dokuments beginnt.
   *        <p>
   *        Ist <code>null</code> oder Leerstring, wenn der gesamte Anfang des
   *        Dokuments extrahiert werden soll.
   * @param fragmentEndRegex Der Reguläre Ausdruck, der die Stelle findet, wo
   *        das zu extrahierende Fragment eines Dokuments endet.
   *        <p>
   *        Ist <code>null</code> oder Leerstring, wenn das gesamte Ende des
   *        Dokuments extrahiert werden soll.
   * @throws RegainException Wenn ein Regulärer Ausdruck einen Syntaxfehler
   *         enthält.
   */
  public AbstractExtractor(String prefix, String fragmentStartRegex,
    String fragmentEndRegex)
    throws RegainException
  {
    mPrefix = prefix;

    try {
      if ((fragmentStartRegex != null) && (fragmentStartRegex.length() != 0)) {
        mFragmentStartRE = new RE(fragmentStartRegex, RE.MATCH_CASEINDEPENDENT);
        mFragmentStartRegex = fragmentStartRegex;
      }
      if ((fragmentEndRegex != null) && (fragmentEndRegex.length() != 0)) {
        mFragmentEndRE = new RE(fragmentEndRegex, RE.MATCH_CASEINDEPENDENT);
        mFragmentEndRegex = fragmentEndRegex;
      }
    }
    catch (RESyntaxException exc) {
      throw new RegainException("Syntax error in regular expression", exc);
    }
  }



  /**
   * Gibt zurück, ob der Extrahierer das gegebene Dokument bearbeiten kann.
   * <p>
   * Dies ist der Fall, wenn die URL mit dem Präfix dieses Extrahierer beginnt.
   *
   * @param rawDocument Das zu prüfenden Dokuments.
   *
   * @return Ob der Extrahierer das gegebene Dokument bearbeiten kann.
   */
  public boolean accepts(RawDocument rawDocument) {
    return rawDocument.getUrl().startsWith(mPrefix);
  }



  /**
   * Extrahiert das Fragment aus dem gegebenen Dokument.
   *
   * @param rawDocument Das Dokument, aus dem das Fragment extrahiert werden
   *        soll.
   * @return Das Fragment
   * @throws RegainException Wenn das Dokument nicht gelesen werden konnte.
   */
  protected String extractFragment(RawDocument rawDocument)
    throws RegainException
  {
    String content = rawDocument.getContentAsString();

    // Find the fragment start
    int fragmentStart = 0;
    if (mFragmentStartRE != null) {
      if (mFragmentStartRE.match(content)) {
        fragmentStart = mFragmentStartRE.getParenEnd(0);
      } else {
        mLog.warn("The regular expression '" + mFragmentStartRegex + "' had no "
          + "match for '" + rawDocument.getUrl() + "'");
      }
    }

    // Find the fragment end
    int fragmentEnd = content.length();
    if (mFragmentEndRE != null) {
      if (mFragmentEndRE.match(content, fragmentStart)) {
        fragmentEnd = mFragmentEndRE.getParenStart(0);
      } else {
        mLog.warn("The regular expression '" + mFragmentEndRegex + "' had no "
          + "match for '" + rawDocument.getUrl() + "'");
      }
    }

    if ((fragmentStart == 0) && (fragmentEnd == content.length())) {
      // Nothing to do -> So don't waste ressources
      return content;
    } else {
      return content.substring(fragmentStart, fragmentEnd);
    }
  }



}
