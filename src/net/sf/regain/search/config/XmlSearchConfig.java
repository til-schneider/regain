/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-08-06 16:04:27 +0200 (Mi, 06 Aug 2008) $
 *   $Author: thtesche $
 * $Revision: 325 $
 */
package net.sf.regain.search.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import net.sf.regain.RegainException;
import net.sf.regain.XmlToolkit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class XmlSearchConfig implements SearchConfig {

  /** All configured indexes. */
  private HashMap mIndexHash;
  
  /** The names of the default indexes. */
  private String[] mDefaultIndexNameArr;


  /**
   * Creates a new instance of XmlSearchConfig.
   * 
   * @param xmlFile The XML file to read the config from.
   * @throws RegainException If reading the config failed.
   */
  public XmlSearchConfig(File xmlFile) throws RegainException {
    Document doc = XmlToolkit.loadXmlDocument(xmlFile);
    Element config = doc.getDocumentElement();

    readIndexList(config);
  }


  /**
   * Reads the search indexes from the config.
   *
   * @param config The configuration to read from.
   * @throws RegainException If the configration has errors.
   */
  private void readIndexList(Element config) throws RegainException {
    Node node;
    
    Node listNode = XmlToolkit.getChild(config, "indexList", true);
    
    // Get the node that holds the default settings for all indexes
    Node defaultNode = XmlToolkit.getChild(listNode, "defaultSettings");
    
    // get the highlighted flag
    node = XmlToolkit.getChild(defaultNode, "Highlighting");
    boolean highlighting = (node == null) ? true : XmlToolkit.getTextAsBoolean(node);
    
    // Get the index nodes
    mIndexHash = new HashMap();
    ArrayList defaultIndexNameList = new ArrayList();
    Node[] nodeArr = XmlToolkit.getChildArr(listNode, "index");
    for (int indexIdx = 0; indexIdx < nodeArr.length; indexIdx++) {
      Node indexNode = nodeArr[indexIdx];
      
      String indexName = XmlToolkit.getAttribute(indexNode, "name", true);
      String directory = XmlToolkit.getChildText(indexNode, "dir", true);

      // Read the openInNewWindowRegex
      node = XmlToolkit.getCascadedChild(indexNode, defaultNode, "openInNewWindowRegex", true);
      String openInNewWindowRegex = XmlToolkit.getText(node, true);

      // Read the useFileToHttpBridge
      node = XmlToolkit.getCascadedChild(indexNode, defaultNode, "useFileToHttpBridge");
      boolean useFileToHttpBridge = true;
      if (node != null) {
        useFileToHttpBridge = XmlToolkit.getTextAsBoolean(node);
      }
      
      // Read the search field list
      node = XmlToolkit.getCascadedChild(indexNode, defaultNode, "searchFieldList");
      String[] searchFieldList = null;
      if (node != null) {
        searchFieldList = XmlToolkit.getTextAsWordList(node, true);
      }

      // Read the rewrite rules
      node = XmlToolkit.getCascadedChild(indexNode, defaultNode, "rewriteRules");
      String[][] rewriteRules = readRewriteRules(node);
      
      // Read the SearchAccessController
      String searchAccessControllerClass = null;
      String searchAccessControllerJar = null;
      Properties searchAccessControllerConfig = null;
      node = XmlToolkit.getCascadedChild(indexNode, defaultNode, "searchAccessController");
      if (node != null) {
        Node classNode = XmlToolkit.getChild(node, "class", true);
        searchAccessControllerClass = XmlToolkit.getText(classNode, true);
        searchAccessControllerJar   = XmlToolkit.getAttribute(classNode, "jar");
        
        Node configNode = XmlToolkit.getChild(node, "config");
        if (configNode != null) {
          searchAccessControllerConfig = new Properties();
          Node[] paramNodeArr = XmlToolkit.getChildArr(configNode, "param");
          for (int i = 0; i < paramNodeArr.length; i++) {
            String name = XmlToolkit.getAttribute(paramNodeArr[i], "name", true);
            String value = XmlToolkit.getText(paramNodeArr[i], true);
            searchAccessControllerConfig.setProperty(name, value);
          }
        }
      }
      
      // Create the index config
      IndexConfig indexConfig = new IndexConfig(indexName, directory,
          openInNewWindowRegex, useFileToHttpBridge, searchFieldList, rewriteRules,
          searchAccessControllerClass, searchAccessControllerJar,
          searchAccessControllerConfig, highlighting);
      mIndexHash.put(indexName, indexConfig);
      
      // Check whether this index is default
      boolean isDefault = XmlToolkit.getAttributeAsBoolean(indexNode, "default", false);
      if (isDefault) {
        defaultIndexNameList.add(indexName);
      }
    }

    // Store the default indexes into an array
    mDefaultIndexNameArr = new String[defaultIndexNameList.size()];
    defaultIndexNameList.toArray(mDefaultIndexNameArr);
  }
  
  
  /**
   * Reads the URL rewrite rules from a node
   * 
   * @param node The node to read from.
   * @return The rewrite rules. May be null.
   * @throws RegainException If the configration has errors.
   */
  private String[][] readRewriteRules(Node node)
    throws RegainException
  {
    if (node == null) {
      return null;
    }
    
    Node[] ruleNodeArr = XmlToolkit.getChildArr(node, "rule");
    String[][] rewriteRules = new String[ruleNodeArr.length][];
    for (int i = 0; i < ruleNodeArr.length; i++) {
      String prefix = XmlToolkit.getAttribute(ruleNodeArr[i], "prefix", true);
      String replacement = XmlToolkit.getAttribute(ruleNodeArr[i], "replacement", true);
      
      // Add this rule
      rewriteRules[i] = new String[] { prefix, replacement };
    }
    
    return rewriteRules;
  }
  
  
  /**
   * Gets the configuration for an index.
   * 
   * @param indexName The name of the index to get the config for.
   * @return The configuration for the wanted index or <code>null</code> if
   *         there is no such index configured.
   */
  public IndexConfig getIndexConfig(String indexName) {
    return (IndexConfig) mIndexHash.get(indexName);
  }


  /**
   * Gets the names of the default indexes.
   * 
   * @return The names of the default indexes or an empty array if no default
   *         index was specified.
   */
  public String[] getDefaultIndexNameArr() {
    return mDefaultIndexNameArr;
  }
  
}
