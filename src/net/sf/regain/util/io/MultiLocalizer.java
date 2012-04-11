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
 *     $Date: 2012-04-04 15:06:49 +0200 (Mi, 04 Apr 2012) $
 *   $Author: benjaminpick $
 * $Revision: 579 $
 */
package net.sf.regain.util.io;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

/**
 * Holds localizers for multiple locales.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MultiLocalizer {
  
  /** The factory to use for creating a localizer for a specific locale. */
  private LocalizerFactory mFactory;
  
  /** Holds for a locale (Locale) a localizer (Localizer) */
  private HashMap<Locale, Localizer> mLocalizerHash;
  

  /**
   * Creates a new instance of MultiLocalizer.
   * 
   * @param clazz The class to create the MultiLocalizer for.
   */
  public MultiLocalizer(Class<?> clazz) {
    mFactory = new ClassLocalizerFactory(clazz);
    mLocalizerHash = new HashMap<Locale, Localizer>();
  }
  
  
  /**
   * Creates a new instance of MultiLocalizer.
   *
   * @param baseurl The URL to load the resource bundle from.
   * @param basename The basename of the resource bundle to load.
   */
  public MultiLocalizer(URL baseurl, String basename) {
    mFactory = new BaseLocalizerFactory(baseurl, basename);
    mLocalizerHash = new HashMap<Locale, Localizer>();
  }
  
  
  /**
   * Gets the localizer for a specific locale.
   * 
   * @param locale The locale to get the localizer for.
   * @return The localizer.
   */
  public Localizer getLocalizer(Locale locale) {
    Localizer localizer = mLocalizerHash.get(locale);
    if (localizer == null) {
      localizer = mFactory.createLocalizer(locale);
      mLocalizerHash.put(locale, localizer);
    }
    return localizer;
  }
  
  
  /**
   * A factory to use for creating a localizer for a specific locale.
   */
  private interface LocalizerFactory {
    /**
     * Creates a localizer for a specific locale.
     * 
     * @param locale The locale to create the localizer for.
     * @return The created localizer.
     */
    public Localizer createLocalizer(Locale locale);
  } // inner interface LocalizerFactory


  /**
   * A factory that creates localizers for a specific class.
   */
  private static class ClassLocalizerFactory implements LocalizerFactory {

    /** The class to create localizers for. */
    private Class<?> mClazz;
    
    /**
     * Creates a new instance of ClassLocalizerFactory.
     * 
     * @param clazz The class to create localizers for.
     */
    public ClassLocalizerFactory(Class<?> clazz) {
      mClazz = clazz;
    }
    
    /**
     * Creates a localizer for a specific locale.
     * 
     * @param locale The locale to create the localizer for.
     * @return The created localizer.
     */
    public Localizer createLocalizer(Locale locale) {
      return new Localizer(mClazz, locale);
    }

  } // inner class ClassLocalizerFactory
  
  
  /**
   * A factory that creates localizers for a specific base.
   */
  private static class BaseLocalizerFactory implements LocalizerFactory {
    
    /** The base URL where the properties files are located. */
    private URL mBaseUrl;
    /** The base name of the properties files. */
    private String mBaseName;
    
    /**
     * Creates a new instance of BaseLocalizerFactory.
     * 
     * @param baseurl The base URL where the properties files are located.
     * @param basename The base name of the properties files.
     */
    public BaseLocalizerFactory(URL baseurl, String basename) {
      mBaseUrl = baseurl;
      mBaseName = basename;
    }

    /**
     * Creates a localizer for a specific locale.
     * 
     * @param locale The locale to create the localizer for.
     * @return The created localizer.
     */
    public Localizer createLocalizer(Locale locale) {
      return new Localizer(mBaseUrl, mBaseName, locale);
    }

  } // inner class BaseLocalizerFactory

}
