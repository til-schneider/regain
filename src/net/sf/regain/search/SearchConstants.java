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
 *  $RCSfile: SearchConstants.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/SearchConstants.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search;

/**
 * Some constants used in several search classes.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public interface SearchConstants {

  /**
   * The name of the attribute containing the current hit Document within the
   * list tag.
   */
  public static final String ATTR_CURRENT_HIT = "currentHit";

  /**
   * The name of the attribute containing the current hit score within the
   * list tag.
   */
  public static final String ATTR_CURRENT_HIT_SCORE = "currentHitScore";

  /**
   * The name of the attribute containing the index of the current hit within
   * the list tag.
   */
  public static final String ATTR_CURRENT_HIT_INDEX = "currentHitIndex";
  
  /** The name of the parameter containing the index of the first result to show. */
  public static final String PARAM_FROM_RESULT = "fromresult";

  /** The name of the parameter containing the maximum number of results to show. */
  public static final String PARAM_MAX_RESULTS = "maxresults";

}
