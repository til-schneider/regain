/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2010 Thomas Tesche, Til Schneider
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
 * Contact: Thomas Tesche: www.thtesche.com, Til Schneider: info@murfman.de
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-08-06 16:04:27 +0200 (Wed, 06 Aug 2008) $
 *   $Author: thtesche $
 * $Revision: 325 $
 */
package net.sf.regain.search.results;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.IndexSearcherManager;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.access.SearchAccessController;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.util.sharedtag.PageRequest;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.ListUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Holds the results of a search on a single or multiple indexes. The class
 * uses the new Lucene API (2.9 and later).
 *
 * @author Thomas Tesche: www.thtesche.com
 */
public class SearchResultsImpl implements SearchResults {

  /** The searcher (single or multi). */
  private Searcher mIndexSearcher;
  /** The Query text. */
  private String mQueryText;
  /** The time the search took. */
  private int mSearchTime;
  /** The index name. In case of an single index it's exactly this name and 'multiindex' in other case. */
  private String mIndexName;
  /** The hits of this search. */
  private ScoreDoc[] hitScoreDocs;
  /** The DocCollector. */
  TopDocsCollector topDocsCollector;
  private static Pattern mimetypeFieldPattern = Pattern.compile("(mimetype:\".*\")");
  /**
   * Der Reguläre Ausdruck, zu dem eine URL passen muss, damit sie in einem
   * neuen Fenster geöffnet wird.
   */
  private RE mOpenInNewWindowRegex;
  /** The boolean query used while searching and highlighting */
  private BooleanQuery mQuery;
  /** The current analyzer */
  private Analyzer mAnalyzer;
  /** The current config. */
  private IndexConfig mIndexConfig;
  /** Factory for create a new LazyList-entry. */
  Factory factory = new Factory() {

    public Object create() {
      return new Document();
    }
  };
  /** held the transformed hits. */
  private List lazyHitList = ListUtils.lazyList(new ArrayList(), factory);

  /**
   * Creates an instanz of SearchResults. This class can search over a single
   * or multiple indexes.
   *
   * @param indexConfig the array of index configs
   * @param queryText The query text to search for.
   * @param groupArr The groups the searching user has reading rights for.
   *        See {@link net.sf.regain.search.access.SearchAccessController}.
   *        Is <code>null</code>, if no access control should be used.
   *
   * @throws RegainException If searching failed.
   */
  public SearchResultsImpl(IndexConfig[] indexConfigs, PageRequest request)
          throws RegainException {

    long startTime = System.currentTimeMillis();
    ArrayList<String> groupsArr = new ArrayList<String>();
    IndexSearcherManager[] indexSearcherManagers = new IndexSearcherManager[indexConfigs.length];

    mQueryText = SearchToolkit.getSearchQuery(request);
    //System.out.println("Initial query: " + mQueryText);

    String mimeTypeFieldText = null;
    String queryText = null;

    if (mQueryText != null) {
      // Remove the mimetype field if the query contains it

      Matcher matcher = mimetypeFieldPattern.matcher(mQueryText);
      boolean found = matcher.find();
      if (found && matcher.groupCount() > 0) {
        // the first group is the mimetype field identifier
        mimeTypeFieldText = matcher.group(1);
        queryText = mQueryText.replace(mimeTypeFieldText, "");
        //System.out.println("Query after mimetype removing: " + queryText);

      } else {
        queryText = mQueryText;
      }
    }

    // If there is at least on index
    if (indexConfigs.length >= 1) {

      for (int i = 0; i < indexConfigs.length; i++) {
        // Get the groups the current user has reading rights for
        String[] groupArr = null;
        SearchAccessController accessController = indexConfigs[i].getSearchAccessController();
        if (accessController != null) {
          groupArr = accessController.getUserGroups(request);
          // Check the Group array
          RegainToolkit.checkGroupArray(accessController, groupArr);
          groupsArr.addAll(Arrays.asList(groupArr));
        }
        // build composed result: all groups over all indeces the user has the rights.
        // find the IndexSearcherManager for every index
        indexSearcherManagers[i] = IndexSearcherManager.getInstance(indexConfigs[i].getDirectory());

      }

      String[] allGroups = groupsArr.toArray(new String[0]);

      // Decide whether to use IndexSearcher (only one index) or MultiSearcher
      if (indexSearcherManagers.length == 1) {
        //System.out.println("SingleSearcher");
        mIndexSearcher = indexSearcherManagers[0].getIndexSearcher();
        mAnalyzer = indexSearcherManagers[0].getAnalyzer();
        mIndexName = indexConfigs[0].getName();

      } else {
        // Collect all IndexSearchers and instantiate a MultiSearcher
        //System.out.println("MultiSearcher");

        Searcher[] searchers = new Searcher[indexConfigs.length];
        for (int j = 0; j < indexSearcherManagers.length; j++) {
          searchers[j] = indexSearcherManagers[j].getIndexSearcher();
        }
        try {
          mIndexSearcher = new MultiSearcher(searchers);
          // get the 'first' analyzer (in fact it is a random choice)
          // All indexes has to be build with the same analyzer
          mAnalyzer = indexSearcherManagers[0].getAnalyzer();
          mIndexName = "multiindex";

        } catch (IOException ex) {
          throw new RegainException("Couldn't instantiate MultiSearcher.", ex);
        }

      }

      mIndexConfig = indexConfigs[0];
      mQuery = null;
      if (queryText != null && queryText.length() > 0) {
        // start the creation of the lucene query object

        try {
          mQuery = new BooleanQuery();

          for (int k = 0; k < indexConfigs.length; k++) {

            String[] searchFieldArr = indexConfigs[k].getSearchFieldList();
            for (int i = 0; i < searchFieldArr.length; i++) {

              QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, searchFieldArr[i], mAnalyzer);
              parser.setDefaultOperator(QueryParser.AND_OPERATOR);
              parser.setAllowLeadingWildcard(true);
              Query fieldQuery = parser.parse(queryText);

              // Add as OR
              mQuery.add(fieldQuery, Occur.SHOULD);
            }
            //System.out.println("Query: '" + queryText + "' -> '" + mQuery.toString() + "'");

          }
        } catch (ParseException exc) {
          throw new RegainException("Error while parsing search pattern '" + mQueryText +
                  "': " + exc.getMessage(), exc);
        }

        // Check whether access control is used
        if (allGroups != null && allGroups.length > 0) {
          // Create a query that matches any group
          BooleanQuery groupQuery = new BooleanQuery();
          for (int i = 0; i < allGroups.length; i++) {
            // Add as OR
            groupQuery.add(new TermQuery(new Term("groups", allGroups[i])), Occur.SHOULD);
          }

          // Create a main query that contains the group query and the search query
          // combined with AND
          BooleanQuery mainQuery = new BooleanQuery();
          mainQuery.add(mQuery, Occur.MUST);
          mainQuery.add(groupQuery, Occur.MUST);

          // Set the main query as query to use
          mQuery = mainQuery;
        }
      }

      // Add the mimetype field search
      if (mimeTypeFieldText != null) {
        BooleanQuery mimetypeFieldQuery = new BooleanQuery();
        mimetypeFieldQuery.add(new TermQuery(new Term("mimetype",
                mimeTypeFieldText.substring(9).replace("\"", ""))),
                Occur.SHOULD);
        BooleanQuery mainQuery = new BooleanQuery();
        if (mQuery != null) {
          mainQuery.add(mQuery, Occur.MUST);
        }
        mainQuery.add(mimetypeFieldQuery, Occur.MUST);

        // Set the main query as query to use
        mQuery = mainQuery;
      }

      if (mQuery != null) {
        //System.out.println("Query: '" + mQueryText + "' -> '" + mQuery.toString() + "'");

        try {
          SortingOption sortingOption = new SortingOption(request.getParameter("order"));
          Sort sort = new Sort(sortingOption.getSortField());
          //System.out.println(sortingOption.toString());

          topDocsCollector = TopFieldCollector.create(sort, 10000, true, true, true, false);

          mIndexSearcher.search(mQuery, topDocsCollector);
          hitScoreDocs = topDocsCollector.topDocs().scoreDocs;

        } catch (IOException exc) {
          throw new RegainException("Searching query failed", exc);
        }

      }

      String openInNewWindowRegex = indexConfigs[0].getOpenInNewWindowRegex();
      if (openInNewWindowRegex != null) {
        try {
          mOpenInNewWindowRegex = new RE(openInNewWindowRegex);
        } catch (RESyntaxException exc) {
          throw new RegainException("Syntax error in openInNewWindowRegex: '" + openInNewWindowRegex + "'", exc);
        }
      }

    } else {
      // no index given
    }

    mSearchTime = (int) (System.currentTimeMillis() - startTime);

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
    if (hitScoreDocs == null) {
      return 0;
    }
    return hitScoreDocs.length;
  }

  /**
   * Gets the document of one hit. For holding the transformed documents we use
   * a lazy list.
   *
   * @param index The index of the hit.
   * @return the document of one hit.
   *
   * @throws RegainException If getting the document failed.
   * @see Document
   */
  public Document getHitDocument(int index) throws RegainException {

    try {
      Document currDoc = (Document) lazyHitList.get(index);
      // The document is empty, so it's created by the factory. Replace it with the real one
      // at this position
      if (currDoc.getFields().isEmpty()) {
        lazyHitList.set(index, mIndexSearcher.doc(hitScoreDocs[index].doc));
      }
    } catch (Exception ex) {
      throw new RegainException("Error while accessing index", ex);
    }
    return (Document) lazyHitList.get(index);

  }

  /**
   * Writes a changed document back to the list.
   * 
   * @param index
   * @param document
   * @throws RegainException
   */
  private void setHitDocument(int index, Document document) throws RegainException {
    lazyHitList.set(index, document);
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
      return hitScoreDocs[index].score;
    } catch (Exception exc) {
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
          throws RegainException {
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
    return mIndexName;
  }

  /**
   * Shortens the summary.
   * 
   * @param index The index of the hit.
   * @throws RegainException if shorten fails.
   */
  public void shortenSummary(int index) throws RegainException {

    try {
      Document document = getHitDocument(index);
      byte[] compressedFieldValue = document.getBinaryValue("summary");
      String text = null;
      if (compressedFieldValue != null) {
        text = CompressionTools.decompressString(compressedFieldValue);
      }

      if (text != null) {
        // Overwrite the content with a shortend summary
        String resSummary = RegainToolkit.createSummaryFromContent(text, 200);
        document.removeField("summary");
        if (resSummary != null) {
          document.add(new Field("summary", resSummary, Field.Store.NO, Field.Index.NOT_ANALYZED));
          document.add(new Field("summary", CompressionTools.compressString(resSummary), Field.Store.YES));
          // write back the transformed document
          setHitDocument(index, document);
        }
      }
    } catch (DataFormatException dataFormatException) {
      throw new RegainException("Error while searching pattern: " + mQueryText, dataFormatException);
    }

  }

  /**
   * Highlights fields in the document. Fields for highlighting will be:
   * - summary
   * - title
   *
   * @param index The index of the hit.
   * @throws RegainException If highlighting failed.
   */
  public void highlightHitDocument(int index) throws RegainException {

    try {
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
//      int docId = hitScoreDocs[index].doc;

      Document document = getHitDocument(index);
      byte[] compressedFieldValue = document.getBinaryValue("summary");
      String text = null;
      if (compressedFieldValue != null) {
        text = CompressionTools.decompressString(compressedFieldValue);
      }

      if (text != null) {
        // Overwrite the content with a shortend summary
        String resSummary = RegainToolkit.createSummaryFromContent(text, 200);
        document.removeField("summary");
        if (resSummary != null) {
          //System.out.println("resSummary " + resSummary);
          document.add(new Field("summary", resSummary, Field.Store.NO, Field.Index.NOT_ANALYZED));
          document.add(new Field("summary", CompressionTools.compressString(resSummary), Field.Store.YES));

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
          //System.out.println("Highlighted summary: " + resHighlSummary);
          // write the result back to the document in a new field
          document.add(new Field("highlightedSummary", resHighlSummary, Field.Store.NO, Field.Index.NOT_ANALYZED));
          document.add(new Field("highlightedSummary", CompressionTools.compressString(resHighlSummary), Field.Store.YES));
        }
      }
      // Highlight the title
      text = document.get("title");
      String resHighlTitle = null;
      if (text != null) {
        TokenStream tokenStream = mAnalyzer.tokenStream("content",
                new StringReader(text));
        // Get the best fragment
        resHighlTitle = highlighter.getBestFragment(tokenStream, text);
      }

      if (resHighlTitle != null) {
        // write the result back to the document in a new field
        //System.out.println("Highlighted title: " + resHighlTitle);
        document.add(new Field("highlightedTitle", resHighlTitle,
                Field.Store.YES, Field.Index.NOT_ANALYZED));

      }
      // write back the transformed document
      setHitDocument(index, document);

      //System.out.println("The document: " + hitDocs.get(index).toString());

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
}
