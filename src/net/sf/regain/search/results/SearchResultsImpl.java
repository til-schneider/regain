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
 */
package net.sf.regain.search.results;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.IndexSearcherManager;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.access.SearchAccessController;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.util.sharedtag.PageRequest;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldValueHitQueue;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
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
  private IndexSearcher mIndexSearcher;
  /** The reader (multi). */
  private MultiReader mMultiReader;
  /** The Query text. */
  private String mQueryText;
  /** The time the search took. */
  private int mSearchTime;
  /** The index name. In case of an single index it's exactly this name and 'multiindex' in other case. */
  private String mIndexName;
  /** The hits of this search. */
  private ScoreDoc[] hitScoreDocs;
  /** The DocCollector. */
  private TopDocsCollector<FieldValueHitQueue.Entry> topDocsCollector;

  private static Pattern mimetypeFieldPattern = Pattern.compile("(mimetype:\"([^:]*)\")");
  private static Pattern negativeMimetypeFieldPattern = Pattern.compile("((-|!|NOT )mimetype:\"([^:]*)\")");
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

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(SearchResultsImpl.class);

  /** held the transformed hits. */
  private List lazyHitList = ListUtils.lazyList(new ArrayList(), new Factory() {
    /** Factory for create a new LazyList-entry. */
    @Override
    public Object create() {
      return new Document();
    }
  });

  /**
   * Creates an instanz of SearchResults. This class can search over a single
   * or multiple indexes.
   *
   * @param indexConfigs The array of index configs
   * @param request The request parameters
   *
   * @throws RegainException If searching failed.
   */
  public SearchResultsImpl(IndexConfig[] indexConfigs, PageRequest request)
          throws RegainException {

    long startTime = System.currentTimeMillis();
    ArrayList<String> groupsArr = new ArrayList<String>();
    IndexSearcherManager[] indexSearcherManagers = new IndexSearcherManager[indexConfigs.length];
    IndexSearcher[] searchers = new IndexSearcher[indexConfigs.length];
    IndexReader[] readerArray = new IndexReader[indexConfigs.length];

    mQueryText = SearchToolkit.getSearchQuery(request);
    mLog.debug("Initial Query: " + mQueryText);

    String queryText = null;

    BooleanQuery mimeQuery = new BooleanQuery();
    queryText = removeMimetypeQuery(mQueryText, mimeQuery);

    try {
    // If there is at least on index
    if (indexConfigs.length >= 1) {

      boolean useAccessController = false;
      for (int i = 0; i < indexConfigs.length; i++) {
        // Get the groups the current user has reading rights for
        String[] groupArr = null;
        SearchAccessController accessController = indexConfigs[i].getSearchAccessController();
        if (accessController != null) {
          useAccessController = true;
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
        readerArray[0] = mIndexSearcher.getIndexReader();
        mMultiReader = new MultiReader(readerArray, false);

      } else {
        // Collect all IndexSearchers and instantiate a MultiSearcher
        for (int j = 0; j < indexSearcherManagers.length; j++) {
          searchers[j] = indexSearcherManagers[j].getIndexSearcher();
          readerArray[j] = searchers[j].getIndexReader();
        }
        mMultiReader = new MultiReader(readerArray, false);

        if (indexSearcherManagers[0] != null) {
          indexSearcherManagers[0].releaseIndexSearcher(mIndexSearcher);
        }
        mIndexSearcher = new IndexSearcher(mMultiReader);
        // get the 'first' analyzer (in fact it is a random choice)
        // All indexes has to be build with the same analyzer
        mAnalyzer = indexSearcherManagers[0].getAnalyzer();
        mIndexName = "multiindex";
      }

      mIndexConfig = indexConfigs[0];
      mQuery = null;
      if (queryText != null && queryText.trim().length() > 0) {
        // start the creation of the lucene query object

        try {
          mQuery = new BooleanQuery();

          for (int k = 0; k < indexConfigs.length; k++) {

            String[] searchFieldArr = indexConfigs[k].getSearchFieldList();
            for (int i = 0; i < searchFieldArr.length; i++) {

              QueryParser parser = new QueryParser(RegainToolkit.getLuceneVersion(), searchFieldArr[i], mAnalyzer);
              parser.setDefaultOperator(QueryParser.AND_OPERATOR);
              parser.setAllowLeadingWildcard(true);

//              if (!searchFieldArr[i].equals("filename")) {
                Query fieldQuery = parser.parse(queryText);
                // Add as OR
                mQuery.add(fieldQuery, Occur.SHOULD);
//              } else {
//                // The field filename is not stemmed
//                mQuery.add(new TermQuery(new Term("filename", queryText)), Occur.SHOULD);
//              }
            }
            if (mLog.isDebugEnabled()) {
              mLog.debug("Query: '" + queryText + "' -> '" + mQuery.toString() + "'");
            }

          }
        } catch (ParseException exc) {
          throw new RegainException("Error while parsing search pattern '" + mQueryText
                  + "': " + exc.getMessage(), exc);
        }

        // Check whether access control is used
        if (useAccessController) {
          mQuery = SearchToolkit.addAccessControlToQuery(mQuery, allGroups);
        }
      }

      // Add the mimetype field search
      if (mimeQuery.getClauses().length > 0) {

        if (mQuery != null) {
          mimeQuery.add(mQuery, Occur.MUST);
        }

        // Set the main query as query to use
        mQuery = mimeQuery;
      }


      if (mQuery != null) {
        mLog.debug("Lucene Query: " + mQuery.toString());

        try {
          SortingOption sortingOption = new SortingOption(request.getParameter("order"));
          Sort sort = new Sort(sortingOption.getSortField());
          mLog.debug("Sort by:" + sortingOption.toString());

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
    } finally {
      for (int i = 0; i < indexSearcherManagers.length; i++)
      {
        if (indexSearcherManagers[i] != null) {
          indexSearcherManagers[i].releaseIndexSearcher(searchers[i]);
        }
      }
    }

    mSearchTime = (int) (System.currentTimeMillis() - startTime);

  }

  /**
   * Create a Query from mime type terms and remove them from the query text
   * @param queryText   Original query text
   * @param mainQuery   MIME clauses that were extracted from the query
   * @return  New query text (old query without the mime clauses)
   */
  private String removeMimetypeQuery(String queryText, BooleanQuery mainQuery)
  {
    if (queryText == null) {
      return null;
    }

    // Remove the mimetype field if the query contains it
    String mimeTypeFieldText = null;
    Matcher matcher = null;
    boolean found;

    // First, negative mime Types
    do
    {
      matcher = negativeMimetypeFieldPattern.matcher(queryText);
      found = matcher.find();

      if (found && matcher.groupCount() > 0) {
        // the first group is the mimetype field identifier
        mimeTypeFieldText = matcher.group(3);
        queryText = queryText.replace(matcher.group(1), "");
        //System.out.println("Query after mimetype removing: " + queryText);

        mainQuery.add(getAtomicMimeTypeQuery(mimeTypeFieldText), Occur.MUST_NOT);
      }
    } while (found);

    // Now positive mimes

    BooleanQuery positiveMimes = new BooleanQuery();
    do
    {
      matcher = mimetypeFieldPattern.matcher(queryText);
      found = matcher.find();

      if (found && matcher.groupCount() > 0) {
        // the first group is the mimetype field identifier
        mimeTypeFieldText = matcher.group(2);
        queryText = queryText.replace(matcher.group(1), "");
        //System.out.println("Query after mimetype removing: " + queryText);

        positiveMimes.add(getAtomicMimeTypeQuery(mimeTypeFieldText), Occur.SHOULD);
      }
    } while (found);
    if (positiveMimes.getClauses().length > 0) {
      mainQuery.add(positiveMimes, Occur.MUST);
    }

    // Remove empty clauses that remained
    Pattern emptyMatcherPattern = Pattern.compile("(\\(\\s*\\))");
    do
    {
      matcher = emptyMatcherPattern.matcher(queryText);
      found = matcher.find();

      if (found && matcher.groupCount() > 0) {
        queryText = queryText.replace(matcher.group(1), "");
      }
    } while (found);

    return queryText;
  }

  /**
   * Helper method for removeMimetypeQuery
   * @param mimeTypeFieldText   The value of a mime Type
   * @return  The corresponding query
   */
  private BooleanQuery getAtomicMimeTypeQuery(String mimeTypeFieldText)
  {
    BooleanQuery mimetypeFieldQuery = new BooleanQuery();
    Term term = new Term("mimetype", mimeTypeFieldText);

    Query query;
    if (mimeTypeFieldText.contains("*")) {
      query = new WildcardQuery(term);
    }
    else {
      query = new TermQuery(term);
    }

    mimetypeFieldQuery.add(query, Occur.SHOULD);
    return mimetypeFieldQuery;
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
  @Override
  public int getHitCount() {
    if (hitScoreDocs == null) {
      return 0;
    }
    return hitScoreDocs.length;
  }

  /**
   * Gets the number of documents in the in index.
   *
   * @return the number of indexed documents.
   */
  @Override
  public int getDocumentCount() {
    if (mMultiReader == null) {
      return 0;
    }
    return mMultiReader.numDocs() - mMultiReader.numDeletedDocs();
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
  @Override
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
  @Override
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
  @Override
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
  @Override
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
  @Override
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
  @Override
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
  @Override
  public String getHitIndexName(int index) throws RegainException {
    return mIndexName;
  }

  /**
   * Shortens the summary.
   *
   * @param index The index of the hit.
   * @throws RegainException if shorten fails.
   */
  @Override
  public void shortenSummary(int index) throws RegainException {
    Document document = getHitDocument(index);
    String text = SearchToolkit.getCompressedFieldValue(document, "summary");


    if (text != null) {
      // Overwrite the content with a shortend summary
      String resSummary = RegainToolkit.createSummaryFromContent(text, 200);
      document.removeField("summary");
      if (resSummary != null) {
        document.add(new Field("summary", resSummary, Field.Store.NO, Field.Index.NOT_ANALYZED));
        document.add(new Field("summary", CompressionTools.compressString(resSummary)));
        // write back the transformed document
        setHitDocument(index, document);
      }
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
  @Override
  public void highlightHitDocument(int index) throws RegainException {

    IndexSearcher searcher = null;
    IndexSearcherManager manager = null;
    try {
      manager = IndexSearcherManager.getInstance(mIndexConfig.getDirectory());
      searcher = manager.getIndexSearcher();
      // The highlighter needs a rewritten query to work with wildcard and fuzzy queries
      Query rewrittenQuery = searcher.rewrite(mQuery);
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
      String text = SearchToolkit.getCompressedFieldValue(document, "summary");

      if (text != null) {
        // Overwrite the content with a shortend summary
        String resSummary = RegainToolkit.createSummaryFromContent(text, 200);
        document.removeField("summary");
        if (resSummary != null) {
          //System.out.println("resSummary " + resSummary);
          document.add(new Field("summary", resSummary, Field.Store.NO, Field.Index.NOT_ANALYZED));
          document.add(new Field("summary", CompressionTools.compressString(resSummary)));

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
          document.add(new Field("highlightedSummary", CompressionTools.compressString(resHighlSummary)));
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

    } finally {
      if (manager != null) {
        manager.releaseIndexSearcher(searcher);
      }
    }

  }

  /**
   * Gets whether the search terms should be highlighted
   *
   * @return whether to highlight
   * @throws RegainException If the value could not read from config
   */
  @Override
  public boolean getShouldHighlight(int index) throws RegainException {
    return mIndexConfig.getShouldHighlight();
  }
}