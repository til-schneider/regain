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
 *  $RCSfile: XmlToolkit.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/XmlToolkit.java,v $
 *     $Date: 2004/07/28 20:26:03 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.config;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.sf.regain.RegainException;

import org.w3c.dom.*;


/**
 * Enthält Hilfsmethoden für die Extraktion von Daten aus dem DOM-Dokument einer
 * XML-Datei.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class XmlToolkit {

  /**
   * Extrahiert den Text eines Knotens, wandelt ihn in einen boolean und gibt das
   * Ergebnis zurück.
   *
   * @param node Der Knoten, dessen Text zurückgeben werden soll.
   *
   * @return Der Text des Knotens als boolean.
   * @throws RegainException Falls der Knoten keinen Text hat oder falls der
   *         Text nicht <CODE>true</CODE> oder <CODE>false</CODE> ist.
   */  
  public static boolean getTextAsBoolean(Node node) throws RegainException {
    String asString = getText(node).trim();
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
   * Ergebnis zurück.
   *
   * @param node Der Knoten, dessen Text zurückgeben werden soll.
   *
   * @return Der Text des Knotens als int.
   * @throws RegainException Falls der Knoten keinen Text hat oder falls der
   *         Text keine Ganzzahl ist.
   */  
  public static int getTextAsInt(Node node) throws RegainException {
    String asString = getText(node).trim();
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
   * Ergebnis zurück.
   *
   * @param node Der Knoten, dessen Text zurückgeben werden soll.
   *
   * @return Der Text des Knotens als double.
   * @throws RegainException Falls der Knoten keinen Text hat oder falls der
   *         Text kein Gleitkommawert ist.
   */  
  public static double getTextAsDouble(Node node) throws RegainException {
    String asString = getText(node).trim();
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
   * alle durch Leerzeichen getrennte Worte enthält.
   *
   * @param node Der Knoten, dessen Text zurückgeben werden soll.
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
   * Gibt den Text eines Knotens zurück und prüft, ob er eine gültige URL ist.
   * <p>
   * Der Text wird als gültige URL angesehen, wenn er keinen Backslash enthält.
   *
   * @param node Der Knoten, dessen Text als URL zurückgeben werden soll.
   *
   * @return Der Text des Knotens.
   * @throws RegainException Wenn der Knoten keinen Text hat oder wenn der Text
   *         keine gültige URL ist.
   */  
  public static String getTextAsUrl(Node node) throws RegainException {
  	String asString = getText(node);
  	
  	// Prüfen, ob der Text einen Backslash enthält
  	if (asString.indexOf('\\') != -1) {
      throw new RegainException("Text of node '" + node.getNodeName()
        + "' is not a valid URL. Use normal slashes instead of backslashes: '"
        + asString + "'");
  	}
  	
  	return asString;
  }



  /**
   * Gibt den Text eines Knotens zurück.
   *
   * @param node Der Knoten, dessen Text zurückgeben werden soll.
   *
   * @return Der Text des Knotens.
   * @throws RegainException Wenn der Knoten keinen Text hat.
   */  
  public static String getText(Node node) throws RegainException {
    return getText(node, true);
  }



  /**
   * Gibt den Text eines Knotens zurück.
   * <p>
   * Wenn der Knoten keinen Text hat, dann entscheidet <CODE>mandatory</CODE> darüber,
   * ob eine Exception geworfen (<CODE>mandatory</CODE> ist <CODE>true</CODE>) oder
   * ob <CODE>null</CODE> zurückgegeben werden soll (<CODE>mandatory</CODE> ist
   * <CODE>false</CODE>)
   *
   * @param node Der Knoten, dessen Text zurückgeben werden soll.
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
    Node textNode = getChild(node, "#text", mandatory);
    if (textNode == null) {
      return null;
    }
    
    String value = textNode.getNodeValue();
    if ((value == null) || (value.length() == 0)) {
      if (mandatory) {
        throw new RegainException("Node '" + node.getNodeName() + "' has no text");
      } else {
        return null;
      }
    } else {
      return value;
    }
  }



  /**
   * Gibt den Kindknoten mit einem bestimmten Namen zurück.
   * <p>
   * Falls der Knoten mehrere Kinder mit diesem Namen hat, so wird das erste Kind
   * zurückgegeben.
   *
   * @param node Der Knoten, dessen Kind zurückgegeben werden soll.
   * @param childNodeName Der Name des Kindknotens.
   *
   * @return Der Kindknoten
   * @throws RegainException Wenn der Knoten kein Kind mit diesem Namen hat.
   */  
  public static Node getChild(Node node, String childNodeName) throws RegainException {
    return getChild(node, childNodeName, true);
  }



  /**
   * Gibt den Kindknoten mit einem bestimmten Namen zurück.
   * <p>
   * Falls der Knoten mehrere Kinder mit diesem Namen hat, so wird das erste Kind
   * zurückgegeben.
   * <p>
   * Falls der Knoten kein Kind mit diesem Name hat, dann entscheidet
   * <CODE>mandatory</CODE> darüber, ob eine Exception geworfen
   * (<CODE>mandatory</CODE> ist <CODE>true</CODE>) oder ob <CODE>null</CODE>
   * zurückgegeben werden soll (<CODE>mandatory</CODE> ist <CODE>false</CODE>)
   *
   * @param node Der Knoten, dessen Kind zurückgegeben werden soll.
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
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node child = list.item(i);
      if (child.getNodeName().equals(childNodeName)) {
        return child;
      }
    }

    // No such child found
    if (mandatory) {
      throw new RegainException("Node '" + node.getNodeName()
        + "' must have a child named '" + childNodeName + "'!");
    } else {
      return null;
    }
  }



  /**
   * Gibt alle Kindknoten mit einem bestimmten Namen zurück.
   * <p>
   * Falls der Knoten kein Kind mit diesem Namen hat, so wird ein leeres Array
   * zurückgegeben.
   *
   * @param node Der Knoten, dessen Kinder zurückgegeben werden soll.
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
   * Gibt den Attributwert eines Knotens als <CODE>boolean</CODE> zurück.
   *
   * @param node Der Knoten, dessen Attribut zurückgegeben werden soll.
   * @param attributeName Der Name des Attributs, das zurückgegeben werden soll.
   *
   * @return Den Attributwert als <CODE>boolean</CODE>.
   * @throws RegainException Falls der Knoten kein solches Attribut hat oder
   *         falls der Wert des Attributs weder <CODE>true</CODE> noch
   *         <CODE>false</CODE> ist.
   */  
  public static boolean getAttributeAsBoolean(Node node, String attributeName)
    throws RegainException
  {
    String asString = getAttribute(node, attributeName);

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
   * Gibt den Attributwert eines Knotens als <CODE>int</CODE> zurück.
   *
   * @param node Der Knoten, dessen Attribut zurückgegeben werden soll.
   * @param attributeName Der Name des Attributs, das zurückgegeben werden soll.
   *
   * @return Den Attributwert als <CODE>int</CODE>.
   * @throws RegainException Falls der Knoten kein solches Attribut hat oder
   *         falls der Wert des Attributs keine Zahl ist.
   */  
  public static int getAttributeAsInt(Node node, String attributeName)
    throws RegainException
  {
    String asString = getAttribute(node, attributeName);

    try {
      return Integer.parseInt(asString);
    }
    catch (NumberFormatException exc) {
      throw new RegainException("Attribute '" + attributeName + "' of node '"
        + node.getNodeName() + "' must be a number: '" + asString + "'");
    }
  }

  
  /**
   * Gibt den Attributwert eines Knotens zurück.
   *
   * @param node Der Knoten, dessen Attribut zurückgegeben werden soll.
   * @param attributeName Der Name des Attributs, das zurückgegeben werden soll.
   *
   * @return Den Attributwert.
   * @throws RegainException Falls der Knoten kein solches Attribut hat.
   */  
  public static String getAttribute(Node node, String attributeName)
    throws RegainException
  {
    return getAttribute(node, attributeName, true);
  }


  /**
   * Gibt den Attributwert eines Knotens zurück.
   *
   * @param node Der Knoten, dessen Attribut zurückgegeben werden soll.
   * @param attributeName Der Name des Attributs, das zurückgegeben werden soll.
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
    Node attributeNode = node.getAttributes().getNamedItem(attributeName);
    if (attributeNode == null) {
      if (mandatory) {
        throw new RegainException("Node '" + node.getNodeName()
          + "' has no attribute '" + attributeName + "'");
      } else {
        return null;
      }
    }

    return attributeNode.getNodeValue();
  }

}
