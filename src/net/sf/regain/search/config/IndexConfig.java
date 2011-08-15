/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2011-08-16 20:54:38 +0200 (Di, 16 Aug 2011) $
 *   $Author: benjaminpick $
 * $Revision: 529 $
 */
package net.sf.regain.search.config;

import java.util.Properties;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.access.SearchAccessController;
import net.sf.regain.search.results.SortingOption;
import org.apache.lucene.util.Version;

/**
 * The configuration for one index.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class IndexConfig {

  /** Default list of index fields to search in. */
  protected static final String[] DEFAULT_SEARCH_FIELD_LIST = {"content", "title", "headlines", "location", "filename"};
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
  /** Flag for highlighting of the search terms in the results */
  private boolean mShouldHighlight;
  /** Index is used as parent cover */
  private boolean mParent;
  /** Index has a parent */
  private boolean mHasParent;
  /** Name of the parent (only used if mHasParent is true) */
  private String mParentName;
  /** The sorting options for the results. */
  private SortingOption[] mSortingOptions;
  private boolean mShowSortFieldContent;

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
   * @param shouldHighlight The flag for highlighting of the search terms in the results 
   * @throws RegainException If loading the SearchAccessController failed.
   */
  public IndexConfig(String name, String directory, String openInNewWindowRegex,
          boolean useFileToHttpBridge, String[] searchFieldList, String[][] rewriteRules,
          String searchAccessControllerClass, String searchAccessControllerJar,
          Properties searchAccessControllerConfig, boolean shouldHighlight,
          SortingOption[] sortingOptions, boolean showSortFieldContent)
          throws RegainException {
    this.mName = name;
    this.mDirectory = directory;
    this.mOpenInNewWindowRegex = openInNewWindowRegex;
    this.mUseFileToHttpBridge = useFileToHttpBridge;
    this.mSearchFieldList = searchFieldList;
    this.mRewriteRules = rewriteRules;
    this.mParent = true;
    this.mHasParent = false;

    if (searchAccessControllerClass != null) {
      this.mSearchAccessController = (SearchAccessController) RegainToolkit.createClassInstance(searchAccessControllerClass,
              SearchAccessController.class,
              searchAccessControllerJar);

      if (searchAccessControllerConfig == null) {
        searchAccessControllerConfig = new Properties();
      }
      this.mSearchAccessController.init(searchAccessControllerConfig);
    }
    this.mShouldHighlight = shouldHighlight;
    this.mSortingOptions = sortingOptions;
    this.mShowSortFieldContent = showSortFieldContent;
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

    if (mSearchFieldList != null) {
      return mSearchFieldList;
    } else {
      return DEFAULT_SEARCH_FIELD_LIST;
    }

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

  /**
   * Gets the flag for highlighting of the search terms
   * 
   * @return the flag for highlighting
   */
  public boolean getShouldHighlight() {
    return mShouldHighlight;
  }

  /**
   * Is index has a parent index?
   *  
   * @return true if index has a parent
   * 		   false if index has no parent
   */
  public boolean hasParent() {
    return mHasParent;
  }

  /**
   * Is index a child of a parent index?
   *   
   * @return true if index is a parent index
   * 		   false if index is not a parent index
   */
  public boolean isParent() {
    return mParent;
  }

  /**
   * Set index as parent if parent is "true" otherwise 
   * set false
   * 
   * @param parent is index a parent index?
   */
  public void setParent(String parent) {
    if ("true".equals(parent)) {
      this.mParent = true;
    } else {
      this.mParent = false;
    }
  }

  /**
   * Gets the name of the parent index.
   * 
   * @return The name of the parent index.
   */
  public String getParentName() {
    return mParentName;
  }

  /**
   * Set the name of the parent index
   * 
   * @param parentName Name of the parent index
   */
  public void setParentName(String parentName) {
    this.mParentName = parentName;
    this.mHasParent = true;
  }

  /**
   * @return the sortingOptions
   */
  public SortingOption[] getSortingOptions() {
    return mSortingOptions;
  }

  /**
   * @return the mShowSortFieldContent
   */
  public boolean getShowSortFieldContent() {
    return mShowSortFieldContent;
  }
}
