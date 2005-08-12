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
 *
 * CVS information:
 *  $RCSfile: CurrentindexTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/desktop/status/sharedlib/CurrentindexTag.java,v $
 *     $Date: 2005/08/07 10:51:08 $
 *   $Author: til132 $
 * $Revision: 1.6 $
 */
package net.sf.regain.ui.desktop.status.sharedlib;

import java.io.File;
import java.util.Date;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.IndexSearcherManager;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.ui.desktop.DesktopConstants;
import net.sf.regain.util.io.Localizer;
import net.sf.regain.util.io.MultiLocalizer;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates the status of the current index.
 *
 * @author Til Schneider, www.murfman.de
 */
public class CurrentindexTag extends SharedTag implements DesktopConstants {

  /** The MultiLocalizer for this class. */
  private static MultiLocalizer mMultiLocalizer = new MultiLocalizer(CurrentindexTag.class);


  /**
   * Called when the parser reaches the end tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    Localizer localizer = mMultiLocalizer.getLocalizer(request.getLocale());
    
    // Get the IndexConfig
    IndexConfig[] configArr = SearchToolkit.getIndexConfigArr(request);
    if (configArr.length > 1) {
      throw new RegainException("The currentindex tag can only be used for one index!");
    }
    IndexConfig config = configArr[0];
    
    File currentIndexDir = new File(config.getDirectory(), "new");
    if (! currentIndexDir.exists()) {
      currentIndexDir = new File(config.getDirectory(), "index");
    }
    
    if (currentIndexDir.exists()) {
      // Get the last update
      String timestamp = RegainToolkit.readStringFromFile(LASTUPDATE_FILE);
      Date lastUpdate = RegainToolkit.stringToLastModified(timestamp);
      
      // Get the index size
      long size = RegainToolkit.getDirectorySize(currentIndexDir);
      String sizeAsString = RegainToolkit.bytesToString(size, request.getLocale());
      
      // Get the document count
      IndexSearcherManager manager = IndexSearcherManager.getInstance(config.getDirectory());
      int docCount = manager.getDocumentCount();
      
      // Print the results
      response.print(localizer.msg("indexInfo", "Last update: {0}<br/>Size: {1}<br/>Document count: {2}",
          lastUpdate, sizeAsString, new Integer(docCount)));
    } else {
      response.print(localizer.msg("noIndex", "There was no search index created so far."));
    }
  }

}
