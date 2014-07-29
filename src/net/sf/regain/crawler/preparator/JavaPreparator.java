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
package net.sf.regain.crawler.preparator;

import net.sf.regain.crawler.preparator.java.*;
import java.util.ArrayList;
import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;

/**
 * Prepares Java source code for indexing <p> The following information will be
 * extracted: class name, member names, return types , code blocks
 *
 * @author Thomas Tesche, cluster:Consult, http://www.thtesche.com/
 */
public class JavaPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of JavaPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public JavaPreparator() throws RegainException {
    super(new String[]{"text/java"});
  }

  /**
   * Prepares the document for indexing
   *
   * @param rawDocument the document
   *
   * @throws RegainException if preparation goes wrong
   */
  @Override
  public void prepare(RawDocument rawDocument) throws RegainException {

    ArrayList<String> contentParts = new ArrayList<String>();
    ArrayList<String> titleParts = new ArrayList<String>();

    try {
      // Creates the parser
      JavaParser parser = new JavaParser();
      parser.setSource(rawDocument.getContentAsString());
      JClassEnum cls = parser.getDeclaredClass();

      titleParts.add(cls.getType().toString() + " : " + cls.getClassName());
      // extract the class info (including inner classes)
      contentParts.add(extractClassInfo(cls, false).toString());

      setTitle(concatenateStringParts(titleParts, Integer.MAX_VALUE));
      setCleanedContent(concatenateStringParts(contentParts, Integer.MAX_VALUE));

    } catch (Exception ex) {
      throw new RegainException("Error parsing Java file: " + rawDocument.getUrl(), ex);
    }

  }

  /**
   * Extract different information from a class. <p>
   *
   * @param cls - the class from which the infos will be extracted
   * @param innerClass - is the class an inner class
   *
   * @return the extracted infos as a StringBuffer
   */
  private StringBuffer extractClassInfo(JClassEnum cls, boolean innerClass) {

    StringBuffer strBuffer = new StringBuffer();

    //For each class add Class Name field
    String class_interface = "";
    if (cls.getType() == Type.INTERFACE) {
      class_interface = " Interface: ";
    } else if (innerClass) {
      class_interface = ", InnerClass: ";
    } else if (cls.getType() == Type.CLASS) {
      class_interface = " Class: ";
    } else if (cls.getType() == Type.ENUM) {
      class_interface = " Enum: ";
    }

    strBuffer.append(class_interface).append(cls.getClassName());

    String superCls = cls.getSuperClass();
    if (superCls != null) //Add the class it extends as extends field
    {
      strBuffer.append(", Superclass: ").append(superCls);
    }
    // Add interfaces it implements
    ArrayList interfaces = cls.getInterfaces();
    for (int i = 0; i < interfaces.size(); i++) {
      strBuffer.append(", implements: ").append((String) interfaces.get(i));
    }

    // Add details on methods declared
    strBuffer.append(extractMethodInfo(cls));

    if (cls.getType() == Type.ENUM) {
      strBuffer.append(extractEnumInfo(cls));
    }

    // Examine inner classes and extract the same details as for the class
    ArrayList innerCls = cls.getInnerClasses();
    for (int i = 0; i < innerCls.size(); i++) {
      strBuffer.append(extractClassInfo((JClassEnum) innerCls.get(i), true));
    }

    return strBuffer;
  }

  /**
   * Extract constants info from Enums.
   *
   * @param the enum to examine
   * @return the result as a StringBuffer
   */
  private StringBuffer extractEnumInfo(JClassEnum cls) {
    StringBuffer strBuffer = new StringBuffer();

    // get all constants
    ArrayList<EnumConstantDeclaration> constants = cls.getConstants();
    for (int i = 0; i < constants.size(); i++) {
      strBuffer.append(" ").append(constants.get(i).getName().getIdentifier());
    }

    return strBuffer;
  }

  /**
   * Extract method details for the class.
   *
   * @param the class to examine
   *
   * @return the result as a StringBuffer
   */
  private StringBuffer extractMethodInfo(JClassEnum cls) {

    StringBuffer strBuffer = new StringBuffer();

    // get all methods
    ArrayList methods = cls.getMethodDeclarations();
    for (int i = 0; i < methods.size(); i++) {
      JMethod method = (JMethod) methods.get(i);

      strBuffer.append(", ");
      // Add return type field
      String returnType = method.getReturnType();
      if (returnType != null) {
        if (!returnType.equalsIgnoreCase("void")) {
          strBuffer.append(" ").append(returnType);
        }
      }
      // Add method name field
      strBuffer.append(" ").append(method.getMethodName()).append("(");

      ArrayList params = method.getParameters();
      for (int k = 0; k < params.size(); k++) // For each method add parameter types
      {
        if (k != 0) {
          strBuffer.append(" ");
        }
        strBuffer.append((String) params.get(k));
      }
      strBuffer.append(") ");
      String code = method.getCodeBlock();
      if (code != null) //add the method code block
      {
        strBuffer.append(code);
      }
    }

    return strBuffer;
  }

  private StringBuffer extractImportDeclarations(JavaParser parser) {

    StringBuffer strBuffer = new StringBuffer();

    ArrayList imports = parser.getImportDeclarations();
    if (imports == null) {
      return strBuffer;
    }
    for (int i = 0; i < imports.size(); i++) //add import declarations as keyword
    {
      strBuffer.append((String) imports.get(i));
    }

    return strBuffer;
  }

  private StringBuffer extractComments(JavaParser parser) {

    StringBuffer strBuffer = new StringBuffer();

    ArrayList comments = parser.getComments();
    if (comments == null) {
      return strBuffer;
    }
    for (int i = 0; i < comments.size(); i++) {
      String docComment = (String) comments.get(i);
      strBuffer.append(docComment);
    }

    return strBuffer;
  }
}
