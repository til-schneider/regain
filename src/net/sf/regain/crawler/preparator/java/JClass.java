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

/**
 * Represents a Java class
 * <p>
 * @author  Renuka Sindhgatta, http://www.oreillynet.com/pub/au/2554
 *
 * Modifications by Thomas Tesche, http://clusterconsult.thtesche.com/
 */
public class JClass {

  private String className = null;
  private boolean isInterface = false;
  private ArrayList methodDeclarations = new ArrayList();
  private ArrayList innerClasses = new ArrayList();
  private String superClass = null;
  private ArrayList interfaces = new ArrayList();

  /**
   * Returns the class name.
   * 
   * @return the class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Sets the class name.
   * 
   * @param className the name of the class
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * Returns if the class is an interface.
   * 
   * @return true if the class is an interface
   */
  public boolean isInterface() {
    return isInterface;
  }

  public void setIsInterface(boolean isInterface) {
    this.isInterface = isInterface;
  }

  /**
   * Returns the method declarations.
   * 
   * @return method declarations
   */
  public ArrayList getMethodDeclarations() {
    return methodDeclarations;
  }

  /**
   * Sets the method declarations.
   * 
   * @param methodDeclarations
   */
  public void setMethodDeclarations(ArrayList methodDeclarations) {
    this.methodDeclarations = methodDeclarations;
  }

  /** 
   * Gets the inner classes of a class.
   * 
   * @return the inner classes
   */
  public ArrayList getInnerClasses() {
    return innerClasses;
  }

  /**
   * Sets the inner classes.
   * 
   * @param innerClasses
   */
  public void setInnerClasses(ArrayList innerClasses) {
    this.innerClasses = innerClasses;
  }

  /** 
   * Returns the super class.
   * 
   * @return the super class
   */
  public String getSuperClass() {
    return superClass;
  }

  /**
   * Sets the super class.
   * 
   * @param superClass
   */
  public void setSuperClass(String superClass) {
    this.superClass = superClass;
  }

  /**
   * Return all interfaces
   * 
   * @return the interfaces
   */
  public ArrayList getInterfaces() {
    return interfaces;
  }

  /**
   * Sets the interfaces.
   * 
   * @param interfaces
   */
  public void setInterfaces(ArrayList interfaces) {
    this.interfaces = interfaces;
  }
}
