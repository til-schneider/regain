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
package net.sf.regain.ui.server.taglib.hit;

import net.sf.regain.util.sharedtag.taglib.SharedTagWrapperTag;

/**
 * Taglib wrapper for the shared typeicon tag.
 *
 * @see net.sf.regain.search.sharedlib.hit.TypeiconTag
 *
 * @author Til Schneider, www.murfman.de
 */
public class TypeiconTag extends SharedTagWrapperTag {

  /**
   * Creates a new instance of PathTag.
   */
  public TypeiconTag() {
    super(new net.sf.regain.search.sharedlib.hit.TypeiconTag());
  }


  /**
   * Sets the path where the type icons are located.
   *
   * @param imgpath The path where the type icons are located.
   */
  public void setImgpath(String imgpath) {
    getNestedTag().setParameter("imgpath", imgpath);
  }


  /**
   * Sets the extension used by the type icons (default is "gif").
   *
   * @param iconextension the extension used by the type icons.
   */
  public void setIconextension(String iconextension) {
    getNestedTag().setParameter("iconextension", iconextension);
  }

}