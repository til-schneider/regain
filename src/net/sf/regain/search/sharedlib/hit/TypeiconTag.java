/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004  Til Schneider
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Til Schneider, info@murfman.de
 */
package net.sf.regain.search.sharedlib.hit;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import net.sf.regain.RegainException;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.results.SearchResults;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.lucene.document.Document;

/**
 * Generates an img showing the hit's type using its extension.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>imgpath</code>: The path where the type icons are located.</li>
 * <li><code>iconextension</code>: The extension used by the type icons (default is "gif").</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public class TypeiconTag extends AbstractHitTag {

  /**
   * A map holding for a lowercase extension (String) whether there is an icon
   * for that extension (Boolean).
   */
  private static HashMap<String, Boolean> mExtensionAvailableMap = new HashMap<String, Boolean>();


  /**
   * Generates the tag.
   *
   * @param request The page request.
   * @param response The page response.
   * @param hit The current search hit.
   * @param hitIndex The index of the hit.
   * @throws RegainException If there was an exception.
   */
  @Override
  protected void printEndTag(PageRequest request, PageResponse response,
    Document hit, int hitIndex)
    throws RegainException
  {
    SearchResults results = SearchToolkit.getSearchResults(request);
    String imgpath = getParameter("imgpath");
    String iconextension = getParameter("iconextension");
    if (iconextension == null) {
      iconextension = "gif";
    }

    // Get the extension of this hit
    String extension = null;
    String url   = results.getHitUrl(hitIndex);
    int lastDot = url.lastIndexOf('.');
    if (lastDot != -1) {
      extension = url.substring(lastDot + 1).toLowerCase();
    }

    // Check whether this extension is available
    String imgFile = imgpath + "/ext_" + extension + "." + iconextension;
    Boolean available;
    synchronized (mExtensionAvailableMap) {
      available = mExtensionAvailableMap.get(extension);
      if (available == null) {
        // This entry is not yet cached -> Check whether there is an icon for
        // that extension (e.g. img/ext/ext_pdf.gif)
        try {
          URL iconUrl = new URL(request.getResourceBaseUrl(), imgpath + "/ext_"
              + extension + "." + iconextension);

          InputStream stream = iconUrl.openStream();
          stream.close();

          available = Boolean.TRUE;
        } catch (Throwable thr) {
          available = Boolean.FALSE;
        }

        mExtensionAvailableMap.put(extension, available);
      }
    }

    // Show the appropriate icon
    if (! available.booleanValue()) {
      imgFile = imgpath + "/no_ext" + "." + iconextension;
    } 
    response.print("<img src=\"" + imgFile + "\" />");
  }

}
