package net.sf.regain.ui.server.taglib.input;

import net.sf.regain.util.sharedtag.taglib.SharedTagWrapperTag;

/**
 * Taglib wrapper for the shared fieldlist tag.
 *
 * @see net.sf.regain.search.sharedlib.input.FieldlistTag
 *
 * @author Til Schneider, www.murfman.de
 */
public class FieldlistTag extends SharedTagWrapperTag {

  /**
   * Creates a new instance of FieldlistTag.
   */
  public FieldlistTag() {
    super(new net.sf.regain.search.sharedlib.input.FieldlistTag());
  }


  /**
   * Sets the name of the field to created the list for.
   *
   * @param field The name of the field to created the list for.
   */
  public void setField(String field) {
    getNestedTag().setParameter("field", field);
  }


  /**
   * Sets the name of the field to created the list for.
   *
   * @param allMsg The message to show for the item that ignores this field.
   */
  public void setAllMsg(String allMsg) {
    getNestedTag().setParameter("allMsg", allMsg);
  }

}