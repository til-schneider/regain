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
 *  $RCSfile: XmlCrawlerConfig.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/XmlCrawlerConfig.java,v $
 *     $Date: 2005/03/30 10:30:03 $
 *   $Author: til132 $
 * $Revision: 1.6 $
 */
package net.sf.regain.crawler.config;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import net.sf.regain.RegainException;
import net.sf.regain.XmlToolkit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Liest die konfigurierenden Einstellungen aus einer XML-Datei und stellt sie
 * zur Verf�gung.
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlCrawlerConfig implements CrawlerConfig {

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

  /** Die Schwarze Liste. */
  private String[] mUrlPrefixBlackList;
  /** Die Wei�e Liste */
  private WhiteListEntry[] mWhiteListEntryArr;

  /**
   * Die regul�ren Ausdr�cke, auf die die URL eines Dokuments passen muss,
   * damit anstatt des wirklichen Dokumententitels der Text des Links, der auf
   * das Dokument gezeigt hat, als Dokumententitel genutzt wird.
   */
  private String[] mUseLinkTextAsTitleRegexList;

  /** The list with the preparator settings. */
  private PreparatorSettings[] mPreparatorSettingsArr;

  /** The list of the auxiliary fields. May be null. */
  private AuxiliaryField[] mAuxiliaryFieldArr;

  /** The class name of the CrawlerAccessController to use. */
  private String mCrawlerAccessControllerClass;
  /** The name of jar file to load the CrawlerAccessController from. */
  private String mCrawlerAccessControllerJar;
  /** The configuration of the CrawlerAccessController. */
  private Properties mCrawlerAccessControllerConfig;


  /**
   * Erzeugt eine neue XmlConfiguration-Instanz.
   *
   * @param xmlFile Die XML-Datei, aus der die Konfiguration gelesen werden soll.
   *
   * @throws RegainException Falls die Konfiguration nicht korrekt gelesen werden
   *         konnte.
   */
  public XmlCrawlerConfig(File xmlFile) throws RegainException {
    Document doc = XmlToolkit.loadXmlDocument(xmlFile);
    Element config = doc.getDocumentElement();

    readProxyConfig(config);
    readLoadUnparsedUrls(config);
    readHttpTimeoutSecs(config);
    readIndexConfig(config);
    readControlFileConfig(config);
    readStartUrls(config);
    readHtmlParserUrlPatterns(config);
    readBlackList(config);
    readWhiteList(config);
    readUseLinkTextAsTitleRegexList(config);
    readPreparatorSettingsList(config, xmlFile);
    readAuxiliaryFieldList(config);
    readCrawlerAccessController(config);
  }


  /**
   * Liest aus der Konfiguration, ob Dokumente geladen werden sollen, die weder
   * indiziert, noch auf URLs durchsucht werden.
   *
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readLoadUnparsedUrls(Element config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "loadUnparsedUrls", true);
    mLoadUnparsedUrls = XmlToolkit.getTextAsBoolean(node);
  }


  /**
   * Liest den Timeout f�r HTTP-Downloads aus der Konfiguration.
   *
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readHttpTimeoutSecs(Element config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "httpTimeout", true);
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

    Node proxyNode = XmlToolkit.getChild(config, "proxy");
    if (proxyNode != null) {
      node = XmlToolkit.getChild(proxyNode, "host");
      if (node != null) {
        mProxyHost = XmlToolkit.getText(node, true);
      }
      node = XmlToolkit.getChild(proxyNode, "port");
      if (node != null) {
        mProxyPort = XmlToolkit.getText(node, true);
      }
      node = XmlToolkit.getChild(proxyNode, "user");
      if (node != null) {
        mProxyUser = XmlToolkit.getText(node, true);
      }
      node = XmlToolkit.getChild(proxyNode, "password");
      if (node != null) {
        mProxyPassword = XmlToolkit.getText(node, true);
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

    Node indexNode = XmlToolkit.getChild(config, "searchIndex", true);

    node = XmlToolkit.getChild(indexNode, "dir", true);
    mIndexDir = XmlToolkit.getText(node, true);
    node = XmlToolkit.getChild(indexNode, "buildIndex", true);
    mBuildIndex = XmlToolkit.getTextAsBoolean(node);
    node = XmlToolkit.getChild(indexNode, "analyzerType", true);
    mAnalyzerType = XmlToolkit.getText(node, true);
    node = XmlToolkit.getChild(indexNode, "stopwordList", true);
    mStopWordList = XmlToolkit.getTextAsWordList(node, true);
    node = XmlToolkit.getChild(indexNode, "exclusionList", true);
    mExclusionList = XmlToolkit.getTextAsWordList(node, false);
    node = XmlToolkit.getChild(indexNode, "writeAnalysisFiles", true);
    mWriteAnalysisFiles = XmlToolkit.getTextAsBoolean(node);
    node = XmlToolkit.getChild(indexNode, "maxFailedDocuments", true);
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

    Node ctrNode = XmlToolkit.getChild(config, "controlFiles");
    if (ctrNode != null) {
      node = XmlToolkit.getChild(ctrNode, "finishedWithoutFatalsFile");
      if (node != null) {
        mFinishedWithoutFatalsFileName = XmlToolkit.getText(node, true).trim();
      }

      node = XmlToolkit.getChild(ctrNode, "finishedWithFatalsFile");
      if (node != null) {
        mFinishedWithFatalsFileName = XmlToolkit.getText(node, true).trim();
      }
    }
  }


  /**
   * Liest die Start-URLs aus der Konfiguration.
   *
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readStartUrls(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "startlist", true);
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
    Node node = XmlToolkit.getChild(config, "htmlParserPatternList", true);
    Node[] nodeArr = XmlToolkit.getChildArr(node, "pattern");
    mHtmlParserUrlPatterns = new UrlPattern[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      String regexPattern = XmlToolkit.getText(nodeArr[i], true);
      int regexGroup = XmlToolkit.getAttributeAsInt(nodeArr[i], "regexGroup");
      boolean parse = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "parse");
      boolean index = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "index");

      mHtmlParserUrlPatterns[i] = new UrlPattern(regexPattern, regexGroup,
        parse, index);
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
    Node node = XmlToolkit.getChild(config, "blacklist", true);
    Node[] nodeArr = XmlToolkit.getChildArr(node, "prefix");
    mUrlPrefixBlackList = new String[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      mUrlPrefixBlackList[i] = XmlToolkit.getText(nodeArr[i], true);
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
    Node node = XmlToolkit.getChild(config, "whitelist", true);
    Node[] nodeArr = XmlToolkit.getChildArr(node, "prefix");
    mWhiteListEntryArr = new WhiteListEntry[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      String prefix = XmlToolkit.getText(nodeArr[i], true);
      String name = XmlToolkit.getAttribute(nodeArr[i], "name");
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
    Node node = XmlToolkit.getChild(config, "useLinkTextAsTitleList", true);
    Node[] nodeArr = XmlToolkit.getChildArr(node, "urlPattern");
    mUseLinkTextAsTitleRegexList = new String[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      mUseLinkTextAsTitleRegexList[i] = XmlToolkit.getText(nodeArr[i], true);
    }
  }



  /**
   * Reads the list of preparator settings.
   *
   * @param config The configuration to read from
   * @param xmlFile The file the configuration was read from.
   * @throws RegainException If the configuration has errors.
   */
  private void readPreparatorSettingsList(Node config, File xmlFile)
    throws RegainException
  {
    Node node = XmlToolkit.getChild(config, "preparatorList", true);
    Node[] nodeArr = XmlToolkit.getChildArr(node, "preparator");
    mPreparatorSettingsArr = new PreparatorSettings[nodeArr.length];
    for (int i = 0; i < nodeArr.length; i++) {
      boolean enabled = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "enabled", true);
      
      node = XmlToolkit.getChild(nodeArr[i], "class", true);
      String className = XmlToolkit.getText(node, true);

      node = XmlToolkit.getChild(nodeArr[i], "config");
      PreparatorConfig prepConfig = null;
      if (node != null) {
        prepConfig = readPreparatorConfig(node, xmlFile);
      }
      
      mPreparatorSettingsArr[i] = new PreparatorSettings(enabled, className, prepConfig);
    }
  }


  /**
   * Reads the list of auxiliary fields.
   *
   * @param config The configuration to read from
   * @throws RegainException If the configuration has errors.
   */
  private void readAuxiliaryFieldList(Node config)
    throws RegainException
  {
    Node node = XmlToolkit.getChild(config, "auxiliaryFieldList");
    if (node != null) {
      Node[] nodeArr = XmlToolkit.getChildArr(node, "auxiliaryField");
      mAuxiliaryFieldArr = new AuxiliaryField[nodeArr.length];
      for (int i = 0; i < nodeArr.length; i++) {
        String fieldName = XmlToolkit.getAttribute(nodeArr[i], "name", true);
        String urlRegex = XmlToolkit.getText(nodeArr[i], true);
        int urlRegexGroup = XmlToolkit.getAttributeAsInt(nodeArr[i], "regexGroup");
        
        mAuxiliaryFieldArr[i] = new AuxiliaryField(fieldName, urlRegex, urlRegexGroup);
      }
    }
  }
  
  
  /**
   * Reads the configuration of a preparator from a node.
   * 
   * @param prepConfig The node to read the preparator config from.
   * @param xmlFile The file the configuration was read from.
   * @return The configuration of a preparator.
   * @throws RegainException If the configuration has errors.
   */
  private PreparatorConfig readPreparatorConfig(Node prepConfig, File xmlFile)
    throws RegainException
  {
    // Check whether the config is in a extra file
    String extraFileName = XmlToolkit.getAttribute(prepConfig, "file");
    if (extraFileName != null) {
      File extraFile = new File(xmlFile.getParentFile(), extraFileName);
      Document doc = XmlToolkit.loadXmlDocument(extraFile);
      prepConfig = doc.getDocumentElement();
    }
    
    // Read the sections
    PreparatorConfig config = new PreparatorConfig();
    Node[] sectionArr = XmlToolkit.getChildArr(prepConfig, "section");
    for (int secIdx = 0; secIdx < sectionArr.length; secIdx++) {
      String sectionName = XmlToolkit.getAttribute(sectionArr[secIdx], "name", true);

      // Read the params
      HashMap paramMap = new HashMap();
      Node[] paramArr = XmlToolkit.getChildArr(sectionArr[secIdx], "param");
      for (int paramIdx = 0; paramIdx < paramArr.length; paramIdx++) {
        String paramName = XmlToolkit.getAttribute(paramArr[paramIdx], "name", true);
        String paramValue = XmlToolkit.getText(paramArr[paramIdx], true);
        paramMap.put(paramName, paramValue);
      }
      
      config.addSection(sectionName, paramMap);
    }
    
    return config;
  }


  /**
   * Reads which CrawlerAccessController to use.
   *
   * @param config The configuration to read from.
   * @throws RegainException If the configuration has errors.
   */
  private void readCrawlerAccessController(Node config)
    throws RegainException
  {
    Node node = XmlToolkit.getChild(config, "crawlerAccessController");
    if (node != null) {
      Node classNode = XmlToolkit.getChild(node, "class", true);
      mCrawlerAccessControllerClass = XmlToolkit.getText(classNode, true);
      mCrawlerAccessControllerJar   = XmlToolkit.getAttribute(classNode, "jar");

      Node configNode = XmlToolkit.getChild(node, "config");
      if (configNode != null) {
        mCrawlerAccessControllerConfig = new Properties();
        Node[] paramNodeArr = XmlToolkit.getChildArr(configNode, "param");
        for (int i = 0; i < paramNodeArr.length; i++) {
          String name = XmlToolkit.getAttribute(paramNodeArr[i], "name", true);
          String value = XmlToolkit.getText(paramNodeArr[i], true);
          mCrawlerAccessControllerConfig.setProperty(name, value);
        }
      }
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
   * Gets the list with the preparator settings.
   *
   * @return The list with the preparator settings.
   */
  public PreparatorSettings[] getPreparatorSettingsList() {
    return mPreparatorSettingsArr;
  }

  
  /**
   * Gets the list of the auxiliary fields.
   * 
   * @return The list of the auxiliary fields. May be null.
   */
  public AuxiliaryField[] getAuxiliaryFieldList() {
    return mAuxiliaryFieldArr;
  }

  
  /**
   * Gets the class name of the
   * {@link net.sf.regain.crawler.access.CrawlerAccessController} to use.
   * Returns <code>null</code> if no CrawlerAccessController should be used.
   * 
   * @return The class name of the CrawlerAccessController. 
   */
  public String getCrawlerAccessControllerClass() {
    return mCrawlerAccessControllerClass;
  }


  /**
   * Gets the name of jar file to load the
   * {@link net.sf.regain.crawler.access.CrawlerAccessController} from.
   * Returns <code>null</code> if the CrawlerAccessController already is in the
   * classpath.
   * 
   * @return The name of jar file to load the CrawlerAccessController from. 
   */
  public String getCrawlerAccessControllerJar() {
    return mCrawlerAccessControllerJar;
  }

  
  /**
   * Gets the configuration of the
   * {@link net.sf.regain.crawler.access.CrawlerAccessController}. May be
   * <code>null</code>.
   * 
   * @return The the configuration of the CrawlerAccessController. 
   */
  public Properties getCrawlerAccessControllerConfig() {
    return mCrawlerAccessControllerConfig;
  }
  
}
