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
package net.sf.regain.ui.desktop.status.sharedlib;

import java.io.File;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.Crawler;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.ui.desktop.IndexUpdateManager;
import net.sf.regain.util.io.Localizer;
import net.sf.regain.util.io.MultiLocalizer;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates the status of the currently running index update.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IndexupdateTag extends SharedTag {

  /** The MultiLocalizer for this class. */
  private static MultiLocalizer mMultiLocalizer = new MultiLocalizer(IndexupdateTag.class);

  /**
   * Called when the parser reaches the end tag.
   *
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  @Override
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    Localizer localizer = mMultiLocalizer.getLocalizer(request.getLocale());

    Crawler crawler = IndexUpdateManager.getInstance().getCurrentCrawler();
    if (crawler == null) {
      response.print(localizer.msg("noIndexUpdate", "Currently is no index update running."));
    } else {
      // Get the IndexConfig
      IndexConfig[] configArr = SearchToolkit.getIndexConfigArr(request);
      if (configArr.length > 1) {
        throw new RegainException("The indexupdate tag can only be used for one index!");
      }
      IndexConfig config = configArr[0];

      // Get the index size
      File indexUpdateDir = new File(config.getDirectory(), "temp");
      long size = RegainToolkit.getDirectorySize(indexUpdateDir);
      String sizeAsString = RegainToolkit.bytesToString(size, request.getLocale());
      String currentJobUrl = crawler.getCurrentJobUrl();

      Object[] args = new Object[] {
        crawler.getFinishedJobCount(),
        sizeAsString,
        crawler.getInitialDocCount(),
        crawler.getAddedDocCount(),
        crawler.getRemovedDocCount(),
        (currentJobUrl == null) ? "?" : currentJobUrl,
        RegainToolkit.toTimeString(crawler.getCurrentJobTime())
      };
      response.print(localizer.msg("indexInfo", "Processed documents: {0}<br/>" +
            "Size: {1}<br/>Initial document count: {2}<br/>" +
            "Added document count: {3}<br/>Removed document count: {4}<br/>" +
            "Current job: {5} (since {6})", args));
    }
  }

}
