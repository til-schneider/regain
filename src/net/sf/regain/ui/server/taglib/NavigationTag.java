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
 *     $Date: 2005-02-24 16:31:49 +0100 (Do, 24 Feb 2005) $
 *   $Author: til132 $
 * $Revision: 30 $
 */
package net.sf.regain.ui.server.taglib;

import net.sf.regain.util.sharedtag.taglib.SharedTagWrapperTag;

/**
 * Taglib wrapper for the shared navigation tag.
 *
 * @see net.sf.regain.search.sharedlib.NavigationTag
 *
 * @author Til Schneider, www.murfman.de
 */
public class NavigationTag extends SharedTagWrapperTag {

  /**
   * Creates a new instance of NavigationTag.
   */
  public NavigationTag() {
    super(new net.sf.regain.search.sharedlib.NavigationTag());
  }


  /**
   * Sets the message to use for labeling the back link.
   * 
   * @param msgBack The message to use for labeling the back link.
   */
  public void setMsgBack(String msgBack) {
    getNestedTag().setParameter("msgBack", msgBack);
  }


  /**
   * Sets the message to use for labeling the forward link.
   * 
   * @param msgForward The message to use for labeling the forward link.
   */
  public void setMsgForward(String msgForward) {
    getNestedTag().setParameter("msgForward", msgForward);
  }


  /**
   * Sets the URL of the page where the links should point to.
   * 
   * @param targetPage The URL of the page where the links should point to.
   */
  public void setTargetPage(String targetPage) {
    getNestedTag().setParameter("targetPage", targetPage);
  }


  /**
   * Sets the style sheet class to use for the link tags.
   * 
   * @param styleSheetClass The style sheet class to use for the link tags.
   */
  public void setClass(String styleSheetClass) {
    getNestedTag().setParameter("class", styleSheetClass);
  }
  
}
