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
package net.sf.regain.crawler.document;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.config.PreparatorSettings;

import org.apache.log4j.Logger;

/**
 * Loads and initializes the preparators.
 *  
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public abstract class PluggableFactory {
  
  /** The logger for this class */
  protected static Logger mLog = Logger.getLogger(PluggableFactory.class);
  
  protected PluggableFactory() { }

  public List<Pluggable> createPluggables(PreparatorSettings[] preparatorSettingsArr)
    throws RegainException
  {
    // Load the preparators
    HashMap<String, Pluggable> preparatorHash = new HashMap<String, Pluggable>();
    File preparatorDir = getPluggableDir();
    if (preparatorDir == null)
    	return null;
    
    File[] jarFileArr = preparatorDir.listFiles(new ExtensionFilter(".jar"));
    for (File jarFile : jarFileArr) {
    	loadPreparatorJar(jarFile, preparatorHash, preparatorSettingsArr);
    }    
    // Create the preparator array
    List<Pluggable> preparatorArr = new ArrayList<Pluggable>(preparatorHash.size());

    // Add the configured preparators in the configured order
    for (int i = 0; i < preparatorSettingsArr.length; i++) {
      if (preparatorSettingsArr[i].isEnabled()) {
        // Add the preparator with this class
        // NOTE: We remove the preparator to avoid a double initializeation
        //       and to get easily the unconfigured preparators
        String prepClassName = preparatorSettingsArr[i].getPreparatorClassName();
        Pluggable prep = preparatorHash.remove(prepClassName);
        if (prep == null) {
          mLog.warn("Crawler configuration contains non-existing preparator or plugin: "
              + prepClassName);
        } else {
          // Initialize the preparator
          prep.init(preparatorSettingsArr[i].getPreparatorConfig());
          
          pluggableAfterInit(prep, preparatorSettingsArr[i]);
          
          // Add it to the array
          preparatorArr.add(prep);
        }
      }
    }
    
    // Add all the preparators that are not configured
    for(Pluggable prep : preparatorHash.values()) {
      // Initialize the preparator with an empty config
      prep.init(new PreparatorConfig());
      
      pluggableAfterInit(prep, null);
      
      // Add it to the array
      preparatorArr.add(prep);
    }
    
    return preparatorArr;
  }


  protected abstract File getPluggableDir() throws RegainException;


  protected abstract void pluggableAfterInit(Pluggable pluggable, PreparatorSettings preparatorSettings) throws RegainException;

/**
   * Loads a preparator jar.
   * 
   * @param file The preparator jar to load.
   * @param preparatorHash The hash where to add all loaded preparators.
   * @param preparatorSettingsArr The preparator settings. Used to determine
   *        whether a preparator is enabled.
   * @throws RegainException If loading the jar failed.
   */
  private void loadPreparatorJar(File file, HashMap<String, Pluggable> preparatorHash,
    PreparatorSettings[] preparatorSettingsArr)
    throws RegainException
  {
    // Find the preparator classes
    JarFile jarFile = null;
    try {
      jarFile = new JarFile(file);

      // Load the manifest
      InputStream in = jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF"));
      Manifest manifest = new Manifest(in);
      in.close();

      // Read the class names
      Attributes attributes = manifest.getMainAttributes();
      String[] classNameArr = getClassNames(file, attributes);
      
      // Load the classes if they are not disabled
      URLClassLoader loader = null;
      for (int i = 0; i < classNameArr.length; i++) {
        // Get the class name
        String className = classNameArr[i];
        if (className.startsWith(".")) {
          className = PreparatorSettings.DEFAULT_PREPARATOR_PACKAGE + className;
        }
        
        if (isPreparatorEnabled(className, preparatorSettingsArr)) {
          // Create the class loader if nessesary
          if (loader == null) {
            loader = new URLClassLoader(new URL[] { file.toURI().toURL() });
          }
          
          // Load the preparator and add it to the preparatorHash
          Pluggable prep = (Pluggable) RegainToolkit.createClassInstance(className, Pluggable.class, loader);
          preparatorHash.put(className, prep);
        }
      }
    }
    catch (Throwable thr) {
      throw new RegainException("Loading preparator or plugin file '" + file
          + "' failed", thr);
    }
    finally {
      if (jarFile != null) {
        try { jarFile.close(); } catch (IOException exc) {}
      }
    }
  }


  protected abstract String[] getClassNames(File pluggableFile, Attributes attributes) throws RegainException;


  /**
   * Checks whether a preparator is enabled.
   *  
   * @param className The class name of the preparator to check.
   * @param preparatorSettingsArr The preparator settings to use to determine
   *        whether a preparator is enabled.
   * @return Whether the preparator is enabled.
   */
  private boolean isPreparatorEnabled(String className,
    PreparatorSettings[] preparatorSettingsArr)
  {
    for (int i = 0; i < preparatorSettingsArr.length; i++) {
      if (preparatorSettingsArr[i].getPreparatorClassName().equals(className)) {
        // These are the settings for the preparator
        return preparatorSettingsArr[i].isEnabled();
      }
    }
    
    // There are no settings for the preparator -> It is enabled
    return true;
  }

  class ExtensionFilter implements FilenameFilter {
	  private String ext;

	  ExtensionFilter(String ext) { this.ext = ext; }
	  @Override
	  public boolean accept(File dir, String name) {
		  return name.toLowerCase().endsWith(ext);
	  }
  }
}
