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
 *  $RCSfile: Main.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/desktop/Main.java,v $
 *     $Date: 2005/03/16 12:53:47 $
 *   $Author: til132 $
 * $Revision: 1.15 $
 */
package net.sf.regain.ui.desktop;

import java.io.File;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.util.sharedtag.simple.ExecuterParser;
import net.sf.regain.util.sharedtag.simple.SimplePageRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Starts the desktop search.
 *
 * @author Til Schneider, www.murfman.de
 */
public class Main implements DesktopConstants {
  
  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(Main.class);


  /**
   * The main entry point.
   * 
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    // Initialize the configuration
    // (Copy all files from the default dir that don't exist in the config dir)
    String[] defaultFileArr = DEFAULT_CONFIG_DIR.list();
    for (int i = 0; i < defaultFileArr.length; i++) {
      File confFile = new File(CONFIG_DIR, defaultFileArr[i]);
      if (! confFile.exists()) {
        // This config file does not exist -> Copy the default file
        File defaultConfFile = new File(DEFAULT_CONFIG_DIR, defaultFileArr[i]);
        try {
          RegainToolkit.copyFile(defaultConfFile, confFile);
        }
        catch (RegainException exc) {
          System.out.println("Copying default config file failed: "
              + defaultConfFile.getAbsolutePath());
          exc.printStackTrace();
          System.exit(1); // Abort
        }
      }
    }

    // Initialize Logging
    File logConfigFile = new File("conf/log4j.properties");
    if (! logConfigFile.exists()) {
      System.out.println("ERROR: Logging configuration file not found: "
        + logConfigFile.getAbsolutePath());
      System.exit(1); // Abort
    }

    LOG_DIR.mkdir();
    PropertyConfigurator.configure(logConfigFile.getAbsolutePath());
    mLog.info("Logging initialized");

    // Initialize the search mask
    SimplePageRequest.setInitParameter("searchConfigFile", "conf/SearchConfiguration.xml");
    SimplePageRequest.setInitParameter("webDir", "web");
    ExecuterParser.registerNamespace("search", "net.sf.regain.search.sharedlib");
    ExecuterParser.registerNamespace("config", "net.sf.regain.ui.desktop.config.sharedlib");
    ExecuterParser.registerNamespace("status", "net.sf.regain.ui.desktop.status.sharedlib");
    
    // Start the Tray icon
    TrayIconManager.getInstance().init();
    
    // Start the webserver
    try {
      DesktopToolkit.checkWebserver();
    }
    catch (RegainException exc) {
      exc.printStackTrace();
      System.exit(1); // Abort
    }
    
    // Start the index update manager
    INDEX_DIR.mkdir();
    IndexUpdateManager.getInstance().init();
  }
  
  
  /**
   * Quits the desktop search.
   */
  public static void quit() {
    System.exit(0);
  }
  
}
