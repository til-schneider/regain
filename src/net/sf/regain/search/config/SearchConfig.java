/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-08-18 10:01:39 +0200 (Do, 18 Aug 2005) $
 *   $Author: til132 $
 * $Revision: 172 $
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
   * Gets the names of the default indexes.
   * 
   * @return The names of the default indexes or an empty array if no default
   *         index was specified.
   */
  public String[] getDefaultIndexNameArr();
  
}
