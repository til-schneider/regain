/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2008  Til Schneider, Thomas Tesche
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
 *     $Date: 2008-11-16 22:23:54 +0100 (So, 16 Nov 2008) $
 *   $Author: thtesche $
 * $Revision: 360 $
 */
package net.sf.regain.crawler;

import java.io.*;
import java.net.*;
import java.text.NumberFormat;
import java.util.ArrayList;

import java.util.regex.Pattern;
import org.apache.log4j.Logger;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.CrawlerConfig;
import net.sf.regain.util.io.HtmlEntities;

/**
 * Contains help methods for the crawler.
 *
 * @author Til Schneider, www.murfman.de
 * @author Gerhard Olsson
 * @author Thomas Tesche
 */
public class CrawlerToolkit {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(CrawlerToolkit.class);

  public static String createURLFromProps(String[] parts) {
    
    String result = "";
    if( parts.length >= 4 ) {
      // We need at least protocol, sld, tld, account/password
      result = parts[0] + "://";
      for( int i=1; i< parts.length-2;i++) {
        // aggregate domain name
        result += parts[i] + ".";
      }
      
      // Remove the last dot
      result = result.substring(0, result.length()-1);
      
      // Analyze length-2 part for portnumber
      if( Pattern.matches("^\\d*$", parts[parts.length-2]) ) {
        result += ":";
      } else {
        result += ".";
      }
      result += parts[parts.length-2] + "/";
        mLog.debug("The result for url assambling is: " +  result );
        System.out.println(result);
    } else {
      mLog.error("This is not a valid authentication entry: " + parts );
    }
    
    return result;
  }
  
  /**
   * Returns a human readable command string for a command.
   *
   * @param commandArr The command separated in executable and parameters.
   * @return The human readable command, where the parameters follow the
   *         execuable separated by spaces.
   */
  private static String toCommand(String[] commandArr) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < commandArr.length; i++) {
      if (i != 0) {
        buffer.append(" ");
      }
      buffer.append(commandArr[i]);
    }
    
    return buffer.toString();
  }


  /**
   * Executes a native command and returns its output.
   * 
   * @param commandArr An array containing ehe command to execute and its parameters.
   * @return The output of the command as arrays of lines.
   * @throws RegainException If executing failed.
   */
  public static String[] executeNativeCommand(String[] commandArr)
    throws RegainException
  {
    InputStream in = null;
    try {
      long startTime = -1;
      if (mLog.isDebugEnabled()) {
        startTime = System.currentTimeMillis();
      }
      Process proc = Runtime.getRuntime().exec(commandArr);

      in = proc.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      ArrayList list = new ArrayList();
      String line;
      while ((line = reader.readLine()) != null) {
        if (mLog.isDebugEnabled()) {
          mLog.debug("  Got line: '" + line + "'");
        }
        list.add(line);
      }

      int exitCode;
      try {
        exitCode = proc.waitFor();
      } catch (InterruptedException exc) {
        throw new RegainException("Waiting for termination of process failed: "
          + commandArr[0], exc);
      }

      if (mLog.isDebugEnabled()) {
        double duration = (double) (System.currentTimeMillis() - startTime) / 1000.0;

        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        mLog.debug("..." + toCommand(commandArr) + " finished ("
            + format.format(duration) + " secs)");
      }

      if (exitCode != 0) {
        throw new RegainException("Native command exited with exit code "
            + exitCode + ": '" + toCommand(commandArr) + "'");
      }

      String[] asArr = new String[list.size()];
      list.toArray(asArr);
      return asArr;
    }
    catch (IOException exc) {
      throw new RegainException("Executing native command failed: '"
          + toCommand(commandArr) + "'", exc);
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }


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
   * @throws RedirectException if the URL redirects to another URL.
   * @throws HttpStreamException if something went wrong.
   */
  public static InputStream getHttpStream(URL url)
    throws RedirectException, HttpStreamException
  {
    URLConnection conn = null;
    try {
      conn = url.openConnection();
      if (conn instanceof HttpURLConnection) {
        HttpURLConnection hconn = (HttpURLConnection) conn;
        // Required in Java 1.5 (redirect followed automatically)
        // (Not available in Java 1.2.2)
        hconn.setInstanceFollowRedirects(false);

        // Set the preferred charset
        String charset = RegainToolkit.getSystemDefaultEncoding() + ",utf-8,*";
        hconn.setRequestProperty("Accept-Charset", charset);

        // Check the response code
        int response = hconn.getResponseCode();
        boolean redirect = (response >= 300 && response <= 399);

        // In the case of a redirect, we want to actually change the URL
        // that was input to the new, redirected URL
        if (redirect) {
          String loc = conn.getHeaderField("Location");
          if (loc != null) {
            String redirectUrl;
            if (loc.startsWith("http")) {
              redirectUrl = new URL(loc).toString();
            } else {
              redirectUrl = new URL(url, loc).toString();
            }
            throw new RedirectException("Redirect '" + url +
                "' -> '" + redirectUrl + "'", redirectUrl);
          }
          throw new IOException("Redirect did not provide a 'Location' header");
        }
      }

      return conn.getInputStream();
    }
    catch (RedirectException thr) {
      throw thr;
    }
    catch (Throwable thr) {
      throw HttpStreamException.createInstance("Could not get HTTP connection to "
          + url.toString(), thr, conn);
    }
  }



  /**
   * L�dt ein Dokument von einem HTTP-Server herunter und gibt seinen Inhalt
   * zurück.
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
    catch (RedirectException exc) {
      throw exc;
    }
    catch (IOException exc) {
      throw new RegainException("Could not load Document with HTTP", exc);
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
   * Loads a file from the file system and returns the content
   *
   * @param file The file to load
   * @return byte[] The content of file
   * @throws RegainException in case of problems while loading
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
   * Loads content from a InputStream and returns the content
   *
   * @param inputStream the stream to read
   * @return byte[] The content of the source
   * @throws RegainException in case of problems while loading
   */
  public static byte[] loadFileFromStream(InputStream inputStream, int length) throws RegainException {
    
    ByteArrayOutputStream out = null;
    try {
      out = new ByteArrayOutputStream(length);

      RegainToolkit.pipe(inputStream, out);

      return out.toByteArray();
    }
    catch (IOException exc) {
      throw new RegainException("Loading inputstream failed ", exc);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
    }
  }


  /**
   * Wandelt die gegebene HTTP-URL in eine absolute URL um.
   * <p>
   * Wenn die URL bereits absolut war, so wird sie unver�ndert zurückgegeben.
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
          // The parentUrl is a domain without a path, e.g. "http://www.murfman.de"
          // -> Use the whole parentUrl
          // NOTE: url start with a /
          url = parentUrl + url;
        }
      } else {
        // This URL is really relative
        int lastSlashPos = parentUrl.lastIndexOf('/');
        // NOTE: http:// has 7 chars
        if (lastSlashPos > 7) {
          String domainWidthPath = parentUrl.substring(0, lastSlashPos + 1);
          url = domainWidthPath + url;
        } else {
          // The parentUrl is a domain without a path, e.g. "http://www.murfman.de"
          // -> Use the whole parentUrl
          url = parentUrl + "/" + url;
        }
      }
    }

    // Check if url contains . in path
    url = RegainToolkit.replace(url, "/./", "/");
    if (url.endsWith("/.")) {
      url = url.substring(0, url.length() - 2);
    }

    // Check if url contains .. in path
    int updirIdx = 0;
    while ((updirIdx = url.indexOf("/..", updirIdx)) != -1) {
      // Check whether a / follows or whether this is the end
      int slashAfterIdx = updirIdx + 3;
      if ((slashAfterIdx >= url.length()) || (url.charAt(slashAfterIdx) == '/')) {
        // We found a "/../" or an "/.." at the end
        // -> Cut the directory before and the .. out 
      
        // Find previous /
        int slashBeforeIdx = url.lastIndexOf('/', updirIdx - 1);

        if (slashBeforeIdx != -1) {
          // Cut the "/somedir/.." out
          url = url.substring(0, slashBeforeIdx) + url.substring(slashAfterIdx);
          updirIdx = slashBeforeIdx;
        } else {
          throw new IllegalArgumentException("Illegal URL: " + url
              + ". (parent URL: " + parentUrl + ") Contains a .. with no / before");
        }
      } else {
        // This is something like "a/..extension/b" -> Go on after the "/.."
        updirIdx += 3;
      }
    }

    return url;
  }

  /**
   * Removes anchors from URLs like http://mydomain.com/index.html#anchor
   * 
   * @param url an URL with or without an anchor
   * @return the URL without an anchor
   */
  public static String removeAnchor(String url){
    // Remove anchors from link.
    int index = url.indexOf('#');
    if (index != -1) {
      return url.substring(0, index);
    } else {
      return url;
    }
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


  /**
   * Initializes the HTTP client
   *
   * @param config The configuration to read the settings from.
   */
  public static void initHttpClient(CrawlerConfig config) {
    String httpProxyHost = config.getProxyHost();
    String httpProxyPort = config.getProxyPort();
    String httpProxyUser = config.getProxyUser();
    String httpProxyPassword = config.getProxyPassword();

    String msg = "";
    if (httpProxyHost != null) {
      System.setProperty("http.proxyHost", httpProxyHost);
      msg += " host: " + httpProxyHost;
    }
    if (httpProxyPort != null) {
      System.setProperty("http.proxyPort", httpProxyPort);
      msg += " port: " + httpProxyPort;
    }
    if (httpProxyUser != null) {
      System.setProperty("http.proxyUser", httpProxyUser);
      msg += " user: " + httpProxyUser;
    }
    if (httpProxyPassword != null) {
      System.setProperty("http.proxyPassword", httpProxyPassword);
      msg += " password: (" + httpProxyPassword.length() + " characters)";
    }

    if (msg.length() != 0) {
      mLog.info("Using proxy:" + msg);
    } else {
      mLog.info("Using no proxy");
    }

    String userAgent = config.getUserAgent();
    if (userAgent != null) {
      System.setProperty("http.agent", userAgent);
      mLog.info("Using HTTP user agent:" + userAgent);
    }
  }


  /**
   * Wandelt alle HTML-Entit�ten in ihre Ensprechungen.
   *
   * @param text Den Text, dessen HTML-Entit�ten gewandelt werden sollen.
   *
   * @return Der gewandelte Text.
   */
  public static String replaceHtmlEntities(String text) {
    StringBuffer clean = new StringBuffer();

    int offset = 0;
    int entityStart;
    while ((entityStart = text.indexOf('&', offset)) != -1) {
      // Append the part since the last entity
      String textPart = text.substring(offset, entityStart);
      clean.append(textPart);

      // Find the end of the entity
      int entityEnd = text.indexOf(';', entityStart);
      if (entityEnd == -1) {
        // Syntax error: The entity doesn't end -> Forget that dirty end
        offset = text.length();
        break;
      }

      // Extract, decode and append the entity
      String entity = text.substring(entityStart, entityEnd + 1);
      String decoded;
      try {
        decoded = HtmlEntities.decode(entity);
      }
      catch (Throwable thr) {
        // This doesn't seem to be a wellformed entity -> Leave the text as it is
        decoded = entity;
      }
      clean.append(decoded);

      // Get the next offset
      offset = entityEnd + 1;
    }

    // Append the part since the last entity
    if (offset < text.length()) {
      clean.append(text.substring(offset, text.length()));
    }

    return clean.toString();
  }


  /**
   * S�ubert HTML-Text von seinen Tags und wandelt alle HTML-Entit�ten in ihre
   * Ensprechungen.
   *
   * @param text Der zu s�ubernde HTML-Text.
   *
   * @return Der von Tags gesüberte Text
   */
  public static String cleanFromHtmlTags(String text) {
    StringBuffer clean = new StringBuffer(text.length());

    int offset = 0;
    int tagStart;
    while ((tagStart = text.indexOf('<', offset)) != -1) {
      // Extract the good part since the last tag
      String goodPart = text.substring(offset, tagStart);

      // Check whether the good part is wasted by cascaded tags
      // Example: In the text "<!-- <br> --> Hello" "<!-- <br>" will be
      //          detected as tag and "--> Hello" as good part.
      //          We now have to scan the good part for a tag rest.
      //          (In this example: "-->")
      int tagRestEnd = goodPart.indexOf('>');
      if (tagRestEnd != -1) {
        goodPart = goodPart.substring(tagRestEnd + 1);
      }

      // Trim the good part
      goodPart = goodPart.trim();

      if (goodPart.length() > 0) {
        // Replace all entities in the text and append the result
        goodPart = replaceHtmlEntities(goodPart);
        clean.append(goodPart);

        // Append a space
        clean.append(" ");
      }

      // Find the end of the tag
      int tagEnd = text.indexOf('>', tagStart);
      if (tagEnd == -1) {
        // Syntax error: The tag doesn't end -> Forget that dirty end
        offset = text.length();
        break;
      }

      // Calculate the next offset
      offset = tagEnd + 1;
    }

    // Extract the good part since the last tag, replace all entities and append
    // the result
    if (offset < text.length()) {
      String goodPart = text.substring(offset, text.length()).trim();
      goodPart = replaceHtmlEntities(goodPart);
      clean.append(goodPart);
    }

    return clean.toString();
  }
  
}
