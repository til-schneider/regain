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
 *  $RCSfile: PreparatorSettings.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/PreparatorSettings.java,v $
 *     $Date: 2005/03/14 15:04:15 $
 *   $Author: til132 $
 * $Revision: 1.4 $
 */
package net.sf.regain.crawler.config;

/**
 * The settings of a preparator
 *
 * @see net.sf.regain.crawler.document.Preparator
 * @author Til Schneider, www.murfman.de
 */
public class PreparatorSettings {
  
  /** The default preparator package. */
  public static final String DEFAULT_PREPARATOR_PACKAGE = "net.sf.regain.crawler.preparator";

  /** Specifies whether the preparator is enabled. */
  private boolean mIsEnabled;
  
  /**
   * The class name of the preparator. The class must implement
   * {@link net.sf.regain.crawler.document.Preparator Preparator}.
   */
  private String mPreparatorClassName;
  
  /**
   * The configuration of the preparator.
   */
  private PreparatorConfig mPreparatorConfig;


  /**
   * Creates a new instance of PreparatorSettings.
   * 
   * @param isEnabled Specifies whether the preparator is enabled.
   * @param preparatorClassName The class name of the preparator. The class must
   *        implement {@link net.sf.regain.crawler.document.Preparator Preparator}.
   * @param preparatorConfig The configuration of the preparator.
   */
  public PreparatorSettings(boolean isEnabled, String preparatorClassName,
    PreparatorConfig preparatorConfig)
  {
    mIsEnabled = isEnabled;
    mPreparatorConfig = preparatorConfig;
    
    if (preparatorClassName.startsWith(".")) {
      mPreparatorClassName = DEFAULT_PREPARATOR_PACKAGE + preparatorClassName;
    } else {
      mPreparatorClassName = preparatorClassName;
    }
  }


  /**
   * Gets whether the preparator is enabled.
   * 
   * @return Whether the preparator is enabled.
   */
  public boolean isEnabled() {
    return mIsEnabled;
  }


  /**
   * Gets the class name of the preparator.
   *
   * @return The class name of the preparator.
   */
  public String getPreparatorClassName() {
    return mPreparatorClassName;
  }

  
  /**
   * Gets the configuration of the preparator.
   * 
   * @return The configuration of the preparator.
   */
  public PreparatorConfig getPreparatorConfig() {
    return mPreparatorConfig;
  }

}
