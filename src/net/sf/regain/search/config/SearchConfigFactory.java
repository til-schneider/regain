/*
 * CVS information:
 *  $RCSfile: SearchConfigFactory.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/config/SearchConfigFactory.java,v $
 *     $Date: 2005/04/11 08:16:25 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.config;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;

/**
 * Creates a SearchConfig instance.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public interface SearchConfigFactory {

  /**
   * Creates the configuration of the search mask.
   * 
   * @param request The page request. May be used to read init parameters.
   * @return The configuration of the search mask.
   * @throws RegainException If loading failed.
   */
  public SearchConfig createSearchConfig(PageRequest request)
    throws RegainException;

}
