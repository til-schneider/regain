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
 *  $RCSfile: DummyConfiguration.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/DummyConfiguration.java,v $
 *     $Date: 2004/07/28 20:26:03 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.config;

import net.sf.regain.crawler.preparator.html.HtmlContentExtractor;
import net.sf.regain.crawler.preparator.html.HtmlPathExtractor;

/**
 * Stellt alle zu konfigurierenden Einstellungen hardcodiert zur Verfügung.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class DummyConfiguration implements Configuration {

  /**
   * Gibt den Host-Namen des Proxy-Servers zurück. Wenn kein Host konfiguriert
   * wurde, wird <CODE>null</CODE> zurückgegeben.
   *
   * @return Der Host-Namen des Proxy-Servers.
   */  
  public String getProxyHost() {
    return "idatmpsrv";
  }



  /**
   * Gibt den Port des Proxy-Servers zurück. Wenn kein Port konfiguriert wurde,
   * wird <CODE>null</CODE> zurückgegeben.
   *
   * @return Der Port des Proxy-Servers.
   */  
  public String getProxyPort() {
    return "3128";
  }



  /**
   * Gibt den Benutzernamen für die Anmeldung beim Proxy-Server zurück. Wenn
   * kein Benutzernamen konfiguriert wurde, wird <CODE>null</CODE> zurückgegeben.
   *
   * @return Der Benutzernamen für die Anmeldung beim Proxy-Server.
   */  
  public String getProxyUser() {
    return null;
  }



  /**
   * Gibt das Passwort für die Anmeldung beim Proxy-Server zurück. Wenn kein
   * Passwort konfiguriert wurde, wird <CODE>null</CODE> zurückgegeben.
   *
   * @return Das Passwort für die Anmeldung beim Proxy-Server.
   */  
  public String getProxyPassword() {
    return null;
  }


  /**
   * Gibt den Timeout für HTTP-Downloads zurück. Dieser Wert bestimmt die
   * maximale Zeit in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   * 
   * @return Den Timeout für HTTP-Downloads
   */
  public int getHttpTimeoutSecs() {
    return 180;
  }


  /**
   * Gibt zurück, ob URLs geladen werden sollen, die weder durchsucht noch
   * indiziert werden.
   *
   * @return Ob URLs geladen werden sollen, die weder durchsucht noch indiziert
   *         werden.
   */  
  public boolean getLoadUnparsedUrls() {
    return false;
  }



  /**
   * Gibt zurück, ob ein Suchindex erstellt werden soll.
   *
   * @return Ob ein Suchindex erstellt werden soll.
   */  
  public boolean getBuildIndex() {
    return true;
  }
  

  /**
   * Gibt das Verzeichnis zurück, in dem der stehen soll.
   *
   * @return Das Verzeichnis, in dem der Suchindex stehen soll.
   */  
  public String getIndexDir() {
    return "c:\\Temp\\searchIndex";
  }


  /**
   * Gibt den zu verwendenden Analyzer-Typ zurück.
   * 
   * @return en zu verwendenden Analyzer-Typ
   */
  public String getAnalyzerType() {
    return "german";
  }


  /**
   * Gibt alle Worte zurück, die nicht indiziert werden sollen.
   *
   * @return Alle Worte, die nicht indiziert werden sollen.
   */
  public String[] getStopWordList() {
    return null;
  }



  /**
   * Gibt alle Worte zurück, die bei der Indizierung nicht vom Analyzer
   * verändert werden sollen.
   *
   * @return Alle Worte, die bei der Indizierung nicht vom Analyzer
   *         verändert werden sollen.
   */
  public String[] getExclusionList() {
    return null;
  }



  /**
   * Gibt zurück, ob Analyse-Deteien geschrieben werden sollen.
   * <p>
   * Diese Dateien helfen, die Qualität der Index-Erstellung zu prüfen und
   * werden in einem Unterverzeichnis im Index-Verzeichnis angelegt.
   *
   * @return Ob Analyse-Deteien geschrieben werden sollen.
   */  
  public boolean getWriteAnalysisFiles() {
    return true;
  }

  
  /**
   * Gibt den maximalen Prozentsatz von gescheiterten Dokumenten zurück. (0..1)
   * <p>
   * Ist das Verhälnis von gescheiterten Dokumenten zur Gesamtzahl von
   * Dokumenten größer als dieser Prozentsatz, so wird der Index verworfen.
   * <p>
   * Gescheiterte Dokumente sind Dokumente die es entweder nicht gibt (Deadlink)
   * oder die nicht ausgelesen werden konnten. 
   * 
   * @return Den maximalen Prozentsatz von gescheiterten Dokumenten zurück.
   */
  public double getMaxFailedDocuments() {
    return 0.1;
  }


  /**
   * Gibt den Namen der Kontrolldatei für erfolgreiche Indexerstellung zurück.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, ohne dass
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zurückgegeben.
   * 
   * @return Der Name der Kontrolldatei für erfolgreiche Indexerstellung 
   */
  public String getFinishedWithoutFatalsFileName() {
    return null;
  }


  /**
   * Gibt den Namen der Kontrolldatei für fehlerhafte Indexerstellung zurück.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, wobei
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zurückgegeben.
   * 
   * @return Der Name der Kontrolldatei für fehlerhafte Indexerstellung 
   */
  public String getFinishedWithFatalsFileName() {
    return null;
  }


  /**
   * Gibt die StartUrls zurück, bei denen der Crawler-Prozeß beginnen soll.
   *
   * @return Die StartUrls.
   */  
  public StartUrl[] getStartUrls() {
    return new StartUrl[] {
      new StartUrl("http://www.dm-drogeriemarkt.de/CDA/Home/", true, true),
      /*
      new StartUrl("http://www.dm-drogeriemarkt.de/CDA/verteilerseite/0,2098,0-15-X,00.html", true, true),
      new StartUrl("http://www.dm-drogeriemarkt.de/CDA/verteilerseite/0,1651,0-16-X,00.html", true, true),
      new StartUrl("http://www.dm-drogeriemarkt.de/CDA/verteilerseite/0,1651,0-17-X,00.html", true, true),
      new StartUrl("http://www.dm-drogeriemarkt.de/CDA/verteilerseite/0,1651,0-18-X,00.html", true, true),
      new StartUrl("http://www.dm-drogeriemarkt.de/CDA/verteilerseite/0,1651,0-19-X,00.html", true, true),
      new StartUrl("http://www.dm-drogeriemarkt.de/CDA/verteilerseite/0,1651,0-173-X,00.html", true, true)
      */
    };
  }



  /**
   * Gibt die UrlPattern zurück, die der HTML-Parser nutzen soll, um URLs zu
   * identifizieren.
   *
   * @return Die UrlPattern für den HTML-Parser.
   */  
  public UrlPattern[] getHtmlParserUrlPatterns() {
    return new UrlPattern[] {
      new UrlPattern("=\"([^\"]*\\.html)\"", 1, true,  true),
      new UrlPattern("=\"([^\"]*\\.(pdf|xls|doc|rtf|ppt))\"",  1, false, true),
      new UrlPattern("=\"([^\"]*\\.(js|css|jpg|gif|png))\"",  1, false, false)
    };
  }



  /**
   * Gibt die UrlPattern zurück, die der Verzeichnis-Parser nutzt, um zu
   * entscheiden, ob und wie eine Datei bearbeitet werden soll.
   *
   * @return Die UrlPattern für den Verzeichnis-Parser.
   */  
  public UrlPattern[] getDirectoryParserUrlPatterns() {
    return new UrlPattern[] {
      new UrlPattern(".*\\.(html|pdf|xls|doc|rtf|ppt)", -1, false,  true)
    };
  }



  /**
   * Gibt die Schwarze Liste zurück.
   * <p>
   * Diese enthält Präfixe, die eine URL <I>nicht</I> haben darf, um bearbeitet
   * zu werden.
   *
   * @return Die Schwarze Liste.
   */  
  public String[] getUrlPrefixBlackList() {
    return new String[] {
      "http://www.dm-drogeriemarkt.de/CDA/Suchen/",
      "http://www.dm-drogeriemarkt.de/CDA/content/print/"
    };
  }



  /**
   * Gibt die Weiße Liste zurück.
   * <p>
   * Diese enthält Präfixe, von denen eine URL einen haben <i>muß</i>, um
   * bearbeitet zu werden.
   *
   * @return Die Weiße Liste
   */  
  public WhiteListEntry[] getWhiteList() {
    return new WhiteListEntry[] {
      // new WhiteListEntry("http://www.dm-drogeriemarkt.de/", "dm-main"),
      new WhiteListEntry("http://www.dm-drogeriemarkt.de/CDA/Home/", "dm-home"),
      new WhiteListEntry("http://www.dm-drogeriemarkt.de/CDA/images/", "dm-images"),
      new WhiteListEntry("http://www.dm-drogeriemarkt.de/CDA/verteilerseite/0,1651,", "dm-test1"),
      new WhiteListEntry("http://www.dm-drogeriemarkt.de/CDA/content/0,1647,0-171", "dm-test2"),
    };
  }

  
  
  /**
   * Gibt die HtmlContentExtractor zurück, die den zu inizierenden Teil aus
   * HTML-Dokumenten extrahieren.
   * <p>
   * Wenn keine Liste vorhanden ist, wird <code>null</code> zurückgegeben.
   *
   * @return Die HtmlContentExtractor zurück, die den zu inizierenden Teil aus
   *         HTML-Dokumenten extrahieren.
   */  
  public HtmlContentExtractor[] getHtmlContentExtractors() {
    return null;
  }



  /**
   * Gibt die HtmlPathExtractor zurück, die den Pfad aus HTML-Dokumenten
   * extrahieren.
   * <p>
   * Wenn keine Liste vorhanden ist, wird <code>null</code> zurückgegeben.
   *
   * @return Die HtmlPathExtractor.
   */  
  public HtmlPathExtractor[] getHtmlPathExtractors() {
    return null;
  }



  
  /**
   * Gibt die regulären Ausdrücke zurück, auf die die URL eines Dokuments passen
   * muss, damit anstatt des wirklichen Dokumententitels der Text des Links, der
   * auf das Dokument gezeigt hat, als Dokumententitel genutzt wird.
   *
   * @return Die regulären Ausdrücke, die Dokumente bestimmen, für die der
   *         Linktext als Titel genommen werden soll.
   */
  public String[] getUseLinkTextAsTitleRegexList() {
    return null;
  }



  /**
   * Gibt die Liste der Einstellungen für die Präperatoren zurück.
   * 
   * @return Die Liste der Einstellungen für die Präperatoren.
   */
  public PreparatorSettings[] getPreparatorSettingsList() {
    return new PreparatorSettings[] {
      new PreparatorSettings("\\.(/|html|htm)$", "net.sf.regain.crawler.document.HtmlPreparator"),
      new PreparatorSettings("\\.xml$", "net.sf.regain.crawler.document.XmlPreparator")
    };
  }
  
}
