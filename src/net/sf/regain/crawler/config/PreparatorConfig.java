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
package net.sf.regain.crawler.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.regain.util.ChainedHashMap;

/**
 * The configuration of a preparator. Consists of a set of sections which
 * contain a set of key-value-pairs.
 *
 * Is the order of sections relevant? Currently, it isn't guaranteed.
 * Ignores Case of section name.
 *
 * @author Til Schneider, www.murfman.de
 */
public class PreparatorConfig implements Iterable<Map.Entry<String, Map<String, String>>>{

  /**
   * The sections of this config.<br>
   * String: The name of the section<br>
   * Map<String,String>: The key-value-pairs of the section.
   * <p>
   * We use no HashMap here, because two sections may have the same name.
   */
  private ChainedHashMap<String, Map<String, String>> mSectionList;

  public PreparatorConfig()
  {
    mSectionList = new ChainedHashMap<String, Map<String, String>>(5, 5);
  }

  /**
   * Adds a section to the config
   *
   * @param name The name of the section to add.
   * @param content The key-value-pairs of the section to add.
   */
  void addSection(String name, Map<String, String> content) {
    mSectionList.put(name.toLowerCase(), content);
  }


  /**
   * Gets the number of sections this config has.
   *
   * @return The number of sections.
   */
  public int getSectionCount() {
    return mSectionList.size();
  }


  /**
   * Gets the first section with the given name.
   *
   * @param name The name of the sections
   * @return The first section with the given name or <code>null</code> if there
   *         is no such section.
   */
  public Map<String, String> getSectionWithName(String name) {
    return mSectionList.get(name.toLowerCase());
  }


  /**
   * Gets all sections with the given name.
   *
   * @param name The name of the sections
   * @return All sections with the given name.
   */
  public List<Map<String, String>> getSectionsWithNameList(String name) {
    if (name == null)
        return new ArrayList<Map<String, String>>();

    return mSectionList.getList(name.toLowerCase());
  }


  /**
   * Iterate through all section-Parameter pairs.
   * Section names will be lowercase.
   * The order that the sections are given isn't guaranteed.
   *
   * Usage:
   *
   * <code>
   * for (Map.Entry<String, Map<String, String>> section : config)
   * {
   *    String sectionName = section.getEntry();
   *    Map<String,Strin> params = section.getValue();
   * }
   * </code>
   */
  public Iterator<Entry<String, Map<String, String>>> iterator()
  {
    return mSectionList.iterator();
  }

  // -------------- For Backward compability only ... --------------

  /**
   * Gets all sections with the given name.
   *
   * @param name The name of the sections
   * @return All sections with the given name.
   * @deprecated Use getSectionsWithNameList() instead.
   */
  @Deprecated
  public Map<String, String>[] getSectionsWithName(String name) {
    List<Map<String, String>> list = getSectionsWithNameList(name);

    // Convert the list into an array
    Map<String,String>[] sectionArr = new Map[list.size()];
    sectionArr = list.toArray(sectionArr);
    return sectionArr;
  }


  @Deprecated
  private String[] sectionNames = null;
  @Deprecated
  private Map<String, String>[] sectionContents = null;

  @Deprecated
  private void fillSectionCache()
  {
    int i = 0;
    sectionNames = new String[mSectionList.size()];
    sectionContents = new Map[mSectionList.size()];
    for (Map.Entry<String, Map<String, String>> entry : this)
    {
      sectionNames[i] = entry.getKey();
      sectionContents[i] = entry.getValue();
      i++;
    }
  }


  /**
   * Gets the name of a section.
   *
   * @param index The index of the section.
   * @return The name of the section.
   * @deprecated This method assumes that the config won't change anymore. Use iterator() instead.
   */
  @Deprecated
  public String getSectionName(int index) {
    if (sectionNames == null)
      fillSectionCache();

    return sectionNames[index];
  }


  /**
   * Gets the key-value-pairs of a section.
   *
   * @param index The index of the section.
   * @return The key-value-pairs of the section.
   * @deprecated This method assumes that the config won't change anymore. Use iterator() instead.
   */
  @Deprecated
  public Map<String, String> getSectionContent(int index) {
    if (sectionContents == null)
      fillSectionCache();

    return sectionContents[index];
  }

}
