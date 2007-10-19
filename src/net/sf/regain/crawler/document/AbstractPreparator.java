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
 *     $Date: 2005-11-21 11:20:09 +0100 (Mo, 21 Nov 2005) $
 *   $Author: til132 $
 * $Revision: 180 $
 */
package net.sf.regain.crawler.document;

import java.util.HashMap;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.config.PreparatorConfig;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Abstract implementation of a preparator.
 * <p>
 * Implements the getter methods and assumes the clean-up between two
 * preparations (See {@link #cleanUp()}).
 * <p>
 * Child class may set the values using the protected setter methods.
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractPreparator implements Preparator {

  /**
   * The regular expression a URL must match to, to be prepared by this
   * preparator.
   */
  private RE mUrlRegex;
  /** Der gefundene Titel. */
  private String mTitle;
  /** Der ges�uberte Inhalt. */
  private String mCleanedContent;
  /** Die Zusammenfassung des Dokuments. */
  private String mSummary;
  /** Die extrahierten �berschriften. Kann <code>null</code> sein */
  private String mHeadlines;
  /** Der Pfad, �ber den das Dokument zu erreichen ist. */
  private PathElement[] mPath;
  /** The additional fields that should be indexed. */
  private HashMap mAdditionalFieldMap;


  /**
   * Creates a new instance of AbstractPreparator.
   * 
   * @param urlRegex The regex a URL must match to to be accepted by this
   *        preparator.
   * @see #accepts(RawDocument)
   */
  public AbstractPreparator(RE urlRegex) {
    mUrlRegex = urlRegex;
  }

  
  /**
   * Creates a new instance of AbstractPreparator.
   * 
   * @param extention The file extension a URL must have to be accepted by
   *        this preparator.
   * @throws RegainException If creating the preparator failed.
   * @see #accepts(RawDocument)
   */
  public AbstractPreparator(String extention) throws RegainException {
    this(createExtentionRegex(extention));
  }


  /**
   * Creates a new instance of AbstractPreparator.
   * 
   * @param extentionArr The file extensions a URL must have one to be accepted
   *        by this preparator.
   * @throws RegainException If creating the preparator failed.
   * @see #accepts(RawDocument)
   */
  public AbstractPreparator(String[] extentionArr) throws RegainException {
    this(createExtentionRegex(extentionArr));
  }

  
  /**
   * Creates a regex that matches a file extensions.
   * 
   * @param extention The file extension to create the regex for.
   * @return The regex.
   * @throws RegainException If the regex couldn't be created.
   */
  private static RE createExtentionRegex(String extention)
    throws RegainException
  {
    String regex = "\\." + extention + "$";
    try {
      return new RE(regex, RE.MATCH_CASEINDEPENDENT);
    } catch (RESyntaxException exc) {
      throw new RegainException("Creating accept regex for preparator failed: "
          + regex, exc);
    }
  }
  

  /**
   * Creates a regex that matches a set of file extensions.
   * 
   * @param extentionArr The file extensions to create the regex for.
   * @return The regex.
   * @throws RegainException If the regex couldn't be created.
   */
  private static RE createExtentionRegex(String[] extentionArr)
    throws RegainException
  {
    String urlRegex;
    if (extentionArr.length == 0) {
      throw new IllegalArgumentException("extentionArr is empty");
    }
    else {
      StringBuffer buffer = new StringBuffer("\\.(");
      for (int i = 0; i < extentionArr.length; i++) {
        if (i > 0) {
          buffer.append("|");
        }
        buffer.append(extentionArr[i]);
      }
      buffer.append(")$");
      urlRegex = buffer.toString();
    }

    try {
      return new RE(urlRegex, RE.MATCH_CASEINDEPENDENT);
    } catch (RESyntaxException exc) {
      throw new RegainException("Creating accept regex for preparator failed: "
          + urlRegex, exc);
    }
  }


  /**
   * Initializes the preparator.
   * <p>
   * Does nothing by default. May be overridden by subclasses.
   *
   * @param config The configuration for this preparator.
   * @throws RegainException If the regular expression or the configuration
   *         has an error.
   */
  public void init(PreparatorConfig config) throws RegainException {
  }


  /**
   * Sets the regular expression a URL must match to, to be prepared by this
   * preparator.
   * 
   * @param urlRegex The new URL regex.
   * @see #accepts(RawDocument)
   */
  public void setUrlRegex(RE urlRegex) {
    mUrlRegex = urlRegex;
  }


  /**
   * Gets whether the preparator is able to process the given document. This is
   * the case, if its URL matches the URL regex.
   *
   * @param rawDocument The document to check.
   * @return Whether the preparator is able to process the given document.
   * @see #setUrlRegex(RE)
   */
  public boolean accepts(RawDocument rawDocument) {
    return mUrlRegex.match(rawDocument.getUrl());
  }



  /**
   * Gibt den Titel des Dokuments zur�ck.
   * <p>
   * Falls kein Titel extrahiert werden konnte, wird <CODE>null</CODE>
   * zur�ckgegeben.
   *
   * @return Der Titel des Dokuments.
   */
  public String getTitle() {
    return mTitle;
  }



  /**
   * Setzt den Titel des Dokuments, das gerade pr�pariert wird.
   *
   * @param title Der Titel.
   */
  protected void setTitle(String title) {
    mTitle = title;
  }



  /**
   * Gibt den von Formatierungsinformation befreiten Inhalt des Dokuments zur�ck.
   *
   * @return Der ges�uberte Inhalt.
   */
  public String getCleanedContent() {
    return mCleanedContent;
  }



  /**
   * Setzt von Formatierungsinformation befreiten Inhalt des Dokuments, das
   * gerade pr�pariert wird.
   *
   * @param cleanedContent Der ges�uberte Inhalt.
   */
  protected void setCleanedContent(String cleanedContent) {
    mCleanedContent = cleanedContent;
  }



  /**
   * Gibt eine Zusammenfassung f�r das Dokument zur�ck.
   * <p>
   * Da eine Zusammenfassung nicht einfach m�glich ist, wird <CODE>null</CODE>
   * zur�ckgegeben.
   *
   * @return Eine Zusammenfassung f�r das Dokument
   */
  public String getSummary() {
    return mSummary;
  }



  /**
   * Setzt die Zusammenfassung des Dokuments, das gerade pr�pariert wird.
   *
   * @param summary Die Zusammenfassung
   */
  protected void setSummary(String summary) {
    mSummary = summary;
  }



  /**
   * Gibt die �berschriften des Dokuments zur�ck.
   * <p>
   * Es handelt sich dabei nicht um die �berschrift des Dokuments selbst,
   * sondern lediglich um Unter-�berschriften, die in dem Dokument verwendendet
   * werden. Mit Hilfe dieser �berschriften l��t sich eine bessere Relevanz
   * berechnen.
   * <p>
   * Wenn keine �berschriften gefunden wurden, dann wird <code>null</code>
   * zur�ckgegeben.
   *
   * @return Die �berschriften des Dokuments.
   */
  public String getHeadlines() {
    return mHeadlines;
  }



  /**
   * Setzt die �berschriften, in im Dokument, das gerade pr�pariert wird,
   * gefunden wurden.
   *
   * @param headlines Die Zusammenfassung
   */
  protected void setHeadlines(String headlines) {
    mHeadlines = headlines;
  }



  /**
   * Gibt den Pfad zur�ck, �ber den das Dokument zu erreichen ist.
   * <p>
   * Falls kein Pfad verf�gbar ist, wird <code>null</code> zur�ckgegeben.
   *
   * @return Der Pfad, �ber den das Dokument zu erreichen ist.
   */
  public PathElement[] getPath() {
    return mPath;
  }



  /**
   * Setzt den Pfad, �ber den das Dokument zu erreichen ist.
   *
   * @param path Der Pfad, �ber den das Dokument zu erreichen ist.
   */
  public void setPath(PathElement[] path) {
    mPath = path;
  }


  /**
   * Gets additional fields that should be indexed.
   * <p>
   * These fields will be indexed and stored.
   * 
   * @return The additional fields or <code>null</code>.
   */
  public Map getAdditionalFields() {
    return mAdditionalFieldMap;
  }


  /**
   * Adds an additional field to the current document.
   * <p>
   * This field will be indexed and stored.
   * 
   * @param fieldName The name of the field.
   * @param fieldValue The value of the field.
   */
  public void addAdditionalField(String fieldName, String fieldValue) {
    if (mAdditionalFieldMap == null) {
      mAdditionalFieldMap = new HashMap();
    }
    mAdditionalFieldMap.put(fieldName, fieldValue);
  }
  

  /**
   * Gibt alle Ressourcen frei, die f�r die Informationen �ber das Dokument
   * reserviert wurden.
   */
  public void cleanUp() {
    mTitle = null;
    mCleanedContent = null;
    mSummary = null;
    mHeadlines = null;
    mPath = null;
    mAdditionalFieldMap = null;
  }


  /**
   * Frees all resources reserved by the preparator.
   * <p>
   * Is called at the end of the crawler process after all documents were
   * processed.
   * 
   * @throws RegainException If freeing the resources failed.
   */
  public void close() throws RegainException {
  }

}
