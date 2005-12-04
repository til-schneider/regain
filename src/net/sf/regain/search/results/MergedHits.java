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
 *  $RCSfile: MergedHits.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/results/MergedHits.java,v $
 *     $Date: 2005/08/08 08:40:41 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.search.results;

import java.io.IOException;
import java.util.ArrayList;

import net.sf.regain.RegainException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

/**
 * Merges several search hits.
 * <p>
 * The hits should be disjunct. In other words: If two of the hits contain the
 * same document, it will come up twice in the results. This class is designed
 * to merge hits from different search indexes, which are disjunct and therefor
 * don't contain the same document.
 * <p>
 * The hits are merged after the zipper principle: The documents are merged
 * after their score. The merging is done on demand.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MergedHits {

  /** The hits to merge. */
  private Hits[] mHitsArr;

  /** The cursors where the merge currently is in each hits object. */
  private int[] mHitCursorArr;

  /** The hits that have been merged so far. */
  private ArrayList mMergedHitList;

  /** The total length. Is -1 util it is calculated the first time. */
  private int mLength;


  /**
   * Creates a new instance of MergedHits.
   * 
   * @param hitsArr The hits to merge.
   */
  public MergedHits(Hits[] hitsArr) {
    mHitsArr = hitsArr;
    mLength = -1;

    // NOTE: In Java int arrays are initialized with 0
    mHitCursorArr = new int[hitsArr.length];

    mMergedHitList = new ArrayList();
  }


  /**
   * Gets the length of the merged hits.
   * 
   * @return The length of the merged hits.
   */
  public int length() {
    if (mLength == -1) {
      mLength = 0;
      for (int i = 0; i < mHitsArr.length; i++) {
        mLength += mHitsArr[i].length();
      }
    }
    
    return mLength;
  }
  
  
  /**
   * Gets a hit.
   * 
   * @param n The index of the hit to get.
   * @return The wanted hit.
   * @throws IOException If getting the hit failed.
   */
  private Hit getHit(int n) throws IOException {
    mergeUntil(n);
    return (Hit) mMergedHitList.get(n);
  }


  /**
   * Gets a certain document.
   * 
   * @param n The index of the document to get.
   * @return The wanted document.
   * @throws IOException If getting the document failed.
   */
  public Document doc(int n) throws IOException {
    return getHit(n).getDocument();
  }


  /**
   * Gets the score of a certain document.
   * 
   * @param n The index of the document to get the score of.
   * @return The score of the document
   * @throws IOException If getting the score failed.
   */
  public float score(int n) throws IOException {
    return getHit(n).getScore();
  }


  /**
   * Gets the index of the Hits object a hit comes from. (The index in the
   * hitsArr that was passed to the constructor).
   * 
   * @param n The index of the hit to get the Hits index for.
   * @return The index of the Hits object a hit comes from.
   * @throws RegainException If getting the hits index failed.
   */
  public int getHitsIndex(int n) throws RegainException {
    try {
      return getHit(n).getHitsIndex();
    }
    catch (IOException exc) {
      throw new RegainException("Getting the hit #" + n + " failed", exc);
    }
  }


  /**
   * Gets the position of a hit in the Hits object it comes from.
   * 
   * @param n The index of the hit to get the Hits position for.
   * @return The position of a hit in the Hits object it comes from.
   * @throws RegainException If getting the hits position failed.
   */
  public int getHitsPosition(int n) throws RegainException {
    try {
      return getHit(n).getHitsPosition();
    }
    catch (IOException exc) {
      throw new RegainException("Getting the hit #" + n + " failed", exc);
    }
  }


  /**
   * Merges the hits until a certain index.
   * 
   * @param n The index to merge to.
   * @throws IOException If getting a score failed.
   */
  private void mergeUntil(int n) throws IOException {
    mMergedHitList.ensureCapacity(n + 1);
    
    while (mMergedHitList.size() < (n + 1)) {
      // Find the document with the highest score
      int maxHitsIndex = -1;
      float maxScore = -1;
      for (int i = 0; i < mHitsArr.length; i++) {
        Hits currHits = mHitsArr[i];
        int currCursor = mHitCursorArr[i];
        if (currCursor < currHits.length()) {
          float currScore = currHits.score(currCursor);
          if (currScore > maxScore) {
            // This is the better score -> Remember it
            maxScore = currScore;
            maxHitsIndex = i;
          } else if (currScore == maxScore) {
            // This score is the same as the max. In order to have a fair
            // algorithm (which means that every Hits is used at some time), we
            // use the Hits with the lower cursor (which is the one where the
            // fewest documents were used until now)
            if (currCursor < mHitCursorArr[maxHitsIndex]) {
              maxScore = currScore;
              maxHitsIndex = i;
            }
          }
        }
      }
      
      // Add the best document
      mMergedHitList.add(new Hit(maxHitsIndex, mHitCursorArr[maxHitsIndex], maxScore));
      
      // Move the max cursor
      mHitCursorArr[maxHitsIndex]++;
    }
  }
  
  
  /**
   * Gets the hits to merge.
   * 
   * @return The hits to merge.
   */
  Hits[] getHitsArr() {
    return mHitsArr;
  }
  
  
  /**
   * Contains the data of one hit.
   */
  private class Hit {
    /** The index of the Hits object this hit comes from. (The index in the mHitsArr) */
    private int mHitsIndex;
    /** The position of this hit in the Hits object it comes from. */
    private int mHitsPosition;
    /** The score of this hit. */
    private float mScore;
    /** The cached document. Is null until the first time requested. */
    private Document mDocument;
    
    /**
     * Creates a new instance of Hit.
     * 
     * @param hitsIndex The index of the Hits object this hit comes from.
     *        (The index in the mHitsArr)
     * @param hitsPosition The position of this hit in the Hits object it comes from.
     * @param score The score of this hit.
     */
    public Hit(int hitsIndex, int hitsPosition, float score) {
      mHitsIndex = hitsIndex;
      mHitsPosition = hitsPosition;
      mScore = score;
    }

    /**
     * Gets the index of the Hits object this hit comes from. (The index in th
     * mHitsArr)
     *
     * @return The index of the Hits object this hit comes from.
     */
    public int getHitsIndex() {
      return mHitsIndex;
    }
    
    /**
     * Gets the position of this hit in the Hits object it comes from.
     * 
     * @return The position of this hit in the Hits object it comes from.
     */
    public int getHitsPosition() {
      return mHitsPosition;
    }
    
    /**
     * Gets the score of this hit.
     * 
     * @return The score of this hit.
     */
    public float getScore() {
      return mScore;
    }
    
    /**
     * Gets the document.
     * 
     * @return The document.
     * @throws IOException If getting the document failed.
     */
    public Document getDocument() throws IOException {
      if (mDocument == null) {
        mDocument = getHitsArr()[mHitsIndex].doc(mHitsPosition);
      }
      
      return mDocument;
    }
    
  } // inner class Hit

}
