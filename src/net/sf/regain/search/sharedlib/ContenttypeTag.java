package net.sf.regain.search.sharedlib;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Override the Content Type for this request.
 *
 * contentType: The MIME Content-Type ("text/html" if not given)
 * @author b
 */
public class ContenttypeTag extends SharedTag
{
  public void printEndTag(PageRequest request, PageResponse response)
  throws RegainException
{
    String mime = getParameter("contentType", true);

    if (!mime.isEmpty())
      response.setHeader("Content-Type", mime);
}
}
