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

package net.sf.regain.crawler.document;

import java.io.File;
import java.util.jar.Attributes;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.PreparatorSettings;

/**
 * Loads and initializes the preparators.
 *  
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class PreparatorFactory extends PluggableFactory {

	/** The singleton. */
	private static PreparatorFactory mSingleton;

	/**
	 * Gets the PluggableFactory instance.
	 * 
	 * @return The PluggableFactory instance.
	 */
	public static PreparatorFactory getInstance() {
		if (mSingleton == null) {
			mSingleton = new PreparatorFactory();
		}
		return mSingleton;
	}

  @Override
	protected void pluggableAfterInit(Pluggable pluggable,
			PreparatorSettings preparatorSettings) throws RegainException {

		if (preparatorSettings == null)
			return;

		Preparator prep = (Preparator) pluggable;

		// Set the url regex
		String urlRegexAsString = preparatorSettings.getUrlRegex();
		if (urlRegexAsString != null) {
			RE urlRegex;
			try {
				urlRegex = new RE(urlRegexAsString);
			}
			catch (RESyntaxException exc) {
				throw new RegainException("urlRegex of preparator " + prep.getClass().getName()
						+ " has wrong syntax", exc);
			}
			prep.setUrlRegex(urlRegex);
		}

		// Set the priority 
		prep.setPriority(preparatorSettings.getPriority());

	}

  @Override
	protected File getPluggableDir() throws RegainException {
		File preparatorDir = new File("preparator");
		if (! preparatorDir.exists()) {
			throw new RegainException("Preparator directory not found: " +
					preparatorDir.getAbsolutePath());
		}
		return preparatorDir;
	}

  @Override
	protected String[] getClassNames(File pluggableFile, Attributes attributes)
	throws RegainException {
		String classNameCsv = attributes.getValue("Preparator-Classes");
		if (classNameCsv == null) {
			throw new RegainException("The manifest in preparator file '" + pluggableFile
					+ "' has no 'Preparator-Classes' attribute");
		}
		String[] classNameArr = RegainToolkit.splitString(classNameCsv, ";", true);
		return classNameArr;
	}
	
	  /**
	   * Creates an array of preparators from the settings.
	   *
	   * @param preparatorSettingsArr The list with the preparator settings.
	   * @return The preparators. 
	   * @throws RegainException If the creation of a preparator failed.
	   */
	  public Preparator[] createPreparatorArr(PreparatorSettings[] preparatorSettingsArr)
	    throws RegainException {
		  return createPluggables(preparatorSettingsArr).toArray(new Preparator[]{});
	  }

    @Override
    protected String getDefaultPackage()
    {
      return PreparatorSettings.DEFAULT_PREPARATOR_PACKAGE;
    }
}
