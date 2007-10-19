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
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-03-16 13:53:50 +0100 (Mi, 16 Mrz 2005) $
 *   $Author: til132 $
 * $Revision: 90 $
 */
package net.sf.regain.ui.desktop;

import java.io.File;

/**
 * Constants used by the desktop search.
 *
 * @author Til Schneider, www.murfman.de
 */
public interface DesktopConstants {

  /** The default port for the webserver. */
  public static final int DEFAULT_PORT = 8020;
  
  /** The directory where the log files are located. */
  public static final File LOG_DIR = new File("log");
  
  /** The directory where the index is located. */
  public static final File INDEX_DIR = new File("searchindex");
  
  /** The file that holds the timestamp when the index was updated the last time. */
  public static final File LASTUPDATE_FILE = new File(INDEX_DIR, "lastupdate");
  
  /** The file that indicates that index should be updated if the file exists. */
  public static final File NEEDSUPDATE_FILE = new File(INDEX_DIR, "needsupdate");
  
  /** The directory where the configuration files are located. */
  public static final File CONFIG_DIR = new File("conf");

  /** The directory where the default configuration files are located. */
  public static final File DEFAULT_CONFIG_DIR = new File(CONFIG_DIR, "default");
  
  /** The file that holds the desktop configuration. */
  public static final File DESKTOP_CONFIG_FILE = new File(CONFIG_DIR, "DesktopConfiguration.xml");

  /** The file that holds the default desktop configuration. */
  public static final File DEFAULT_DESKTOP_CONFIG_FILE = new File(DEFAULT_CONFIG_DIR, "DesktopConfiguration.xml");
  
  /** The file that holds the crawler configuration. */
  public static final File CRAWLER_CONFIG_FILE = new File(CONFIG_DIR, "CrawlerConfiguration.xml");

  /** The file that holds the crawler configuration. */
  public static final File DEFAULT_CRAWLER_CONFIG_FILE = new File(DEFAULT_CONFIG_DIR, "CrawlerConfiguration.xml");

}
