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
 *     $Date: 2008-11-23 23:46:59 +0100 (So, 23 Nov 2008) $
 *   $Author: thtesche $
 * $Revision: 364 $
 */
package net.sf.regain.crawler.document;

import java.util.HashMap;
import java.util.Map;

import java.util.Vector;
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
  /** Der gesä
   * uberte Inhalt. */
  private String mCleanedContent;
  /** Die Zusammenfassung des Dokuments. */
  private String mSummary;
  /** Die extrahierten Überschriften. Kann <code>null</code> sein */
  private String mHeadlines;
  /** Der Pfad, über den das Dokument zu erreichen ist. */
  private PathElement[] mPath;
  /** The additional fields that should be indexed. */
  private HashMap mAdditionalFieldMap;
  /** The assigned mimetypes for the preparator */
  private String[] mMimeTypes;
  /** The priority of the preparator. Used for the selection of preparators */
  private int mPriority; 

  /**
   * Creates a new instance of AbstractPreparator.
   * <p>
   * The preparator won't accept any documents until a new rule was defined
   * using {@link #setUrlRegex(RE)}.
   *
   * @see #setUrlRegex(RE)
   * @see #accepts(RawDocument)
   */
  public AbstractPreparator() {
  }


  /**
   * Creates a new instance of AbstractPreparator.
   * <p>
   * If <code>urlRegex</code> is null, the preparator won't accept any documents.
   *
   * @param urlRegex the regex a URL must match to to be accepted by this
   *        preparator (may be null)
   *
   * @see #setUrlRegex(RE)
   * @see #accepts(RawDocument)
   */
  public AbstractPreparator(RE urlRegex) {
    mUrlRegex = urlRegex;
  }


  /**
   * Creates a new instance of AbstractPreparator.
   * <p>
   * If <code>extention</code> is null or empty, the preparator won't accept any
   * documents.
   * 
   * @param extention The file extension a URL must have to be accepted by
   *        this preparator.
   * @throws RegainException If creating the preparator failed.
   *
   * @see #setUrlRegex(RE)
   * @see #accepts(RawDocument)
   */
  public AbstractPreparator(String mimeType) throws RegainException {
    mMimeTypes = new String[] {mimeType};
    // this(createExtentionRegex(extention));
  }


  /**
   * Creates a new instance of AbstractPreparator.
   * <p>
   * If <code>extentionArr</code> is null or empty, the preparator won't accept
   * any documents.
   *
   * @param extentionArr The file extensions a URL must have one to be accepted
   *        by this preparator.
   * @throws RegainException If creating the preparator failed.
   *
   * @see #setUrlRegex(RE)
   * @see #accepts(RawDocument)
   */
  public AbstractPreparator(String[] mimeTypeArr) throws RegainException {
    mMimeTypes = mimeTypeArr;
    // this(createExtentionRegex(extentionArr));
  }

  
  /**
   * Creates a regex that matches a file extensions.
   * <p>
   * If <code>extention</code> is null or empty, null will be returned.
   *
   * @param extention The file extension to create the regex for.
   * @return The regex.
   * @throws RegainException If the regex couldn't be created.
   */
  private static RE createExtentionRegex(String extention)
    throws RegainException
  {
    if (extention == null || extention.length() == 0) {
      return null;
    }

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
   * <p>
   * If <code>extentionArr</code> is null or empty, null will be returned.
   *
   * @param extentionArr The file extensions to create the regex for.
   * @return The regex.
   * @throws RegainException If the regex couldn't be created.
   */
  private static RE createExtentionRegex(String[] extentionArr)
    throws RegainException
  {
    if (extentionArr == null || extentionArr.length == 0) {
      return null;
    }

    StringBuffer buffer = new StringBuffer("\\.(");
    for (int i = 0; i < extentionArr.length; i++) {
      if (i > 0) {
        buffer.append("|");
      }
      buffer.append(extentionArr[i]);
    }
    buffer.append(")$");

    String urlRegex = buffer.toString();
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
   * <p>
   * If <code>urlRegex</code> is null, the preparator won't accept any documents.
   *
   * @param urlRegex the new URL regex (may be null)
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
    if (mUrlRegex == null) {
      if( mMimeTypes != null && mMimeTypes.length > 0 ) {
        for( String mimeType : mMimeTypes ){
          if( mimeType.equals(rawDocument.getMimeType()))
            return true;
        }
        return false;
      } else {
        return false;
      }
    } else {
      return mUrlRegex.match(rawDocument.getUrl());
    }
  }



  /**
   * Gibt den Titel des Dokuments zurück.
   * <p>
   * Falls kein Titel extrahiert werden konnte, wird <CODE>null</CODE>
   * zurückgegeben.
   *
   * @return Der Titel des Dokuments.
   */
  public String getTitle() {
    return mTitle;
  }



  /**
   * Setzt den Titel des Dokuments, das gerade Präpariert wird.
   *
   * @param title Der Titel.
   */
  protected void setTitle(String title) {
    mTitle = title;
  }



  /**
   * Gibt den von Formatierungsinformation befreiten Inhalt des Dokuments zurück.
   *
   * @return Der ges�uberte Inhalt.
   */
  public String getCleanedContent() {
    return mCleanedContent;
  }



  /**
   * Setzt von Formatierungsinformation befreiten Inhalt des Dokuments, das
   * gerade Präpariert wird.
   *
   * @param cleanedContent Der ges�uberte Inhalt.
   */
  protected void setCleanedContent(String cleanedContent) {
    mCleanedContent = cleanedContent;
  }



  /**
   * Gibt eine Zusammenfassung für das Dokument zurück.
   * <p>
   * Da eine Zusammenfassung nicht einfach m�glich ist, wird <CODE>null</CODE>
   * zurückgegeben.
   *
   * @return Eine Zusammenfassung für das Dokument
   */
  public String getSummary() {
    return mSummary;
  }



  /**
   * Setzt die Zusammenfassung des Dokuments, das gerade Präpariert wird.
   *
   * @param summary Die Zusammenfassung
   */
  protected void setSummary(String summary) {
    mSummary = summary;
  }



  /**
   * Gibt die überschriften des Dokuments zurück.
   * <p>
   * Es handelt sich dabei nicht um die überschrift des Dokuments selbst,
   * sondern lediglich um Unter-überschriften, die in dem Dokument verwendendet
   * werden. Mit Hilfe dieser überschriften läßt sich eine bessere Relevanz
   * berechnen.
   * <p>
   * Wenn keine überschriften gefunden wurden, dann wird <code>null</code>
   * zurückgegeben.
   *
   * @return Die überschriften des Dokuments.
   */
  public String getHeadlines() {
    return mHeadlines;
  }



  /**
   * Setzt die überschriften, in im Dokument, das gerade Präpariert wird,
   * gefunden wurden.
   *
   * @param headlines Die Zusammenfassung
   */
  protected void setHeadlines(String headlines) {
    mHeadlines = headlines;
  }



  /**
   * Gibt den Pfad zurück, über den das Dokument zu erreichen ist.
   * <p>
   * Falls kein Pfad verfügbar ist, wird <code>null</code> zurückgegeben.
   *
   * @return Der Pfad, über den das Dokument zu erreichen ist.
   */
  public PathElement[] getPath() {
    return mPath;
  }



  /**
   * Setzt den Pfad, über den das Dokument zu erreichen ist.
   *
   * @param path Der Pfad, über den das Dokument zu erreichen ist.
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
   * Gets the priority of the preparator
   * @return int the priority
   */
  public int getPriority() {
    return mPriority;
  }

  /**
   * Sets the priority of the preparator
   * @param priority read from config or default value settings
   */
  public void setPriority(int priority) {
    this.mPriority = priority;
  }

  /**
   * Release all ressources used for handling a document.
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
   * Concatenate all parts together, use ', ' as delimiter. If a parts is empty or consists
   * only of whitespaces the part will be negleted.
   * 
   * @param parts for concatenation
   * @param maxPartsUsed number of partsused for concatenation
   * @return the resulting string whith all single parts concatenated
   */
   protected String concatenateStringParts(Vector<String> parts, int maxPartsUsed) {

    String result = "";

    if (parts.size() > 0) {
      int end = parts.size();
      if (maxPartsUsed < parts.size()) {
        end = maxPartsUsed;
      }
      for (int i = 0; i < end; i++) {
        // Iterate over single parts
        if (parts.get(i).length() > 0) {
          result += parts.get(i);
          if (i < end - 1) {
            result += ", ";
          }
        }
      }
    }
    return result;
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
