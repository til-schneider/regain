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
package net.sf.regain.ui.desktop.config;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.XmlToolkit;
import net.sf.regain.ui.desktop.DesktopConstants;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Reads the configuration of the desktop search from a XML file.
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlDesktopConfig implements DesktopConfig, DesktopConstants {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(XmlDesktopConfig.class);

  /** The XML file to read the configuration from. */
  private File mXmlFile;

  /** The timestamp when the config file was last modified. */
  private long mConfigFileLastModified;

  /** The index update interval. */
  private int mInterval;

  /** The port of the webserver. */
  private int mPort;

  /** Flag whether external access is allowed. */
  private boolean mExternalAccessAllowed;

  /**
   * The executable of the browser. Is <code>null</code> if the browser should
   * be auto-detected.
   */
  private String mBrowser;

  /**
   * Namespaces that should be registered for the simple server.
   */
  private Map<String, String> mNamespaces;


  /**
   * Creates a new instance of XmlDesktopConfig.
   *
   * @param xmlFile The XML file to read the configuration from.
   */
  public XmlDesktopConfig(File xmlFile) {
    mXmlFile = xmlFile;
    mConfigFileLastModified = -1;
  }


  /**
   * Gets the index update interval from the desktop configuration.
   *
   * @return The index update interval.
   * @throws RegainException If loading the config failed.
   */
  public int getInterval() throws RegainException {
    loadConfig();
    return mInterval;
  }


  /**
   * Gets the port of the webserver.
   *
   * @return The port of the webserver.
   * @throws RegainException If loading the config failed.
   */
  public int getPort() throws RegainException {
    loadConfig();
    return mPort;
  }


  /**
   * Gets the executable of the browser that should be used for showing web
   * pages. Returns <code>null</code> if the browser should be auto-detected.
   *
   * @return The executable of the browser.
   * @throws RegainException If loading the config failed.
   */
  public String getBrowser() throws RegainException {
    loadConfig();
    return mBrowser;
  }

  /**
   * Gets the setting wheter external access to the instance is allowed or not.
   * pages. Returns FALSE if no config entry exists
   *
   * @return the boolean whether external access is allowed or not
   * @throws RegainException If loading the config failed.
   */
  public boolean getExternalAccessAllowed() throws RegainException {
    loadConfig();
    return mExternalAccessAllowed;
  }

  /**
   * Loads the config if the config was not yet loaded or if the file has changed.
   *
   * @throws RegainException If loading the config failed.
   */
  private void loadConfig() throws RegainException {
    long lastModified = mXmlFile.lastModified();
    if (lastModified != mConfigFileLastModified) {
      mLog.info("New desktop configuration found on " + new java.util.Date());

      // The config has changed -> Load it
      Document doc = XmlToolkit.loadXmlDocument(mXmlFile);
      Element config = doc.getDocumentElement();

      Node node = XmlToolkit.getChild(config, "interval", true);
      mInterval = (node == null ) ? DEFAULT_INTERVAL : XmlToolkit.getTextAsInt(node);

      node = XmlToolkit.getChild(config, "port");
      mPort = (node == null) ? DEFAULT_PORT : XmlToolkit.getTextAsInt(node);

      node = XmlToolkit.getChild(config, "browser");
      mBrowser = (node == null) ? null : XmlToolkit.getText(node, false, true);

      node = XmlToolkit.getChild(config, "allow_external_access");
      mExternalAccessAllowed = ( node == null ) ? false : XmlToolkit.getTextAsBoolean(node);

      mNamespaces = new Hashtable<String, String>();
      node = XmlToolkit.getChild(config, "simple_register_namespace");
      if (node != null)
      {
        Node[] nodes = XmlToolkit.getChildArr(node, "namespace");
        for (Node n : nodes)
        {
          String name = XmlToolkit.getAttribute(n, "name", true);
          if (name.isEmpty())
            throw new RegainException("Node 'namespace' has an empty attribute 'name'");
          mNamespaces.put(name, XmlToolkit.getText(n, true));
        }
      }

      mConfigFileLastModified = lastModified;
    }
  }


  /**
   * Gets Tag namespaces that should be registered so they can be used in the JSP-File.
   *
   * @return  A map of Alias - Package Name
   * @throws RegainException  If loading the config failed.
   */
  public Map<String, String> getSimpleNamespaces() throws RegainException
  {
    loadConfig();
    return mNamespaces;
  }

}
