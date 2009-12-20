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

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

/**
 * Holds the results of a search on a multiple indexes.
 *
 * @author Til Schneider, www.murfman.de
 * @deprecated Will be removed in release 2.0
 */
public class MultipleSearchResults implements SearchResults {

  /**
   * The results of from the single searches that should be merged by this
   * MultipleSearchResults.
   */
  private SingleSearchResults[] mChildResultsArr;
  
  /** The merged hits. */
  private MergedHits mMergedHits;
  
  /** The cached search time. */
  private int mSearchTime = -1;


  /**
   * Creates a new instance of MultipleSearchResults.
   * 
   * @param childResultsArr The results of from the single searches that should
   *        be merged by this MultipleSearchResults.
   */
  public MultipleSearchResults(SingleSearchResults[] childResultsArr) {
    mChildResultsArr = childResultsArr;
    
    Hits[] hitsArr = new Hits[mChildResultsArr.length];
    for (int i = 0; i < hitsArr.length; i++) {
      hitsArr[i] = mChildResultsArr[i].getHits();
    }
    mMergedHits = new MergedHits(hitsArr);
  }


  /**
   * Gets the number of hits the search had.
   *
   * @return the number of hits the search had.
   */
  public int getHitCount() {
    return mMergedHits.length();
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
      return mMergedHits.doc(index);
    }
    catch (IOException exc) {
      throw new RegainException("Error while getting document of merged search hit #"
          + index, exc);
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
      return mMergedHits.score(index);
    } catch (IOException exc) {
      throw new RegainException("Error while getting score of merged search hit #"
          + index, exc);
    }
  }
  
  
  /**
   * Gets the SingleSearchResults a certain hit came from.
   * 
   * @param index The index of the hit in the merged hits.
   * @return The SingleSearchResults the hit came from.
   * @throws RegainException If getting the index of the SingleSearchResults failed.
   */
  private SingleSearchResults getResultsForHit(int index) throws RegainException {
    // Get the hits index from the MergedHits
    int hitsIndex = mMergedHits.getHitsIndex(index);
    
    // NOTE: As we added the hits in the same order as our child results
    //       to MergedHits, the hits index is at the same time the results index.
    return mChildResultsArr[hitsIndex];
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
    // Get the SingleSearchResults the hit came from
    SingleSearchResults results = getResultsForHit(index);
    
    // Get the index of the hit in the SingleSearchResults
    int hitsPosition = mMergedHits.getHitsPosition(index);
    
    // Get the hit URL
    return results.getHitUrl(hitsPosition);
  }


  /**
   * Gets the name of the index a hit comes from.
   * 
   * @param index The index of the hit to get the index name for.
   * @return The name of the index a hit comes from.
   * @throws RegainException If getting the index name failed.
   */
  public String getHitIndexName(int index) throws RegainException {
    // Get the SingleSearchResults the hit came from
    SingleSearchResults results = getResultsForHit(index);

    // Get the index of the hit in the SingleSearchResults
    int hitsPosition = mMergedHits.getHitsPosition(index);
    
    // Gets the index name;
    return results.getHitIndexName(hitsPosition);
  }


  /**
   * Gets whether a hit should be opened in a new window.
   *
   * @param index The index of the hit.
   * @return Whether the hit should be opened in a new window.
   * @throws RegainException If the hit could not be read.
   */
  public boolean getOpenHitInNewWindow(int index) throws RegainException {
    // Get the SingleSearchResults the hit came from
    SingleSearchResults results = getResultsForHit(index);
    
    // Get the index of the hit in the SingleSearchResults
    int hitsPosition = mMergedHits.getHitsPosition(index);
    
    // Get whether a hit should be opened in a new window.
    return results.getOpenHitInNewWindow(hitsPosition);
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
   * @throws RegainException If the hit could not be read.
   */
  public boolean getUseFileToHttpBridgeForHit(int index) throws RegainException {
    // Get the SingleSearchResults the hit came from
    SingleSearchResults results = getResultsForHit(index);
    
    // Get the index of the hit in the SingleSearchResults
    int hitsPosition = mMergedHits.getHitsPosition(index);
    
    // Get whether a hit should be opened in a new window.
    return results.getUseFileToHttpBridgeForHit(hitsPosition);
  }


  /**
   * Gets the time the search took in milliseconds.
   *
   * @return The search time.
   */
  public int getSearchTime() {
    if (mSearchTime == -1) {
      mSearchTime = 0;
      for (int i = 0; i < mChildResultsArr.length; i++) {
        mSearchTime += mChildResultsArr[i].getSearchTime();
      }
    }
    
    return mSearchTime;
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
   
    // Get the SingleSearchResults the hit came from
    SingleSearchResults results = getResultsForHit(index);
    
    // Get the index of the hit in the SingleSearchResults
    int hitsPosition = mMergedHits.getHitsPosition(index);
    
    results.highlightHitDocument(hitsPosition);
  }
  
    /**
   * Gets whether the search terms should be highlighted
   *
   * @return whether to highlight
   * @throws RegainException If the value could not read from config
   */
  public boolean getShouldHighlight(int index) throws RegainException {
    // Get the SingleSearchResults the hit came from
    SingleSearchResults results = getResultsForHit(index);

    // Get the index of the hit in the SingleSearchResults
    int hitsPosition = mMergedHits.getHitsPosition(index);

    // Get whether a hit should be highlighted.
    return results.getShouldHighlight(hitsPosition);
  }

  /**
   * Shortens the summary.
   *
   * @param index The index of the hit.
   * @throws RegainException if shorten fails.
   */
  public void shortenSummary(int index) throws RegainException {
    // Get the SingleSearchResults the hit came from
    SingleSearchResults results = getResultsForHit(index);

    // Get the index of the hit in the SingleSearchResults
    int hitsPosition = mMergedHits.getHitsPosition(index);

    // Shortens the summary
    results.shortenSummary(hitsPosition);

  }
}
