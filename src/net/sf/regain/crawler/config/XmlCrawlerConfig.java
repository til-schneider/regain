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
 *  $RCSfile$
 *   $Source$
 *     $Date: 2007-10-20 18:22:56 +0200 (Sa, 20 Okt 2007) $
 *   $Author: til132 $
 * $Revision: 247 $
 */
package net.sf.regain.crawler.config;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import net.sf.regain.RegainException;
import net.sf.regain.XmlToolkit;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Liest die konfigurierenden Einstellungen aus einer XML-Datei und stellt sie
 * zur Verfï¿½gung.
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlCrawlerConfig implements CrawlerConfig {

  /** Der Host-Namen des Proxy-Servers. */
  private String mProxyHost;
  /** Der Port des Proxy-Servers. */
  private String mProxyPort;
  /** Der Benutzernamen fï¿½r die Anmeldung beim Proxy-Server. */
  private String mProxyUser;
  /** Das Passwort fï¿½r die Anmeldung beim Proxy-Server. */
  private String mProxyPassword;
  /** The user agent the crawler should in order to identify at the HTTP server(s). */
  private String mUserAgent;
  /**
   * Gibt an, ob URLs geladen werden sollen, die weder durchsucht noch indiziert
   * werden.
   */
  private boolean mLoadUnparsedUrls;
  /** Gibt an, ob ein Suchindex erstellt werden soll. */
  private boolean mBuildIndex;
  /**
   * Der Timeout fï¿½r HTTP-Downloads. Dieser Wert bestimmt die maximale Zeit
   * in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   */
  private int mHttpTimeoutSecs;
  /** Das Verzeichnis, in dem der Suchindex stehen soll. */
  private String mIndexDir;

  /** The maximum number of terms per document. */
  private int mMaxFieldLength;

  /** Der zu verwendende Analyzer-Typ. */
  private String mAnalyzerType;

  /** Enthï¿½lt alle Worte, die nicht indiziert werden sollen. */
  private String[] mStopWordList;
  /**
   * Enthï¿½lt alle Worte, die bei der Indizierung nicht vom Analyzer verï¿½ndert
   * werden sollen.
   */
  private String[] mExclusionList;

  /** Gibt an, ob Analyse-Deteien geschrieben werden sollen. */
  private boolean mWriteAnalysisFiles;
  /** The interval between two breakpoint in minutes. */
  private int mBreakpointInterval;
  /**
   * Der maximale Prozentsatz von gescheiterten Dokumenten (0..100), der fï¿½r
   * die Freigabe eines Index toleriert wird.
   */
  private double mMaxFailedDocuments;

  /** Der Nam der Kontrolldatei fï¿½r erfolgreiche Indexerstellung. */
  private String mFinishedWithoutFatalsFileName;
  /** Der Name der Kontrolldatei fï¿½r fehlerhafte Indexerstellung. */
  private String mFinishedWithFatalsFileName;

  /** Die StartUrls. */
  private StartUrl[] mStartUrls;

  /** Die UrlPattern, die der HTML-Parser nutzen soll, um URLs zu identifizieren. */
  private UrlPattern[] mHtmlParserUrlPatterns;

  /** The black list. */
  private UrlMatcher[] mBlackList;
  /** The white list. */
  private WhiteListEntry[] mWhiteListEntryArr;

  /** The names of the fields to prefetch the destinct values for. */
  private String[] mValuePrefetchFields;

  /**
   * Die regulï¿½ren Ausdrï¿½cke, auf die die URL eines Dokuments passen muss,
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
    readUserAgent(config);
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
    Node node = XmlToolkit.getChild(config, "loadUnparsedUrls");
    mLoadUnparsedUrls = (node == null) ? false : XmlToolkit.getTextAsBoolean(node);
  }


  /**
   * Liest den Timeout fï¿½r HTTP-Downloads aus der Konfiguration.
   *
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readHttpTimeoutSecs(Element config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "httpTimeout");
    mHttpTimeoutSecs = (node == null) ? 180 : XmlToolkit.getTextAsInt(node);
  }


  /**
   * Reads the user agent from the config.
   *
   * @param config The configuration to read from.
   * @throws RegainException If the configuration has an error.
   */
  private void readUserAgent(Element config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "userAgent", false);
    if (node != null) {
      mUserAgent = XmlToolkit.getText(node);
    }
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
    node = XmlToolkit.getChild(indexNode, "buildIndex");
    mBuildIndex = (node == null) ? true : XmlToolkit.getTextAsBoolean(node);
    node = XmlToolkit.getChild(indexNode, "analyzerType", true);
    mAnalyzerType = XmlToolkit.getText(node, true);
    node = XmlToolkit.getChild(indexNode, "maxFieldLength", false);
    mMaxFieldLength = (node == null) ? -1 : XmlToolkit.getTextAsInt(node);
    node = XmlToolkit.getChild(indexNode, "stopwordList", true);
    mStopWordList = XmlToolkit.getTextAsWordList(node, true);
    node = XmlToolkit.getChild(indexNode, "exclusionList", true);
    mExclusionList = XmlToolkit.getTextAsWordList(node, false);
    node = XmlToolkit.getChild(indexNode, "writeAnalysisFiles");
    mWriteAnalysisFiles = (node == null) ? false : XmlToolkit.getTextAsBoolean(node);

    node = XmlToolkit.getChild(indexNode, "breakpointInterval");
    mBreakpointInterval = (node == null) ? 10 : XmlToolkit.getTextAsInt(node);

    node = XmlToolkit.getChild(indexNode, "maxFailedDocuments");
    mMaxFailedDocuments = (node == null) ? 1.0 : (XmlToolkit.getTextAsDouble(node) / 100.0);

    node = XmlToolkit.getChild(indexNode, "valuePrefetchFields", false);
    mValuePrefetchFields = (node == null) ? null : XmlToolkit.getTextAsWordList(node, false);
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
   * Liest die URL-Patterns fï¿½r den HTML-Parser aus der Konfiguration.
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
   * Reads the black list from the configuration.
   * <p>
   * Documents that have an URL that matches to one entry of the black list,
   * won't be processed.
   *
   * @param config The configuration to read from.
   * @throws RegainException If the configuration has an error.
   */
  private void readBlackList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "blacklist", true);
    Node[] prefixNodeArr = XmlToolkit.getChildArr(node, "prefix");
    Node[] regexNodeArr = XmlToolkit.getChildArr(node, "regex");
    
    mBlackList = new UrlMatcher[prefixNodeArr.length + regexNodeArr.length];
    for (int i = 0; i < prefixNodeArr.length; i++) {
      String prefix = XmlToolkit.getText(prefixNodeArr[i], true);
      mBlackList[i] = new PrefixUrlMatcher(prefix);
    }
    for (int i = 0; i < regexNodeArr.length; i++) {
      String regex = XmlToolkit.getText(regexNodeArr[i], true);
      mBlackList[prefixNodeArr.length + i] = new RegexUrlMatcher(regex);
    }
  }


  /**
   * Reads the white list from the configuration.
   * <p>
   * Documents will only be processed if their URL matches to one entry from the
   * white list.
   *
   * @param config The configuration to read from.
   * @throws RegainException If the configuration has an error.
   */
  private void readWhiteList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "whitelist", true);
    Node[] prefixNodeArr = XmlToolkit.getChildArr(node, "prefix");
    Node[] regexNodeArr = XmlToolkit.getChildArr(node, "regex");

    mWhiteListEntryArr = new WhiteListEntry[prefixNodeArr.length + regexNodeArr.length];
    for (int i = 0; i < prefixNodeArr.length; i++) {
      String prefix = XmlToolkit.getText(prefixNodeArr[i], true);
      UrlMatcher matcher = new PrefixUrlMatcher(prefix);
      String name = XmlToolkit.getAttribute(prefixNodeArr[i], "name");
      mWhiteListEntryArr[i] = new WhiteListEntry(matcher, name);
    }
    for (int i = 0; i < regexNodeArr.length; i++) {
      String regex = XmlToolkit.getText(regexNodeArr[i], true);
      UrlMatcher matcher = new RegexUrlMatcher(regex);
      String name = XmlToolkit.getAttribute(regexNodeArr[i], "name");
      mWhiteListEntryArr[prefixNodeArr.length + i] = new WhiteListEntry(matcher, name);
    }
  }


  /**
   * Liest die Liste der regulï¿½ren Ausdrï¿½cke aus der Konfiguration, auf die die
   * URL eines Dokuments passen muss, damit anstatt des wirklichen
   * Dokumententitels der Text des Links, der auf das Dokument gezeigt hat, als
   * Dokumententitel genutzt wird.
   *
   * @param config Die Konfiguration, aus der gelesen werden soll.
   * @throws RegainException Wenn die Konfiguration fehlerhaft ist.
   */
  private void readUseLinkTextAsTitleRegexList(Node config) throws RegainException {
    Node node = XmlToolkit.getChild(config, "useLinkTextAsTitleList");
    if (node == null) {
      mUseLinkTextAsTitleRegexList = new String[0];
    } else {
      Node[] nodeArr = XmlToolkit.getChildArr(node, "urlPattern");
      mUseLinkTextAsTitleRegexList = new String[nodeArr.length];
      for (int i = 0; i < nodeArr.length; i++) {
        mUseLinkTextAsTitleRegexList[i] = XmlToolkit.getText(nodeArr[i], true);
      }
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

      node = XmlToolkit.getChild(nodeArr[i], "urlPattern", false);
      String urlRegex = null;
      if (node != null) {
        urlRegex = XmlToolkit.getText(node, true);
      }

      node = XmlToolkit.getChild(nodeArr[i], "config");
      PreparatorConfig prepConfig;
      if (node != null) {
        prepConfig = readPreparatorConfig(node, xmlFile, className);
      } else {
        prepConfig = new PreparatorConfig();
      }

      mPreparatorSettingsArr[i] = new PreparatorSettings(enabled, className, urlRegex, prepConfig);
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
        RE urlRegex = readRegexChild(nodeArr[i]);
        String value = XmlToolkit.getAttribute(nodeArr[i], "value");
        boolean toLowerCase = XmlToolkit.getAttributeAsBoolean(nodeArr[i],
                "toLowerCase", true);
        int urlRegexGroup = XmlToolkit.getAttributeAsInt(nodeArr[i], "regexGroup", -1);
        if ((value == null) && (urlRegexGroup == -1)) {
          throw new RegainException("The node 'auxiliaryField' must have " +
                "either the attribute 'value' or the attribute 'regexGroup'");
        }

        boolean store    = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "store", true);
        boolean index    = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "index", true);
        boolean tokenize = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "tokenize", false);

        mAuxiliaryFieldArr[i] = new AuxiliaryField(fieldName, value,
            toLowerCase, urlRegex, urlRegexGroup, store, index, tokenize);
      }
    }
  }


  /**
   * Reads the regex child node from a node. Can also read the old style, where
   * the regex is directly in the node text.
   * 
   * @param node The node to read the regex node from
   * @return The compiled regular expression
   * @throws RegainException If there is no regular expression or if the regex
   *         could not be compiled.
   */
  private RE readRegexChild(Node node) throws RegainException {
      // Check whether the node has a regex child node
      Node regexNode = XmlToolkit.getChild(node, "regex");
      if (regexNode != null) {
          boolean caseSensitive = XmlToolkit.getAttributeAsBoolean(regexNode,
              "caseSensitive", false);
          String regex = XmlToolkit.getText(regexNode, true);

          int flags = caseSensitive ? RE.MATCH_NORMAL : RE.MATCH_CASEINDEPENDENT;
          try {
              return new RE(regex, flags);
          } catch (RESyntaxException exc) {
              throw new RegainException("Regex of node '" + node.getNodeName()
                  + "' has a wrong syntax: '" + regex + "'", exc);
          }
      } else {
          // This is the old style -> Use the text as regex
          String regex = XmlToolkit.getText(node, true);
          try {
              return new RE(regex, RE.MATCH_CASEINDEPENDENT);
          } catch (RESyntaxException exc) {
              throw new RegainException("Regex of node '" + node.getNodeName()
                  + "' has a wrong syntax: '" + regex + "'", exc);
          }
      }
  }
  
  
  /**
   * Reads the configuration of a preparator from a node.
   * 
   * @param prepConfig The node to read the preparator config from.
   * @param xmlFile The file the configuration was read from.
   * @param className The class name of the preparator.
   * @return The configuration of a preparator.
   * @throws RegainException If the configuration has errors.
   */
  private PreparatorConfig readPreparatorConfig(Node prepConfig, File xmlFile,
    String className)
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

        if (paramMap.containsKey(paramName)) {
          throw new RegainException("Preparator configuration of '" + className
              + "' has multiple '" + paramName + "' parameters in section '"
              + sectionName + "'");
        }

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
   * Gibt den Host-Namen des Proxy-Servers zurï¿½ck. Wenn kein Host konfiguriert
   * wurde, wird <CODE>null</CODE> zurï¿½ckgegeben.
   *
   * @return Der Host-Namen des Proxy-Servers.
   */
  public String getProxyHost() {
    return mProxyHost;
  }



  /**
   * Gibt den Port des Proxy-Servers zurï¿½ck. Wenn kein Port konfiguriert wurde,
   * wird <CODE>null</CODE> zurï¿½ckgegeben.
   *
   * @return Der Port des Proxy-Servers.
   */
  public String getProxyPort() {
    return mProxyPort;
  }



  /**
   * Gibt den Benutzernamen fï¿½r die Anmeldung beim Proxy-Server zurï¿½ck. Wenn
   * kein Benutzernamen konfiguriert wurde, wird <CODE>null</CODE> zurï¿½ckgegeben.
   *
   * @return Der Benutzernamen fï¿½r die Anmeldung beim Proxy-Server.
   */
  public String getProxyUser() {
    return mProxyUser;
  }



  /**
   * Gibt das Passwort fï¿½r die Anmeldung beim Proxy-Server zurï¿½ck. Wenn kein
   * Passwort konfiguriert wurde, wird <CODE>null</CODE> zurï¿½ckgegeben.
   *
   * @return Das Passwort fï¿½r die Anmeldung beim Proxy-Server.
   */
  public String getProxyPassword() {
    return mProxyPassword;
  }


  // overridden
  public String getUserAgent() {
    return mUserAgent;
  }


  /**
   * Gibt den Timeout fï¿½r HTTP-Downloads zurï¿½ck. Dieser Wert bestimmt die
   * maximale Zeit in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   *
   * @return Den Timeout fï¿½r HTTP-Downloads
   */
  public int getHttpTimeoutSecs() {
    return mHttpTimeoutSecs;
  }


  /**
   * Gibt zurï¿½ck, ob URLs geladen werden sollen, die weder durchsucht noch
   * indiziert werden.
   *
   * @return Ob URLs geladen werden sollen, die weder durchsucht noch indiziert
   *         werden.
   */
  public boolean getLoadUnparsedUrls() {
    return mLoadUnparsedUrls;
  }



  /**
   * Gibt zurï¿½ck, ob ein Suchindex erstellt werden soll.
   *
   * @return Ob ein Suchindex erstellt werden soll.
   */
  public boolean getBuildIndex() {
    return mBuildIndex;
  }


  /**
   * Gibt das Verzeichnis zurï¿½ck, in dem der Suchindex am Ende stehen soll.
   *
   * @return Das Verzeichnis, in dem der Suchindex am Ende stehen soll.
   */
  public String getIndexDir() {
    return mIndexDir;
  }


  /**
   * Gibt den zu verwendenden Analyzer-Typ zurï¿½ck.
   *
   * @return en zu verwendenden Analyzer-Typ
   */
  public String getAnalyzerType() {
    return mAnalyzerType;
  }


  // overridden
  public int getMaxFieldLength() {
    return mMaxFieldLength;
  }


  /**
   * Gibt alle Worte zurï¿½ck, die nicht indiziert werden sollen.
   *
   * @return Alle Worte, die nicht indiziert werden sollen.
   */
  public String[] getStopWordList() {
    return mStopWordList;
  }



  /**
   * Gibt alle Worte zurï¿½ck, die bei der Indizierung nicht vom Analyzer
   * verï¿½ndert werden sollen.
   *
   * @return Alle Worte, die bei der Indizierung nicht vom Analyzer
   *         verï¿½ndert werden sollen.
   */
  public String[] getExclusionList() {
    return mExclusionList;
  }



  /**
   * Gibt zurï¿½ck, ob Analyse-Deteien geschrieben werden sollen.
   * <p>
   * Diese Dateien helfen, die Qualitï¿½t der Index-Erstellung zu prï¿½fen und
   * werden in einem Unterverzeichnis im Index-Verzeichnis angelegt.
   *
   * @return Ob Analyse-Deteien geschrieben werden sollen.
   */
  public boolean getWriteAnalysisFiles() {
    return mWriteAnalysisFiles;
  }


  /**
   * Returns the interval between two breakpoint in minutes. If set to 0, no
   * breakpoints will be created.
   *
   * @return the interval between two breakpoint in minutes.
   */
  public int getBreakpointInterval() {
    return mBreakpointInterval;
  }


  /**
   * Gibt den maximalen Prozentsatz von gescheiterten Dokumenten zurï¿½ck. (0..1)
   * <p>
   * Ist das Verhï¿½lnis von gescheiterten Dokumenten zur Gesamtzahl von
   * Dokumenten grï¿½ï¿½er als dieser Prozentsatz, so wird der Index verworfen.
   * <p>
   * Gescheiterte Dokumente sind Dokumente die es entweder nicht gibt (Deadlink)
   * oder die nicht ausgelesen werden konnten.
   *
   * @return Den maximalen Prozentsatz von gescheiterten Dokumenten zurï¿½ck.
   */
  public double getMaxFailedDocuments() {
    return mMaxFailedDocuments;
  }


  /**
   * Gibt den Namen der Kontrolldatei fï¿½r erfolgreiche Indexerstellung zurï¿½ck.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, ohne dass
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zurï¿½ckgegeben.
   *
   * @return Der Name der Kontrolldatei fï¿½r erfolgreiche Indexerstellung
   */
  public String getFinishedWithoutFatalsFileName() {
    return mFinishedWithoutFatalsFileName;
  }


  /**
   * Gibt den Namen der Kontrolldatei fï¿½r fehlerhafte Indexerstellung zurï¿½ck.
   * <p>
   * Diese Datei wird erzeugt, wenn der Index erstellt wurde, wobei
   * fatale Fehler aufgetreten sind.
   * <p>
   * Wenn keine Kontrolldatei erzeugt werden soll, dann wird <code>null</code>
   * zurï¿½ckgegeben.
   *
   * @return Der Name der Kontrolldatei fï¿½r fehlerhafte Indexerstellung
   */
  public String getFinishedWithFatalsFileName() {
    return mFinishedWithFatalsFileName;
  }


  /**
   * Gibt die StartUrls zurï¿½ck, bei denen der Crawler-Prozeï¿½ beginnen soll.
   *
   * @return Die StartUrls.
   */
  public StartUrl[] getStartUrls() {
    return mStartUrls;
  }



  /**
   * Gibt die UrlPattern zurï¿½ck, die der HTML-Parser nutzen soll, um URLs zu
   * identifizieren.
   *
   * @return Die UrlPattern fï¿½r den HTML-Parser.
   */
  public UrlPattern[] getHtmlParserUrlPatterns() {
    return mHtmlParserUrlPatterns;
  }


  /**
   * Gets the black list.
   * <p>
   * The black list is an array of UrlMatchers, a URLs <i>must not</i> match to,
   * in order to be processed.
   * 
   * @return The black list.
   */
  public UrlMatcher[] getBlackList() {
    return mBlackList;
  }


  /**
   * Gets the white list.
   * <p>
   * The black list is an array of WhiteListEntry, a URLs <i>must</i> match to,
   * in order to be processed.
   *
   * @return Die Weiße Liste
   */
  public WhiteListEntry[] getWhiteList() {
    return mWhiteListEntryArr;
  }


  // overridden
  public String[] getValuePrefetchFields() {
    return mValuePrefetchFields;
  }


  /**
   * Gibt die regulï¿½ren Ausdrï¿½cke zurï¿½ck, auf die die URL eines Dokuments passen
   * muss, damit anstatt des wirklichen Dokumententitels der Text des Links, der
   * auf das Dokument gezeigt hat, als Dokumententitel genutzt wird.
   *
   * @return Die regulï¿½ren Ausdrï¿½cke, die Dokumente bestimmen, fï¿½r die der
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
