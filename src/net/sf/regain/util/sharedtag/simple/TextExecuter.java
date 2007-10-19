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
 *     $Date: 2005-03-01 17:04:30 +0100 (Di, 01 Mrz 2005) $
 *   $Author: til132 $
 * $Revision: 46 $
 */
package net.sf.regain.util.sharedtag.simple;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

/**
 * An executer node that prints some text.
 *
 * @author Til Schneider, www.murfman.de
 */
public class TextExecuter extends Executer {
  
  /** The text to print. */
  private String mText;
  
  
  /**
   * Creates a new instance of TextExecuter.
   * 
   * @param text The text to print.
   */
  public TextExecuter(String text) {
    mText = text;
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
    executeChildren(request, response);
    response.print(mText);
  }
  

  /**
   * Prints this tag to System.out.
   * 
   * @param prefix The prefix to put in front of every line.
   */
  public void printTag(String prefix) {
    printChildren(prefix);
    System.out.println(prefix + "Text");
  }
  
}
