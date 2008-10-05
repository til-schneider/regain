/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-09-17 22:15:48 +0200 (Mi, 17 Sep 2008) $
 *   $Author: thtesche $
 * $Revision: 337 $
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
  
   /**
   * Gets the names of all indexes in the Configuration file.
   * 
   * @return The names of the indexes or an empty array if no
   *         index was specified in the configuration file.
   */
  public String[] getAllIndexNameArr();  

}
