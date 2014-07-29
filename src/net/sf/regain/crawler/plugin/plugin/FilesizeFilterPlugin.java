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

package net.sf.regain.crawler.plugin.plugin;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.plugin.AbstractCrawlerPlugin;

/**
 * Dynamically blacklist all files above/below a certain maximum/minimum.
 * @author Benjamin
 *
 */
public class FilesizeFilterPlugin extends AbstractCrawlerPlugin
{
  private static final long NO_MINIMUM_FILESIZE = 0;
  private static final long NO_MAXIMUM_FILESIZE = -1;


  /**
   * Logger instance
   */
  private static Logger mLog = Logger.getLogger(FilesizeFilterPlugin.class);

  
  private long paramFilesizeMinimum = NO_MINIMUM_FILESIZE;
  private long paramFilesizeMaximum = NO_MAXIMUM_FILESIZE;
  
  private boolean pluginEnabled = false;
  
  /**
   * Get&parse parameters of plugin.
   */
  @Override
  public void init(PreparatorConfig config) throws RegainException {
    Map<String, String> pluginConfig = config.getSectionWithName("limits");
    if (pluginConfig != null)
    {
      try {
        paramFilesizeMinimum = Integer.valueOf(pluginConfig.get("filesizeMinimumBytes"));
      } catch (NumberFormatException e) {
        mLog.warn("Could not parse minimum filesize, using default (" + NO_MINIMUM_FILESIZE + ", no minimum size)");
      }

      try {
        paramFilesizeMaximum = Integer.valueOf(pluginConfig.get("filesizeMaximumBytes"));
      } catch (NumberFormatException e) {
        mLog.warn("Could not parse minimum filesize, using default (" + NO_MAXIMUM_FILESIZE + ", no maximum size)");
      }
      
    }
    
    if (paramFilesizeMinimum > NO_MINIMUM_FILESIZE || paramFilesizeMaximum > NO_MAXIMUM_FILESIZE)
      pluginEnabled = true;
  }
  
  
  /**
   * Allows to blacklist specific URLs.
   * This function is called when the URL would normally be accepted,
   * i.e. included in whitelist, not included in blacklist.
   * 
   * @param url       URL of the crawling job that should normally be added.
   * @param sourceUrl The URL where the url above has been found (a-Tag, PDF or similar)
   * @param sourceLinkText  The label of the URL in the document where the url above has been found.
   * @return  True: blacklist this URL. False: Allow this URL.
   */
  @Override
  public boolean checkDynamicBlacklist(String url, String sourceUrl, String sourceLinkText) 
  { 
    if (!pluginEnabled)
      return false;
    
    File file = null;
    try
    {
      file = RegainToolkit.urlToFile(url);
    }
    catch (RegainException e)
    {
      // Not convertible, because not a file://. So allow.
      return false;
    }
    if (!file.isFile())
      return false;
    
    long filesize = file.length();
    
    if (paramFilesizeMinimum > NO_MINIMUM_FILESIZE && filesize < paramFilesizeMinimum)
      return true; // Too small
    
    if (paramFilesizeMaximum > NO_MAXIMUM_FILESIZE && filesize > paramFilesizeMaximum)
      return true; // Too big

    return false;
  }
}
