/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2011  Til Schneider
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
 * Contact: Til Schneider, info@murfman.de, Thomas Tesche, www.clustersystems.info
 */
package net.sf.regain.ui.desktop.config.sharedlib;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.XmlToolkit;
import net.sf.regain.search.NoncesManager;
import net.sf.regain.ui.desktop.DesktopConstants;
import net.sf.regain.ui.desktop.DesktopToolkit;
import net.sf.regain.ui.desktop.IndexUpdateManager;
import net.sf.regain.ui.desktop.config.DesktopConfig;
import net.sf.regain.util.io.Localizer;
import net.sf.regain.util.io.MultiLocalizer;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Generates the settings form. Saves the new settings sent to the page and
 * stores the values that should be shown by child tags in the page context.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FormTag extends SharedTag implements DesktopConstants {

  /** The URL prefix of file URLs. */
  private static final String FILE_PROTOCOL = "file://";
  /** The URL prefix of http URLs. */
  private static final String HTTP_PROTOCOL = "http://";
  /** The URL prefix of http URLs. */
  private static final String IMAP_PROTOCOL = "imap";
  /** The MultiLocalizer for this class. */
  private static MultiLocalizer mMultiLocalizer = new MultiLocalizer(FormTag.class);
  
  /** Create and check nonces */ 
  private static final String FORM_ACTION_NONCE = "config";

  public FormTag()
  {
    super();
  }
  
  /**
   * Called when the parser reaches the start tag.
   * <p>
   * Initializes the list generation.
   *  
   * @param request The page request.
   * @param response The page response.
   * @return {@link #EVAL_TAG_BODY} if you want the tag body to be evaluated or
   *         {@link #SKIP_TAG_BODY} if you want the tag body to be skipped.
   * @throws RegainException If there was an exception.
   */
  @Override
  public int printStartTag(PageRequest request, PageResponse response)
          throws RegainException {
    int interval = request.getParameterAsInt("interval", -1);
    String[] dirlist;
    String[] dirblacklist;
    String[] sitelist;
    String[] siteblacklist;
    String[] imaplist;

    Localizer localizer = mMultiLocalizer.getLocalizer(request.getLocale());
    NoncesManager nonces = new NoncesManager();

    int port;
    if (interval == -1) {
      // This is the first call -> Load the settings
      DesktopConfig desktopConfig = DesktopToolkit.getDesktopConfig();
      Document crawlerDoc = XmlToolkit.loadXmlDocument(CRAWLER_CONFIG_FILE);

      interval = desktopConfig.getInterval();
      dirlist = getStartlistEntries(crawlerDoc, FILE_PROTOCOL);
      dirblacklist = getBlacklistEntries(crawlerDoc, FILE_PROTOCOL);
      sitelist = getStartlistEntries(crawlerDoc, HTTP_PROTOCOL);
      siteblacklist = getBlacklistEntries(crawlerDoc, HTTP_PROTOCOL);
      imaplist = getCompleteStartlistEntries(crawlerDoc, IMAP_PROTOCOL);
      port = desktopConfig.getPort();
    } else {
      if (!nonces.checkNonce(request, FORM_ACTION_NONCE))
      {
        response.sendError(403);
        return SKIP_TAG_BODY;
      }
      
      // There were new settings sent -> Check the input
      ArrayList<String> errorList = new ArrayList<String>();

      // Get the input
      dirlist = request.getParametersNotNull("dirlist");
      dirblacklist = request.getParametersNotNull("dirblacklist");
      sitelist = request.getParametersNotNull("sitelist");
      siteblacklist = request.getParametersNotNull("siteblacklist");
      imaplist = request.getParametersNotNull("imaplist");
      port = request.getParameterAsInt("port", DEFAULT_PORT);

      // Check the input
      checkDirectoryList(errorList, dirlist, localizer);
      checkDirectoryList(errorList, dirblacklist, localizer);
      checkWebsiteList(errorList, sitelist, localizer);
      checkWebsiteList(errorList, siteblacklist, localizer);
      checkImapList(errorList, imaplist, localizer);
      checkStartUrlLists(errorList, dirlist, sitelist, imaplist, localizer);

      if (errorList.isEmpty()) {
        // There were no errors -> Save the values
        saveSettings(interval, dirlist, dirblacklist, sitelist, siteblacklist, imaplist, port);
        DesktopToolkit.checkWebserver();
        response.print(localizer.msg("settingsSaved", "Your settings where saved!"));
//        response.print("Ihre Einstellungen wurden gespeichert!");
      } else {
        // There were errors -> Show them
        response.print(localizer.msg("settingsContainsError", "The following errors where detected:<ul>"));
//        response.print("Leider enth&auml;lt Ihre Eingabe noch Fehler:<ul>");
        for (int i = 0; i < errorList.size(); i++) {
          response.print("<li>" + errorList.get(i) + "</li>");
        }
        response.print("</ul>");
      }
    }

    // Save the current settings to the page context
    request.setContextAttribute("settings.interval", Integer.toString(interval));
    request.setContextAttribute("settings.dirlist", dirlist);
    request.setContextAttribute("settings.dirblacklist", dirblacklist);
    request.setContextAttribute("settings.sitelist", sitelist);
    request.setContextAttribute("settings.siteblacklist", siteblacklist);
    request.setContextAttribute("settings.imaplist", imaplist);
    request.setContextAttribute("settings.port", Integer.toString(port));

    String action = getParameter("action", true);
    response.print("<form name=\"settings\" action=\"" + action + "\" "
            + "method=\"post\" onsubmit=\"prepareEditListsForSubmit()\">");

    response.print(nonces.generateHTML(request, FORM_ACTION_NONCE));
    
    return EVAL_TAG_BODY;
  }

  /**
   * Called when the parser reaches the end tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  @Override
  public void printEndTag(PageRequest request, PageResponse response)
          throws RegainException {
    response.print("</form>");
  }

  /**
   * Checks a list of diretory names.
   * 
   * @param errorList The list where to store the error messages.
   * @param dirlist The list to check.
   */
  private void checkDirectoryList(ArrayList<String> errorList, String[] dirlist, Localizer localizer) {
    for (int i = 0; i < dirlist.length; i++) {
      if (dirlist[i] == null)
         continue;

      File dir = new File(dirlist[i]);
      StringBuilder tmpString = new StringBuilder();
      if (!dir.exists()) {
        tmpString.append(localizer.msg("checkDir1", "The directory '"));
        tmpString.append(dirlist[i].toString());
        tmpString.append(localizer.msg("checkDir2", "' doesn't exist"));
        
        errorList.add(tmpString.toString());
      } else if (!dir.isDirectory()) {
        tmpString.append(localizer.msg("checkDir3", "'"));
        tmpString.append(dirlist[i].toString());
        tmpString.append(localizer.msg("checkDir4", "' is not a directory"));
        
        errorList.add(tmpString.toString());
      }

      // Make the path URL conform
      dirlist[i] = RegainToolkit.replace(dirlist[i], "\\", "/");
    }
  }

  /**
   * Checks a list of website names.
   * 
   * @param errorList The list where to store the error messages.
   * @param sitelist The list to check.
   */
  private void checkWebsiteList(List<String> errorList, String[] sitelist, Localizer localizer) {
    for (int i = 0; i < sitelist.length; i++) {
        String urlAsString = sitelist[i];
        if (!urlAsString.startsWith(HTTP_PROTOCOL)) {
          urlAsString = HTTP_PROTOCOL + urlAsString;
        }
      checkValidUrl(errorList, sitelist[i], urlAsString, localizer.msg("checkWebsite1", "'"), localizer.msg("checkWebsite2", "' is not a valid http URL"));
    }
  }

  /**
   * Check if at least one of the whitelists contains some entry
   * 
   * @param errorList The list where to store the error messages.
   * @param dirlist
   * @param sitelist
   * @param imaplist
   * @param localizer
   */
  private void checkStartUrlLists(ArrayList<String> errorList, String[] dirlist, String[] sitelist, String[] imaplist, Localizer localizer)
  {
    if (dirlist.length == 0 && sitelist.length == 0 && imaplist.length == 0)
    {
      errorList.add(localizer.msg("checkDirWhitelistsEmpty", "No directory/website/imap found to start crawling!"));
    }
  }


  /**
   * Build Url to test if it is valid.
   * 
   * @param errorList  The list where to store the error messages.
   * @param origUrl    The URL as it was added
   * @param urlAsString The URL to build
   * @param msg1      First part of the error message
   * @param msg2      Second part of the error message
   */
  private void checkValidUrl(List<String> errorList, String origUrl, String urlAsString, String msg1, String msg2)
  {
    try {
      // Only instantiated to produce an exception
      // in case of a malformed URL
      new URL(urlAsString);
    } catch (MalformedURLException exc) {
      StringBuilder tmpString = new StringBuilder();
      tmpString.append(msg1).append(origUrl).append(msg2);
      errorList.add(tmpString.toString());
      System.err.println(exc.getMessage());
    }
  }

  /**
   * Checks a list of Imap urls.
   * 
   * @param errorList The list where to store the error messages.
   * @param imaplist The list to check.
   */
  private void checkImapList(List<String> errorList, String[] imaplist, Localizer localizer) {
    for (int i = 0; i < imaplist.length; i++) {
        checkValidUrl(errorList, imaplist[i], imaplist[i], localizer.msg("checkIMAP1", "'"), localizer.msg("checkIMAP2", "' is not a valid IMAP URL"));
    }
  }

  /**
   * Gets the startlist from the crawler configuration.
   * 
   * @param crawlerDoc The document that holds the crawler configuration.
   * @param prefix The prefix of the wanted entries. Entries having another
   *        prefix will be ignored.
   * @return The startlist.
   * @throws RegainException If reading the config failed.
   */
  private String[] getStartlistEntries(Document crawlerDoc, String prefix)
          throws RegainException {
    return getListEntries(crawlerDoc, prefix, "startlist", "start");
  }

  /**
   * Gets the startlist with complete urls from the crawler configuration for the given prefix.
   * 
   * @param crawlerDoc The document that holds the crawler configuration.
   * @param prefix The prefix of the wanted entries. Entries having another
   *        prefix will be ignored.
   * @return The startlist.
   * @throws RegainException If reading the config failed.
   */
  private String[] getCompleteStartlistEntries(Document crawlerDoc, String prefix)
          throws RegainException {
    String[] startList = getListEntries(crawlerDoc, prefix, "startlist", "start");
    for (int i = 0; i < startList.length; i++) {
      startList[i] = prefix + startList[i];
    }

    return startList;
  }

  /**
   * Gets the blacklist from the crawler configuration.
   * 
   * @param crawlerDoc The document that holds the crawler configuration.
   * @param prefix The prefix of the wanted entries. Entries having another
   *        prefix will be ignored.
   * @return The blacklist.
   * @throws RegainException If reading the config failed.
   */
  private String[] getBlacklistEntries(Document crawlerDoc, String prefix)
          throws RegainException {
    return getListEntries(crawlerDoc, prefix, "blacklist", "prefix");
  }

  /**
   * Gets a list from the crawler configuration.
   * 
   * @param crawlerDoc The document that holds the crawler configuration.
   * @param prefix The prefix of the wanted entries. Entries having another
   *        prefix will be ignored.
   * @param listNodeName The name of the node holding the list.
   * @param entryNodeName The name of the child node holding one list entry.
   * @return The list.
   * @throws RegainException If reading the config failed.
   */
  private String[] getListEntries(Document crawlerDoc, String prefix,
          String listNodeName, String entryNodeName)
          throws RegainException {
    Element config = crawlerDoc.getDocumentElement();
    Node startlist = XmlToolkit.getChild(config, listNodeName, true);
    Node[] startArr = XmlToolkit.getChildArr(startlist, entryNodeName);
    ArrayList<String> entries = new ArrayList<String>();
    for (int i = 0; i < startArr.length; i++) {
      String startUrl = XmlToolkit.getTextOrCData(startArr[i], true);
      if (startUrl.startsWith(prefix)) {
        String entry = startUrl;
        if (entry.startsWith(FILE_PROTOCOL)) {
          entry = RegainToolkit.urlToFileName(entry);
        }
        entries.add(entry);
      }
    }

    // Convert the ArrayList to a String[]
    String[] asArr = new String[entries.size()];
    entries.toArray(asArr);
    return asArr;
  }

  /**
   * Saves the settings.
   * 
   * @param interval The index update interval.
   * @param dirlist The list of directories that should be indexed.
   * @param dirblacklist The list of directories that should be excluded.
   * @param sitelist The list of websites that should be indexed.
   * @param siteblacklist The list of websites that should be excluded.
   * @param port The port of the webserver.
   * @throws RegainException If saving the config failed.
   */
  private void saveSettings(int interval, String[] dirlist,
          String[] dirblacklist, String[] sitelist, String[] siteblacklist, String[] imaplist, int port)
          throws RegainException {
    Node node;

    // Load the config files
    Document desktopDoc = XmlToolkit.loadXmlDocument(DESKTOP_CONFIG_FILE);
    Element desktopConfig = desktopDoc.getDocumentElement();

    // NOTE: For the crawler configuration we use the default file, so values
    //       that are not affected by the config form will be up-to-date when
    //       regain was updated to a new version.
    Document crawlerDoc = XmlToolkit.loadXmlDocument(DEFAULT_CRAWLER_CONFIG_FILE);
    Element crawlerConfig = crawlerDoc.getDocumentElement();

    // Set the interval
    Node intervalNode = XmlToolkit.getChild(desktopConfig, "interval", true);
    XmlToolkit.setText(desktopDoc, intervalNode, Integer.toString(interval));

    // Clear the startlist, whitelist and blacklist
    Node startlistNode = XmlToolkit.getChild(crawlerConfig, "startlist", true);
    XmlToolkit.removeAllChildren(startlistNode);

    Node whitelistNode = XmlToolkit.getChild(crawlerConfig, "whitelist", true);
    XmlToolkit.removeAllChildren(whitelistNode);

    Node blacklistNode = XmlToolkit.getChild(crawlerConfig, "blacklist", true);
    XmlToolkit.removeAllChildren(blacklistNode);

    // Fill the startlist
    for (int i = 0; i < dirlist.length; i++) {
      String url = RegainToolkit.fileNameToUrl(dirlist[i]);
      node = XmlToolkit.addChildWithText(crawlerDoc, startlistNode, "start", url);
      XmlToolkit.setAttribute(crawlerDoc, node, "parse", "true");
      XmlToolkit.setAttribute(crawlerDoc, node, "index", "false");
    }
    for (int i = 0; i < sitelist.length; i++) {
      String url = /*HTTP_PROTOCOL + */ sitelist[i];
      if (!url.startsWith(HTTP_PROTOCOL)) {
        url = HTTP_PROTOCOL + url;
      }
      if (url.contains("&")) {
        node = XmlToolkit.addChildWithCData(crawlerDoc, startlistNode, "start", url);
      } else {
        node = XmlToolkit.addChildWithText(crawlerDoc, startlistNode, "start", url);
      }
      XmlToolkit.setAttribute(crawlerDoc, node, "parse", "true");
      XmlToolkit.setAttribute(crawlerDoc, node, "index", "true");
    }
    for (int i = 0; i < imaplist.length; i++) {
      node = XmlToolkit.addChildWithText(crawlerDoc, startlistNode, "start", imaplist[i]);
      XmlToolkit.setAttribute(crawlerDoc, node, "parse", "true");
      XmlToolkit.setAttribute(crawlerDoc, node, "index", "true");
    }

    // Add the file protocol to the whitelist
    for (int i = 0; i < dirlist.length; i++) {
      String url = RegainToolkit.fileNameToUrl(dirlist[i]);
      XmlToolkit.addChildWithText(crawlerDoc, whitelistNode, "prefix", url);
    }

    // Add the sitelist to the whitelist
    for (int i = 0; i < sitelist.length; i++) {
      String url = /*HTTP_PROTOCOL + */ sitelist[i];
      if (!url.startsWith(HTTP_PROTOCOL)) {
        url = HTTP_PROTOCOL + url;
      }
      if (url.contains("&")) {
        XmlToolkit.addChildWithCData(crawlerDoc, whitelistNode, "prefix", url);
      } else {
        XmlToolkit.addChildWithText(crawlerDoc, whitelistNode, "prefix", url);
      }
    }

    // Add the imaplist to the whitelist
    for (int i = 0; i < imaplist.length; i++) {
      XmlToolkit.addChildWithText(crawlerDoc, whitelistNode, "prefix", imaplist[i]);
    }

    // Fill the blacklist
    for (int i = 0; i < dirblacklist.length; i++) {
      String url = RegainToolkit.fileNameToUrl(dirblacklist[i]);
      XmlToolkit.addChildWithText(crawlerDoc, blacklistNode, "prefix", url);
    }
    for (int i = 0; i < siteblacklist.length; i++) {
      String url = siteblacklist[i];
      if (!url.startsWith(HTTP_PROTOCOL)) {
        url = HTTP_PROTOCOL + url;
      }
      if (url.contains("&")) {
        XmlToolkit.addChildWithCData(crawlerDoc, blacklistNode, "prefix", url);
      } else {
        XmlToolkit.addChildWithText(crawlerDoc, blacklistNode, "prefix", url);
      }
    }

    // Set the port
    Node portNode = XmlToolkit.getOrAddChild(desktopDoc, desktopConfig, "port");
    XmlToolkit.setText(desktopDoc, portNode, Integer.toString(port));

    // Pretty print the nodes
    XmlToolkit.prettyPrint(crawlerDoc, startlistNode);
    XmlToolkit.prettyPrint(crawlerDoc, whitelistNode);
    XmlToolkit.prettyPrint(crawlerDoc, blacklistNode);

    // Save the config
    XmlToolkit.saveXmlDocument(DESKTOP_CONFIG_FILE, desktopDoc);
    XmlToolkit.saveXmlDocument(CRAWLER_CONFIG_FILE, crawlerDoc);

    // Create the needsupdate file, so the index will be updated
    IndexUpdateManager.getInstance().startIndexUpdate();
  }
}
