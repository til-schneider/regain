/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2011  Til Schneider, Thomas Tesche
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
package net.sf.regain.ui.desktop;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import net.sf.regain.util.io.Localizer;

/**
 * Manages the tray icon.
 *
 * @author Thomas Tesche, www.clustersystems.de
 */
public class TrayIconHandler {

  /** The localizer for this class. */
  private static Localizer localizer = new Localizer(TrayIconHandler.class);
  /** The singleton instance of the TrayIconHandler. */
  private static TrayIconHandler trayIconHandlerSingleton;
  /** Should a tray icon be displayed? */
  private boolean active;
  /** the TrayIcon itself. */
  private TrayIcon trayIcon = null;

  /**
   * Gets or create the singleton instances of TrayIconHandler.
   *
   * @return The singleton.
   */
  public static TrayIconHandler getInstance() {
    if (trayIconHandlerSingleton == null) {
      trayIconHandlerSingleton = new TrayIconHandler();
    }
    return trayIconHandlerSingleton;
  }

  /**
   * Initializes the tray icon.
   *
   * @param useTrayIcon should the tray icon be displayed
   */
  public void init(boolean useTrayIcon) {
    active = useTrayIcon;
    if (!active || !SystemTray.isSupported()) {
      return;
    }

    // NOTE: The SystemTray must be created get before the TrayIconHandler is created.
    //       Otherwise a UnsatisfiedLinkError will raise on linux systems
    SystemTray tray = SystemTray.getSystemTray();
    Image icon = loadImageFromClasspath("net/sf/regain/ui/desktop/regain_icon_16.gif");

    PopupMenu menu = new PopupMenu();
    MenuItem item;

    item = new MenuItem(localizer.msg("search", "Search"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("searchinput.jsp");
      }
    });
    menu.add(item);

    item = new MenuItem(localizer.msg("status", "Status"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("status.jsp");
      }
    });
    menu.add(item);

    item = new MenuItem(localizer.msg("config", "Preferences"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("config.jsp");
      }
    });
    menu.add(item);

    menu.addSeparator();

    item = new MenuItem(localizer.msg("exit", "Exit"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        Main.quit();
      }
    });
    menu.add(item);

    trayIcon = new TrayIcon(icon, "regain", menu);
    trayIcon.setImageAutoSize(true);
    trayIcon.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("searchinput.jsp");
      }
    });

    try {
      tray.add(trayIcon);
    } catch (AWTException e) {
      System.err.println("TrayIcon could not be added.");
    }
  }

  /**
   * Sets whether there is currently an index update running.
   *
   * @param updateRunning Whether there is currently an index update running.
   */
  public void setIndexUpdateRunning(boolean updateRunning) {
    if (!active) {
      return;
    }

    if (updateRunning) {
      Image image = loadImageFromClasspath("net/sf/regain/ui/desktop/regain_icon_scan_16.gif");
      trayIcon.setImage(image);
      trayIcon.setToolTip("regain - " + localizer.msg("indexUpdate", "Updating index"));

    } else {
      Image image = loadImageFromClasspath("net/sf/regain/ui/desktop/regain_icon_16.gif");
      trayIcon.setImage(image);
      trayIcon.setToolTip("regain");
    }
  }

  /**
   * Load the image from the classpath.
   *
   * @param name qualified name of the image (package + image name)
   * @return the image in case it could be loaded otherwise false
   */
  private Image loadImageFromClasspath(String name) {
    ClassLoader loader = ClassLoader.getSystemClassLoader();

    if (loader != null) {
      URL url = loader.getResource(name);
      if (url == null) {
        url = loader.getResource("/" + name);
      }
      if (url != null) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = tk.getImage(url);
        return img;
      }
    }

    return null;
  }
}