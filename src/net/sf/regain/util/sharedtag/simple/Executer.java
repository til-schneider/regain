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
 *  $RCSfile: Executer.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/util/sharedtag/simple/Executer.java,v $
 *     $Date: 2005/03/01 15:59:39 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.util.sharedtag.simple;

import java.util.ArrayList;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

/**
 * A tree node that generates a response when executed.
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class Executer {
  
  /** The children of this node. */
  private ArrayList mChildExecuterList;
  
  
  /**
   * Adds a child to this node.
   * 
   * @param child The child to add.
   */
  public void addChildExecuter(Executer child) {
    if (mChildExecuterList == null) {
      mChildExecuterList = new ArrayList();
    }
    mChildExecuterList.add(child);
  }
  
  
  /**
   * Gets the number of children of this node.
   * 
   * @return The number of children of this node.
   */
  protected int childCount() {
    if (mChildExecuterList == null) {
      return 0;
    } else {
      return mChildExecuterList.size();
    }
  }
  
  
  /**
   * Gets a child node.
   * 
   * @param index The index of the node to get.
   * @return The wanted child node.
   */
  protected Executer getChild(int index) {
    return (Executer) mChildExecuterList.get(index);
  }


  /**
   * Executes this node.
   * 
   * @param request The request.
   * @param response The response.
   * @throws RegainException If executing failed.
   */
  public abstract void execute(PageRequest request, PageResponse response)
    throws RegainException;
  
  
  /**
   * Executes all child nodes
   * 
   * @param request The request.
   * @param response The response.
   * @throws RegainException If executing failed.
   */
  protected void executeChildren(PageRequest request, PageResponse response)
    throws RegainException
  {
    for (int i = 0; i < childCount(); i++) {
      getChild(i).execute(request, response);
    }
  }


  /**
   * Prints the children to System.out.
   * 
   * @param prefix The prefix to put in front of every line.
   */
  protected void printChildren(String prefix) {
    prefix += "  ";
    for (int i = 0; i < childCount(); i++) {
      getChild(i).printTag(prefix);
    }
  }


  /**
   * Prints this tag to System.out.
   */
  public void printTag() {
    printTag("");
  }
  
  
  /**
   * Prints this tag to System.out.
   * 
   * @param prefix The prefix to put in front of every line.
   */
  public abstract void printTag(String prefix);
  
}
