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
 *  $RCSfile: XmlConfiguration.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/XmlConfiguration.java,v $
 *     $Date: 2004/07/28 20:26:03 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.config;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.preparator.html.HtmlContentExtractor;
import net.sf.regain.crawler.preparator.html.HtmlPathExtractor;

import org.w3c.dom.*;


/**
 * Liest die konfigurierenden Einstellungen aus einer XML-Datei und stellt sie
 * zur Verf�gung.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class XmlConfiguration implements Configuration {

  /** Der Host-Namen des Proxy-Servers. */  
  private String mProxyHost;
  /** Der Port des Proxy-Servers. */  
  private String mProxyPort;
  /** Der Benutzernamen f�r die Anmeldung beim Proxy-Server. */  
  private String mProxyUser;
  /** Das Passwort f�r die Anmeldung beim Proxy-Server. */  
  private String mProxyPassword;
  /**
   * Gibt an, ob URLs geladen werden sollen, die weder durchsucht noch indiziert
   * werden.
   */
  private boolean mLoadUnparsedUrls;
  /** Gibt an, ob ein Suchindex erstellt werden soll. */  
  private boolean mBuildIndex;
  /**
   * Der Timeout f�r HTTP-Downloads. Dieser Wert bestimmt die maximale Zeit
   * in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   */
  private int mHttpTimeoutSecs;
  /** Das Verzeichnis, in dem der Suchindex stehen soll. */  
  private String mIndexDir;
  /** Der zu verwendende Analyzer-Typ. */
  private String mAnalyzerType;
  
  /** Enth�lt alle Worte, die nicht indiziert werden sollen. */
  private String[] mStopWordList;
  /**
   * Enth�lt alle Worte, die bei der Indizierung nicht vom Analyzer ver�ndert
   * werden sollen.
   */
  private String[] mExclusionList;
  
  /** Gibt an, ob Analyse-Deteien geschrieben werden sollen. */  
  private boolean mWriteAnalysisFiles;
  /**
   * Der maximale Prozentsatz von gescheiterten Dokumenten (0..100), der f�r
   * die Freigabe eines Index toleriert wird.
   */
  private double mMaxFailedDocuments;

  /** Der Nam der Kontrolldatei f�r erfolgreiche Indexerstellung. */
  private String mFinishedWithoutFatalsFileName;
  /** Der Name der Kontrolldatei f�r fehlerhafte Indexerstellung. */
  private String mFinishedWithFatalsFileName;
    
  /** Die StartUrls. */  
  private StartUrl[] mStartUrls;

  /** Die UrlPattern, die der HTML-Parser nutzen soll, um URLs zu identifizieren. */
  private UrlPattern[] mHtmlParserUrlPatterns;
  /**
   * Die UrlPattern, die der Verzeichnis-Parser nutzt, um zu entscheiden, ob und
   * wie eine Datei bearbeitet werden soll.
   */
  private UrlPattern[] mDirectoryParserUrlPatterns;
  
  /** Die Schwarze Liste. */  
  private String[] mUrlPrefixBlackList;
  /** Die Wei�e Liste */  
  private WhiteListEntry[] mWhiteListEntryArr;  
  /**
   * Die HtmlContentExtractor, die den jeweiligen zu indizierenden Inhalt aus
   * den HTML-Dokumenten schneiden.
   */
  private HtmlContentExtractor[] mHtmlContentExtractorArr;
  /**
   * Die HtmlPathExtractor zur�ck, die den Pfad aus HTML-Dokumenten extrahieren.
   */
  private HtmlPathExtractor[] mHtmlPathExtractorArr;
  
  /**
   * Die regul�ren Ausdr�cke, auf die die URL eines Dokuments passen muss,
   * damit anstatt des wirklichen Dokumententitels der Text des Links, der auf
   * das Dokument gezeigt hat, als Dokumententitel genutzt wird.
   */
  private String[] mUseLinkTextAsTitleRegexList;
  
  /** Die Liste der Einstellungen f�r die Pr�peratoren. */
  private PreparatorSettings[] mPreparatorSettingsArr;
  



  /**
   * Erzeugt eine neue XmlConfiguration-Instanz.
   *
   * @param xmlFile Die XML-Datei, aus der die Konfiguration gelesen werden soll.
   *
   * @throws RegainException Falls die Konfiguration nicht korrekt gelesen werden
   *         konnte.
   */
  public XmlConfiguration(File xmlFile) throws RegainException {
    Document doc = loadXmlDocument(xmlFile);

    Element config = doc.getDocumentElement();

    readProxyConfig(config);
    readLoadUnparsedUrls(config);
    readHttpTimeoutSecs(config);
    readIndexConfig(config);
    readControlFileConfig(config);
    readHtmlContentExtractorList(config);
    readHtmlPathExtractorList(config);
    readStartUrls(config);
    readHtmlParserUrlPatterns(config);
    readDirectoryParserUrlPatterns(config);
    readBlackList(config);
    readWhiteList(config);
    readUseLinkTextAsTitleRegexList(config);
    readPreparatorSettingsList(config);
  }



  /**
   * L�dt eine XML-Datei und gibt ihren Inhalt als Document zur�ck.
   * 
   * @param xmlFile Die zu ladende XML-Datei.
   * @return Das XML-Dokument der Datei
   * @throws RegainException Wenn der Inhalt der Datei nicht korrekt
   *         geparst werden konnte.
   */
  private Document loadXmlDocument(File xmlFile) throws RegainException {
    DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (Exception exc) {
      throw new RegainException("Creating XML document builder failed!", exc);
    }

    Document doc;
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(xmlFile);
      doc = builder.parse(stream);
    }
    catch (Exception exc) {
      throw new RegainException("Parsing XML failed: "
                                + xmlFile.getAbsolutePath(), exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
    
    return doc;
  }



  /**
   * Liest aus der Konfiguration, ob Dokumente geladen werden sollen, die weder
   * indiziert, noch auf URLs durchsucht werden.
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readLoadUnparsedUrls(Element config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "loadUnparsedUrls");
    mLoadUnparsedUrls = XmlToolkit.getTextAsBoolean(node);
  }

  
  /**
   * Liest den Timeout f�r HTTP-Downloads aus der Konfiguration.
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readHttpTimeoutSecs(Element config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "httpTimeout");
    mHttpTimeoutSecs = XmlToolkit.getTextAsInt(node);
  }
  

  /**
   * Liest die Proxy-Einstellungen aus der Konfiguration. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readProxyConfig(Node config) throws RegainException {
    Node node;
    
    Node proxyNode = XmlToolkit.getChild(config, "proxy", false);
    if (proxyNode != null) {
      node = XmlToolkit.getChild(proxyNode, "host", false);
      if (node != null) {
        mProxyHost = XmlToolkit.getText(node);
      }
      node = XmlToolkit.getChild(proxyNode, "port", false);
      if (node != null) {
        mProxyPort = XmlToolkit.getText(node);
      }
      node = XmlToolkit.getChild(proxyNode, "user", false);
      if (node != null) {
        mProxyUser = XmlToolkit.getText(node);
      }
      node = XmlToolkit.getChild(proxyNode, "password", false);
      if (node != null) {
        mProxyPassword = XmlToolkit.getText(node);
      }
    }
  }



  /**
   * Liest die Einstellungen aus der Konfiguration, die den Suchindex betreffen. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readIndexConfig(Node config) throws RegainException {
    Node node;
    
    Node indexNode = XmlToolkit.getChild(config, "searchIndex");
    
    node = XmlToolkit.getChild(indexNode, "dir");
    mIndexDir = XmlToolkit.getText(node);
    node = XmlToolkit.getChild(indexNode, "buildIndex");
    mBuildIndex = XmlToolkit.getTextAsBoolean(node);
    node = XmlToolkit.getChild(indexNode, "analyzerType");
    mAnalyzerType = XmlToolkit.getText(node);
    node = XmlToolkit.getChild(indexNode, "stopwordList");
    mStopWordList = XmlToolkit.getTextAsWordList(node, false);
    node = XmlToolkit.getChild(indexNode, "exclusionList");
    mExclusionList = XmlToolkit.getTextAsWordList(node, false);
    node = XmlToolkit.getChild(indexNode, "writeAnalysisFiles");
    mWriteAnalysisFiles = XmlToolkit.getTextAsBoolean(node);
    node = XmlToolkit.getChild(indexNode, "maxFailedDocuments");
    mMaxFailedDocuments = XmlToolkit.getTextAsDouble(node) / 100.0;
  }


  /**
   * Liest die Namen der Kontrolldateien aus der Konfiguration.
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readControlFileConfig(Node config) throws RegainException {
    Node node;
    
    Node ctrNode = XmlToolkit.getChild(config, "controlFiles", false);
    if (ctrNode != null) {
      node = XmlToolkit.getChild(ctrNode, "finishedWithoutFatalsFile", false);
      if (node != null) {
        mFinishedWithoutFatalsFileName = XmlToolkit.getText(node).trim();
      }

      node = XmlToolkit.getChild(ctrNode, "finishedWithFatalsFile", false);
      if (node != null) {
        mFinishedWithFatalsFileName = XmlToolkit.getText(node).trim();
      }
    }
  }


  /**
   * Liest die Liste der HTML-Inhalt-Extraktoren aus der Konfiguration.
   * <p>
   * Diese dienen dazu, den zu indizierenden Inhalt aus einem HTML-Dokument zu
   * extrahieren. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readHtmlContentExtractorList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "htmlContentExtractorList");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "contentExtractor");
    mHtmlContentExtractorArr = new HtmlContentExtractor[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      node = XmlToolkit.getChild(nodeArr[i], "prefix");
      String prefix = XmlToolkit.getTextAsUrl(node);
      
      String contentStartRegex = null;
      node = XmlToolkit.getChild(nodeArr[i], "startRegex", false);
      if (node != null) {
        contentStartRegex = XmlToolkit.getText(node);
      }
      
      String contentEndRegex = null;
      node = XmlToolkit.getChild(nodeArr[i], "endRegex", false);
      if (node != null) {
        contentEndRegex = XmlToolkit.getText(node);
      }
      
      String headlineRegex = null;
      int headlineRegexGroup = -1;
      node = XmlToolkit.getChild(nodeArr[i], "headlineRegex", false);
      if (node != null) {
        headlineRegex = XmlToolkit.getText(node);
        headlineRegexGroup = XmlToolkit.getAttributeAsInt(node, "regexGroup");
      }
      
      mHtmlContentExtractorArr[i] = new HtmlContentExtractor(prefix,  
        contentStartRegex, contentEndRegex, headlineRegex, headlineRegexGroup);
    }
  }



  /**
   * Liest die Liste der HTML-Pfad-Extraktoren aus der Konfiguration.
   * <p>
   * Diese dienen dazu, den den Pfad aus HTML-Dokumenten extrahieren, �ber den
   * das Dokument zu erreichen ist.
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readHtmlPathExtractorList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "htmlPathExtractorList");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "pathExtractor");
    mHtmlPathExtractorArr = new HtmlPathExtractor[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      node = XmlToolkit.getChild(nodeArr[i], "prefix");
      String prefix = XmlToolkit.getTextAsUrl(node);
      
      String pathStartRegex = null;
      node = XmlToolkit.getChild(nodeArr[i], "startRegex", false);
      if (node != null) {
        pathStartRegex = XmlToolkit.getText(node);
      }

      String pathEndRegex = null;
      node = XmlToolkit.getChild(nodeArr[i], "endRegex", false);
      if (node != null) {
        pathEndRegex = XmlToolkit.getText(node);
      }
      
      node = XmlToolkit.getChild(nodeArr[i], "pathNodeRegex");
      String pathElementRegex = XmlToolkit.getText(node);
      int pathElementUrlGroup = XmlToolkit.getAttributeAsInt(node, "urlRegexGroup");
      int pathElementTitleGroup = XmlToolkit.getAttributeAsInt(node, "titleRegexGroup");
      
      mHtmlPathExtractorArr[i] = new HtmlPathExtractor(prefix,
        pathStartRegex, pathEndRegex, pathElementRegex,
        pathElementUrlGroup, pathElementTitleGroup);
    }
  }



  /**
   * Liest die Start-URLs aus der Konfiguration. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readStartUrls(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "startlist");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "start");
    mStartUrls = new StartUrl[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      String url = XmlToolkit.getTextAsUrl(nodeArr[i]);
      boolean parse = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "parse");
      boolean index = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "index");

      mStartUrls[i] = new StartUrl(url, parse, index);
    }
  }



  /**
   * Liest die URL-Patterns f�r den HTML-Parser aus der Konfiguration.
   * <p>
   * Diese werden beim durchsuchen eines HTML-Dokuments dazu verwendet, URLs
   * zu identifizieren. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readHtmlParserUrlPatterns(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "htmlParserPatternList");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "pattern");
    mHtmlParserUrlPatterns = new UrlPattern[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      String regexPattern = XmlToolkit.getText(nodeArr[i]);
      int regexGroup = XmlToolkit.getAttributeAsInt(nodeArr[i], "regexGroup");
      boolean parse = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "parse");
      boolean index = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "index");

      mHtmlParserUrlPatterns[i] = new UrlPattern(regexPattern, regexGroup,
        parse, index);
    }
  }



  /**
   * Liest die URL-Patterns f�r den Verzeichnis-Parser aus der Konfiguration.
   * <p>
   * Diese werden beim durchsuchen eines Verzeichnisses dazu verwendet, zu
   * entscheiden, ob ein Dokument indiziert werden soll.  
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readDirectoryParserUrlPatterns(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "directoryParserPatternList");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "pattern");
    mDirectoryParserUrlPatterns = new UrlPattern[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      String regexPattern = XmlToolkit.getText(nodeArr[i]);
      int regexUrlGroup = -1;
      boolean shouldBeParsed = false;
      boolean shouldBeIndexed = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "index");

      mDirectoryParserUrlPatterns[i] = new UrlPattern(regexPattern, regexUrlGroup,
        shouldBeParsed, shouldBeIndexed);
    }
  }



  /**
   * Liest die Schwarze Liste aus der Konfiguration.
   * <p>
   * Dokumente, deren URL mit einem Pr�fix aus der Schwarzen Liste beginnen,
   * werden nicht bearbeitet. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readBlackList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "blacklist");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "prefix");
    mUrlPrefixBlackList = new String[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      mUrlPrefixBlackList[i] = XmlToolkit.getText(nodeArr[i]);
    }
  }



  /**
   * Liest die Wei�e Liste aus der Konfiguration.
   * <p>
   * Dokumente werden nur dann bearbeitet, wenn deren URL mit einem Pr�fix aus
   * der Wei�en Liste beginnt. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readWhiteList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "whitelist");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "prefix");
    mWhiteListEntryArr = new WhiteListEntry[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      String prefix = XmlToolkit.getText(nodeArr[i]);
      String name = XmlToolkit.getAttribute(nodeArr[i], "name", false);
      mWhiteListEntryArr[i] = new WhiteListEntry(prefix, name);
    }
  }



  /**
   * Liest die Liste der regul�ren Ausdr�cke aus der Konfiguration, auf die die
   * URL eines Dokuments passen muss, damit anstatt des wirklichen
   * Dokumententitels der Text des Links, der auf das Dokument gezeigt hat, als
   * Dokumententitel genutzt wird. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readUseLinkTextAsTitleRegexList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "useLinkTextAsTitleList");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "urlPattern");
    mUseLinkTextAsTitleRegexList = new String[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      mUseLinkTextAsTitleRegexList[i] = XmlToolkit.getText(nodeArr[i]);
    }
  }



  /**
   * Liest die Liste der Einstellungen f�r die Pr�peratoren. 
   * 
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readPreparatorSettingsList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "preparatorList");
    Node[] nodeArr = XmlToolkit.getChildArr(node, "preparator");
    mPreparatorSettingsArr = new PreparatorSettings[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      node = XmlToolkit.getChild(nodeArr[i], "urlPattern");
      String urlRegex = XmlToolkit.getText(node);

      node = XmlToolkit.getChild(nodeArr[i], "class");
      String className = XmlToolkit.getText(node);
      
      mPreparatorSettingsArr[i] = new PreparatorSettings(urlRegex, className);
    }
  }



  /**
   * Gibt den Host-Namen des Proxy-Servers zur�ck. Wenn kein Host konfiguriert
   * wurde, wird <CODE>null</CODE> zur�ckgegeben.
   *
   * @return Der Host-Namen des Proxy-Servers.
   */  
  public String getProxyHost() {
    return mProxyHost;
  }



  /**
   * Gibt den Port des Proxy-Servers zur�ck. Wenn kein Port konfiguriert wurde,
   * wird <CODE>null</CODE> zur�ckgegeben.
   *
   * @return Der Port des Proxy-Servers.
   */  
  public String getProxyPort() {
    return mProxyPort;
  }



  /**
   * Gibt den Benutzernamen f�r die Anmeldung beim Proxy-Server zur�ck. Wenn
   * kein Benutzernamen konfiguriert wurde, wird <CODE>null</CODE> zur�ckgegeben.
   *
   * @return Der Benutzernamen f�r die Anmeldung beim Proxy-Server.
   */  
  public String getProxyUser() {
    return mProxyUser;
  }



  /**
   * Gibt das Passwort f�r die Anmeldung beim Proxy-Server zur�ck. Wenn kein
   * Passwort konfiguriert wurde, wird <CODE>null</CODE> zur�ckgegeben.
   *
   * @return Das Passwort f�r die Anmeldung beim Proxy-Server.
   */  
  public String getProxyPassword() {
    return mProxyPassword;
  }


  /**
   * Gibt den Timeout f�r HTTP-Downloads zur�ck. Dieser Wert bestimmt die
   * maximale Zeit in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   * 
   * @return Den Timeout f�r HTTP-Downloads
   */
  public int getHttpTimeoutSecs() {
    return mHttpTimeoutSecs;
  }
  

  /**
   * Gibt zur�ck, ob URLs geladen werden sollen, die weder durchsucht noch
   * indiziert werden.
   *
   * @return Ob URLs geladen werden sollen, die weder durchsucht noch indiziert
   *         werden.
   */  
  public boolean getLoadUnparsedUrls() {
    return mLoadUnparsedUrls;
  }



  /**
   * Gibt zur�ck, ob ein Suchindex erstellt werden soll.
   *
   * @return Ob ein Suchindex erstellt werden soll.
   */  
  public boolean getBuildIndex() {
    return mBuildIndex;
  }
    

  /**
   * Gibt das Verzeichnis zur�ck, in dem der Suchindex am Ende stehen soll.
   *
   * @return Das Verzeichnis, in dem der Suchindex am Ende stehen soll.
   */  
  public String getIndexDir() {
    return mIndexDir;
  }


  /**
   * Gibt den zu verwendenden Analyzer-Typ zur�ck.
   * 
   * @return en zu verwendenden Analyzer-Typ
   */
  public String getAnalyzerType() {
    return mAnalyzerType;
  }


  /**
   * Gibt alle Worte zur�ck, die nicht indiziert werden sollen.
   *
   * @return Alle Worte, die nicht indiziert werden sollen.
   */
  public String[] getStopWordList() {
    return mStopWordList;
  }



  /**
   * Gibt alle Worte zur�ck, die bei der Indizierung nicht vom Analyzer
   * ver�ndert werden sollen.
   *
   * @return Alle Worte, die bei der Indizierung nicht vom Analyzer
   *         ver�ndert werden sollen.
   */
  public String[] getExclusionList() {
    return mExclusionList;
  }



  /**
   * Gibt zur�ck, ob Analyse-Deteien geschrieben werden sollen.
   * <p>
   * Diese Dateien helfen, die Qualit�t der Index-Erstellung zu pr�fen und
   * werden in einem Unterverzeichnis im Index-Verzeichnis angelegt.
   *
   * @return Ob Analyse-Deteien geschrieben werden sollen.
   */  
  public boolean getWriteAnalysisFiles() {
    return mWriteAnalysisFiles;
  }


  /**
   * Gibt den maximalen Prozentsatz von gescheiterten Dokumenten zur�ck. (0..1)
   * <p>
   * Ist das Verh�lnis von gescheiterten Dokumenten zur Gesamtzahl von
   * Dokumenten gr��er als dieser Prozentsatz, so wird der Index verworfen.
   * <p>
   * Gescheiterte Dokumente sind Dokumente die es entweder nicht gibt (Deadlink)
   * oder die nicht ausgelesen werden konnten. 
   * 
   * @return Den maximalen Prozentsatz von gescheiterten Dokumenten zur�ck.
   */
  public double getMaxFailedDocuments() {
    return mMaxFailedDocuments;
  }

  
  /**
   * Gibt den Namen der Kontrolldatei f�r erfolgreiche Indexerstellung zur�ck.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, ohne dass
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zur�ckgegeben.
   * 
   * @return Der Name der Kontrolldatei f�r erfolgreiche Indexerstellung 
   */
  public String getFinishedWithoutFatalsFileName() {
    return mFinishedWithoutFatalsFileName;
  }


  /**
   * Gibt den Namen der Kontrolldatei f�r fehlerhafte Indexerstellung zur�ck.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, wobei
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zur�ckgegeben.
   * 
   * @return Der Name der Kontrolldatei f�r fehlerhafte Indexerstellung 
   */
  public String getFinishedWithFatalsFileName() {
    return mFinishedWithFatalsFileName;
  }
  

  /**
   * Gibt die StartUrls zur�ck, bei denen der Crawler-Proze� beginnen soll.
   *
   * @return Die StartUrls.
   */  
  public StartUrl[] getStartUrls() {
    return mStartUrls;
  }



  /**
   * Gibt die UrlPattern zur�ck, die der HTML-Parser nutzen soll, um URLs zu
   * identifizieren.
   *
   * @return Die UrlPattern f�r den HTML-Parser.
   */  
  public UrlPattern[] getHtmlParserUrlPatterns() {
    return mHtmlParserUrlPatterns;
  }



  /**
   * Gibt die UrlPattern zur�ck, die der Verzeichnis-Parser nutzt, um zu
   * entscheiden, ob und wie eine Datei bearbeitet werden soll.
   *
   * @return Die UrlPattern f�r den Verzeichnis-Parser.
   */  
  public UrlPattern[] getDirectoryParserUrlPatterns() {
    return mDirectoryParserUrlPatterns;
  }



  /**
   * Gibt die Schwarze Liste zur�ck.
   * <p>
   * Diese enth�lt Pr�fixe, die eine URL <I>nicht</I> haben darf, um bearbeitet
   * zu werden.
   *
   * @return Die Schwarze Liste.
   */  
  public String[] getUrlPrefixBlackList() {
    return mUrlPrefixBlackList;
  }



  /**
   * Gibt die Wei�e Liste zur�ck.
   * <p>
   * Diese enth�lt Pr�fixe, von denen eine URL einen haben <i>mu�</i>, um
   * bearbeitet zu werden.
   *
   * @return Die Wei�e Liste
   */  
  public WhiteListEntry[] getWhiteList() {
    return mWhiteListEntryArr;
  }



  /**
   * Gibt die HtmlContentExtractor zur�ck, die den zu inizierenden Teil aus
   * HTML-Dokumenten extrahieren.
   * <p>
   * Wenn keine Liste vorhanden ist, wird <code>null</code> zur�ckgegeben.
   *
   * @return Die HtmlContentExtractor zur�ck, die den zu inizierenden Teil aus
   *         HTML-Dokumenten extrahieren.
   */  
  public HtmlContentExtractor[] getHtmlContentExtractors() {
    return mHtmlContentExtractorArr;
  }



  /**
   * Gibt die HtmlPathExtractor zur�ck, die den Pfad aus HTML-Dokumenten
   * extrahieren.
   * <p>
   * Wenn keine Liste vorhanden ist, wird <code>null</code> zur�ckgegeben.
   *
   * @return Die HtmlPathExtractor.
   */  
  public HtmlPathExtractor[] getHtmlPathExtractors() {
    return mHtmlPathExtractorArr;
  }



  /**
   * Gibt die regul�ren Ausdr�cke zur�ck, auf die die URL eines Dokuments passen
   * muss, damit anstatt des wirklichen Dokumententitels der Text des Links, der
   * auf das Dokument gezeigt hat, als Dokumententitel genutzt wird.
   *
   * @return Die regul�ren Ausdr�cke, die Dokumente bestimmen, f�r die der
   *         Linktext als Titel genommen werden soll.
   */
  public String[] getUseLinkTextAsTitleRegexList() {
    return mUseLinkTextAsTitleRegexList;
  }



  /**
   * Gibt die Liste der Einstellungen f�r die Pr�peratoren zur�ck.
   * 
   * @return Die Liste der Einstellungen f�r die Pr�peratoren.
   */
  public PreparatorSettings[] getPreparatorSettingsList() {
    return mPreparatorSettingsArr;
  }

}
