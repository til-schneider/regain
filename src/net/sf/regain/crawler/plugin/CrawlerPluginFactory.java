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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package net.sf.regain.crawler.plugin;

import java.io.File;
import java.util.jar.Attributes;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.config.PreparatorSettings;
import net.sf.regain.crawler.document.Pluggable;
import net.sf.regain.crawler.document.PluggableFactory;

/**
 * Loads and initializes the crawler plugins.
 *  
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 * @author adapted by Benjamin
 */
public class CrawlerPluginFactory extends PluggableFactory {
	private static final String DEFAULT_CRAWLERPLUGIN_PACKAGE = "net.sf.regain.crawler.plugin.plugin";

  /** The singleton. */
	private static CrawlerPluginFactory mSingleton = null;

	private CrawlerPluginManager pluginManager = null;
	
	protected CrawlerPluginFactory()
	{
	  super();
		pluginManager = CrawlerPluginManager.getInstance();
	}
	
	/**
	 * Gets the PluggableFactory instance.
	 * 
	 * @return The PluggableFactory instance.
	 */
	public static CrawlerPluginFactory getInstance() {
		if (mSingleton == null) {
			mSingleton = new CrawlerPluginFactory();
			
		}
		return mSingleton;
	}
	
	@Override
	protected File getPluggableDir() throws RegainException {
		File preparatorDir = new File("plugins");
		if (! preparatorDir.exists()) {
			mLog.warn("No plugin directory found at " + preparatorDir.getAbsolutePath());
			return null;
		}
		return preparatorDir;
	}

	@Override
	protected void pluggableAfterInit(Pluggable pluggable, PreparatorSettings preparatorSettings) throws RegainException {
		if (preparatorSettings == null)
			pluginManager.registerPlugin((CrawlerPlugin) pluggable);
		else
			pluginManager.registerPlugin((CrawlerPlugin) pluggable, preparatorSettings.getPriority());
		mLog.info("Plugin " + pluggable.getClass().getName() + " was registered.");
	}

	@Override
	protected String[] getClassNames(File pluggableFile, Attributes attributes)
			throws RegainException {
		String clazz = attributes.getValue("Plugin-Class");
		if (clazz == null)
			throw new RegainException("The plugin file " + pluggableFile + " did not have the required attribute 'Plugin-Class' in its manifest.xml.");
		return new String[]{clazz};
	}

  @Override
  protected String getDefaultPackage()
  {
    return DEFAULT_CRAWLERPLUGIN_PACKAGE;
  }
  
  

}
