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
package net.sf.regain.crawler.preparator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import net.sf.regain.RegainException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

/**
 * Parses Java source code for indexing <p> The following information will be
 * extracted: class name, member names, comments (javadoc)
 *
 * @author Renuka Sindhgatta, http://www.oreillynet.com/pub/au/2554
 *
 * @author Thomas Tesche <thomas.tesche@clustersystems.de>
 */
public class JavaParser {

  private ASTParser _parser = ASTParser.newParser(AST.JLS3);
  private CompilationUnit _unit = null;
  private JClassEnum _class = null;

  /**
   * Member sets the source for the parser and creates a compilation unit
   *
   * @param sourceStr the source code
   * @throws net.sf.regain.RegainException if building of a compilation unit
   * failed
   */
  public void setSource(String sourceStr) throws RegainException {

    try {
      _parser.setKind(ASTParser.K_COMPILATION_UNIT);
      _parser.setSource(sourceStr.toString().toCharArray());

      // In order to parse 1.5 code, some compiler options need to be set to 1.5
      Map options = JavaCore.getOptions();
      JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
      _parser.setCompilerOptions(options);

      _unit = (CompilationUnit) _parser.createAST(null);

    } catch (Exception ex) {
      throw new RegainException("Error parsing Java file", ex);
    }

  }

  public ArrayList getImportDeclarations() {
    List imports = _unit.imports();
    if (imports.isEmpty()) {
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

  public List getComments() {
    List comments = _unit.getCommentList();
    if (comments.isEmpty()) {
      return null;
    }
    List<String> javaDocComments = new ArrayList<String>();
    ListIterator<Object> iterator = comments.listIterator();
    while (iterator.hasNext()) {
      Object object = iterator.next();
      if (object instanceof Javadoc) {
        String comment = ((Javadoc) object).getComment();
        javaDocComments.add(comment);
      }
    }
    return javaDocComments;
  }

  public JClassEnum getDeclaredClass() {
    List types = _unit.types();
    ListIterator typeIter = types.listIterator(0);
    if (typeIter.hasNext()) {
      Object object = typeIter.next();
      _class = new JClassEnum();
      if (object instanceof TypeDeclaration) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) object;
        setClassInformation(_class, typeDeclaration);
        return _class;

      } else if (object instanceof EnumDeclaration) {
        EnumDeclaration enumDeclaration = (EnumDeclaration) object;
        setEnumInformation(_class, enumDeclaration);
        return _class;

      }
    }
    return null;
  }

  private void setEnumInformation(JClassEnum cls, EnumDeclaration declaration) {

    cls.setType(Type.ENUM);
    cls.setClassName(declaration.getName().getIdentifier());

    List interfaceList = declaration.superInterfaceTypes();

    ListIterator interfaces = interfaceList.listIterator();
    while (interfaces.hasNext()) {
      {
        SimpleType sin = (SimpleType) interfaces.next();
        cls.getInterfaces().add(sin.toString());
      }
    }

    List<EnumConstantDeclaration> constantsList = declaration.enumConstants();
    for (EnumConstantDeclaration constant : constantsList) {
      cls.getConstants().add(constant);

    }
  }

  private void setClassInformation(JClassEnum cls, TypeDeclaration declaration) {

    if (declaration.isInterface()) {
      cls.setType(Type.INTERFACE);
    } else {
      cls.setType(Type.CLASS);
    }

    cls.setClassName(declaration.getName().getIdentifier());

    SimpleType _superClass = (SimpleType) declaration.getSuperclassType();
    if (_superClass != null) {
      cls.setSuperClass(_superClass.getName().getFullyQualifiedName());
    }

    List interfaceLst = declaration.superInterfaceTypes();

    ListIterator interfaces = interfaceLst.listIterator();
    while (interfaces.hasNext()) {
      {
        SimpleType sin = (SimpleType) interfaces.next();
        cls.getInterfaces().add(sin.toString());
      }

    }
    addMethods(cls, declaration);

    TypeDeclaration[] innerTypes = declaration.getTypes();
    for (int i = 0; i < innerTypes.length; i++) {
      JClassEnum innerCls = new JClassEnum();
      setClassInformation(innerCls, innerTypes[i]);
      cls.getInnerClasses().add(innerCls);
    }

  }

  private void addMethods(JClassEnum cls, TypeDeclaration object) {
    MethodDeclaration[] met = object.getMethods();
    for (int i = 0; i < met.length; i++) {
      MethodDeclaration dec = met[i];
      JMethod method = new JMethod();
      method.setMethodName(dec.getName().toString());
      org.eclipse.jdt.core.dom.Type returnType = dec.getReturnType2();
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
