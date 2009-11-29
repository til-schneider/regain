/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2010 Thomas Tesche, Til Schneider
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
 * Contact: Thomas Tesche: www.thtesche.com, Til Schneider: info@murfman.de
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-08-06 16:04:27 +0200 (Wed, 06 Aug 2008) $
 *   $Author: thtesche $
 * $Revision: 325 $
 */
package net.sf.regain.search.results;

import java.util.HashSet;
import net.sf.regain.RegainException;
import org.apache.lucene.search.SortField;

/**
 * Holds the sorting for the current search request.
 *
 * @author Thomas Tesche: www.thtesche.com
 */
public class SortingOption implements Comparable {

  public static String RELEVANCE = "relevance";
  public static String FILENAME = "filename_sort";
  public static String SIZE = "size";
  public static String MIMETYPE = "mimetype";
  public static String LAST_MODIFIED = "last-modified";
  public static String TITLE = "title_sort";
  public static String PATH = "path_sort";
  private String sortFieldName;
  private boolean reverse;
  private int sortType;
  private String description;
  private int id;

  /**
   * Creates the class from a given sorting option.
   *
   * @param orderFromRequest the order String from the request. The orderFromRequest
   *        is build from the field and the sorting direction e.g. size_desc.
   */
  public SortingOption(String orderFromRequest) {

    if (orderFromRequest == null || orderFromRequest.length() == 0) {
      setRelevance();

    } else if (orderFromRequest.equalsIgnoreCase(RELEVANCE)) {
      setRelevance();

    } else if (orderFromRequest.startsWith(FILENAME)) {
      sortFieldName = FILENAME;
      reverse = isReverse(orderFromRequest);
      sortType = SortField.STRING;

    } else if (orderFromRequest.startsWith(SIZE)) {
      sortFieldName = SIZE;
      reverse = isReverse(orderFromRequest);
      sortType = SortField.LONG;

    } else if (orderFromRequest.startsWith(MIMETYPE)) {
      sortFieldName = MIMETYPE;
      reverse = isReverse(orderFromRequest);
      sortType = SortField.STRING;

    } else if (orderFromRequest.startsWith(LAST_MODIFIED)) {
      sortFieldName = LAST_MODIFIED;
      reverse = isReverse(orderFromRequest);
      sortType = SortField.STRING;

    } else if (orderFromRequest.startsWith(TITLE)) {
      sortFieldName = TITLE;
      reverse = isReverse(orderFromRequest);
      sortType = SortField.STRING;

    } else if (orderFromRequest.startsWith(PATH)) {
      sortFieldName = PATH;
      reverse = isReverse(orderFromRequest);
      sortType = SortField.STRING;

    } else {
      setRelevance();

    }
  }

  /**
   * Creates the instance from given config entries.
   *
   * @param description the description shown in the dropdown box
   * @param field the field name for sorting
   * @param order asc|desc indicates the sorting direction
   * @param id the position in the dropdown list
   */
  public SortingOption(String description, String field, String order, int id) throws RegainException {
    this.description = description;
    this.sortFieldName = field;
    this.id = id;
    this.reverse = !isReverse(order);
    HashSet<String> fieldHash = new HashSet<String>();
    fieldHash.add(RELEVANCE);
    fieldHash.add(FILENAME);
    fieldHash.add(SIZE);
    fieldHash.add(MIMETYPE);
    fieldHash.add(LAST_MODIFIED);
    fieldHash.add(TITLE);
    fieldHash.add(PATH);

    // Check the validity of the new option
    if (this.description == null || this.description.length() < 3 ||
            !fieldHash.contains(this.sortFieldName) || this.id < 0) {
      throw new RegainException("The sorting option is not properly defined.");
    }
  }

  private boolean isReverse(String option) {
    return option.toLowerCase().endsWith("desc");
  }

  private void setRelevance() {
    sortFieldName = RELEVANCE;
    reverse = false;
    sortType = SortField.FLOAT;

  }

  /**
   * @return the sortField
   */
  public String getSortFieldName() {
    return sortFieldName;
  }

  /**
   * @return the sortField plus the sorting direction
   */
  public String getFieldNameAndOrder() {
    if(!this.reverse) {
      return sortFieldName + "_desc";
    } else {
      return sortFieldName + "_asc";
    }
  }


  /**
   * @return the reverse
   */
  public boolean isReverse() {
    return reverse;
  }

  /**
   * @return the sortType
   */
  public int getSortType() {
    return sortType;
  }

  public SortField getSortField() {
    if (sortFieldName.equalsIgnoreCase(RELEVANCE)) {
      return SortField.FIELD_SCORE;
    } else {
      return new SortField(getSortFieldName(), getSortType(), isReverse());
    }
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Sort this option against another option.
   *
   * @param sortingOption
   * @return
   * @throws ClassCastException
   */
  public int compareTo(Object sortingOption) throws ClassCastException {
    if (!(sortingOption instanceof SortingOption)) {
      throw new ClassCastException("Couldn't compare SortingOptions.");
    }
    int otherId = ((SortingOption) sortingOption).getId();
    return this.id - otherId;
  }

  @Override
  public String toString() {
    return "id: " + id + ", description: " + description + ", field: " + sortFieldName +
            ", isReverse: " + reverse;
  }
}
