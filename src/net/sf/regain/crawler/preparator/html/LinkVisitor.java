/*
 * regain/2 - A file search engine providing plenty of formats
 * Copyright (C) 2004, 2088  Til Schneider, Thomas Tesche
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
 * Author: Thomas Tesche, cluster:Consult, Gletscherstr.13, 16341 Panketal
 *         +49 30 946 300 34, thomas.tesche@thtesche.com
 */
package net.sf.regain.crawler.preparator.html;

import java.util.ArrayList;
import org.htmlparser.Tag;
import org.htmlparser.visitors.NodeVisitor;

/**
 *
 * @author thtesche
 */
public class LinkVisitor extends NodeVisitor {

  ArrayList<Tag> mExtLinks = new ArrayList<Tag>();
  ArrayList<Tag> mExtFrames = new ArrayList<Tag>();

  public ArrayList<Tag> getLinks() {
    return mExtLinks;
  }

  public ArrayList<Tag> getFrames() {
    return mExtFrames;
  }

  @Override
  public void visitTag(Tag tag) {

    String name = tag.getTagName();
    if ("a".equalsIgnoreCase(name)) {
      String hrefValue = tag.getAttribute("href");
      if (hrefValue != null) {
        mExtLinks.add(tag);
      } else {
        //System.err.println("Corrupt html found!");
      }
    }

    if ("frame".equalsIgnoreCase(name)) {
      String srcValue = tag.getAttribute("src");
      if (srcValue != null) {
        mExtFrames.add(tag);
      } else {
        //System.err.println("Corrupt html found!");
      }
    }
  }
}
