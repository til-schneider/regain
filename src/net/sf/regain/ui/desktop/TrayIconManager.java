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
 *     $Date: 2005-03-16 09:55:48 +0100 (Mi, 16 Mrz 2005) $
 *   $Author: til132 $
 * $Revision: 87 $
 */
package net.sf.regain.ui.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import net.sf.regain.util.io.Localizer;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

/**
 * Manages the tray icon.
 *
 * @author Til Schneider, www.murfman.de
 */
public class TrayIconManager {
  
  /** The localizer for this class. */
  private static Localizer mLocalizer = new Localizer(TrayIconManager.class);
  
  /** The singleton. */
  private static TrayIconManager mSingleton;
  
  /** The try icon */
  private TrayIcon mTrayIcon;
  
  
  /**
   * Gets the singleton.
   * 
   * @return The singleton.
   */
  public static TrayIconManager getInstance() {
    if (mSingleton == null) {
      mSingleton = new TrayIconManager();
    }
    return mSingleton;
  }
  
  
  /**
   * Initializes the tray icon.
   */
  public void init() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception evt) {
      evt.printStackTrace();
    }

    JPopupMenu menu = new JPopupMenu();
    JMenuItem item;

    item = new JMenuItem(mLocalizer.msg("search", "Search"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("searchinput.jsp");
      }
    });
    menu.add(item);

    item = new JMenuItem(mLocalizer.msg("status", "Status"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("status.jsp");
      }
    });
    menu.add(item);
    
    item = new JMenuItem(mLocalizer.msg("config", "Preferences"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("config.jsp");
      }
    });
    menu.add(item);

    menu.addSeparator();
    
    item = new JMenuItem(mLocalizer.msg("exit", "Exit"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Main.quit();
      }
    });
    menu.add(item);
    
    ImageIcon icon = new ImageIcon(getClass().getResource("regain_icon_16.gif"));

    // NOTE: The SystemTray must be created get before the TrayIcon is created.
    //       Otherwise a UnsatisfiedLinkError will raise on linux systems
    SystemTray tray = SystemTray.getDefaultSystemTray();
    
    mTrayIcon = new TrayIcon(icon, "regain", menu);
    mTrayIcon.setIconAutoSize(true);
    mTrayIcon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        DesktopToolkit.openPageInBrowser("searchinput.jsp");
      }
    });
    
    tray.addTrayIcon(mTrayIcon);
  }


  /**
   * Sets whether there is currently an index update running.
   * 
   * @param updateRunning Whether there is currently an index update running.
   */
  public void setIndexUpdateRunning(boolean updateRunning) {
    if (updateRunning) {
      ImageIcon icon = new ImageIcon(getClass().getResource("regain_icon_scan_16.gif"));
      mTrayIcon.setIcon(icon);
      mTrayIcon.setToolTip("regain - " + mLocalizer.msg("indexUpdate", "Updating index"));
    } else {
      ImageIcon icon = new ImageIcon(getClass().getResource("regain_icon_16.gif"));
      mTrayIcon.setIcon(icon);
      mTrayIcon.setToolTip("regain");
    }
  }

}
