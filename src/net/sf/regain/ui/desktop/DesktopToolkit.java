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
 *  $RCSfile: DesktopToolkit.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/desktop/DesktopToolkit.java,v $
 *     $Date: 2005/03/16 08:55:48 $
 *   $Author: til132 $
 * $Revision: 1.5 $
 */
package net.sf.regain.ui.desktop;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.apache.log4j.Logger;

import simple.http.ProtocolHandler;
import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;
import simple.http.load.MapperEngine;
import simple.http.serve.FileContext;
import simple.http.serve.HandlerFactory;

import net.sf.regain.RegainException;
import net.sf.regain.ui.desktop.config.DesktopConfig;
import net.sf.regain.ui.desktop.config.XmlDesktopConfig;
import net.sf.regain.util.sharedtag.simple.SharedTagService;
import net.sf.regain.util.ui.BrowserLauncher;

/**
 * A toolkit for the desktop search containing helper methods.
 *
 * @author Til Schneider, www.murfman.de
 */
public class DesktopToolkit implements DesktopConstants {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(DesktopToolkit.class);
  
  /** The desktop configuration. */
  private static DesktopConfig mConfig;
  
  /** The simpleweb connection */
  private static Connection mSimplewebConnection;
  
  /** The current webserver socket. */
  private static ServerSocket mCurrentSocket;


  /**
   * Gets the desktop configuration.
   * 
   * @return The desktop configuration.
   */
  public static DesktopConfig getDesktopConfig() {
    if (mConfig == null) {
      File xmlFile = new File("conf/DesktopConfiguration.xml");
      mConfig = new XmlDesktopConfig(xmlFile);
    }
    return mConfig;
  }


  /**
   * Opens a page in the browser.
   * 
   * @param page The page to open.
   */
  public static void openPageInBrowser(String page) {
    String url = "http://localhost:" + mCurrentSocket.getLocalPort() + "/" + page;
    try {
      String browser = getDesktopConfig().getBrowser();
      if (browser != null) {
        BrowserLauncher.setBrowser(browser);
      }
      
      BrowserLauncher.openURL(url);
    }
    catch (Exception exc) {
      mLog.error("Opening browser failed", exc);
      
      // Show the browser selector
      BrowserSelectorFrame frame = new BrowserSelectorFrame(page);
      frame.show();
    }
  }
  
  
  /**
   * Checks whether the webserver is running on the right port.
   * 
   * @throws RegainException If creating or remapping the webserver failed.
   */
  public static void checkWebserver() throws RegainException {
    int port = getDesktopConfig().getPort();
    if ((mCurrentSocket == null) || (mCurrentSocket.getLocalPort() != port)) {
      if (mCurrentSocket != null) {
        // The port has changed -> Close the old socket
        try {
          mCurrentSocket.close();
        }
        catch (IOException exc) {
          throw new RegainException("Closing the old socket failed", exc);
        }
      }
      
      // Create the simpleweb connection if nessesary
      if (mSimplewebConnection == null) {
        try {
          FileContext context = new FileContext(new File("web"));
          MapperEngine engine = new MapperEngine(context);
          
          engine.load("SharedTagService", SharedTagService.class.getName());
          engine.link("*", "SharedTagService");
          
          ProtocolHandler handler = HandlerFactory.getInstance(engine);
          
          mSimplewebConnection = ConnectionFactory.getConnection(handler);
        }
        catch (Exception exc) {
          throw new RegainException("Creating simpleweb server failed", exc);
        }
      }

      mLog.info("Listening on port " + port + "...");
      try {
        mCurrentSocket = new ServerSocket(port);
      }
      catch (IOException exc) {
        throw new RegainException("Creating socket for port " + port + " failed.", exc);
      }
      mSimplewebConnection.connect(mCurrentSocket);
    }
  }
  
}
