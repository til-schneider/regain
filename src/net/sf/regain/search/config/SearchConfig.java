/*
 * CVS information:
 *  $RCSfile: SearchConfig.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/config/SearchConfig.java,v $
 *     $Date: 2005/02/26 14:51:10 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.search.config;

/**
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public interface SearchConfig {

  /**
   * Gets the configuration for an index.
   * 
   * @param indexName The name of the index to get the config for.
   * @return The configuration for the wanted index or <code>null</code> if
   *         there is no such index configured.
   */
  public IndexConfig getIndexConfig(String indexName);
  
  /**
   * Gets the name of the default index.
   * 
   * @return The name of the default index or <code>null</code> if no default
   *         index was specified.
   */
  public String getDefaultIndexName();
  
}
