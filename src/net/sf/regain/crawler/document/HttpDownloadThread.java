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
 *  $RCSfile: HttpDownloadThread.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/document/HttpDownloadThread.java,v $
 *     $Date: 2004/07/28 20:26:03 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.document;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.CrawlerToolkit;

/**
 * Thread, der einen HTTP-Download übernimmt.
 * <p>
 * Hilfsklasse für {@link RawDocument#loadContent(String)}.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class HttpDownloadThread extends Thread {
  
  /** Die URL des zu ladenden Dokuments. */
  private String mDocUrl;
  /** Der Thread, der am Ende des Downloads benachrichtigt werden soll. */
  private Thread mWaitingThread;
  /**
   * Der Inhalt des Dokuments. Ist <code>null</code>, solange der Download
   * nicht erfolgreich abgeschlossen ist.
   */
  private byte[] mDocContent;
  /**
   * Der Fehler, der beim Download aufgetreten ist. Ist <code>null</code>,
   * solange kein Fehler auftrat.
   */
  private Throwable mError;
  
  
  /**
   * Erzeugt einen neuen HttpDownloadThread
   * 
   * @param url Die URL des zu ladenden Dokuments.
   * @param waitingThread Der Thread, der am Ende des Downloads benachrichtigt
   *        werden soll.
   */
  public HttpDownloadThread(String url, Thread waitingThread) {
    mDocUrl = url;
    mWaitingThread = waitingThread;
  }
  
  
  /**
   * Führt den Download aus.
   * <p>
   * Am Ende ist entweder mDocContent oder mError gesetzt, bevor der wartende
   * Thread benachrichtigt wird.
   */
  public void run() {
    try {
      mDocContent = CrawlerToolkit.loadHttpDocument(mDocUrl);
    }
    catch (RegainException exc) {
      mError = exc;
    }
    
    synchronized (this) {
      if (mWaitingThread != null) {
        mWaitingThread.interrupt();
      }
    }
  }
  
  
  /**
   * Gibt den Download auf. Es wird sichergestellt, dass der wartende Thread
   * nun nicht mehr benachrichtigt wird.
   */
  public void cancel() {
    synchronized (this) {
      mWaitingThread = null;
    }
  }
  
  
  /**
   * Gibt den Inhalt des heruntergeladenen Dokuments zurück.
   * <p>
   * Falls das Dokument noch nicht vollständig heruntergeladen wurde, wird
   * <code>null</code> zurückgegeben.
   * 
   * @return Der Inhalt des heruntergeladenen Dokuments
   */
  public byte[] getContent() {
    return mDocContent;
  }
  
  
  /**
   * Gibt den Fehler zurück, der beim Download auftrat.
   * <p>
   * Falls noch kein Fehler auftrat, wird <code>null</code> zurückgegeben.
   * 
   * @return Der Fehler, der beim Download auftrat.
   */
  public Throwable getError() {
    return mError;
  }

}