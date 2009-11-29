/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2009-10-11 22:38:58 +0200 (So, 11 Okt 2009) $
 *   $Author: thtesche $
 * $Revision: 412 $
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
  @Override
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    response.print(SearchToolkit.getSearchQuery(request));
  }

}
