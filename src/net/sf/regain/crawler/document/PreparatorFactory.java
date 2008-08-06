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
 *     $Date: 2008-08-06 16:04:27 +0200 (Mi, 06 Aug 2008) $
 *   $Author: thtesche $
 * $Revision: 325 $
 */
package net.sf.regain.crawler.document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.config.PreparatorSettings;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Loads and initializes the preparators.
 *  
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class PreparatorFactory {
  
  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(PreparatorFactory.class);
  
  /** The singleton. */
  private static PreparatorFactory mSingleton;


  /**
   * Gets the PreparatorFactory instance.
   * 
   * @return The PreparatorFactory instance.
   */
  public static PreparatorFactory getInstance() {
    if (mSingleton == null) {
      mSingleton = new PreparatorFactory();
    }
    return mSingleton;
  }


  /**
   * Creates an array of preparators from the settings.
   *
   * @param preparatorSettingsArr The list with the preparator settings.
   * @return The preparators. 
   * @throws RegainException If the creation of a preparator failed.
   */
  public Preparator[] createPreparatorArr(
    PreparatorSettings[] preparatorSettingsArr)
    throws RegainException
  {
    // Load the preparators
    HashMap preparatorHash = new HashMap();
    File preparatorDir = new File("preparator");
    if (! preparatorDir.exists()) {
      throw new RegainException("Preparator directory not found: " +
          preparatorDir.getAbsolutePath());
    }
    File[] jarFileArr = preparatorDir.listFiles();
    for (int i = 0; i < jarFileArr.length; i++) {
      if (jarFileArr[i].getName().toLowerCase().endsWith(".jar")) {
        loadPrepararorJar(jarFileArr[i], preparatorHash, preparatorSettingsArr);
      }
    }
    
    // Create the preparator array
    Preparator[] preparatorArr = new Preparator[preparatorHash.size()];
    int prepIdx = 0;

    // Add the configured preparators in the configured order
    for (int i = 0; i < preparatorSettingsArr.length; i++) {
      if (preparatorSettingsArr[i].isEnabled()) {
        // Add the preparator with this class
        // NOTE: We remove the preparator to avoid a double initializeation
        //       and to get easily the unconfigured preparators
        String prepClassName = preparatorSettingsArr[i].getPreparatorClassName();
        Preparator prep = (Preparator) preparatorHash.remove(prepClassName);
        if (prep == null) {
          mLog.warn("Crawler configuration contains non-existing preparator: "
              + prepClassName);
        } else {
          // Initialize the preparator
          prep.init(preparatorSettingsArr[i].getPreparatorConfig());
          
          // Set the url regex
          String urlRegexAsString = preparatorSettingsArr[i].getUrlRegex();
          if (urlRegexAsString != null) {
            RE urlRegex;
            try {
              urlRegex = new RE(urlRegexAsString);
            }
            catch (RESyntaxException exc) {
              throw new RegainException("urlRegex of preparator " + prepClassName
                  + " has wrong syntax", exc);
            }
            prep.setUrlRegex(urlRegex);
          }
          
          // Set the priority 
          prep.setPriority(preparatorSettingsArr[i].getPriority());
          
          // Add it to the array
          preparatorArr[prepIdx] = prep;
          prepIdx++;
        }
      }
    }
    
    // Add all the preparators that are not configured
    Iterator iter = preparatorHash.values().iterator();
    while (iter.hasNext()) {
      Preparator prep = (Preparator) iter.next();
      
      // Initialize the preparator with an empty config
      prep.init(new PreparatorConfig());
      
      // Add it to the array
      preparatorArr[prepIdx] = prep;
      prepIdx++;
    }
    
    return preparatorArr;
  }


  /**
   * Loads a preparator jar.
   * 
   * @param file The preparator jar to load.
   * @param preparatorHash The hash where to add all loaded preparators.
   * @param preparatorSettingsArr The preparator settings. Used to determine
   *        whether a preparator is enabled.
   * @throws RegainException If loading the jar failed.
   */
  private void loadPrepararorJar(File file, HashMap preparatorHash,
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
      String classNameCsv = attributes.getValue("Preparator-Classes");
      if (classNameCsv == null) {
        throw new RegainException("The manifest in preparator file '" + file
            + "' has no 'Preparator-Classes' attribute");
      }
      String[] classNameArr = RegainToolkit.splitString(classNameCsv, ";", true);
      
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
          Preparator prep = (Preparator) RegainToolkit.createClassInstance(className, Preparator.class, loader);
          preparatorHash.put(className, prep);
        }
      }
    }
    catch (Throwable thr) {
      throw new RegainException("Loading preparator file '" + file
          + "' failed", thr);
    }
    finally {
      if (jarFile != null) {
        try { jarFile.close(); } catch (IOException exc) {}
      }
    }
  }


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

}
