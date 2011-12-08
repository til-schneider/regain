package net.sf.regain.search.sharedlib.hit;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.results.SearchResults;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.lucene.document.Document;

public class FilenameTag extends AbstractHitTag
{
  /**
   * Generates the tag.
   *
   * @param request The page request.
   * @param response The page response.
   * @param hit The current search hit.
   * @param hitIndex The index of the hit.
   * @throws RegainException If there was an exception.
   */
  protected void printEndTag(PageRequest request, PageResponse response,
    Document hit, int hitIndex)
    throws RegainException
  {
    // Get the search results
    SearchResults results = SearchToolkit.getSearchResults(request);

    String url = results.getHitUrl(hitIndex);
    String title;
    int lastSlash = url.lastIndexOf("/");
    if (lastSlash == -1) {
      title = url;
    } else {
      title = RegainToolkit.urlDecode(url.substring(lastSlash + 1), RegainToolkit.INDEX_ENCODING);
    }
    
    response.print(title);
  }

}
