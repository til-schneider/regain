/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-04-15 10:47:04 +0200 (Fr, 15 Apr 2005) $
 *   $Author: til132 $
 * $Revision: 132 $
 */
package net.sf.regain.search.config;

import java.util.Properties;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.access.SearchAccessController;

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
  
  /** Whether the file-to-http-bridge should be used. */
  private boolean mUseFileToHttpBridge;
  
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

  /** The SearchAccessController to use. May be <code>null</code>. */
  private SearchAccessController mSearchAccessController;


  /**
   * Creates a new instance of IndexConfig.
   * 
   * @param name The name of the index.
   * @param directory The directory where the index is located.
   * @param openInNewWindowRegex The regular expression that identifies URLs
   *        that should be opened in a new window.
   * @param useFileToHttpBridge Whether the file-to-http-bridge should be used.
   *        See {@link #getUseFileToHttpBridge()} for details.
   * @param searchFieldList The index fields to search by default.
   * @param rewriteRules The URL rewrite rules. Contains pairs of URL prefixes:
   *        The first prefix will be replaced by the second.
   * @param searchAccessControllerClass The class name of the
   *        {@link SearchAccessController} to use.
   * @param searchAccessControllerJar The name of jar file to load the
   *        {@link SearchAccessController} from.
   * @param searchAccessControllerConfig The configuration for the
   *        {@link SearchAccessController}.
   * @throws RegainException If loading the SearchAccessController failed.
   */
  public IndexConfig(String name, String directory, String openInNewWindowRegex,
    boolean useFileToHttpBridge, String[] searchFieldList, String[][] rewriteRules,
    String searchAccessControllerClass, String searchAccessControllerJar,
    Properties searchAccessControllerConfig)
    throws RegainException
  {
    mName = name;
    mDirectory = directory;
    mOpenInNewWindowRegex = openInNewWindowRegex;
    mUseFileToHttpBridge = useFileToHttpBridge;
    mSearchFieldList = searchFieldList;
    mRewriteRules = rewriteRules;
    
    if (searchAccessControllerClass != null) {
      mSearchAccessController = (SearchAccessController)
        RegainToolkit.createClassInstance(searchAccessControllerClass,
                                          SearchAccessController.class,
                                          searchAccessControllerJar);
      
      if (searchAccessControllerConfig == null) {
        searchAccessControllerConfig = new Properties();
      }
      mSearchAccessController.init(searchAccessControllerConfig);
    }
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
   * Gets whether the file-to-http-bridge should be used for file-URLs.
   * <p>
   * Mozilla browsers have a security mechanism that blocks loading file-URLs
   * from pages loaded via http. To be able to load files from the search
   * results, regain offers the file-to-http-bridge that provides all files that
   * are listed in the index via http.
   * 
   * @return Whether the file-to-http-bridge should be used.
   */
  public boolean getUseFileToHttpBridge() {
    return mUseFileToHttpBridge;
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


  /**
   * Gets the SearchAccessController to use. Returns <code>null</code> if no
   * SearchAccessController should be used.
   * 
   * @return The SearchAccessController. 
   */
  public SearchAccessController getSearchAccessController() {
    return mSearchAccessController;
  }

}
