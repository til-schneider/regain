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
 *     $Date: 2010-11-07 16:02:14 +0100 (So, 07 Nov 2010) $
 *   $Author: thtesche $
 * $Revision: 465 $
 */
package net.sf.regain.crawler.preparator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.PathElement;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.html.HtmlContentExtractor;
import net.sf.regain.crawler.preparator.html.HtmlPathExtractor;

import net.sf.regain.crawler.preparator.html.LinkVisitor;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.beans.StringBean;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;

/**
 * Prepares a HTML-document for indexing.
 * <p>
 * The document will be parsed and a title will be extracted.
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
    super(new String[]{"text/html", "application/xhtml+xml"});
  //super(createAcceptRegex());
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
      throw new RegainException("Creating accept regex for preparator failed: " + regex, exc);
    }
  }

  /**
   * Initializes the preparator.
   * 
   * @param config The configuration.
   * @throws RegainException If the configuration has an error.
   */
  @Override
  public void init(PreparatorConfig config) throws RegainException {
    // Read the content extractors
    Map[] sectionArr = config.getSectionsWithName("contentExtractor");
    mContentExtractorArr = new HtmlContentExtractor[sectionArr.length];
    for (int i = 0; i < mContentExtractorArr.length; i++) {
      String prefix = (String) sectionArr[i].get("prefix");
      String contentStartRegex = (String) sectionArr[i].get("startRegex");
      String contentEndRegex = (String) sectionArr[i].get("endRegex");
      String headlineRegex = (String) sectionArr[i].get("headlineRegex");
      int headlineRegexGroup = getIntParam(sectionArr[i], "headlineRegex.group");

      mContentExtractorArr[i] = new HtmlContentExtractor(prefix,
        contentStartRegex, contentEndRegex, headlineRegex, headlineRegexGroup);
    }

    // Read the path extractors
    sectionArr = config.getSectionsWithName("pathExtractor");
    mPathExtractorArr = new HtmlPathExtractor[sectionArr.length];
    for (int i = 0; i < mPathExtractorArr.length; i++) {
      String prefix = (String) sectionArr[i].get("prefix");
      String pathStartRegex = (String) sectionArr[i].get("startRegex");
      String pathEndRegex = (String) sectionArr[i].get("endRegex");
      String pathNodeRegex = (String) sectionArr[i].get("pathNodeRegex");
      int pathNodeUrlGroup = getIntParam(sectionArr[i], "pathNodeRegex.urlGroup");
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
    throws RegainException {
    String asString = (String) configSection.get(paramName);
    if (asString == null) {
      throw new RegainException("Error in configuration for " + getClass().getName() + ": Preparator param '" + paramName + "' is not set");
    }

    asString = asString.trim();
    try {
      return Integer.parseInt(asString);
    } catch (NumberFormatException exc) {
      throw new RegainException("Error in configuration for " + getClass().getName() + ": Preparator param '" + paramName + "' is not a number: '" + asString + "'", exc);
    }
  }

  /**
   * Prepares a document for indexing.
   *
   * @param rawDocument document which will be prepared
   *
   * @throws RegainException if something goes wrong while preparation
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
    boolean isContentCutted = false;
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
      if (!cuttedContent.equals(rawDocument.getContentAsString())) {
        isContentCutted = true;
      }
    }

    // Using HTMLParser to extract the content
    String cleanedContent = null;
    Page htmlPage = new Page(cuttedContent, "UTF-8");
    Parser parser = new Parser(new Lexer(htmlPage));
    StringBean stringBean = new StringBean();

    // replace multiple whitespace with one whitespace
    stringBean.setCollapse(true);
    // Do not extract URLs
    stringBean.setLinks(false);
    // replace &nbsp; with whitespace
    stringBean.setReplaceNonBreakingSpaces(true);

    try {
      // Parse the content
      parser.visitAllNodesWith(stringBean);
      cleanedContent = stringBean.getStrings();

    } catch (ParserException ex) {
      throw new RegainException("Error while parsing content: ", ex);
    }

    // The result of parsing the html-content
    setCleanedContent(cleanedContent);

    // Extract links
    LinkVisitor linkVisitor = new LinkVisitor();
    if (isContentCutted) {
      // This means a new parser run which is expensive but neccessary
      htmlPage = new Page(rawDocument.getContentAsString(), "UTF-8");
      parser = new Parser(new Lexer(htmlPage));
    } else {
      parser.reset();
    }

    try {
      // Parse the content
      parser.visitAllNodesWith(linkVisitor);
      ArrayList<Tag> links = linkVisitor.getLinks();
      htmlPage.setBaseUrl(rawDocument.getUrl());

      // Iterate over all links found
      Iterator linksIter = links.iterator();
      while (linksIter.hasNext()) {
        LinkTag currTag = ((LinkTag) linksIter.next());
        String link = CrawlerToolkit.removeAnchor(currTag.extractLink());

        // find urls which do not end with an '/' but are a directory
        link = CrawlerToolkit.completeDirectory(link);

        //link = CrawlerToolkit.toAbsoluteUrl(link, rawDocument.getUrl());
        String linkText = (currTag.getLinkText() == null) ? "" : currTag.getLinkText();

        // store all http(s)-links the link
        if (currTag.isHTTPLikeLink()) {
          rawDocument.addLink(link, linkText);
        }
      }

    } catch (ParserException ex) {
      throw new RegainException("Error while extracting links: ", ex);
    }

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
    while ((pos = content.indexOf('<', pos + 1)) != -1) {
      // A tag starts here -> Check whether this is the title tag
      if (isIndexOf(content, TITLE_START_TAG, pos)) {
        // The title starts here -> Remember the start pos
        startPos = pos + TITLE_START_TAG.length();
        break;
      } else if (isIndexOf(content, "<body", pos)) {
        // The body starts here -> Give up
        break;
      }
    }

    // Scan until the end of the title
    if (startPos != -1) {
      pos = startPos - 1;
      while ((pos = content.indexOf('<', pos + 1)) != -1) {
        if (isIndexOf(content, "</title>", pos)) {
          // We found the title's end tag -> extract the title
          return content.substring(startPos, pos);
        } else if (pos > startPos + 1000) {
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
