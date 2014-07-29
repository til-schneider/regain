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
