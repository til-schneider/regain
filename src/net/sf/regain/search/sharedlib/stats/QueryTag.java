/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-03-08 09:52:08 +0100 (Di, 08 Mrz 2005) $
 *   $Author: til132 $
 * $Revision: 59 $
 */
package net.sf.regain.search.sharedlib.stats;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates the query as String.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class QueryTag extends SharedTag {

  /**
   * Called when the parser reaches the end tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    response.print(SearchToolkit.getSearchQuery(request));
  }

}
