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
 *     $Date: 2011-08-05 21:13:49 +0200 (Fr, 05 Aug 2011) $
 *   $Author: benjaminpick $
 * $Revision: 512 $
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
   * We use no HashMap here, because two sections may have the same name. --> ChainedHashtable?
   */
  private ArrayList<Object []> mSectionList;
  
  
  /**
   * Adds a section to the config
   * 
   * @param name The name of the section to add.
   * @param content The key-value-pairs of the section to add.
   */
  void addSection(String name, Map<String, String> content) {
    if (mSectionList == null) {
      mSectionList = new ArrayList<Object []>();
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
    Object[] section = mSectionList.get(index);
    return (String) section[0];
  }

  
  /**
   * Gets the key-value-pairs of a section.
   * 
   * @param index The index of the section.
   * @return The key-value-pairs of the section.
   */
  public Map<String, String> getSectionContent(int index) {
    Object[] section = mSectionList.get(index);
    return (Map<String, String>) section[1];
  }

  
  /**
   * Gets the first section with the given name.
   * 
   * @param name The name of the sections
   * @return The first section with the given name or <code>null</code> if there
   *         is no such section.
   */
  public Map<String, String> getSectionWithName(String name) {
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
    ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
    for (int i = 0; i < getSectionCount(); i++) {
      if (name.equalsIgnoreCase(getSectionName(i))) {
        list.add(getSectionContent(i));
      }
    }
    
    // Convert the list into an array
    Map[] sectionArr = new Map[list.size()];
    sectionArr = list.toArray(sectionArr);
    return sectionArr;
  }
  
}
