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
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/Main.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.*;

import org.apache.log4j.*;


/**
 * Die Main-Klasse mit dem Programmeinstiegstpunkt. Sie lie�t die Konfiguration
 * aus, stellt die Proxy-Einstellungen ein und startet den Crawler.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class Main {

  /** Die Kategorie, die zum Loggen genutzt werden soll. */
  private static Category mCat = Category.getInstance(Main.class);

  /** Der Dateiname der Log4J-Properties-Datei. */
  private static final String LOG4J_PROP_FILE_NAME = "log4j.properties";



  /**
   * Der Programmeinstiegspunkt.
   *
   * @param args Die Kommandozeilenargumente
   */
  public static void main(String[] args) {
    // Kommandozeilenparameter lesen
    String spiderConfigFileName = "CrawlerConfiguration.xml";
    String logConfigFileName = LOG4J_PROP_FILE_NAME;
    String[] onlyEntriesArr = null;
    boolean updateIndex = true;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-forceNewIndex")) {
        updateIndex = false;
      }
      else if (args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("/?")) {
        showHelp();
      }
      else if (args[i].equalsIgnoreCase("-onlyEntries")) {
        i++;
        String ssv = readParam(args, i);
        onlyEntriesArr = RegainToolkit.splitString(ssv, ",");
      }
      else if (args[i].equalsIgnoreCase("-config")) {
        i++;
        spiderConfigFileName = readParam(args, i);
      }
      else if (args[i].equalsIgnoreCase("-logConfig")) {
        i++;
        logConfigFileName = readParam(args, i);
      }
      else {
        showHelp();
      }
    }

    // Initialize Logging
    File logConfigFile = new File(logConfigFileName);
    if (! logConfigFile.exists()) {
      System.out.println("ERROR: Logging configuration file not found: "
        + logConfigFile.getAbsolutePath());
      return; // Abort
    }

    PropertyConfigurator.configure(logConfigFile.getAbsolutePath());
    mCat.info("Logging initialized");
    
    // Load spider configuration
    File xmlFile = new File(spiderConfigFileName);
    Configuration config;
    try {
      config = new XmlConfiguration(xmlFile);
    }
    catch (RegainException exc) {
      mCat.error("Loading XML Configuration failed", exc);
      return; // Abort
    }

    // Proxy settings
    initProxy(config);

    // Create spider
    Crawler spider = null;
    try {
      spider = new Crawler(config);
    }
    catch (RegainException exc) {
      mCat.error("There was an error when initializing the spider!", exc);
    }

    // Let the spider do its job
    if (spider != null) {
      spider.run(updateIndex, onlyEntriesArr);
      
      // Returncode ermitteln
      int returnCode;
      if (spider.getErrorCount() > 0) {
        if (spider.getFatalErrorCount() > 0) {
          // We return 100 for fatal errors
          returnCode = 100;
        } else {
          // We return 1 for non-fatal errors
          returnCode = 1;
        }
      } else {
        // Print the active thread list (for debugging)
        //CrawlerToolkit.printActiveThreads();

        // We do an explizit exit here, because the VM does not quit when there
        // are still threads running (e.g. the AwtEventThread).
        returnCode = 0;
      }
      
      // Kontrolldatei erstellen
      if (returnCode == 100) {
        deleteControlFile(config.getFinishedWithoutFatalsFileName());
        createControlFile(config.getFinishedWithFatalsFileName());
      } else {
        deleteControlFile(config.getFinishedWithFatalsFileName());
        createControlFile(config.getFinishedWithoutFatalsFileName());
      }
      
      // Beenden
      System.exit(returnCode);
    }
  }


  /**
   * Liest einen Parameter.
   * <p>
   * Falls es den Parameter nicht gibt, wird der Hilfetext ausgegeben und
   * beendet.
   * 
   * @param args Die Parameter.
   * @param paramIdx Der Index des zu lesenden Parameter
   * @return Der Wert des Parameters
   */
  private static String readParam(String[] args, int paramIdx) {
    if (paramIdx < args.length) {
      return args[paramIdx];
    } else {
      showHelp();
      
      // This will not happen, because showHelp() calls System.exit()
      return null;
    }
  }


  /**
   * Schreibt einen Hilfetext nach System.out und endet mit dem Fehlercode 100.
   */
  private static void showHelp() {
    System.out.println(
      "Allowed parameters:\n" +
      "  --help:             Shows this help page\n" +
      "  -forceNewIndex:     Forces the creation of a new search index\n" + 
      "  -onlyEntries <CSV>: The white list entries to use, separated by comma (,)\n" +
      "                      (Default: all entries)" +
      "  -config <file>:     The configuration file to use\n" +
      "                      (Default: SpiderConfiguration.xml)" +
      "  -logConfig <file>:  The logging configuration file to use\n" +
      "                      (Default: log4j.properties)");
    
    System.exit(100);
  }


  /**
   * Initialisiert die Proxy-Einstellungen.
   *
   * @param config Die Konfiguration, aus der die Einstellungen gelesen werden
   *        sollen.
   */
  private static void initProxy(Configuration config) {
    String httpProxyHost = config.getProxyHost();
    String httpProxyPort = config.getProxyPort();
    String httpProxyUser = config.getProxyUser();
    String httpProxyPassword = config.getProxyPassword();

    String msg = "";
    if (httpProxyHost != null) {
      System.setProperty("http.proxyHost", httpProxyHost);
      msg += " host: " + httpProxyHost;
    }
    if (httpProxyPort != null) {
      System.setProperty("http.proxyPort", httpProxyPort);
      msg += " port: " + httpProxyPort;
    }
    if (httpProxyUser != null) {
      System.setProperty("http.proxyUser", httpProxyUser);
      msg += " user: " + httpProxyUser;
    }
    if (httpProxyPassword != null) {
      System.setProperty("http.proxyPassword", httpProxyPassword);
      msg += " password: (" + httpProxyPassword.length() + " characters)";
    }
    
    if (msg.length() != 0) {
      mCat.info("Using proxy:" + msg);
    } else {
      mCat.info("Using no proxy");
    }
  }
  
  
  /**
   * Erzeugt eine leere Datei.
   * <p>
   * Wenn die Erzeugung der Datei fehl schlug wird das gelogt.
   * 
   * @param fileName Der Dateiname der zu erzeugenden Datei.
   */
  private static void createControlFile(String fileName) {
    if (fileName != null) {
      FileOutputStream stream = null;
      try {
        stream = new FileOutputStream(fileName);
        mCat.info("Created control file: '" + fileName + "'");
      }
      catch (IOException exc) {
        mCat.warn("Creating control file failed: '" + fileName + "'", exc);
      }
      finally {
        if (stream != null) {
          try { stream.close(); } catch (IOException exc) {}
        }
      }
    }
  }


  /**
   * L�scht eine Datei.
   * <p>
   * Wenn die L�schung der Datei fehl schlug wird das gelogt.
   * 
   * @param fileName Der Dateiname der zu l�schenden Datei.
   */
  private static void deleteControlFile(String fileName) {
    if (fileName != null) {
      File file = new File(fileName);
      if (file.exists()) {
        if (! file.delete()) {
          mCat.warn("Deleting old control file failed: '" + fileName + "'");
        }
      }
    }
  }
    
}
