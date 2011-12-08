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
 * Contact: Til Schneider, info@murfman.de, Thomas Tesche, thomas.tesche@clustersystems.de
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2011-09-20 13:52:46 +0200 (Di, 20 Sep 2011) $
 *   $Author: benjaminpick $
 * $Revision: 534 $
 */
package net.sf.regain.ui.desktop;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.ui.desktop.config.DesktopConfig;
import net.sf.regain.util.sharedtag.simple.ExecuterParser;
import net.sf.regain.util.sharedtag.simple.SimplePageRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Starts the desktop search.
 *
 * @author Til Schneider, www.murfman.de
 * @auther Thomas Tesche, www.clustersystems.de
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
		// Command line Options
		boolean useTrayIcon = true;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-noTrayIcon")) {
				useTrayIcon = false;
			}
		}

		initializeConfig();
		initializeLogging();
		intializeSearchMask();

		// Start the Tray icon
		TrayIconHandler.getInstance().init(useTrayIcon);

		// Start the webserver
		try {
			DesktopToolkit.checkWebserver();
		} catch (RegainException exc) {
			exc.printStackTrace(System.err);
			System.exit(1); // Abort
		}

		// Start the index update manager
		INDEX_DIR.mkdir();
		IndexUpdateManager.getInstance().init();

		// Opening browser only in trayIcon-less mode
		if (!useTrayIcon) {
			DesktopToolkit.openPageInBrowser("welcome.jsp");
		}
	}

	public static void intializeSearchMask() {
		// Initialize the search mask
		URL baseurl;
		try {
			baseurl = new File("web").toURI().toURL();
		} catch (MalformedURLException exc) {
			exc.printStackTrace(System.err);
			System.exit(1); // Abort
			return;
		}
		
		DesktopConfig mConfig = DesktopToolkit.getDesktopConfig();
		
		SimplePageRequest.setResourceBaseUrl(baseurl);
		SimplePageRequest.setWorkingDir(new File("."));
		SimplePageRequest.setInitParameter("searchConfigFile", "conf/SearchConfiguration.xml");

		// Set Namespaces of Tags.
		Map<String,String> namespaces = null;
		try
    {
		  namespaces = mConfig.getSimpleNamespaces();
    }
    catch (RegainException e)
    {
      e.printStackTrace(System.err);
      System.exit(1);
    }
    if (namespaces.isEmpty())
    {
      System.out.println("Warning: No Tag Namespaces found in DesktopConfiguration.xml. For backwards compability, default namespaces are used.");
      namespaces.put("search", "net.sf.regain.search.sharedlib");
      namespaces.put("config", "net.sf.regain.ui.desktop.config.sharedlib");
      namespaces.put("status", "net.sf.regain.ui.desktop.status.sharedlib");
    }

    for (Map.Entry<String, String> entry : namespaces.entrySet())
    {
      ExecuterParser.registerNamespace(entry.getKey(), entry.getValue());
    }
	}

	public static void initializeLogging() {
		File logConfigFile = new File("conf/log4j.properties");
		if (!logConfigFile.exists()) {
			System.out.println("ERROR: Logging configuration file not found: "
					+ logConfigFile.getAbsolutePath());
			System.exit(1); // Abort
		}

		if (!LOG_DIR.mkdir() && !LOG_DIR.exists())
		  throw new RuntimeException("Could not create log directory.");
		
		PropertyConfigurator.configure(logConfigFile.getAbsolutePath());
		mLog.info("Logging initialized");
	}

	public static void initializeConfig() {
		testIfFileExists(CONFIG_DIR, "conf");
		testIfFileExists(DEFAULT_CONFIG_DIR, "conf/default");

		// (Copy all files from the default dir that don't exist in the config dir)
		String[] defaultFileArr = DEFAULT_CONFIG_DIR.list();
		for (int i = 0; i < defaultFileArr.length; i++) {
			File confFile = new File(CONFIG_DIR, defaultFileArr[i]);
			if (!confFile.exists()) {
				// This config file does not exist -> Copy the default file
				File defaultConfFile = new File(DEFAULT_CONFIG_DIR, defaultFileArr[i]);
				try {
					RegainToolkit.copyFile(defaultConfFile, confFile);
				} catch (RegainException exc) {
					System.out.println("Copying default config file failed: "
							+ defaultConfFile.getAbsolutePath());
					exc.printStackTrace(System.err);
					System.exit(1); // Abort
				}
			}
		}
	}

	/**
	 * Quits the desktop search.
	 */
	public static void quit() {
		System.exit(0);
	}

	/**
	 * Checks whether the given file exists and notice the user with an
	 * appropriate message.
	 *
	 * @param file 	the file/dir to check
	 * @param label 	the file name for the message
	 */
	private static void testIfFileExists(File file, String label) {
		if (file == null || !file.exists()) {
			if (file == null)
				System.err.println("Error: Config Dir '" + label + "' not found.");
			else
				System.err.println("Error: Config Dir '" + file.getAbsolutePath() + "' (" + label + ") not found.");
			System.err.println("Current Working Directory is: " + System.getProperty("user.dir"));
			System.exit(1);
		}
	}
}
