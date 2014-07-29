package net.sf.regain.ui.server.taglib.stats;

import net.sf.regain.util.sharedtag.taglib.SharedTagWrapperTag;

/**
 * Taglib wrapper for the shared query tag.
 *
 * @see net.sf.regain.search.sharedlib.stats.QueryTag
 *
 * @author Til Schneider, www.murfman.de
 */
public class QueryTag extends SharedTagWrapperTag {

  /**
   * Creates a new instance of QueryTag.
   */
  public QueryTag() {
    super(new net.sf.regain.search.sharedlib.stats.QueryTag());
  }

}