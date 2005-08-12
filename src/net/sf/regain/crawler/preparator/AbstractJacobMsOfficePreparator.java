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
 *  $RCSfile: AbstractJacobMsOfficePreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/AbstractJacobMsOfficePreparator.java,v $
 *     $Date: 2005/08/13 11:33:30 $
 *   $Author: til132 $
 * $Revision: 1.3 $
 */
package net.sf.regain.crawler.preparator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.AbstractPreparator;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public abstract class AbstractJacobMsOfficePreparator extends AbstractPreparator {
  
  /** The properties that should be extracted. */
  private String[] mWantedPropertiesArr;
  
  /**
   * Holds the document properties that may be extracted from a word document.
   * (key: The property name (String), value: The property constant (Variant))
   */
  private HashMap mPropertyMap;


  /**
   * Creates a new instance of JacobMsWordPreparator.
   * 
   * @param extentionArr The file extensions a URL must have one to be accepted
   *        by this preparator.
   */
  public AbstractJacobMsOfficePreparator(String[] extentionArr) {
    super(extentionArr);
    
    // NOTE: See: http://mypage.bluewin.ch/reprobst/WordFAQ/DokEigen.htm#DokEigen04
    mPropertyMap = new HashMap();
    mPropertyMap.put("propTitle",       new Variant(1));  // german: Titel
    mPropertyMap.put("subject",         new Variant(2));  // german: Thema
    mPropertyMap.put("author",          new Variant(3));  // german: Autor
    mPropertyMap.put("keywords",        new Variant(4));  // german: Stichwoerter
    mPropertyMap.put("comments",        new Variant(5));  // german: Kommentar
    mPropertyMap.put("template",        new Variant(6));  // german: Vorlage
    mPropertyMap.put("lastAuthor",      new Variant(7));  // german: Zuletzt gespeichert von
    mPropertyMap.put("revision",        new Variant(8));  // german: Version
    // mPropertyMap.put("appName",      new Variant(9));  // N/A
    mPropertyMap.put("timeLastPrinted", new Variant(10)); // german: Gedruckt am
    mPropertyMap.put("timeCreated",     new Variant(11)); // german: Erstellt am
    mPropertyMap.put("timeLastSaved",   new Variant(12)); // german: Geaendert am
    mPropertyMap.put("totalEditTime",   new Variant(13)); // german: Gesamtbearbeitungszeit
    mPropertyMap.put("pages",           new Variant(14)); // german: Seiten
    mPropertyMap.put("words",           new Variant(15)); // german: Woerter
    mPropertyMap.put("characters",      new Variant(16)); // german: Zeichen (ohne Leerzeichen)
    mPropertyMap.put("security",        new Variant(17)); // german: Dokumentenschutz
    mPropertyMap.put("category",        new Variant(18)); // german: Kategorie
    // mPropertyMap.put("format",       new Variant(19)); // N/A
    mPropertyMap.put("manager",         new Variant(20)); // german: Manager
    mPropertyMap.put("company",         new Variant(21)); // german: Firma
    mPropertyMap.put("bytes",           new Variant(22)); // german: Bytes
    mPropertyMap.put("lines",           new Variant(23)); // german: Zeilen
    mPropertyMap.put("paras",           new Variant(24)); // german: Absätze
    mPropertyMap.put("slides",          new Variant(25)); // N/A (MS PowerPoint)
    mPropertyMap.put("notes",           new Variant(26)); // N/A (MS PowerPoint)
    mPropertyMap.put("hiddenSlides",    new Variant(27)); // N/A (MS PowerPoint)
    mPropertyMap.put("mmClips",         new Variant(28)); // N/A (MS PowerPoint)
    mPropertyMap.put("hyperlinkBase",   new Variant(29)); // german: Hyperlinkbasis
    mPropertyMap.put("charsWSpaces",    new Variant(30)); // german: Buchstaben (mit Leerzeichen)
  }


  /**
   * Initializes the preparator.
   * 
   * @param config The configuration.
   * @throws RegainException If the configuration has an error.
   */
  public void init(PreparatorConfig config) throws RegainException {
    Map main = config.getSectionWithName("main");
    if (main != null) {
      String properties = (String) main.get("properties");
      if (properties != null) {
        mWantedPropertiesArr = RegainToolkit.splitString(properties, ";", true);
        
        // Check the properties
        for (int i = 0; i < mWantedPropertiesArr.length; i++) {
          if (mPropertyMap.get(mWantedPropertiesArr[i]) == null) {
            // This propery does not exist -> Show an error that lists the
            // properties
            StringBuffer possProp = new StringBuffer();
            Iterator iter = mPropertyMap.keySet().iterator();
            while (iter.hasNext()) {
              String property = (String) iter.next();
              if (possProp.length() > 0) {
                possProp.append(", ");
              }
              possProp.append(property);
            }
            
            throw new RegainException("MS Word property '"
                + mWantedPropertiesArr[i] + "' does not exist. Possible "
                + "properties are: " + possProp.toString());
          }
        }
      }
    }
  }

  
  /**
   * Reads the configured document properties from a MS Office document.
   * 
   * @param document The document to read the properties from.
   */
  protected void readProperties(Dispatch document) {
    // Read the document properties
    // NOTE: VB-Code: Autor = ActiveDocument.BuiltInDocumentProperties(wdPropertyAuthor).Value
    if (mWantedPropertiesArr != null) {
      for (int i = 0; i < mWantedPropertiesArr.length; i++) {
        // NOTE: We should always get a propertyConstant here since we checked
        //       it in the readConfig method.
        String propertyName = mWantedPropertiesArr[i];
        Variant propertyConstant = (Variant) mPropertyMap.get(propertyName);
        Object property = Dispatch.call(document, "BuiltInDocumentProperties", propertyConstant).getDispatch();
        String value = Dispatch.get(property, "Value").toString();
        
        addAdditionalField(propertyName, value);
      }
    }
  }
  
}
