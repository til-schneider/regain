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
 *  $RCSfile: HtmlPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/HtmlPreparator.java,v $
 *     $Date: 2005/03/16 15:52:50 $
 *   $Author: til132 $
 * $Revision: 1.8 $
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

  /** Ein regul�rer Ausdruck, der den Titel eines HTMl-Dokuments findet. */
  private RE mExtractHtmlTitleRE;

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
   */
  public HtmlPreparator() {
    super(new RE("(^http://[^/]*$)|(^http://.*/[^\\.]*$)|(\\.(/|html|htm)$)"));
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

    // Create the title extractor regex.
    try {
      mExtractHtmlTitleRE = new RE("<title>([^<]{1,500})</title>", RE.MATCH_CASEINDEPENDENT);
    }
    catch (RESyntaxException exc) {
      // Since the regular expression is hard coded this will never happen
      throw new RegainException("Syntax error in regular expression", exc);
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
    String contentAsString = rawDocument.getContentAsString();

    // Get the title
    String title = extractHtmlTitle(contentAsString);
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
    if (mExtractHtmlTitleRE.match(content)) {
      return mExtractHtmlTitleRE.getParen(1);
    }

    return null;
  }

}
