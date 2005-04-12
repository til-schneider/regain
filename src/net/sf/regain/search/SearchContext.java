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
 *     $Date: 2005/04/08 10:22:15 $
 *   $Author: til132 $
 * $Revision: 1.7 $
 */
package net.sf.regain.search;

import java.io.IOException;

import net.sf.regain.RegainException;
import net.sf.regain.search.config.IndexConfig;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Contains the search context for one query. Using this context you have access of
 * all hits the search had.
 *
 * @see SearchToolkit#getSearchContext(net.sf.regain.util.sharedtag.PageRequest)
 * @author Til Schneider, www.murfman.de
 */
public class SearchContext {

  /** The configuration for the index. */
  private IndexConfig mIndexConfig;
  
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
   * @param indexConfig The configuration for the index.
   * @param queryText The query text to search for.
   * @param groupArr The groups the searching user has reading rights for.
   *        See {@link net.sf.regain.search.access.SearchAccessController}.
   *        Is <code>null</code>, if no access control should be used.
   *
   * @throws RegainException If searching failed.
   */
  public SearchContext(IndexConfig indexConfig, String queryText, String[] groupArr)
    throws RegainException
  {
    long startTime = System.currentTimeMillis();

    mIndexConfig = indexConfig;
    mQueryText = queryText;

    if (queryText != null) {
      IndexSearcherManager manager = IndexSearcherManager.getInstance(indexConfig.getDirectory());

      // Get the Analyzer
      Analyzer analyzer = manager.getAnalyzer();

      BooleanQuery query;
      try {
        query = new BooleanQuery();

        String[] searchFieldArr = indexConfig.getSearchFieldList();
        for (int i = 0; i < searchFieldArr.length; i++) {
          QueryParser parser = new QueryParser(searchFieldArr[i], analyzer);
          parser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
          Query fieldQuery = parser.parse(queryText);

          // Add as OR
          query.add(fieldQuery, false, false);
        }
      } catch (ParseException exc) {
        throw new RegainException("Error while parsing search pattern '"
          + queryText + "': " + exc.getMessage(), exc);
      }
      
      // Check whether access control is used
      if (groupArr != null) {
        // Create a query that matches any group
        BooleanQuery groupQuery = new BooleanQuery();
        for (int i = 0; i < groupArr.length; i++) {
          // Add as OR
          groupQuery.add(new TermQuery(new Term("groups", groupArr[i].trim())), false, false);
        }
        
        // Create a main query that contains the group query and the search query
        // combined with AND
        BooleanQuery mainQuery = new BooleanQuery();
        mainQuery.add(query, true, false);
        mainQuery.add(groupQuery, true, false);
        
        // Set the main query as query to use
        query = mainQuery;
      }

      // System.out.println("Query: '" + queryText + "' -> '" + query.toString() + "'");
      
      try {
        mHits = manager.search(query);
      } catch (RegainException exc) {
        throw new RegainException("Error while searching pattern: " + queryText, exc);
      }
    }

    mSearchTime = (int)(System.currentTimeMillis() - startTime);

    String openInNewWindowRegex = indexConfig.getOpenInNewWindowRegex();
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
   * Gets the name of the index.
   * 
   * @return The name of the index.
   */
  public String getIndexName() {
    return mIndexConfig.getName();
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
  
  
  /**
   * Rewrites the given URL according to the rewrite rules specified in the
   * index config.
   * 
   * @param url The URL to rewrite (comes from the index).
   * @return The rewritten URL (shown to the user).
   */
  public String rewriteUrl(String url) {
    if (url == null) {
      return null;
    }
    
    // Get the rules
    String[][] rewriteRules = mIndexConfig.getRewriteRules();
    if (rewriteRules != null) {
      for (int i = 0; i < rewriteRules.length; i++) {
        String[] rule = rewriteRules[i];
        String prefix = rule[0];
        if (url.startsWith(prefix)) {
          String replacement = rule[1];
          return replacement + url.substring(prefix.length());
        }
      }
    }
    
    // The URL does not match any rewrite rule -> Don't change it
    return url;
  }

}
