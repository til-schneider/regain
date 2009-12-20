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
 *     $Date: 2009-11-26 18:14:25 +0100 (Do, 26 Nov 2009) $
 *   $Author: thtesche $
 * $Revision: 430 $
 */
package net.sf.regain.search.results;

import java.io.IOException;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.IndexSearcherManager;
import net.sf.regain.search.config.IndexConfig;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.io.StringReader;
import java.util.zip.DataFormatException;
import org.apache.lucene.document.CompressionTools;

/**
 * Holds the results of a search on a single index.
 *
 * @author Til Schneider, www.murfman.de
 * @deprecated Will be removed in release 2.0
 */
public class SingleSearchResults implements SearchResults {

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

  /** The boolean query used while searching and highlighting */
  private BooleanQuery mQuery;
  
  /** The current analyzer */
  private Analyzer mAnalyzer;

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
  public SingleSearchResults(IndexConfig indexConfig, String queryText,
    String[] groupArr)
    throws RegainException
  {
    long startTime = System.currentTimeMillis();

    mIndexConfig = indexConfig;
    mQueryText = queryText;

    if (queryText != null) {
      IndexSearcherManager manager = IndexSearcherManager.getInstance(indexConfig.getDirectory());

      // Get the Analyzer
      mAnalyzer = manager.getAnalyzer();
      
      try {
        mQuery = new BooleanQuery();

        String[] searchFieldArr = indexConfig.getSearchFieldList();
        for (int i = 0; i < searchFieldArr.length; i++) {
          QueryParser parser = new QueryParser(searchFieldArr[i], mAnalyzer);
          parser.setDefaultOperator(QueryParser.AND_OPERATOR);
          parser.setAllowLeadingWildcard(true);
          Query fieldQuery = parser.parse(queryText);

          // Add as OR
          mQuery.add(fieldQuery, Occur.SHOULD);
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
          groupQuery.add(new TermQuery(new Term("groups", groupArr[i])), Occur.SHOULD);
        }

        // Create a main query that contains the group query and the search query
        // combined with AND
        BooleanQuery mainQuery = new BooleanQuery();
        mainQuery.add(mQuery, Occur.MUST);
        mainQuery.add(groupQuery, Occur.MUST);

        // Set the main query as query to use
        mQuery = mainQuery;
      }

      System.out.println("Query: '" + queryText + "' -> '" + mQuery.toString() + "'");
      
      try {
        mHits = manager.search_old(mQuery);
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
   * Gets the query text of the search.
   *
   * @return The query text.
   */
  public String getQueryText() {
    return mQueryText;
  }
  
 
  /**
   * Gets the search hits.
   * 
   * @return The search hits.
   */
  Hits getHits() {
    return mHits;
  }

  /**
   * Gets the number of documents in the in index.
   *
   * @return the number of indexed documents.
   */
  public int getDocumentCount() {
    return 0;
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
   * Gets whether a hit should be opened in a new window.
   *
   * @param index The index of the hit.
   * @return Whether the hit should be opened in a new window.
   * @throws RegainException If getting the URL failed.
   */
  public synchronized boolean getOpenHitInNewWindow(int index)
    throws RegainException
  {
    String url = getHitUrl(index);
    
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
   * Gets whether the file-to-http-bridge should be used for a certain hit.
   * <p>
   * Mozilla browsers have a security mechanism that blocks loading file-URLs
   * from pages loaded via http. To be able to load files from the search
   * results, regain offers the file-to-http-bridge that provides all files that
   * are listed in the index via http.
   *
   * @param index The index of the hit. 
   * @return Whether the file-to-http-bridge should be used.
   */
  public boolean getUseFileToHttpBridgeForHit(int index) {
    return mIndexConfig.getUseFileToHttpBridge();
  }


  /**
   * Gets the url from a hit and rewrites it according to the rewrite rules
   * specified in the index config.
   * 
   * @param index The index of the hit to get the URL for.
   * @return The url of the wanted hit.
   * @throws RegainException If getting the hit document failed.
   */
  public String getHitUrl(int index) throws RegainException {
    String url = getHitDocument(index).get("url");    
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

  /**
   * Gets the name of the index a hit comes from.
   * 
   * @param index The index of the hit to get the index name for.
   * @return The name of the index a hit comes from.
   * @throws RegainException If getting the index name failed.
   */
  public String getHitIndexName(int index) throws RegainException {
    return mIndexConfig.getName();
  }

  /**
   * Highlights fields in the document. Fields for highlighting will be:
   * - content
   * - title
   *
   * @param index The index of the hit.
   *
   * @throws RegainException If highlighting failed.
   */
  public void highlightHitDocument(int index) throws RegainException {

    try {
      // Lines added by Anders to make wildcard and fuzzy queries highlighted
      IndexSearcherManager manager = IndexSearcherManager.getInstance(mIndexConfig.getDirectory());
      // The highlighter needs a rewritten query to work with wildcard and fuzzy queries
      Query rewrittenQuery = manager.rewrite(mQuery);
      QueryScorer queryScorer = new QueryScorer(rewrittenQuery);
      // End added by Anders

      Highlighter highlighter = new Highlighter(
        new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>"), queryScorer);

      // Remark: the summary is at this point not a summary. It contains the 
      // first n characters from the document. n is configurable (default: 250000)
      // We transform this summary into 
      // a) a summary matching the search terms (highlighting)
      // b) and a shortend summary (200 characters)
      String text = CompressionTools.decompressString(mHits.doc(index).getBinaryValue("summary"));

      if( text != null ) {
        // Overwrite the content with a shortend summary
        String resSummary = RegainToolkit.createSummaryFromContent(text, 200);
          mHits.doc(index).removeField("summary");
        if( resSummary != null ) {
          mHits.doc(index).add(new Field("summary", resSummary,
                     Field.Store.YES, Field.Index.NOT_ANALYZED));
        } else {
          // write back uncompressed summary
          mHits.doc(index).add(new Field("summary", text,
                     Field.Store.YES, Field.Index.NOT_ANALYZED));
        }

        String resHighlSummary = null;
        // Remove 'html', this works the same way as PageResponse.printNoHTML()
        text = RegainToolkit.replace(text, "<", "&lt;");
        text = RegainToolkit.replace(text, ">", "&gt;");

        TokenStream tokenStream = mAnalyzer.tokenStream("content", 
                new StringReader(text));
        // Get 3 best fragments and seperate with a " ... "
        resHighlSummary = highlighter.getBestFragments(tokenStream, text, 3, " ... ");

        if (resHighlSummary != null) {
          // write the result back to the document in a new field 
          mHits.doc(index).add(new Field("highlightedSummary", resHighlSummary,
                   Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
      }
      // Highlight the title
      text = mHits.doc(index).get("title");
      String resHighlTitle = null;
      if (text != null) {
        TokenStream tokenStream = mAnalyzer.tokenStream("content", 
                new StringReader(text));
        // Get the best fragment 
        resHighlTitle = highlighter.getBestFragment(tokenStream, text);
      }

      if (resHighlTitle != null) {
        // write the result back to the document in a new field 
        mHits.doc(index).add(new Field("highlightedTitle", resHighlTitle,
                 Field.Store.YES, Field.Index.NOT_ANALYZED));
      }
      
      
    } catch (org.apache.lucene.index.CorruptIndexException exCorr) {
      throw new RegainException("Error while searching pattern: " + mQueryText, exCorr);

     } catch (org.apache.lucene.search.highlight.InvalidTokenOffsetsException exToken) {
      throw new RegainException("Error while searching pattern: " + mQueryText, exToken);

    } catch (IOException exIO) {
      throw new RegainException("Error while searching pattern: " + mQueryText, exIO);

    } catch (DataFormatException ex) {
      throw new RegainException("Error while searching pattern: " + mQueryText, ex);
    }

  }

  /**
   * Gets whether the search terms should be highlighted
   *
   * @return whether to highlight
   * @throws RegainException If the value could not read from config
   */
  public boolean getShouldHighlight(int index) throws RegainException {
    return mIndexConfig.getShouldHighlight();
  }

  /**
   * Shortens the summary.
   *
   * @param index The index of the hit.
   * @throws RegainException if shorten fails.
   */
  public void shortenSummary(int index) throws RegainException {

    try {
      byte[] compressedFieldValue = mHits.doc(index).getBinaryValue("summary");
      String text = null;
      if (compressedFieldValue != null) {
        text = CompressionTools.decompressString(compressedFieldValue);
      }

      if (text != null) {
        // Overwrite the content with a shortend summary
        String resSummary = RegainToolkit.createSummaryFromContent(text, 200);
        mHits.doc(index).removeField("summary");
        if (resSummary != null) {
          mHits.doc(index).add(new Field("summary", resSummary, Field.Store.NO, Field.Index.NOT_ANALYZED));
          mHits.doc(index).add(new Field("summary", CompressionTools.compressString(resSummary), Field.Store.YES));

        }
      }
    } catch (org.apache.lucene.index.CorruptIndexException exCorr) {
      throw new RegainException("Error while searching pattern: " + mQueryText, exCorr);

    } catch (IOException exIO) {
      throw new RegainException("Error while searching pattern: " + mQueryText, exIO);

    } catch (DataFormatException dataFormatException) {
      throw new RegainException("Error while searching pattern: " + mQueryText, dataFormatException);
    }

  }
}
