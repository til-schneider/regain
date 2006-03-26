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
 *  $RCSfile: MemoryAppender.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/util/io/MemoryAppender.java,v $
 *     $Date: 2006/04/12 14:44:19 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.util.io;

import java.util.Iterator;
import java.util.LinkedList;

import net.sf.regain.RegainException;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A Log4j appender that keeps a number of logging events in memory. These
 * events may be layouted on demand.
 *
 * @author Til Schneider, www.murfman.de
 */
public class MemoryAppender extends AppenderSkeleton {

  /** Holds the cached log messages. */
  private LinkedList mCache;

  /** The maximum cache size. */
  private int mMaxCacheSize;


  /**
   * Creates a new MemoryAppender instance.
   */
  public MemoryAppender() {
    mCache = new LinkedList();
    mMaxCacheSize = 30;
  }


  /**
   * Sets the maximum cache size.
   *
   * @param maxCacheSize the maximum cache size.
   */
  public void setMaxCacheSize(int maxCacheSize) {
    mMaxCacheSize = maxCacheSize;
  }

  
  /**
   * Prints the cached logging events to a page printer.
   *
   * @param printer The page printer to print to.
   * @throws RegainException If printing failed.
   */
  public void printLog(Printer printer) throws RegainException {
    synchronized (mCache) {
      Iterator iter = mCache.iterator();
      while (iter.hasNext()) {
        Object[] itemArr = (Object[]) iter.next();
        LoggingEvent evt = (LoggingEvent) itemArr[0];
        String formattedEvt = (String) itemArr[1];

        if (formattedEvt == null) {
          formattedEvt = getLayout().format(evt);
          itemArr[1] = formattedEvt;
        }

        printer.print(formattedEvt);
      }
    }
  };


  // overridden
  protected void append(LoggingEvent evt) {
    synchronized (mCache) {
      mCache.add(new Object[] { evt, null });
  
      if (mCache.size() > mMaxCacheSize) {
        mCache.removeFirst();
      }
    }
  }


  // overridden
  public void close() {
  }


  // overridden
  public boolean requiresLayout() {
    return true;
  }

}
