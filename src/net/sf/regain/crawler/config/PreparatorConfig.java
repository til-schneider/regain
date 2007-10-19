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
 *     $Date: 2005-02-22 10:25:37 +0100 (Di, 22 Feb 2005) $
 *   $Author: til132 $
 * $Revision: 24 $
 */
package net.sf.regain.crawler.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * The configuration of a preparator. Consists of a set of sections which
 * contain a set of key-value-pairs.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class PreparatorConfig {

  /**
   * The sections of this config. A section is a Object[] with two elements:<br>
   * 0 (String): The name of the section<br>
   * 1 (Map): The key-value-pairs of the section.
   * <p>
   * We use no HashMap here, because two sections may have the same name.
   */
  private ArrayList mSectionList;
  
  
  /**
   * Adds a section to the config
   * 
   * @param name The name of the section to add.
   * @param content The key-value-pairs of the section to add.
   */
  void addSection(String name, Map content) {
    if (mSectionList == null) {
      mSectionList = new ArrayList();
    }
    
    mSectionList.add(new Object[] { name, content });
  }
  
  
  /**
   * Gets the number of sections this config has.
   * 
   * @return The number of sections.
   */
  public int getSectionCount() {
    if (mSectionList == null) {
      return 0;
    } else {
      return mSectionList.size();
    }
  }
  
  
  /**
   * Gets the name of a section.
   * 
   * @param index The index of the section.
   * @return The name of the section.
   */
  public String getSectionName(int index) {
    Object[] section = (Object[]) mSectionList.get(index);
    return (String) section[0];
  }

  
  /**
   * Gets the key-value-pairs of a section.
   * 
   * @param index The index of the section.
   * @return The key-value-pairs of the section.
   */
  public Map getSectionContent(int index) {
    Object[] section = (Object[]) mSectionList.get(index);
    return (Map) section[1];
  }

  
  /**
   * Gets the first section with the given name.
   * 
   * @param name The name of the sections
   * @return The first section with the given name or <code>null</code> if there
   *         is no such section.
   */
  public Map getSectionWithName(String name) {
    for (int i = 0; i < getSectionCount(); i++) {
      if (name.equalsIgnoreCase(getSectionName(i))) {
        return getSectionContent(i);
      }
    }
    
    return null;
  }
  
  
  /**
   * Gets all sections with the given name.
   * 
   * @param name The name of the sections
   * @return All sections with the given name.
   */
  public Map[] getSectionsWithName(String name) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < getSectionCount(); i++) {
      if (name.equalsIgnoreCase(getSectionName(i))) {
        list.add(getSectionContent(i));
      }
    }
    
    // Convert the list into an array
    Map[] sectionArr = new Map[list.size()];
    list.toArray(sectionArr);
    return sectionArr;
  }
  
}
