/*
 * CVS information:
 *  $RCSfile: XmlSearchConfig.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/config/XmlSearchConfig.java,v $
 *     $Date: 2005/02/26 14:51:11 $
 *   $Author: til132 $
 * $Revision: 1.3 $
 */
package net.sf.regain.search.config;

import java.io.File;
import java.util.HashMap;

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
  
  /** The name of the default index. */
  private String mDefaultIndexName;


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
    
    // Get the index nodes
    mIndexHash = new HashMap();
    mDefaultIndexName = null;
    Node[] nodeArr = XmlToolkit.getChildArr(listNode, "index");
    for (int i = 0; i < nodeArr.length; i++) {
      String name = XmlToolkit.getAttribute(nodeArr[i], "name", true);
      String directory = XmlToolkit.getChildText(nodeArr[i], "dir", true);

      node = XmlToolkit.getCascadedChild(nodeArr[i], defaultNode, "openInNewWindowRegex", true);
      String openInNewWindowRegex = XmlToolkit.getText(node, true);

      node = XmlToolkit.getCascadedChildKram(nodeArr[i], defaultNode, "searchFieldList");
      String[] searchFieldList = null;
      if (node != null) {
        searchFieldList = XmlToolkit.getTextAsWordList(node, true);
      }

      node = XmlToolkit.getCascadedChildKram(nodeArr[i], defaultNode, "rewriteRules");
      String[][] rewriteRules = readRewriteRules(node);
      
      IndexConfig indexConfig = new IndexConfig(name, directory,
          openInNewWindowRegex, searchFieldList, rewriteRules);
      mIndexHash.put(name, indexConfig);
      
      boolean isDefault = XmlToolkit.getAttributeAsBoolean(nodeArr[i], "default", false);
      if (isDefault) {
        if (mDefaultIndexName != null) {
          throw new RegainException("The index '" + name + "' can't be marked " +
              "as default index, because index '" + mDefaultIndexName
              + "' already is marked as default.");
        } else {
          mDefaultIndexName = name;
        }
      }
    }
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
   * Gets the name of the default index.
   * 
   * @return The name of the default index or <code>null</code> if no default
   *         index was specified.
   */
  public String getDefaultIndexName() {
    return mDefaultIndexName;
  }
  
}
