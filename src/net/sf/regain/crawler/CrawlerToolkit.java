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
 *  $RCSfile: CrawlerToolkit.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/CrawlerToolkit.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler;

import java.io.*;
import java.net.*;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;


/**
 * Enth�lt Hilfsmethoden f�r den Crawler und seine Hilfsklassen.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class CrawlerToolkit {

  /**
   * Originally copied from javax.swing.JEditorPane#getStream(...).
   * <p>
   * Fetches a stream for the given URL, which is about to
   * be loaded by the <code>setPage</code> method.  By
   * default, this simply opens the URL and returns the
   * stream.  This can be reimplemented to do useful things
   * like fetch the stream from a cache, monitor the progress
   * of the stream, etc.
   * <p>
   * This method is expected to have the the side effect of
   * establishing the content type, and therefore setting the
   * appropriate <code>EditorKit</code> to use for loading the stream.
   * <p>
   * If this the stream was an http connection, redirects
   * will be followed and the resulting URL will be set as
   * the <code>Document.StreamDescriptionProperty</code> so that relative
   * URL's can be properly resolved.
   *
   * @param url the URL of the page
   *
   * @return a stream reading data from the specified URL.
   * @throws HttpStreamException if something went wrong.
   */
  public static InputStream getHttpStream(URL url) throws HttpStreamException {
    URLConnection conn = null;
    try {
      conn = url.openConnection();
      if (conn instanceof HttpURLConnection) {
        HttpURLConnection hconn = (HttpURLConnection) conn;
        // hconn.setInstanceFollowRedirects(false); // Not available in Java 1.2.2
        int response = hconn.getResponseCode();
        boolean redirect = (response >= 300 && response <= 399);

        // In the case of a redirect, we want to actually change the URL
        // that was input to the new, redirected URL
        if (redirect) {
          String loc = conn.getHeaderField("Location");
          if (loc.startsWith("http", 0)) {
            url = new URL(loc);
          } else {
            url = new URL(url, loc);
          }
          return getHttpStream(url);
        }
      }

      return conn.getInputStream();
    }
    catch (IOException exc) {
      throw HttpStreamException.createInstance("Getting HTTP connection to "
        + url.toString() + " failed", exc, conn);
    }
  }



  /**
   * L�dt ein Dokument von einem HTTP-Server herunter und gibt seinen Inhalt
   * zur�ck.
   *
   * @param url Die URL des zu ladenden Dokuments.
   *
   * @return Den Inhalt des Dokuments.
   * @throws RegainException Wenn das Laden fehl schlug.
   */
  public static byte[] loadHttpDocument(String url) throws RegainException {
    InputStream in = null;
    ByteArrayOutputStream out = null;
    try {
      in = getHttpStream(new URL(url));

      out = new ByteArrayOutputStream();

      RegainToolkit.pipe(in, out);
      
      out.close();
      return out.toByteArray();
    }
    catch (IOException exc) {
      throw new RegainException("Loading Dokument by HTTP failed", exc);
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (Exception exc) {}
      }
      if (out != null) {
        try { out.close(); } catch (Exception exc) {}
      }
    }
  }
  
  
  /**
   * Kopiert eine Datei.
   * 
   * @param from Die Quelldatei
   * @param to Die Zieldatei
   * @throws RegainException Wenn das Kopieren fehl schlug.
   */
  public static void copyFile(File from, File to) throws RegainException {
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      in = new FileInputStream(from);
      out = new FileOutputStream(to);
      
      RegainToolkit.pipe(in, out);
    }
    catch (IOException exc) {
      throw new RegainException("Copying file from " + from.getAbsolutePath()
        + " to " + to.getAbsolutePath() + " failed", exc);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }
  
  
  /**
   * L�dt eine Datei vom Dateisystem und gibt den Inhalt zur�ck.
   *
   * @param file Die zu ladende Datei
   * @return Den Inhalt der Datei.
   * @throws RegainException Falls das Laden fehl schlug.
   */
  public static byte[] loadFile(File file) throws RegainException {
    if (file.isDirectory()) {
      throw new RegainException("Can't load a directory: "
        + file.getAbsolutePath());
    }

    FileInputStream in = null;
    ByteArrayOutputStream out = null;
    try {
      in = new FileInputStream(file);
      out = new ByteArrayOutputStream((int) file.length());
      
      RegainToolkit.pipe(in, out);
      
      return out.toByteArray();
    }
    catch (IOException exc) {
      throw new RegainException("Loading file failed " + file.getAbsolutePath(), exc);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }



  /**
   * Wandelt die gegebene HTTP-URL in eine absolute URL um.
   * <p>
   * Wenn die URL bereits absolut war, so wird sie unver�ndert zur�ckgegeben.
   *
   * @param url Die umzuwandelnde URL.
   * @param parentUrl Die URL auf die sich die umzuwandelnde URL bezieht. Diese
   *        URL muss absolut sein.
   *
   * @return Die absolute Version der gegebenen URL.
   */
  public static String toAbsoluteUrl(String url, String parentUrl) {
    if (! (url.startsWith("http://") || url.startsWith("file://"))) {
      // This is a relative URL

      if (parentUrl.startsWith("http://") && url.startsWith("/")) {
        // NOTE: In HTTP there are two kinds of relative URLs:
        // Some start with '/': They are absolute within the domain
        // Others don't start with '/': They are really realtive

        // This URL is absolute within the domain
        // NOTE: 7 for skipping 'http://'
        int firstSlashPos = parentUrl.indexOf('/', 7);
        if (firstSlashPos != -1) {
          String domain = parentUrl.substring(0, firstSlashPos);
          url = domain + url;
        } else {
          throw new IllegalArgumentException("Parent URL is not absolute: " + parentUrl);
        }
      } else {
        // This URL is really relative
        int lastSlashPos = parentUrl.lastIndexOf('/');
        if (lastSlashPos != -1) {
          String domainWidthPath = parentUrl.substring(0, lastSlashPos + 1);
          url = domainWidthPath + url;
        } else {
          throw new IllegalArgumentException("Parent URL is not absolute: " + parentUrl);
        }
      }
    }

    return url;
  }



  /**
   * Gibt die Datei zur�ck, die hinter einer URL mit dem file:// Protokoll
   * steht.
   *
   * @param url Die URL, f�r die die Datei zur�ckgegeben werden soll.
   * @return Die zur URL passende Datei.
   * @throws RegainException Wenn das Protokoll der URL nicht
   *         <code>file://</code> ist.
   */
  public static File urlToFile(String url) throws RegainException {
    if (! url.startsWith("file://")) {
      throw new RegainException("URL must have the file:// protocol to get a "
        + "File for it");
    }
    
    // Cut the file://
    String fileName = url.substring(7);

    // Replace %20 by spaces
    fileName = RegainToolkit.replace(fileName, "%20", " ");
    
    return new File(fileName);
  }

  
  
  /**
   * Gibt die URL einer Datei zur�ck.
   *
   * @param file Die Datei, deren URL zur�ckgegeben werden soll.
   * @return Die URL der Datei.
   */
  public static String fileToUrl(File file) {
    String fileName = file.getAbsolutePath();
    
    // Replace spaces by %20
    fileName = RegainToolkit.replace(fileName, " ", "%20");
    
    // Replace file separators by /
    fileName = RegainToolkit.replace(fileName, File.separator, "/");
    
    return "file://" + fileName;
  }



  /**
   * Prints the active threads to System.out. Usefull for debugging.
   */
  public static void printActiveThreads() {
    ThreadGroup group = Thread.currentThread().getThreadGroup();
    Thread[] activeArr = new Thread[group.activeCount()];
    group.enumerate(activeArr);
    System.out.print("active threads: ");
    for (int i = 0; i < activeArr.length; i++) {
      if (i != 0) {
        System.out.print(", ");
      }
      System.out.print(activeArr[i].getName());
    }
    System.out.println();
  }
  
}
