/*
 * CVS information:
 *  $RCSfile: IndexConfig.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/config/IndexConfig.java,v $
 *     $Date: 2005/02/11 11:24:56 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.search.config;

/**
 * The configuration for one index.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class IndexConfig {

  /** Default list of index fields to search in. */
  protected static final String[] DEFAULT_SEARCH_FIELD_LIST
    = { "content", "title", "headlines" };
  
  /** The name of the index. */
  private String mName;
  
  /** The directory where the index is located. */
  private String mDirectory;
  
  /**
   * The regular expression that identifies URLs that should be opened in a new
   * window.
   */
  private String mOpenInNewWindowRegex;
  
  /**
   * The index fields to search by default.
   * <p>
   * NOTE: The user may search in other fields also using the "field:"-operator.
   * Read the
   * <a href="http://jakarta.apache.org/lucene/docs/queryparsersyntax.html">lucene query syntax</a>
   * for details.
   */
  private String[] mSearchFieldList;
  
  /**
   * The URL rewrite rules.
   * <p>
   * Contains pairs of URL prefixes: The first prefix will be replaced by the
   * second.
   * <p>
   * E.g.:
   * <pre>
   * new String[][] {
   *   { "file://c:/webcontent", "http://www.mydomain.de" },
   *   { "file://n:/docs", "file://///fileserver/public/docs" },
   * };
   * </pre>
   */
  private String[][] mRewriteRules;


  /**
   * Creates a new instance of IndexConfig.
   * 
   * @param name The name of the index.
   * @param directory The directory where the index is located.
   * @param openInNewWindowRegex The regular expression that identifies URLs
   *        that should be opened in a new window.
   * @param searchFieldList The index fields to search by default.
   * @param rewriteRules The URL rewrite rules. Contains pairs of URL prefixes:
   *        The first prefix will be replaced by the second.
   */
  public IndexConfig(String name, String directory, String openInNewWindowRegex,
    String[] searchFieldList, String[][] rewriteRules)
  {
    mName = name;
    mDirectory = directory;
    mOpenInNewWindowRegex = openInNewWindowRegex;
    mSearchFieldList = searchFieldList;
    mRewriteRules = rewriteRules;
  }
  
  
  /**
   * Gets the name of the index.
   * 
   * @return The name of the index.
   */
  public String getName() {
    return mName;
  }

  
  /**
   * Gets the directory where the index is located.
   * 
   * @return The directory where the index is located.
   */
  public String getDirectory() {
    return mDirectory;
  }
  
  
  /**
   * Gets the regular expression that identifies URLs that should be opened in
   * a new window.
   * 
   * @return The regular expression that identifies URLs that should be opened
   *         in a new window.
   */
  public String getOpenInNewWindowRegex() {
    return mOpenInNewWindowRegex;
  }
  
  
  /**
   * Gets the index fields to search by default.
   * <p>
   * NOTE: The user may search in other fields also using the "field:"-operator.
   * Read the
   * <a href="http://jakarta.apache.org/lucene/docs/queryparsersyntax.html">lucene query syntax</a>
   * for details.
   * 
   * @return The index fields to search by default.
   */
  public String[] getSearchFieldList() {
    return mSearchFieldList;
  }

  
  /**
   * Gets the URL rewrite rules.
   * <p>
   * The returned array contains pairs of URL prefixes: The first prefix will be
   * replaced by the second.
   * <p>
   * E.g.:
   * <pre>
   * new String[][] {
   *   { "file://c:/webcontent", "http://www.mydomain.de" },
   *   { "file://n:/docs", "file://///fileserver/public/docs" },
   * };
   * </pre>
   *
   * @return The URL rewrite rules. May be null;
   */
  public String[][] getRewriteRules() {
    return mRewriteRules;
  }
  
}
