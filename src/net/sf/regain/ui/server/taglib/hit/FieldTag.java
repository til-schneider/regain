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
 *     $Date: 2011-09-20 16:25:38 +0200 (Di, 20 Sep 2011) $
 *   $Author: benjaminpick $
 * $Revision: 536 $
 */
package net.sf.regain.ui.server.taglib.hit;

import net.sf.regain.util.sharedtag.taglib.SharedTagWrapperTag;

/**
 * Taglib wrapper for the shared field tag.
 *
 * @see net.sf.regain.search.sharedlib.hit.FieldTag
 *
 * @author Til Schneider, www.murfman.de
 */
public class FieldTag extends SharedTagWrapperTag {

  /**
   * Creates a new instance of FieldTag.
   */
  public FieldTag() {
    super(new net.sf.regain.search.sharedlib.hit.FieldTag());
  }


  /**
   * Sets the name of the index field to generate.
   * 
   * @param field The name of the index field to generate.
   */
  public void setField(String field) {
    getNestedTag().setParameter("field", field);
  }
  
  /**
   * Override highlight settings of index
   * @param bool
   */
  public void setHighlight(String bool)
  {
    getNestedTag().setParameter("highlight", bool);
  }

}