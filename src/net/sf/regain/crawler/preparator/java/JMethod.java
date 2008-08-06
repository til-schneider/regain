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
 * Represents methods of a Java class
 * <p>
 * @author  Renuka Sindhgatta, http://www.oreillynet.com/pub/au/2554
 *
 * Modifications by Thomas Tesche, http://clusterconsult.thtesche.com/
 */
public class JMethod {

  private String methodName = null;
  private ArrayList parameters = new ArrayList();
  private String codeBlock = null;
  private String returnType = null;

  /**
   *  Returns the name of the method.
   * 
   * @return the method name
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Sets the name of a method.
   * 
   * @param methodName
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Returns the parameters of a method.
   * 
   * @return all parameters
   */
  public ArrayList getParameters() {
    return parameters;
  }

  /**
   * Sets the parameters of a method.
   * 
   * @param parameters
   */
  public void setParameters(ArrayList parameters) {
    this.parameters = parameters;
  }

  /**
   * Returns the code block of a method
   * 
   * @return the code block
   */
  public String getCodeBlock() {
    return codeBlock;
  }

  /**
   * Sets the code block for the method.
   * 
   * @param codeBlock
   */
  public void setCodeBlock(String codeBlock) {
    this.codeBlock = codeBlock;
  }

  /**
   * Returns the return type of a method.
   * 
   * @return the return type
   */
  public String getReturnType() {
    return returnType;
  }

  /** 
   * Sets the return type of method.
   * 
   * @param returnType
   */
  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }
}
