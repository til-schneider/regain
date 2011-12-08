package net.sf.regain.ui.server.taglib;

import net.sf.regain.util.sharedtag.taglib.SharedTagWrapperTag;

/**
 * Wrapper class for Content Type override
 * @author b
 *
 */
public class ContenttypeTag extends SharedTagWrapperTag
{
  public ContenttypeTag() {
    super(new net.sf.regain.search.sharedlib.ContenttypeTag());
  }
  
  public void setContentType(String mime) {
    getNestedTag().setParameter("contentType", mime);
  }
}
