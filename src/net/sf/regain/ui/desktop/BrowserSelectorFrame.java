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
 *  $RCSfile: BrowserSelectorFrame.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/desktop/BrowserSelectorFrame.java,v $
 *     $Date: 2005/03/16 08:55:48 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.ui.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.regain.RegainException;
import net.sf.regain.XmlToolkit;
import net.sf.regain.util.io.Localizer;

/**
 * Shows a frame that asks the user to select a browser.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class BrowserSelectorFrame implements DesktopConstants {
  
  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(BrowserSelectorFrame.class);
  
  /** The localizer for this class. */
  private static Localizer mLocalizer = new Localizer(BrowserSelectorFrame.class);
  
  /** The frame. */
  private JFrame mFrame;
  
  /** Option pane that contains the message. */
  private JOptionPane mOptionPane;
  
  /** The page to open when a browser was chosen. */
  private String mPageUrl;
  

  /**
   * Creates a new instance of BrowserSelectorFrame.
   * 
   * @param pageUrl The page to open when a browser was chosen.
   */
  public BrowserSelectorFrame(String pageUrl) {
    mPageUrl = pageUrl;
    
    String msg = mLocalizer.msg("error.title", "Error");
    mFrame = new JFrame(msg);
    
    JPanel main = new JPanel(new BorderLayout());
    mFrame.setContentPane(main);

    msg = mLocalizer.msg("error.message", "No browser found.\n\nDo you want to choose a browser now?");
    mOptionPane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION);
    mOptionPane.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY) &&
            evt.getNewValue() != null &&
            evt.getNewValue() != JOptionPane.UNINITIALIZED_VALUE)
        {
          handleOptionPaneChanged();
        }
      }
    });
    mFrame.setContentPane(mOptionPane);
    
    mFrame.pack();
  }


  /**
   * Handles a changed value. Called when yes or no was pressed.
   */
  private void handleOptionPaneChanged() {
    int option = ((Integer) mOptionPane.getValue()).intValue();
    
    if (option == 0) {
      // Yes
      try {
        chooseBrowser();
      }
      catch (RegainException exc) {
        mLog.error("Choosing browser failed", exc);
      }
      
      mFrame.dispose();
    }
    else if (option == 1) {
      // No
      mFrame.dispose();
    }
  }


  /**
   * Centers and shows the frame.
   */
  public void show() {
    // Center the frame
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension wD = mFrame.getSize();
    Point wPos = new Point((screenSize.width - wD.width) / 2,
                           (screenSize.height - wD.height) / 2);
    wPos.x = Math.max(0, wPos.x); // Make x > 0
    wPos.y = Math.max(0, wPos.y); // Make y > 0
    mFrame.setLocation(wPos);
    
    // Show the frame
    mFrame.show();
  }


  /**
   * Chooses the browser.
   * 
   * @throws RegainException If saving the config failed.
   */
  private void chooseBrowser() throws RegainException {
    JFileChooser fileChooser = new JFileChooser();
    
    String msg = mLocalizer.msg("chooseBrowser", "Choose browser");
    fileChooser.setDialogTitle(msg);
    fileChooser.showOpenDialog(mFrame);

    File file = fileChooser.getSelectedFile();
    if (file != null) {
      Document desktopDoc = XmlToolkit.loadXmlDocument(DESKTOP_CONFIG_FILE);
      Element desktopConfig = desktopDoc.getDocumentElement();

      // Set the browser
      Node browserNode = XmlToolkit.getOrAddChild(desktopDoc, desktopConfig, "browser");
      XmlToolkit.setText(desktopDoc, browserNode, file.getAbsolutePath());
      
      // Save the config
      XmlToolkit.saveXmlDocument(DESKTOP_CONFIG_FILE, desktopDoc);
      
      DesktopToolkit.openPageInBrowser(mPageUrl);
    }
  }

}
