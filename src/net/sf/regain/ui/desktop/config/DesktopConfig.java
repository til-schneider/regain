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
 *  $RCSfile: DesktopConfig.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/ui/desktop/config/DesktopConfig.java,v $
 *     $Date: 2005/03/16 08:55:48 $
 *   $Author: til132 $
 * $Revision: 1.3 $
 */
package net.sf.regain.ui.desktop.config;

import net.sf.regain.RegainException;

/**
 * The configuration of the desktop search.
 *
 * @author Til Schneider, www.murfman.de
 */
public interface DesktopConfig {

  /**
   * Gets the index update interval from the desktop configuration.
   * 
   * @return The index update interval.
   * @throws RegainException If loading the config failed.
   */
  public int getInterval() throws RegainException;

  /**
   * Gets the port of the webserver.
   * 
   * @return The port of the webserver.
   * @throws RegainException If loading the config failed.
   */
  public int getPort() throws RegainException;
  
  /**
   * Gets the executable of the browser that should be used for showing web
   * pages. Returns <code>null</code> if the browser should be auto-detected. 
   * 
   * @return The executable of the browser.
   * @throws RegainException If loading the config failed.
   */
  public String getBrowser() throws RegainException;

}
