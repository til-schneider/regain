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
 *  $RCSfile: ExecuterParser.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/util/sharedtag/simple/ExecuterParser.java,v $
 *     $Date: 2005/11/21 10:19:29 $
 *   $Author: til132 $
 * $Revision: 1.7 $
 */
package net.sf.regain.util.sharedtag.simple;

import java.io.File;
import java.util.HashMap;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.util.sharedtag.SharedTag;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Parses JSP code and creates an Executer tree.
 *
 * @see Executer
 * @author Til Schneider, www.murfman.de
 */
public class ExecuterParser {
  
  /** The regex that matches a include tag. */
  private RE mIncludeRegex;
  
  /** The regex that matches JSP code. */
  private RE mJspCodeRegex;
  
  /** The regex that matches a JSP tag. */
  private RE mJspTagRegex;
  
  /** The regex that matches a parameter. */
  private RE mParamRegex;
  
  /** The registered SharedTag namespaces. */
  private static HashMap mNamespaceHash;
  
  
  /**
   * Creates a new instance of ExecuterParser.
   *
   * @throws RegainException If creating the regexes failed.
   */
  public ExecuterParser() throws RegainException {
    try {
      // NOTE: The regex objects are not static in order to be able to use
      //       several ExecuterParser instances in concurrent threads
      mIncludeRegex = new RE("<%@include\\s*file=\"([^\"]*)\"\\s*%>");
      mJspCodeRegex = new RE("<%([^%]*)%>");
      mJspTagRegex = new RE("<([\\w/]*):(\\w*)(([^>\"]*\"[^\"]*\")*\\w*/?)>");
      mParamRegex = new RE("(\\w+)\\s*=\\s*\"([^\"]*)\"");
    } catch (RESyntaxException exc) {
      throw new RegainException("Creating the ExecuterParser regexes failed", exc);
    }
  }
  
  
  /**
   * Registers a SharedTag namespace.
   * 
   * @param namespace The namespace to register.
   * @param packageName The package where the SharedTags classes of this
   *        namespace are in.
   */
  public static void registerNamespace(String namespace, String packageName) {
    if (mNamespaceHash == null) {
      mNamespaceHash = new HashMap();
    }
    
    mNamespaceHash.put(namespace, packageName);
  }
  

  /**
   * Parses the JSP code.
   * 
   * @param baseDir The base directory where to search for the JSP file.
   * @param filename The name of the JSP file to parse.
   * @return An Executer tree that can execute the JSP page.
   * @throws RegainException If parsing failed.
   */
  public synchronized Executer parse(File baseDir, String filename)
    throws RegainException
  {
    String jspCode = prepareJspCode(baseDir, filename);
    
    // Get the position where the real content starts
    int startPos = jspCode.indexOf("<html>");
    if (startPos == -1) {
      startPos = 0;
    }

    // Parse the content
    Executer executer = new TextExecuter("");
    int pos = parse(executer, jspCode, startPos);
    if (pos < jspCode.length()) {
      throw new RegainException("Last taglib tag is not closed! (offset: " + pos + ")");
    }
    
    return executer;
  }

  
  /**
   * Prepares the JSP code before it is parsed for shared tags. 
   * 
   * @param baseDir The base directory where to search for the JSP file.
   * @param filename The name of the JSP file to prepare.
   * @return The prepared code.
   * @throws RegainException If loading the requested file failed.
   */
  private String prepareJspCode(File baseDir, String filename)
    throws RegainException
  {
    File file = new File(baseDir, filename);
    if (! file.exists()) {
      throw new RegainException("JSP file does not exist: " + file.getAbsolutePath());
    }
    String jspCode = RegainToolkit.readStringFromFile(file);
    
    // Add all inludes
    // NOTE: For performance reasons we create only a StringBuffer if a match is
    //       found. Otherwise the jspCode String remains the same.
    int pos = 0;
    StringBuffer buffer = null;
    while (mIncludeRegex.match(jspCode, pos)) {
      if (buffer == null) {
        buffer = new StringBuffer(jspCode.length());
      }
      
      // Extract the values from the regex
      // NOTE: We do this before other pages are parsed, because the regex is
      //       shared.
      int startPos = mIncludeRegex.getParenStart(0);
      int endPos = mIncludeRegex.getParenEnd(0);
      String incFilename = mIncludeRegex.getParen(1);
      
      // Add the text before
      buffer.append(jspCode.substring(pos, startPos));
      
      // Include the file
      buffer.append(prepareJspCode(baseDir, incFilename));
      
      pos = endPos;
    }
    if (buffer != null) {
      // Add the text after the last include
      buffer.append(jspCode.substring(pos, jspCode.length()));
      jspCode = buffer.toString();
    }
    
    // Remove all JSP code
    pos = 0;
    buffer = null;
    while (mJspCodeRegex.match(jspCode, pos)) {
      if (buffer == null) {
        buffer = new StringBuffer(jspCode.length());
      }

      // Add the text before
      buffer.append(jspCode.substring(pos, mJspCodeRegex.getParenStart(0)));

      pos = mJspCodeRegex.getParenEnd(0);
    }
    if (buffer != null) {
      // Add the text after the last JSP code
      buffer.append(jspCode.substring(pos, jspCode.length()));
      jspCode = buffer.toString();
    }
    
    return jspCode;
  }
  

  /**
   * Parses the content of an executer.
   * 
   * @param parent The executer where to add the extracted child executers.
   * @param jspCode The JSP code to parse.
   * @param pos The position where to start parsing.
   * @return The position where parsing stopped.
   * @throws RegainException If parsing failed.
   */
  private int parse(Executer parent, String jspCode, int pos)
    throws RegainException
  {
    String startNamespace = null;
    String startTagName = null;
    while (mJspTagRegex.match(jspCode, pos)) {
      // System.out.println("Pattern matches");
      
      // Add the text since the last pos
      addText(parent, jspCode, pos, mJspTagRegex.getParenStart(0));

      // There is a tag -> Extract the interesting values
      String namespace = mJspTagRegex.getParen(1);
      String tagName = mJspTagRegex.getParen(2);
      String params = mJspTagRegex.getParen(3);
      int tagEndPos = mJspTagRegex.getParenEnd(0);
      
      // Check whether this is a end tag
      if (! namespace.startsWith("/")) {
        // It's a start tag
        // System.out.println("Tag starts " + namespace + ":" + tagName);
        
        // Create the shared tag
        SharedTag tag = createSharedTag(namespace, tagName, params);
        SharedTagExecuter child = new SharedTagExecuter(tag);
        parent.addChildExecuter(child);
        
        // Check whether the start tag is closed immediately
        if (params.endsWith("/")) {
          // The start tag is closed immediately -> Set the pos to the end of the tag
          pos = tagEndPos;
        } else {
          // The start tag is not closed immediately -> Remember it
          startNamespace = namespace;
          startTagName = tagName;

          // Parse the tag content
          pos = parse(child, jspCode, tagEndPos);
        }
      } else {
        // It's a end tag
        if (startNamespace == null) {
          // System.out.println("End of parent tag");

          // There was no start tag -> It must be the end tag of the parent
          return mJspTagRegex.getParenStart(0);
        } else {
          // System.out.println("End tag " + namespace + ":" + tagName);
          
          // The current tag is finished -> Check namespace and tag name
          namespace = namespace.substring(1); // Remove the leading /
          if (! namespace.equals(startNamespace) || ! tagName.equals(startTagName)) {
            throw new RegainException("End tag " + namespace + ":" + tagName
                + " does not match to start tag " + startNamespace + ":" + startTagName);
          }

          startNamespace = null;
          startTagName = null;
          
          pos = tagEndPos;
        }
      }
      // System.out.println("Next pos " + pos);
    }

    // System.out.println("Adding trailing text");

    // There is no taglib tag any more
    // -> Add the text since the last pos to the end
    addText(parent, jspCode, pos, jspCode.length());
    return jspCode.length();
  }


  /**
   * Adds a text executer to another executer.
   * 
   * @param parent The executer where to add the TextExecuter.
   * @param jspCode The JSP code from where to extract the text.
   * @param startPos The start position of the text.
   * @param endPos The end position of the text.
   */
  private void addText(Executer parent, String jspCode, int startPos,
    int endPos)
  {
    if (startPos < endPos) {
      String text = jspCode.substring(startPos, endPos);
      parent.addChildExecuter(new TextExecuter(text));
    }
  }


  /**
   * Creates a SharedTag.
   * 
   * @param namespace The namespace of the tag.
   * @param tagName The tag's name.
   * @param params The tag's parameters
   * @return The created tag.
   * @throws RegainException If creating the tag failed.
   */
  private SharedTag createSharedTag(String namespace, String tagName,
    String params)
    throws RegainException
  {
    String packageName = (String) mNamespaceHash.get(namespace);
    if (packageName == null) {
      throw new RegainException("Unknown tag namespace: '" + namespace + "'");
    }
    
    String className = packageName;
    
    // Add the sub packages (E.g. stats_size)
    int linePos;
    String cutTagName = tagName;
    while ((linePos = cutTagName.indexOf('_')) != -1) {
      className += "." + cutTagName.substring(0, linePos);
      cutTagName = cutTagName.substring(linePos + 1);
    }
    
    // Add the className
    className += "." + Character.toUpperCase(cutTagName.charAt(0))
      + cutTagName.substring(1) + "Tag";
    
    // Get the tag class
    Class tagClass;
    try {
      tagClass = Class.forName(className);
    }
    catch (ClassNotFoundException exc) {
      throw new RegainException("Class for tag " + namespace + ":" + tagName
          + " not found: " + className, exc);
    }

    // Create the tag instance
    SharedTag tag;
    try {
      tag = (SharedTag) tagClass.newInstance();
    }
    catch (Exception exc) {
      throw new RegainException("Creating tag instance for tag " + namespace
          + ":" + tagName + " could not be created: " + className, exc);
    }
    
    // Set the params
    int pos = 0;
    while (mParamRegex.match(params, pos)) {
      String name = mParamRegex.getParen(1);
      String value = mParamRegex.getParen(2);
      tag.setParameter(name, value);
      
      pos = mParamRegex.getParenEnd(0);
    }
    
    return tag;
  }
  
}
