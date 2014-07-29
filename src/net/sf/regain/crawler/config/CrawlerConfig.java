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

import java.util.Properties;

/**
 * Stellt alle zu konfigurierenden Einstellungen zur Verfï¿œgung.
 *
 * @author Til Schneider, www.murfman.de
 */
public interface CrawlerConfig {

  /**
   * Gibt den Host-Namen des Proxy-Servers zurï¿œck. Wenn kein Host konfiguriert
   * wurde, wird <CODE>null</CODE> zurï¿œckgegeben.
   *
   * @return Der Host-Namen des Proxy-Servers.
   */
  public String getProxyHost();

  /**
   * Gibt den Port des Proxy-Servers zurï¿œck. Wenn kein Port konfiguriert wurde,
   * wird <CODE>null</CODE> zurï¿œckgegeben.
   *
   * @return Der Port des Proxy-Servers.
   */
  public String getProxyPort();

  /**
   * Returns the maximum count of equal occurences of path-parts in an URI.
   *
   * @return MaxCycleCount
   */
  public int getMaxCycleCount();

  /**
   * Gibt den Benutzernamen fï¿œr die Anmeldung beim Proxy-Server zurï¿œck. Wenn
   * kein Benutzernamen konfiguriert wurde, wird <CODE>null</CODE> zurï¿œckgegeben.
   *
   * @return Der Benutzernamen fï¿œr die Anmeldung beim Proxy-Server.
   */
  public String getProxyUser();

  /**
   * Gibt das Passwort fï¿œr die Anmeldung beim Proxy-Server zurï¿œck. Wenn kein
   * Passwort konfiguriert wurde, wird <CODE>null</CODE> zurï¿œckgegeben.
   *
   * @return Das Passwort fï¿œr die Anmeldung beim Proxy-Server.
   */
  public String getProxyPassword();

  /**
   * Gibt den Timeout fï¿œr HTTP-Downloads zurï¿œck. Dieser Wert bestimmt die
   * maximale Zeit in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   *
   * @return Den Timeout fï¿œr HTTP-Downloads
   */
  public int getHttpTimeoutSecs();

  /**
   * Returns the user agent the crawler should in order to identify at the HTTP
   * server(s). If null, the default (Java) user agent should be used.
   * 
   * @return the user agent to use.
   */
  public String getUserAgent();

  /**
   * Gibt zurï¿œck, ob URLs geladen werden sollen, die weder durchsucht noch
   * indiziert werden.
   *
   * @return Ob URLs geladen werden sollen, die weder durchsucht noch indiziert
   *         werden.
   */
  public boolean getLoadUnparsedUrls();

  /**
   * Gibt zurï¿œck, ob ein Suchindex erstellt werden soll.
   *
   * @return Ob ein Suchindex erstellt werden soll.
   */
  public boolean getBuildIndex();

  /**
   * Gibt das Verzeichnis zurï¿œck, in dem der Suchindex stehen soll.
   *
   * @return Das Verzeichnis, in dem der Suchindex stehen soll.
   */
  public String getIndexDir();

  /**
   * Gibt den zu verwendenden Analyzer-Typ zurï¿œck.
   *
   * @return en zu verwendenden Analyzer-Typ
   */
  public String getAnalyzerType();

  /**
   * Returns the maximum number of terms that will be indexed for a single field
   * in a document.
   * <p>
   * Is <= 0 if lucene's default should be used.
   *
   * @return the maximum number of terms per document.
   */
  public int getMaxFieldLength();

  /**
   * Gibt alle Worte zurï¿œck, die nicht indiziert werden sollen.
   *
   * @return Alle Worte, die nicht indiziert werden sollen.
   */
  public String[] getStopWordList();

  /**
   * Gibt alle Worte zurï¿œck, die bei der Indizierung nicht vom Analyzer
   * verändert werden sollen.
   *
   * @return Alle Worte, die bei der Indizierung nicht vom Analyzer
   *         verändert werden sollen.
   */
  public String[] getExclusionList();

  /**
   * Gibt zurï¿œck, ob Analyse-Deteien geschrieben werden sollen.
   * <p>
   * Diese Dateien helfen, die Qualitï¿œt der Index-Erstellung zu prüfen und
   * werden in einem Unterverzeichnis im Index-Verzeichnis angelegt.
   *
   * @return Ob Analyse-Deteien geschrieben werden sollen.
   */
  public boolean getWriteAnalysisFiles();

  /**
   * Returns the interval between two breakpoint in minutes. If set to 0, no
   * breakpoints will be created.
   *
   * @return the interval between two breakpoint in minutes.
   */
  public int getBreakpointInterval();

  /**
   * Gibt den maximalen Prozentsatz von gescheiterten Dokumenten zurï¿œck. (0..1)
   * <p>
   * Ist das Verhï¿œlnis von gescheiterten Dokumenten zur Gesamtzahl von
   * Dokumenten grï¿œï¿œer als dieser Prozentsatz, so wird der Index verworfen.
   * <p>
   * Gescheiterte Dokumente sind Dokumente die es entweder nicht gibt (Deadlink)
   * oder die nicht ausgelesen werden konnten.
   *
   * @return Den maximalen Prozentsatz von gescheiterten Dokumenten zurï¿œck.
   */
  public double getMaxFailedDocuments();

  /**
   * Gibt den Namen der Kontrolldatei fï¿œr erfolgreiche Indexerstellung zurï¿œck.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, ohne dass
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zurï¿œckgegeben.
   *
   * @return Der Name der Kontrolldatei fï¿œr erfolgreiche Indexerstellung
   */
  public String getFinishedWithoutFatalsFileName();

  /**
   * Gibt den Namen der Kontrolldatei fï¿œr fehlerhafte Indexerstellung zurï¿œck.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, wobei
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zurï¿œckgegeben.
   *
   * @return Der Name der Kontrolldatei fï¿œr fehlerhafte Indexerstellung
   */
  public String getFinishedWithFatalsFileName();

  /**
   * Returns the flag for enabling/disabling the content-preview
   *
   * @return boolean true if content preview is enabled and the whole content should be
   * stored in the index
   */
  public boolean getStoreContentForPreview();

  /**
   * Gibt die StartUrls zurück, bei denen der Crawler-Prozeß beginnen soll.
   *
   * @return Die StartUrls.
   */
  public StartUrl[] getStartUrls();

  /**
   * Gibt die UrlPattern zurück, die der HTML-Parser nutzen soll, um URLs zu
   * identifizieren.
   *
   * @return Die UrlPattern für den HTML-Parser.
   */
  public UrlPattern[] getHtmlParserUrlPatterns();

  /**
   * Gets the black list.
   * <p>
   * The black list is an array of UrlMatchers, a URLs <i>must not</i> match to,
   * in order to be processed.
   * 
   * @return The black list.
   */
  public UrlMatcher[] getBlackList();

  /**
   * Gets the white list.
   * <p>
   * The black list is an array of WhiteListEntry, a URLs <i>must</i> match to,
   * in order to be processed.
   *
   * @return Die Weiï¿œe Liste
   */
  public WhiteListEntry[] getWhiteList();

  /**
   * The names of the fields to prefetch the destinct values for.
   * <p>
   * Used for speeding up the search:input_fieldlist tag.
   *
   * @return the names of the fields to prefetch the destinct values for.
   *         May be null or empty.
   */
  public String[] getValuePrefetchFields();

  /**
   * Gibt die regulï¿œren Ausdrï¿œcke zurï¿œck, auf die die URL eines Dokuments passen
   * muss, damit anstatt des wirklichen Dokumententitels der Text des Links, der
   * auf das Dokument gezeigt hat, als Dokumententitel genutzt wird.
   *
   * @return Die regulï¿œren Ausdrï¿œcke, die Dokumente bestimmen, fï¿œr die der
   *         Linktext als Titel genommen werden soll.
   */
  public String[] getUseLinkTextAsTitleRegexList();

  /**
   * Gets the list with the preparator settings.
   *
   * @return The list with the preparator settings.
   */
  public PreparatorSettings[] getPreparatorSettingsList();
  
  /**
   * Gets the list with the crawler plugin settings.
   *
   * @return The list with the crawler plugin settings.
   */
  public PreparatorSettings[] getCrawlerPluginSettingsList();
  
  /**
   * Gets the list of the auxiliary fields.
   * 
   * @return The list of the auxiliary fields. May be null.
   */
  public AuxiliaryField[] getAuxiliaryFieldList();
  
  /**
   * Gets the class name of the
   * {@link net.sf.regain.crawler.access.CrawlerAccessController} to use.
   * Returns <code>null</code> if no CrawlerAccessController should be used.
   * 
   * @return The class name of the CrawlerAccessController. 
   */
  public String getCrawlerAccessControllerClass();
  
  /**
   * Gets the name of jar file to load the
   * {@link net.sf.regain.crawler.access.CrawlerAccessController} from.
   * Returns <code>null</code> if the CrawlerAccessController already is in the
   * classpath.
   * 
   * @return The name of jar file to load the CrawlerAccessController from. 
   */
  public String getCrawlerAccessControllerJar();

  /**
   * Gets the configuration of the
   * {@link net.sf.regain.crawler.access.CrawlerAccessController}. May be
   * <code>null</code>.
   * 
   * @return The the configuration of the CrawlerAccessController. 
   */
  public Properties getCrawlerAccessControllerConfig();
  
  /**
   * Returns maximum amount of characters which will be copied from content to summary
   *
   * @return MaxSummaryLength
   */
  public int getMaxSummaryLength();

   /**
   * Returns the names of the fields that shouldn't be tokenized.
   * 
   * @param config The crawler configuration.
   * @return The names of the fields that shouldn't be tokenized.
   */
  public String[] getUntokenizedFieldNames();
  
  /**
   * Returns the URLCleaners. URLCleaners are regex which replace
   * parts of the URL with an empty string (in fact the remove the match
   * from the URL.
   * 
   * @return the paramters
   */
  public String[] getURLCleaners();

}

