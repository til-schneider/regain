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
package net.sf.regain.crawler.preparator;

import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.PathElement;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.html.HtmlContentExtractor;
import net.sf.regain.crawler.preparator.html.HtmlPathExtractor;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Pr�pariert ein HTML-Dokument f�r die Indizierung.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Til Schneider, www.murfman.de
 */
public class HtmlPreparator extends AbstractPreparator {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(HtmlPreparator.class);

  /**
   * Die HtmlContentExtractor, die den jeweiligen zu indizierenden Inhalt aus
   * den HTML-Dokumenten schneiden.
   */
  private HtmlContentExtractor[] mContentExtractorArr;

  /**
   * Die HtmlPathExtractor, die den jeweiligen Pfad aus den HTML-Dokumenten
   * extrahieren.
   */
  private HtmlPathExtractor[] mPathExtractorArr;
  

  /**
   * Creates a new instance of HtmlPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public HtmlPreparator() throws RegainException {
    super(createAcceptRegex());
  }


  /**
   * Creates a regex that matches a URL that contains HTML.
   * 
   * @return The regex.
   * @throws RegainException If the regex couldn't be created.
   */
  private static RE createAcceptRegex() throws RegainException {
    String regex = "(^http://[^/]*/?$)|(^http://.*/[^\\.]*$)|(^http://.*/$)|(\\.(html|htm|jsp|php\\d?|asp)$)";
    try {
      return new RE(regex, RE.MATCH_CASEINDEPENDENT);
    } catch (RESyntaxException exc) {
      throw new RegainException("Creating accept regex for preparator failed: "
          + regex, exc);
    }
  }


  /**
   * Initializes the preparator.
   * 
   * @param config The configuration.
   * @throws RegainException If the configuration has an error.
   */
  public void init(PreparatorConfig config) throws RegainException {
    // Read the content extractors
    Map[] sectionArr = config.getSectionsWithName("contentExtractor");
    mContentExtractorArr = new HtmlContentExtractor[sectionArr.length];
    for (int i = 0; i < mContentExtractorArr.length; i++) {
      String prefix            = (String) sectionArr[i].get("prefix");
      String contentStartRegex = (String) sectionArr[i].get("startRegex");
      String contentEndRegex   = (String) sectionArr[i].get("endRegex");
      String headlineRegex     = (String) sectionArr[i].get("headlineRegex");
      int headlineRegexGroup   = getIntParam(sectionArr[i], "headlineRegex.group");
      
      mContentExtractorArr[i] = new HtmlContentExtractor(prefix,
          contentStartRegex, contentEndRegex, headlineRegex, headlineRegexGroup);
    }
   
    // Read the path extractors
    sectionArr = config.getSectionsWithName("pathExtractor");
    mPathExtractorArr = new HtmlPathExtractor[sectionArr.length];
    for (int i = 0; i < mPathExtractorArr.length; i++) {
      String prefix          = (String) sectionArr[i].get("prefix");
      String pathStartRegex  = (String) sectionArr[i].get("startRegex");
      String pathEndRegex    = (String) sectionArr[i].get("endRegex");
      String pathNodeRegex   = (String) sectionArr[i].get("pathNodeRegex");
      int pathNodeUrlGroup   = getIntParam(sectionArr[i], "pathNodeRegex.urlGroup");
      int pathNodeTitleGroup = getIntParam(sectionArr[i], "pathNodeRegex.titleGroup");
      
      mPathExtractorArr[i] = new HtmlPathExtractor(prefix, pathStartRegex,
          pathEndRegex, pathNodeRegex, pathNodeUrlGroup,
          pathNodeTitleGroup);
    }
  }

  
  /**
   * Gets an int parameter from a configuration section
   * 
   * @param configSection The configuration section to get the int param from.
   * @param paramName The name of the parameter
   * @return The value of the parameter.
   * @throws RegainException If the parameter is not set or is not a number.
   */
  private int getIntParam(Map configSection, String paramName)
    throws RegainException
  {
    String asString = (String) configSection.get(paramName);
    if (asString == null) {
      throw new RegainException("Error in configuration for "
          + getClass().getName() + ": Preparator param '" + paramName
          + "' is not set");
    }
    
    asString = asString.trim();
    try {
      return Integer.parseInt(asString);
    }
    catch (NumberFormatException exc) {
      throw new RegainException("Error in configuration for "
          + getClass().getName() + ": Preparator param '" + paramName
          + "' is not a number: '" + asString + "'", exc);
    }
  }
  

  /**
   * Pr�pariert ein Dokument f�r die Indizierung.
   *
   * @param rawDocument Das zu pr�pariernde Dokument.
   *
   * @throws RegainException Wenn die Pr�paration fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    // Get the title
    String title = extractHtmlTitle(rawDocument.getContentAsString());
    setTitle(title);

    // Find the content extractor that is responsible for this document
    HtmlContentExtractor contentExtractor = null;
    if (mContentExtractorArr != null) {
      for (int i = 0; i < mContentExtractorArr.length; i++) {
        if (mContentExtractorArr[i].accepts(rawDocument)) {
          contentExtractor = mContentExtractorArr[i];
        }
      }
    }

    // Cut the content and extract the headlines
    String cuttedContent;
    String headlines;
    if (contentExtractor == null) {
      // There is no HtmlContentExtractor responsible for this document
      if (mLog.isDebugEnabled()) {
        mLog.debug("No HTML content extractor is responsible for " + rawDocument.getUrl());
      }

      cuttedContent = rawDocument.getContentAsString();
      headlines = null;
    } else {
      cuttedContent = contentExtractor.extractContent(rawDocument);

      headlines = contentExtractor.extractHeadlines(cuttedContent);
    }

    // Clean the content from tags
    String cleanedContent = CrawlerToolkit.cleanFromHtmlTags(cuttedContent);
    setCleanedContent(cleanedContent);

    if (headlines != null) {
      // Replace HTML Entities
      headlines = CrawlerToolkit.replaceHtmlEntities(headlines);

      // Set the headlines
      setHeadlines(headlines);
    }

    // Find the path extractor that is responsible for this document
    HtmlPathExtractor pathExtractor = null;
    if (mPathExtractorArr != null) {
      for (int i = 0; i < mPathExtractorArr.length; i++) {
        if (mPathExtractorArr[i].accepts(rawDocument)) {
          pathExtractor = mPathExtractorArr[i];
        }
      }
    }

    // Extract the path from the document
    if (pathExtractor != null) {
      PathElement[] path = pathExtractor.extractPath(rawDocument);
      setPath(path);
    }
  }


  /**
   * Extrahiert den Titel aus einem HTML-Dokument.
   *
   * @param content Der Inhalt (die HTML-Rohdaten) des Dokuments, dessen Titel
   *        ermittelt werden soll.
   *
   * @return Den Titel des HTML-Dokuments.
   */
  private String extractHtmlTitle(String content) {
    final String TITLE_START_TAG = "<title>";
    
    // NOTE: We don't use a regex here, beause it's far too slow and it doesn't
    //       stop when the body begins.
    int pos = -1;
    int startPos = -1;
    while((pos = content.indexOf('<', pos + 1)) != -1) {
      // A tag starts here -> Check whether this is the title tag
      if (isIndexOf(content, TITLE_START_TAG, pos)) {
        // The title starts here -> Remember the start pos
        startPos = pos + TITLE_START_TAG.length();
        break;
      }
      else if (isIndexOf(content, "<body", pos)) {
        // The body starts here -> Give up
        break;
      }
    }
    
    // Scan until the end of the title
    if (startPos != -1) {
      pos = startPos - 1;
      while((pos = content.indexOf('<', pos + 1)) != -1) {
        if (isIndexOf(content, "</title>", pos)) {
          // We found the title's end tag -> extract the title
          return content.substring(startPos, pos);
        }
        else if (pos > startPos + 1000) {
          // This is too long -> There won't come a end tag -> Give up
          break;
        }
      }
    }

    return null;
  }


  /**
   * Checks whether an expected substring is at a certain position. 
   * 
   * @param content The String to check the excepted substring.
   * @param expected The expected substring.
   * @param pos The position where the substring is expected.
   * @return Whether the expected substring is really at this position.
   */
  private boolean isIndexOf(String content, String expected, int pos) {
    if (content.length() < pos + expected.length()) {
      // The expected String doesn't match here
      return false;
    }
    
    String substring = content.substring(pos, pos + expected.length());
    return expected.equalsIgnoreCase(substring);
  }

}
