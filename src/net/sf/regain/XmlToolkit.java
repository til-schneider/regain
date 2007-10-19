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
 *     $Date: 2005-11-14 09:12:56 +0100 (Mo, 14 Nov 2005) $
 *   $Author: til132 $
 * $Revision: 178 $
 */
package net.sf.regain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Enth�lt Hilfsmethoden f�r die Extraktion von Daten aus dem DOM-Dokument einer
 * XML-Datei.
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlToolkit {

  /**
   * Loads an XML file and returns its content as Document.
   *
   * @param xmlFile The XML file to load.
   * @return The XML document of the file.
   * @throws RegainException If loading the XML file failed.
   */
  public static Document loadXmlDocument(File xmlFile) throws RegainException {
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
   * Saves an XML Document to a file.
   *
   * @param xmlFile The XML file to save to.
   * @param doc The XML document to save.
   * @throws RegainException If saving the XML file failed.
   */
  public static void saveXmlDocument(File xmlFile, Document doc)
    throws RegainException 
  {
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(xmlFile);
      String encoding = "ISO-8859-1";
      PrintStream out = new PrintStream(stream, true, encoding);
      
      out.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
      out.println();
      out.println("<!DOCTYPE entities [");
      out.println("  <!ENTITY minus \"&#45;\">");
      out.println("  <!ENTITY lt \"&#60;\">");
      out.println("  <!ENTITY gt \"&#62;\">");
      out.println("]>");
      out.println();
      
      Element root = doc.getDocumentElement();
      
      printNode(out, "", root);

      out.close();
    }
    catch (Exception exc) {
      throw new RegainException("Saving XML file failed: "
                                + xmlFile.getAbsolutePath(), exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
  }

  
  /**
   * Prints a XML node.
   * 
   * @param out The PrintStream where to print the node.
   * @param prefix The prefix to put before every line.
   * @param node The node to print.
   * @throws IOException If printing failed.
   */
  private static void printNode(PrintStream out, String prefix, Node node)
    throws IOException
  {
    prefix = "";
    
    String name = node.getNodeName();
    
    boolean isText = name.equals("#text");
    boolean isComment = name.equals("#comment");
    if (isText) {
      // This is a text tag
      String text = node.getNodeValue();
      text = RegainToolkit.replace(text, "<", "&lt;");
      text = RegainToolkit.replace(text, ">", "&gt;");
      text = RegainToolkit.replace(text, "--", "&minus;&minus;");
      out.print(text);
    } else if (isComment) {
      // This is a comment tag
      String comment = node.getNodeValue();
      out.print("<!--" + comment + "-->");
    } else {
      // This is a normal tag
      out.print(prefix + "<" + name);
      if (node.hasAttributes()) {
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
          Node attrib = attributes.item(i);
          out.print(" " + attrib.getNodeName() + "=\"" + attrib.getNodeValue() + "\"");
        }
      }
  
      if (! node.hasChildNodes()) {
        out.print("/>");
      } else {
        out.print(">");
        NodeList childList = node.getChildNodes();
        String childPrefix = prefix + "  ";
        for (int i = 0; i < childList.getLength(); i++) {
          printNode(out, childPrefix, childList.item(i));
        }
        out.print(prefix + "</" + name + ">");
      }
    }
  }
  

  /**
   * Extrahiert den Text eines Knotens, wandelt ihn in einen boolean und gibt das
   * Ergebnis zur�ck.
   *
   * @param node Der Knoten, dessen Text zur�ckgeben werden soll.
   *
   * @return Der Text des Knotens als boolean.
   * @throws RegainException Falls der Knoten keinen Text hat oder falls der
   *         Text nicht <CODE>true</CODE> oder <CODE>false</CODE> ist.
   */
  public static boolean getTextAsBoolean(Node node) throws RegainException {
    String asString = getText(node, true, true);
    if (asString.equalsIgnoreCase("true")) {
      return true;
    } else if (asString.equalsIgnoreCase("false")) {
      return false;
    } else {
      throw new RegainException("Value of node '" + node.getNodeName()
        + "' must be either 'true' or 'false'!");
    }
  }


  /**
   * Extrahiert den Text eines Knotens, wandelt ihn in ein int und gibt das
   * Ergebnis zur�ck.
   *
   * @param node Der Knoten, dessen Text zur�ckgeben werden soll.
   *
   * @return Der Text des Knotens als int.
   * @throws RegainException Falls der Knoten keinen Text hat oder falls der
   *         Text keine Ganzzahl ist.
   */
  public static int getTextAsInt(Node node) throws RegainException {
    String asString = getText(node, true, true);
    try {
      return Integer.parseInt(asString);
    }
    catch (NumberFormatException exc) {
      throw new RegainException("Value of node '" + node.getNodeName() +
          "' must be an integer: '" + asString + "'", exc);
    }
  }


  /**
   * Extrahiert den Text eines Knotens, wandelt ihn in ein double und gibt das
   * Ergebnis zur�ck.
   *
   * @param node Der Knoten, dessen Text zur�ckgeben werden soll.
   *
   * @return Der Text des Knotens als double.
   * @throws RegainException Falls der Knoten keinen Text hat oder falls der
   *         Text kein Gleitkommawert ist.
   */
  public static double getTextAsDouble(Node node) throws RegainException {
    String asString = getText(node, true, true);
    try {
      return Double.parseDouble(asString);
    }
    catch (NumberFormatException exc) {
      throw new RegainException("Value of node '" + node.getNodeName() +
        "' must be a floating-point number (double): '" + asString + "'", exc);
    }
  }


  /**
   * Extrahiert den Text eines Knotens, wandelt ihn in ein String-Array um, das
   * alle durch Leerzeichen getrennte Worte enth�lt.
   *
   * @param node Der Knoten, dessen Text zur�ckgeben werden soll.
   * @param mandatory Gibt an, ob eine Exception geworfen werden soll, falls der
   *        Text fehlt.
   *
   * @return Der Text des Knotens Wort-Listen-Array.
   * @throws RegainException Falls der Knoten keinen Text hat.
   */
  public static String[] getTextAsWordList(Node node, boolean mandatory)
    throws RegainException
  {
    String asString = getText(node, mandatory);

    if (asString == null) {
      return null;
    } else {
      StringTokenizer tokenizer = new StringTokenizer(asString);
      String[] wordList = new String[tokenizer.countTokens()];
      for (int i = 0; i < wordList.length; i++) {
        wordList[i] = tokenizer.nextToken();
      }
      return wordList;
    }
  }



  /**
   * Gibt den Text eines Knotens zur�ck und pr�ft, ob er eine g�ltige URL ist.
   * <p>
   * Der Text wird als g�ltige URL angesehen, wenn er keinen Backslash enth�lt.
   *
   * @param node Der Knoten, dessen Text als URL zur�ckgeben werden soll.
   *
   * @return Der Text des Knotens.
   * @throws RegainException Wenn der Knoten keinen Text hat oder wenn der Text
   *         keine g�ltige URL ist.
   */
  public static String getTextAsUrl(Node node) throws RegainException {
    String asString = getText(node, true);

    // Check whether the text contains a back slash
    if (asString.indexOf('\\') != -1) {
      throw new RegainException("Text of node '" + node.getNodeName()
              + "' is not a valid URL. Use normal slashes instead of backslashes: '"
              + asString + "'");
    }

    return asString;
  }


  /**
   * Gets the text of a node.
   *
   * @param node The node to get the text from.
   * @return The text of the node or <code>null</code> if the node has no text.
   */
  public static String getText(Node node) {
    Node textNode = getChild(node, "#text");
    if (textNode == null) {
      return null;
    }

    return textNode.getNodeValue();
  }

  
  /**
   * Gibt den Text eines Knotens zur�ck.
   * <p>
   * Wenn der Knoten keinen Text hat, dann entscheidet <CODE>mandatory</CODE> dar�ber,
   * ob eine Exception geworfen (<CODE>mandatory</CODE> ist <CODE>true</CODE>) oder
   * ob <CODE>null</CODE> zur�ckgegeben werden soll (<CODE>mandatory</CODE> ist
   * <CODE>false</CODE>)
   *
   * @param node Der Knoten, dessen Text zur�ckgeben werden soll.
   * @param mandatory Gibt an, ob eine Exception geworfen werden soll, falls der
   *        Text fehlt.
   *
   * @return Der Text des Knotens.
   * @throws RegainException Wenn der Knoten keinen Text hat und
   *         <CODE>mandatory</CODE> <CODE>true</CODE> ist.
   */
  public static String getText(Node node, boolean mandatory)
    throws RegainException
  {
    return getText(node, mandatory, false);
  }
  

  /**
   * Gibt den Text eines Knotens zur�ck.
   * <p>
   * Wenn der Knoten keinen Text hat, dann entscheidet <CODE>mandatory</CODE> dar�ber,
   * ob eine Exception geworfen (<CODE>mandatory</CODE> ist <CODE>true</CODE>) oder
   * ob <CODE>null</CODE> zur�ckgegeben werden soll (<CODE>mandatory</CODE> ist
   * <CODE>false</CODE>)
   *
   * @param node Der Knoten, dessen Text zur�ckgeben werden soll.
   * @param mandatory Gibt an, ob eine Exception geworfen werden soll, falls der
   *        Text fehlt.
   * @param trimmed Specifies whether the text should be trimmed.
   *
   * @return Der Text des Knotens.
   * @throws RegainException Wenn der Knoten keinen Text hat und
   *         <CODE>mandatory</CODE> <CODE>true</CODE> ist.
   */
  public static String getText(Node node, boolean mandatory, boolean trimmed)
    throws RegainException
  {
    String text = getText(node);
    
    if (trimmed && (text != null)) {
      text = text.trim();
    }
    
    if (mandatory && ((text == null) || (text.length() == 0))) {
      throw new RegainException("Node '" + node.getNodeName() + "' has no text");
    } else {
      return text;
    }
  }


  /**
   * Gets a child node with a certain name.
   * <p>
   * If the node has more than one such children, then the first child is
   * returned.
   *
   * @param node The node whichs child should be returned.
   * @param childNodeName The name of the child node.
   * @return The child node or <code>null</code> if there is no such child.
   */
  public static Node getChild(Node node, String childNodeName) {
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node child = list.item(i);
      if (child.getNodeName().equals(childNodeName)) {
        return child;
      }
    }

    // No such child found
    return null;
  }
  
  
  /**
   * Gibt den Kindknoten mit einem bestimmten Namen zur�ck.
   * <p>
   * Falls der Knoten mehrere Kinder mit diesem Namen hat, so wird das erste Kind
   * zur�ckgegeben.
   * <p>
   * Falls der Knoten kein Kind mit diesem Name hat, dann entscheidet
   * <CODE>mandatory</CODE> dar�ber, ob eine Exception geworfen
   * (<CODE>mandatory</CODE> ist <CODE>true</CODE>) oder ob <CODE>null</CODE>
   * zur�ckgegeben werden soll (<CODE>mandatory</CODE> ist <CODE>false</CODE>)
   *
   * @param node Der Knoten, dessen Kind zur�ckgegeben werden soll.
   * @param childNodeName Der Name des Kindknotens.
   * @param mandatory Gibt an, ob eine Exception geworfen werden soll, falls der
   *        Knoten kein Kind mit diesem Namen hat.
   *
   * @return Der Kindknoten
   * @throws RegainException Wenn der Knoten kein Kind mit diesem Namen hat und
   *         <CODE>mandatory</CODE> <CODE>true</CODE> ist.
   */
  public static Node getChild(Node node, String childNodeName, boolean mandatory)
    throws RegainException
  {
    Node childNode = getChild(node, childNodeName);

    if (mandatory && (childNode == null)) {
      throw new RegainException("Node '" + node.getNodeName()
        + "' must have a child named '" + childNodeName + "'!");
    } else {
      return childNode;
    }
  }



  /**
   * Gibt alle Kindknoten mit einem bestimmten Namen zur�ck.
   * <p>
   * Falls der Knoten kein Kind mit diesem Namen hat, so wird ein leeres Array
   * zur�ckgegeben.
   *
   * @param node Der Knoten, dessen Kinder zur�ckgegeben werden soll.
   * @param childNodeName Der Name der Kindknoten.
   *
   * @return Die Kindknoten.
   */
  public static Node[] getChildArr(Node node, String childNodeName) {
    ArrayList list = new ArrayList();

    NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node child = nodeList.item(i);
      if (child.getNodeName().equals(childNodeName)) {
        list.add(child);
      }
    }

    Node[] nodeArr = new Node[list.size()];
    list.toArray(nodeArr);

    return nodeArr;
  }

  
  /**
   * Gets a child node from a parent node. If the parent node has no such child
   * the child of the <code>defaultNode</code> is used.
   * 
   * @param node The node to get the child from.
   * @param defaultNode The node to get the child from if <code>node</code> has
   *        no such child.
   * @param childNodeName The name of the child.
   * @return The child with the given name or <code>null</code> if both the
   *         <code>node</code> and the <code>defaultNode</code> have no child
   *         with the given name. 
   */
  public static Node getCascadedChild(Node node, Node defaultNode,
    String childNodeName)
  {
    Node child = XmlToolkit.getChild(node, childNodeName);
    if (child == null) {
      // Try to get the cascaded child
      child = XmlToolkit.getChild(defaultNode, childNodeName);
    }
    
    return child;
  }
  
  
  /**
   * Gets a child node from a parent node. If the parent node has no such child
   * the child of the <code>defaultNode</code> is used.
   * 
   * @param node The node to get the child from.
   * @param defaultNode The node to get the child from if <code>node</code> has
   *        no such child.
   * @param childNodeName The name of the child.
   * @param mandatory Specifies whether to throw an exception if none of the
   *        nodes have such a child.
   * @return The child with the given name.
   * @throws RegainException If both the <code>node</code> and the
   *         <code>defaultNode</code> have no child with the given name and
   *         <code>mandatory</code> is <code>true</code>. 
   */
  public static Node getCascadedChild(Node node, Node defaultNode,
    String childNodeName, boolean mandatory)
    throws RegainException
  {
    Node child = getCascadedChild(node, defaultNode, childNodeName);
    if (mandatory && (child == null)) {
      throw new RegainException("Node '" + node.getNodeName()
          + "' or node '" + defaultNode.getNodeName()
          + "' must have a child named '" + childNodeName + "'!");
    }
    
    return child;
  }


  /**
   * Gets the text of a child node.
   * 
   * @param node The (parent) node that has the child to get the text from.
   * @param childNodeName The name of the child node.
   * @param mandatory Specifies whether an exception should be thrown if the
   *        child has no text.
   * @return The text of the child node or <code>null</code> if the child has no
   *         text and <code>mandatory</code> is <code>false</code>.
   * @throws RegainException If the given node has no child with the given name
   *         of if the child node has no text and <code>mandatory</code> is
   *         <code>true</code>.
   */
  public static String getChildText(Node node, String childNodeName,
    boolean mandatory)
    throws RegainException
  {
    Node child = getChild(node, childNodeName, mandatory);
    if (child == null) {
      // NOTE: mandatory must be false otherwise getChild() would have thrown
      //       an exception
      return null;
    } else {
      return getText(child, mandatory);
    }
  }


  /**
   * Gets an attribute value from a node and converts it to a boolean.
   * 
   * @param node The node to get the attribute value from.
   * @param attributeName The name of the attribute to get.
   * @return The value of the attribute or <code>defaultValue</code> if there is
   *         no such attribute.
   * @throws RegainException If there is no such attribute or if the attribute
   *         value is no boolean.
   */
  public static boolean getAttributeAsBoolean(Node node, String attributeName)
    throws RegainException
  {
    String asString = getAttribute(node, attributeName, true);

    if (asString.equalsIgnoreCase("true")) {
      return true;
    } else if (asString.equalsIgnoreCase("false")) {
      return false;
    } else {
      throw new RegainException("Attribute '" + attributeName + "' of node '"
        + node.getNodeName() + "' must be either 'true' or 'false': '"
        + asString + "'");
    }
  }
  
  
  /**
   * Gets an attribute value from a node and converts it to a boolean.
   * 
   * @param node The node to get the attribute value from.
   * @param attributeName The name of the attribute to get.
   * @param defaultValue The default value to return if there is no such
   *        attribute.
   * @return The value of the attribute or <code>defaultValue</code> if there is
   *         no such attribute.
   * @throws RegainException If the attribute value is no boolean.
   */
  public static boolean getAttributeAsBoolean(Node node, String attributeName,
    boolean defaultValue)
    throws RegainException
  {
    String asString = getAttribute(node, attributeName);

    if (asString == null) {
      return defaultValue;
    } else if (asString.equalsIgnoreCase("true")) {
      return true;
    } else if (asString.equalsIgnoreCase("false")) {
      return false;
    } else {
      throw new RegainException("Attribute '" + attributeName + "' of node '"
        + node.getNodeName() + "' must be either 'true' or 'false': '"
        + asString + "'");
    }
  }


  /**
   * Gets an attribute value from a node and converts it to an int.
   * 
   * @param node The node to get the attribute value from.
   * @param attributeName The name of the attribute to get.
   * @return The value of the attribute or <code>defaultValue</code> if there is
   *         no such attribute.
   * @throws RegainException If there is no such attribute or if the attribute
   *         value is no int.
   */
  public static int getAttributeAsInt(Node node, String attributeName)
    throws RegainException
  {
    String asString = getAttribute(node, attributeName, true);

    try {
      return Integer.parseInt(asString);
    }
    catch (NumberFormatException exc) {
      throw new RegainException("Attribute '" + attributeName + "' of node '"
        + node.getNodeName() + "' must be a number: '" + asString + "'");
    }
  }


  /**
   * Gets an attribute value from a node and converts it to an int.
   * 
   * @param node The node to get the attribute value from.
   * @param attributeName The name of the attribute to get.
   * @param defaultValue The default value to return if there is no such
   *        attribute.
   * @return The value of the attribute or <code>defaultValue</code> if there is
   *         no such attribute.
   * @throws RegainException If the attribute value is no int.
   */
  public static int getAttributeAsInt(Node node, String attributeName,
    int defaultValue)
    throws RegainException
  {
    String asString = getAttribute(node, attributeName);

    if (asString == null) {
      return defaultValue;
    } else {
      try {
        return Integer.parseInt(asString);
      }
      catch (NumberFormatException exc) {
        throw new RegainException("Attribute '" + attributeName + "' of node '"
          + node.getNodeName() + "' must be a number: '" + asString + "'");
      }
    }
  }


  /**
   * Gets an attribute value from a node.
   *
   * @param node The node to get the attribute value from.
   * @param attributeName The name of the wanted attribute.
   * @return The attribute value or <code>null</code> if there is no such
   *         attribute.
   */
  public static String getAttribute(Node node, String attributeName) {
    Node attributeNode = node.getAttributes().getNamedItem(attributeName);
    if (attributeNode == null) {
      return null;
    } else {
      return attributeNode.getNodeValue();
    }
  }


  /**
   * Gibt den Attributwert eines Knotens zur�ck.
   *
   * @param node Der Knoten, dessen Attribut zur�ckgegeben werden soll.
   * @param attributeName Der Name des Attributs, das zur�ckgegeben werden soll.
   * @param mandatory Gibt an, ob eine Exception geworfen werden soll, falls der
   *        Knoten kein solches Attribut hat.
   *
   * @return Den Attributwert.
   * @throws RegainException Falls der Knoten kein solches Attribut hat und
   *         <CODE>mandatory</CODE> <CODE>true</CODE> ist.
   */
  public static String getAttribute(Node node, String attributeName,
    boolean mandatory)
    throws RegainException
  {
    String value = getAttribute(node, attributeName);
    if (value == null) {
      if (mandatory) {
        throw new RegainException("Node '" + node.getNodeName()
          + "' has no attribute '" + attributeName + "'");
      } else {
        return null;
      }
    } else {
      return value;
    }
  }


  /**
   * Sets the text of a node.
   * 
   * @param doc The document the node comes from.
   * @param node The node whichs text should be changed.
   * @param text The text to set.
   */
  public static void setText(Document doc, Node node, String text) {
    Node textNode = getChild(node, "#text");
    
    if (textNode == null) {
      textNode = doc.createTextNode(text);
      node.appendChild(textNode);
    } else {
      textNode.setNodeValue(text);
    }
  }


  /**
   * Removes all child nodes from a node. 
   * 
   * @param node The node to remove the children from.
   */
  public static void removeAllChildren(Node node) {
    NodeList nodeList = node.getChildNodes();
    for (int i = nodeList.getLength() - 1; i >= 0; i--) {
      node.removeChild(nodeList.item(i));
    }
  }


  /**
   * Removes all child nodes with a certain name. 
   * 
   * @param node The node to remove the children from.
   * @param childNodeName The name of the children to remove.
   */
  public static void removeAllChildren(Node node, String childNodeName) {
    Node[] childArr = getChildArr(node, childNodeName);
    for (int i = 0; i < childArr.length; i++) {
      node.removeChild(childArr[i]);
    }
  }


  /**
   * Adds a child node to a node.
   * 
   * @param doc The document the node comes from.
   * @param node The node were to add the child.
   * @param childNodeName The name of the child node to add.
   * @return The added child node.
   */
  public static Node addChild(Document doc, Node node, String childNodeName) {
    Node childNode = doc.createElement(childNodeName);
    node.appendChild(childNode);
    return childNode;
  }


  /**
   * Gets a child node or creates it if no such node exists. 
   * 
   * @param doc The document the node comes from.
   * @param node The node were to get the child from or where to add the child.
   * @param childNodeName The name of the child node to get or add.
   * @return The child node.
   */
  public static Node getOrAddChild(Document doc, Node node, String childNodeName) {
    Node child = getChild(node, childNodeName);
    if (child == null) {
      child = addChild(doc, node, childNodeName);
    }
    return child;
  }


  /**
   * Adds a child node to a node and gives it a text.
   * 
   * @param doc The document the node comes from.
   * @param node The node where to add the child.
   * @param childNodeName The name of the child node to add.
   * @param text The text to set to the child.
   * @return The added child node.
   */
  public static Node addChildWithText(Document doc, Node node,
    String childNodeName, String text)
  {
    Node childNode = addChild(doc, node, childNodeName);
    setText(doc, childNode, text);
    return childNode;
  }
  
  
  /**
   * Sets an attribute of a node.
   * 
   * @param doc The document the node comes from.
   * @param node The node where to set the attribute.
   * @param attribName The name of the attribute to set.
   * @param attribValue The value of the attribute to set.
   */
  public static void setAttribute(Document doc, Node node, String attribName,
    String attribValue)
  {
    Attr attr = doc.createAttribute(attribName);
    attr.setNodeValue(attribValue);
    node.getAttributes().setNamedItem(attr);
  }
  
  
  /**
   * Pretty prints a node.
   * 
   * @param doc The document the node comes from.
   * @param node The node that should be pretty printed.
   */
  public static void prettyPrint(Document doc, Node node) {
    // Get the text before the node and extract the indenting
    Node parent = node.getParentNode();

    String indenting = "";
    NodeList siblingList = parent.getChildNodes();
    for (int i = 1; i < siblingList.getLength(); i++) {
      Node sibling = siblingList.item(i);
      if (sibling == node) {
        Node nodeBefore = siblingList.item(i - 1);
        // Check whether this is a text node
        if (nodeBefore.getNodeName().equals("#text")) {
          // There is text before the node -> Extract the indenting
          String text = nodeBefore.getNodeValue();
          int newlinePos = text.lastIndexOf('\n');
          if (newlinePos != -1) {
            indenting = text.substring(newlinePos);
            if (indenting.trim().length() != 0) {
              // The indenting is no whitespace -> Forget it
              indenting = "";
            }
          }
        }
        break;
      }
    }
    
    // Now pretty print the node
    prettyPrint(doc, node, indenting);
  }  


  /**
   * Pretty prints a node.
   * 
   * @param doc The document the node comes from.
   * @param node The node that should be pretty printed.
   * @param prefix The prefix the node should get.
   */
  private static void prettyPrint(Document doc, Node node, String prefix) {
    String childPrefix = prefix + "  ";
    
    // Add the indenting to the children
    NodeList childList = node.getChildNodes();
    boolean hasChildren = false;
    for (int i = childList.getLength() - 1; i >= 0; i--) {
      Node child = childList.item(i);
      boolean isNormalNode = (! child.getNodeName().startsWith("#"));
      if (isNormalNode) {
        // Add the indenting to this node
        Node textNode = doc.createTextNode(childPrefix);
        node.insertBefore(textNode, child);
        
        // pretty print the child's children
        prettyPrint(doc, child, childPrefix);
        
        hasChildren = true;
      }
    }
    
    // Add the indenting to the end tag
    if (hasChildren) {
      Node textNode = doc.createTextNode(prefix);
      node.appendChild(textNode);
    }
  }
    
}
