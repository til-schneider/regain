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
 *     $Date: 2005-03-09 16:47:07 +0100 (Mi, 09 Mrz 2005) $
 *   $Author: til132 $
 * $Revision: 60 $
 */
package net.sf.regain.util.sharedtag.simple;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * An executer node that executes a SharedTag.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SharedTagExecuter extends Executer {
  
  /** The shared tag that is executed by this executer. */
  private SharedTag mTag;
  

  /**
   * Creates a new instance of SharedTagExecuter.
   * 
   * @param tag The shared tag that is executed by this executer.
   */
  public SharedTagExecuter(SharedTag tag) {
    mTag = tag;
  }


  /**
   * Executes this node.
   * 
   * @param request The request.
   * @param response The response.
   * @throws RegainException If executing failed.
   */
  public void execute(PageRequest request, PageResponse response)
    throws RegainException
  {
    // Set the context
    mTag.setContext(request);
    
    // Print the start tag
    int result = mTag.printStartTag(request, response);
    
    // Print the body
    while (result == SharedTag.EVAL_TAG_BODY) {
      executeChildren(request, response);
      result = mTag.printAfterBody(request, response);
    }
    
    // Print the end tag
    mTag.printEndTag(request, response);
    
    // Unset the context
    mTag.unsetContext();
  }
  

  /**
   * Prints this tag to System.out.
   * 
   * @param prefix The prefix to put in front of every line.
   */
  public void printTag(String prefix) {
    if (childCount() == 0) {
      System.out.println(prefix + "<" + mTag.getTagName() + "/>");
    } else {
      System.out.println(prefix + "<" + mTag.getTagName() + ">");
      printChildren(prefix);
      System.out.println(prefix + "</" + mTag.getTagName() + ">");
    }
  }
  
}
