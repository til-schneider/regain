/*
 * CVS information:
 *  $RCSfile: CheckTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/server/taglib/CheckTag.java,v $
 *     $Date: 2005/03/02 14:36:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.ui.server.taglib;

import net.sf.regain.util.sharedtag.taglib.SharedTagWrapperTag;

/**
 * Taglib wrapper for the shared list tag.
 *
 * @see net.sf.regain.search.sharedlib.ListTag
 * @author Til Schneider, www.murfman.de
 */
public class CheckTag extends SharedTagWrapperTag {

  /**
   * Creates a new instance of ListTag.
   */
  public CheckTag() {
    super(new net.sf.regain.search.sharedlib.CheckTag());
  }


  /**
   * Sets the URL to redirect to if there is no index.
   * 
   * @param noIndexUrl The URL to redirect to if there is no index.
   */
  public void setNoIndexUrl(String noIndexUrl) {
    getNestedTag().setParameter("noIndexUrl", noIndexUrl);
  }

  
  /**
   * Sets the URL to redirect to if there is no query.
   * 
   * @param noQueryUrl The URL to redirect to if there is no query.
   */
  public void setNoQueryUrl(String noQueryUrl) {
    getNestedTag().setParameter("noQueryUrl", noQueryUrl);
  }
  
}
