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
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.DocumentFactory;
import net.sf.regain.crawler.document.PathElement;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.html.HtmlContentExtractor;
import net.sf.regain.crawler.preparator.html.HtmlEntities;
import net.sf.regain.crawler.preparator.html.HtmlPathExtractor;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Präpariert ein HTML-Dokument für die Indizierung.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class HtmlPreparator extends AbstractPreparator {

  /** Die Kategorie, die zum Loggen genutzt werden soll. */  
  private static Category mCat = Category.getInstance(HtmlPreparator.class);

  /** Die DocumentFactory. Wird genutzt, um Analyse-Dateien zu schreiben. */  
  private DocumentFactory mDocumentFactory;

  /** Ein regulärer Ausdruck, der den Titel eines HTMl-Dokuments findet. */  
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
   * Erzeugt eine neue HtmlPreparator-Instanz.
   *
   * @param documentFactory Die DocumentFactory. Wird genutzt, um
   *        Analyse-Dateien zu schreiben.
   * @param contentExtractorArr Die HtmlContentExtractor, die den jeweiligen zu
   *        indizierenden Inhalt aus den HTML-Dokumenten schneiden.
   * @param pathExtractorArr Die HtmlPathExtractor, die den jeweiligen Pfad aus
   *        den HTML-Dokumenten extrahieren.
   * @throws RegainException Wenn der reguläre Ausdruck, der den Titel
   *         extrahiert. Da dieser hardcodiert ist, wird der Fehler nicht
   *         auftreten.
   */
  public HtmlPreparator(DocumentFactory documentFactory,
    HtmlContentExtractor[] contentExtractorArr, HtmlPathExtractor[] pathExtractorArr)
    throws RegainException
  {
    mDocumentFactory = documentFactory;
    mContentExtractorArr = contentExtractorArr;
    mPathExtractorArr = pathExtractorArr;

    try {
      mExtractHtmlTitleRE = new RE("<title>(.*)</title>", RE.MATCH_CASEINDEPENDENT);
    }
    catch (RESyntaxException exc) {
      // Since the regular expression is hard coded this will never happen
      throw new RegainException("Syntax error in regular expression", exc);
    }
  }



  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */  
  public void prepare(RawDocument rawDocument) throws RegainException {
    String url = rawDocument.getUrl();
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
      mCat.warn("No HTML content extractor is responsible for " + rawDocument.getUrl());
      
      cuttedContent = rawDocument.getContentAsString();
      headlines = null;
    } else {
      cuttedContent = contentExtractor.extractContent(rawDocument);

      headlines = contentExtractor.extractHeadlines(cuttedContent);
    }

    // Write the cuttet content to an analysis file
    mDocumentFactory.writeAnalysisFile(url, "cutted", cuttedContent);

    // Clean the content from tags
    String cleanedContent = cleanFromTags(cuttedContent);
    setCleanedContent(cleanedContent);

    if (headlines != null) {
      // Replace HTML Entities
      headlines = replaceHtmlEntities(headlines);

      // Write the headlines to an analysis file
      mDocumentFactory.writeAnalysisFile(url, "headlines", headlines);

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
   * Säubert HTML-Text von seinen Tags und wandelt alle HTML-Entitäten in ihre
   * Ensprechungen.
   *
   * @param text Der zu säubernde HTML-Text.
   *
   * @return Der von Tags gesäberte Text
   */  
  public static String cleanFromTags(String text) {
    StringBuffer clean = new StringBuffer();

    int offset = 0;
    int tagStart;
    while ((tagStart = text.indexOf('<', offset)) != -1) {
      // Extract the good part since the last tag
      String goodPart = text.substring(offset, tagStart);

      // Check whether the good part is wasted by cascaded tags
      // Example: In the text "<!-- <br> --> Hello" "<!-- <br>" will be
      //          detected as tag and "--> Hello" as good part.
      //          We now have to scan the good part for a tag rest.
      //          (In this example: "-->")
      int tagRestEnd = goodPart.indexOf('>');
      if (tagRestEnd != -1) {
        goodPart = goodPart.substring(tagRestEnd + 1);
      }

      // Trim the good part
      goodPart = goodPart.trim();

      if (goodPart.length() > 0) {
        // Replace all entities in the text and append the result
        goodPart = replaceHtmlEntities(goodPart);
        clean.append(goodPart);

        // Append a space
        clean.append(" ");
      }

      // Find the end of the tag
      int tagEnd = text.indexOf('>', tagStart);
      if (tagEnd == -1) {
        // Syntax error: The tag doesn't end -> Forget that dirty end
        offset = text.length();
        break;
      }

      // Calculate the next offset
      offset = tagEnd + 1;
    }

    // Extract the good part since the last tag, replace all entities and append
    // the result
    if (offset < text.length()) {
      String goodPart = text.substring(offset, text.length()).trim();
      goodPart = replaceHtmlEntities(goodPart);
      clean.append(goodPart);
    }

    return clean.toString();
  }



  /**
   * Wandelt alle HTML-Entitäten in ihre Ensprechungen.
   *
   * @param text Den Text, dessen HTML-Entitäten gewandelt werden sollen.
   *
   * @return Der gewandelte Text.
   */  
  public static String replaceHtmlEntities(String text) {
    StringBuffer clean = new StringBuffer();

    int offset = 0;
    int entityStart;
    while ((entityStart = text.indexOf('&', offset)) != -1) {
      // Append the part since the last entity
      String textPart = text.substring(offset, entityStart);
      clean.append(textPart);

      // Find the end of the entity
      int entityEnd = text.indexOf(';', entityStart);
      if (entityEnd == -1) {
        // Syntax error: The entity doesn't end -> Forget that dirty end
        offset = text.length();
        break;
      }

      // Extract, decode and append the entity
      String entity = text.substring(entityStart, entityEnd + 1);
      String decoded = HtmlEntities.decode(entity);
      clean.append(decoded);

      // Get the next offset
      offset = entityEnd + 1;
    }

    // Append the part since the last entity
    if (offset < text.length()) {
      clean.append(text.substring(offset, text.length()));
    }

    return clean.toString();
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
