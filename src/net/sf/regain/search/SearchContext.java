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
 *  $RCSfile: SearchContext.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/SearchContext.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search;

import java.io.IOException;

import net.sf.regain.RegainException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Contains the search context for one query. Using this context you have access of
 * all hits the search had.
 *
 * @see SearchToolkit#getSearchContextFromPageContext(javax.servlet.jsp.PageContext)
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class SearchContext {

  /** The Query text. */
  private String mQueryText;

  /** The hits of this search. */
  private Hits mHits;

  /** The time the search took. */
  private int mSearchTime;
  
  /**
   * Der Reguläre Ausdruck, zu dem eine URL passen muss, damit sie in einem
   * neuen Fenster geöffnet wird.
   */
  private RE mOpenInNewWindowRegex;



  /**
   * Creates a new instance of SearchContext.
   *
   * @param indexDir The directory where the lucene search index is located.
   * @param openInNewWindowRegex Der Reguläre Ausdruck, zu dem eine URL passen
   *        muss, damit sie in einem neuen Fenster geöffnet wird.
   * @param searchFieldArr The names of the fields that should be searched by
   *        default.
   * @param queryText The query text to search for.
   *
   * @throws RegainException If searching failed.
   */
  public SearchContext(String indexDir, String openInNewWindowRegex,
    String[] searchFieldArr, String queryText)
    throws RegainException
  {
    long startTime = System.currentTimeMillis();

    // AND als Default-Operation setzen
    // ChangeableQueryParser.setDefaultOperator(ChangeableQueryParser.AND_OPERATOR);
    
    mQueryText = queryText;
    
    if (queryText != null) {
      IndexSearcherManager manager = IndexSearcherManager.getInstance(indexDir);

      // Get the Analyzer
      Analyzer analyzer = manager.getAnalyzer();

      BooleanQuery query;
      try {
        query = new BooleanQuery();
        
        for (int i = 0; i < searchFieldArr.length; i++) {
          QueryParser parser = new QueryParser(searchFieldArr[i], analyzer);
          parser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
          Query fieldQuery = parser.parse(queryText);
          
          query.add(fieldQuery, false, false);
          
          /*
          if (i == 0) {
            System.out.println("Query: '" + queryText + "' -> '"
              + fieldQuery.toString(searchFieldArr[i]) + "'");
          }
          */
        }
      } catch (ParseException exc) {
        throw new RegainException("Error while parsing search pattern '"
          + queryText + "': " + exc.getMessage(), exc);
      }

      try {
        mHits = manager.search(query);
      } catch (RegainException exc) {
        throw new RegainException("Error while searching pattern: " + queryText, exc);
      }
    }

    mSearchTime = (int)(System.currentTimeMillis() - startTime);

    if (openInNewWindowRegex != null) {
      try {
        mOpenInNewWindowRegex = new RE(openInNewWindowRegex);
      }
      catch (RESyntaxException exc) {
        throw new RegainException("Syntax error in openInNewWindowRegex: '"
          + openInNewWindowRegex + "'", exc);
      }
    }
  }


  /**
   * Gets the query text of the search.
   * 
   * @return The query text.
   */
  public String getQueryText() {
    return mQueryText;
  }


  /**
   * Gets the number of hits the search had.
   *
   * @return the number of hits the search had.
   */
  public int getHitCount() {
    if (mHits == null) {
      return 0;
    }
    
    return mHits.length();
  }



  /**
   * Gets the document of one hit.
   *
   * @param index The index of the hit.
   * @return the document of one hit.
   *
   * @throws RegainException If getting the document failed.
   * @see Document
   */
  public Document getHitDocument(int index) throws RegainException {
    try {
      return mHits.doc(index);
    } catch (IOException exc) {
      throw new RegainException("Error while getting document of search hit #" + index, exc);
    }
  }



  /**
   * Gets the score of one hit.
   *
   * @param index The index of the hit.
   * @return the score of one hit.
   *
   * @throws RegainException If getting the score failed.
   * @see Hits#score(int)
   */
  public float getHitScore(int index) throws RegainException {
    try {
      return mHits.score(index);
    } catch (IOException exc) {
      throw new RegainException("Error while getting score of search hit #" + index, exc);
    }
  }



  /**
   * Gets the time the search took in milliseconds.
   *
   * @return The search time.
   */
  public int getSearchTime() {
    return mSearchTime;
  }


  /**
   * Gibt zurück, ob die URL in einem neuen Fenster geöffnet werden soll.
   * 
   * @param url Die zu prüfende URL
   * @return Ob die URL in einem neuen Fenster geöffnet werden soll.
   */
  public synchronized boolean getOpenUrlInNewWindow(String url) {
    if (mOpenInNewWindowRegex == null) {
      return false;
    } else {
      if (mOpenInNewWindowRegex.match(url)) {
        return true;
      } else {
        return false;
      }
    }
  }

}
