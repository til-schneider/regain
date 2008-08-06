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
 *     $Date: 2008-03-16 20:50:37 +0100 (So, 16 Mär 2008) $
 *   $Author: thtesche $
 * $Revision: 281 $
 */
package net.sf.regain.crawler.preparator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.sf.regain.RegainException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Parses Java source code for indexing
 * <p>
 * The following information will be extracted:
 * class name, member names, comments (javadoc) 
 *
 * @author  Renuka Sindhgatta, http://www.oreillynet.com/pub/au/2554
 * 
 * Modifications by Thomas Tesche, http://clusterconsult.thtesche.com/
 */
public class JavaParser {

  private ASTParser _parser = ASTParser.newParser(AST.JLS3);
  private CompilationUnit _unit = null;
  private JClass _class = null;

  /**
   * Member sets the source for the parser and creates a compilation unit
   * 
   * @param sourceStr the source code
   * @throws net.sf.regain.RegainException if building of a compilation unit failed
   */
  public void setSource(String sourceStr) throws RegainException {

    try {
      _parser.setKind(ASTParser.K_COMPILATION_UNIT);
      _parser.setSource(sourceStr.toString().toCharArray());
      _unit = (CompilationUnit) _parser.createAST(null);

    } catch (Exception ex) {
      throw new RegainException("Error parsing Java file", ex);
    }

  }

  public ArrayList getImportDeclarations() {
    List imports = _unit.imports();
    if (imports.size() == 0) {
      return null;
    }
    ArrayList importDecl = new ArrayList();
    ListIterator iter = imports.listIterator();
    while (iter.hasNext()) {
      ImportDeclaration decl = (ImportDeclaration) iter.next();
      importDecl.add(decl.getName().toString());
    }
    return importDecl;
  }

  public ArrayList getComments() {
    List comments = _unit.getCommentList();
    if (comments.size() == 0) {
      return null;
    }
    ArrayList javaDocComments = new ArrayList();
    ListIterator iterator = comments.listIterator();
    while (iterator.hasNext()) {
      Object object = iterator.next();
      if (object instanceof Javadoc) {
        String comment = ((Javadoc) object).getComment();

        javaDocComments.add(comment);
      }
    }
    return javaDocComments;
  }

  public JClass getDeclaredClass() {
    List types = _unit.types();
    ListIterator typeIter = types.listIterator(0);
    if (typeIter.hasNext()) {
      TypeDeclaration object = (TypeDeclaration) typeIter.next();
      _class = new JClass();
      setClassInformation(_class, object);
      return _class;
    }
    return null;
  }

  private void setClassInformation(JClass cls, TypeDeclaration object) {

    cls.setIsInterface(object.isInterface());
    cls.setClassName(object.getName().getIdentifier());

    SimpleType _superClass = (SimpleType) object.getSuperclassType();
    if (_superClass != null) {
      cls.setSuperClass(_superClass.getName().getFullyQualifiedName());
    }

    List interfaceLst = object.superInterfaceTypes();

    ListIterator interfaces = interfaceLst.listIterator();
    while (interfaces.hasNext()) {
      {
        SimpleType sin = (SimpleType) interfaces.next();
        cls.getInterfaces().add(sin.toString());
      }

    }
    addMethods(cls, object);

    TypeDeclaration[] innerTypes = object.getTypes();
    for (int i = 0; i < innerTypes.length; i++) {
      JClass innerCls = new JClass();
      setClassInformation(innerCls, innerTypes[i]);
      cls.getInnerClasses().add(innerCls);
    }

  }

  private void addMethods(JClass cls, TypeDeclaration object) {
    MethodDeclaration[] met = object.getMethods();
    for (int i = 0; i < met.length; i++) {
      MethodDeclaration dec = met[i];
      JMethod method = new JMethod();
      method.setMethodName(dec.getName().toString());
      Type returnType = dec.getReturnType2();
      if (returnType != null) {
        method.setReturnType(returnType.toString());
      }
      Block d = dec.getBody();
      if (d == null) {
        continue;
      }
      method.setCodeBlock(d.toString());
      List param = dec.parameters();
      ListIterator paramList = param.listIterator();
      while (paramList.hasNext()) {
        SingleVariableDeclaration sin = (SingleVariableDeclaration) paramList.next();
        method.getParameters().add(sin.getType().toString());
      }

      cls.getMethodDeclarations().add(method);
    }
  }
}
 
