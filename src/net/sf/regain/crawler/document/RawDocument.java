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
 *     $Date: 2008-11-16 22:23:54 +0100 (So, 16 Nov 2008) $
 *   $Author: thtesche $
 * $Revision: 360 $
 */
package net.sf.regain.crawler.document;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import jcifs.smb.SmbFile;
import net.sf.regain.ImapToolkit;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.Profiler;
import net.sf.regain.crawler.RedirectException;

import org.apache.log4j.Logger;


/**
 * enthält alle Rohdaten eines Dokuments.
 * <p>
 * Falls der Inhalt des Dokuments zur besseren Bearbeitung in Form eines Strings
 * gebraucht wird, dann wird dieser zum sp�test m�glichen Zeitpunkt erstellt.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RawDocument {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(RawDocument.class);

  /** Der Profiler der das Laden via HTTP misst. */
  private static final Profiler HTTP_LOADING_PROFILER
    = new Profiler("Documents loaded with HTTP", "docs");

  /** Der Profiler der das Laden vom Dateisystem misst. */
  private static final Profiler FILE_LOADING_PROFILER
    = new Profiler("Documents loaded from the file system", "docs");
  
  /** The pattern which matches for imap-urls (folder, message and attachment) */
  //Pattern imapPattern = Pattern.compile(".*(message_([0-9]+))(_attachment_([0-9]+))$");

  /**
   * Der Timeout für HTTP-Downloads. Dieser Wert bestimmt die maximale Zeit
   * in Sekunden, die ein HTTP-Download insgesamt dauern darf.
   */
  private static int mHttpTimeoutSecs = 180;

  /** Die URL des Dokuments. */
  private String mUrl;

  /** Die URL jenen Dokuments, in dem die URL dieses Dokuments gefunden wurde. */
  private String mSourceUrl;

  /**
   * Der Text des Links in dem die URL gefunden wurde. Ist <code>null</code>,
   * falls die URL nicht in einem Link (also einem a-Tag) gefunden wurde oder
   * wenn aus sonstigen Gr�nden kein Link-Text vorhanden ist.
   */
  private String mSourceLinkText;

  /**
   * Der Inhalt des Dokuments.
   * Ist <CODE>null</CODE>, wenn sich das Dokument in einer Datei befindet und
   * noch nicht angefragt wurde.
   */
  private byte[] mContent;

  /**
   * Der Inhalt des Dokuments als String. Ist <CODE>null</CODE>, bis er zum
   * ersten mal angefragt wurde.
   */
  private String mContentAsString;

  /**
   * Die Datei, in der sich der Inhalt des Dokuments befindet.
   * Ist <CODE>null</CODE>, wenn das Dokument über HTTP bezogen wird und noch
   * nicht angefragt wurde.
   */
  private File mContentAsFile;

  /**
   * Ist die Datei {@link #mContentAsFile} tempor�r. Wenn <code>true</code>,
   * dann wird die Datei am Ende gel�scht.
   *
   * @see #dispose()
   */
  private boolean mContentAsFileIsTemporary;

  /**
   * The mimetype of the document. This value will be set through file extension
   * mapping or by using the *nix-file command (java version)
   */
  private String mMimeType;

  /**
   * HashMap links containing the URL as key and the linktext as value
   */
  private HashMap <String,String>mLinks;
  
  /**
   * Erzeugt eine neue RawDocument-Instanz.
   *
   * @param url Die URL des Dokuments.
   * @param sourceUrl Die URL jenen Dokuments, in dem die URL dieses Dokuments
   *        gefunden wurde.
   * @param sourceLinkText Der Text des Links in dem die URL gefunden wurde. Ist
   *        <code>null</code>, falls die URL nicht in einem Link (also einem
   *        a-Tag) gefunden wurde oder wenn aus sonstigen Gr�nden kein Link-Text
   *        vorhanden ist.
   * @throws RegainException Wenn das Dokument nicht geladen werden konnte.
   */
  public RawDocument(String url, String sourceUrl, String sourceLinkText)
    throws RegainException
  {
    mLinks = new HashMap<String,String>();
    mUrl = url;
    mSourceUrl = sourceUrl;
    mSourceLinkText = sourceLinkText;

    if (url.startsWith("file://")) {
      mContentAsFile = RegainToolkit.urlToFile(url);
    } else if( url.startsWith("smb://" )) {
      mContent = null;
      mContentAsFile = null;
   } else if( url.startsWith("imap://") || url.startsWith("imaps://")) {
      mContent = null;
      mContentAsFile = null;
    } else {
      mContent = loadContent(url);
      mContentAsFile = null;
    }
  }


  /**
   * Setzt den Timeout für HTTP-Downloads.
   * <p>
   * Dieser Wert bestimmt die maximale Zeit, die ein HTTP-Download insgesamt
   * dauern darf.
   *
   * @param httpTimeoutSecs Der neue Timeout.
   */
  public static void setHttpTimeoutSecs(int httpTimeoutSecs) {
    mHttpTimeoutSecs = httpTimeoutSecs;
  }

  /**
   * Loads a mime message from an IMAP server.
   * 
   * @param url the URL of the mime message
   * @return content of the message
   * @throws RegainException if loading fails
   */
  
  private byte[] loadIMAPMessage(String url) throws RegainException {
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] bytearrayMessage = null;

    try {
      Matcher matcher = ImapToolkit.getMessagePattern().matcher(url);
      matcher.find();
      if( matcher.groupCount()>0 ) {
        // We found a message url. Determine the message UID from the url.
        int messageUID = Integer.parseInt(matcher.group(3));
        mLog.debug("Read mime message uid: " + messageUID + " for IMAP url: " + url);
        Session session = Session.getInstance(new Properties());
    
        URLName originURLName = new URLName(ImapToolkit.cutMessageIdentifier(url));
        // Replace all %20 with whitespace in folder pathes
        String folder = "";
        if(originURLName.getFile()!=null){
          folder = originURLName.getFile().replaceAll("%20", " ");
        }
        URLName urlName = new URLName(originURLName.getProtocol(), originURLName.getHost(), 
          originURLName.getPort(), folder, originURLName.getUsername(), originURLName.getPassword());
    
        IMAPSSLStore imapStore = new IMAPSSLStore(session, urlName);
        imapStore.connect();
        IMAPFolder currentFolder;

        if (urlName.getFile() == null) {
          // There is no folder given
          currentFolder = (IMAPFolder) imapStore.getDefaultFolder();
        } else {
          currentFolder = (IMAPFolder) imapStore.getFolder(urlName.getFile());
        }

        currentFolder.open(Folder.READ_WRITE);
        MimeMessage cplMessage = (MimeMessage) currentFolder.getMessageByUID(messageUID);
    
        if (cplMessage != null) {
          cplMessage.setFlag(Flags.Flag.SEEN, true);
          cplMessage.writeTo(baos);
          bytearrayMessage = baos.toByteArray();
        }

        currentFolder.close(false);
        imapStore.close();       
        
      }

    } catch (Throwable thr) {
      throw new RegainException( thr.getMessage(), thr );
    }  
    
    return bytearrayMessage;
  }
  
  /**
   * Loads the content from a smb file
   * 
   * @param url The URL
   * @return content of the document 
   * @throws net.sf.regain.RegainException if loading fails
   */
  private byte[] loadSmbFile(String url) throws RegainException {
        
    InputStream in = null;
    try {
      SmbFile smbFile = RegainToolkit.urlToSmbFile(url);
    
      if( smbFile.canRead() && !smbFile.isDirectory() ) {
        in = smbFile.getInputStream();
        
        return CrawlerToolkit.loadFileFromStream(in,smbFile.getContentLength());
        
      } else {
        throw new RegainException("Can't load content from: "
        + smbFile.getCanonicalPath());
      }
      
    } catch (Throwable thr) {
      throw new RegainException( thr.getMessage(), thr );
      
    } finally {
       if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
    
  }

  /**
   * Load content from URL for http/https documents.
   *
   * @param url The URL.
   * @return content of document.
   * @throws RegainException if loading was erroneous
   */
  private byte[] loadContent(String url) throws RegainException {
    HTTP_LOADING_PROFILER.startMeasuring();
    HttpDownloadThread loaderThread
      = new HttpDownloadThread(url, Thread.currentThread());
    loaderThread.start();

    // Warten bis entweder der Timeout abläuft, oder bis dieser Thread vom
    // HttpContentLoaderThread unterbrochen wird.
    try {
      Thread.sleep(mHttpTimeoutSecs * 1000);
    }
    catch (InterruptedException exc) {}

    // Prüfen, ob wir mittlerweile den Inhalt haben
    byte[] content = loaderThread.getContent();
    if (content != null) {
      HTTP_LOADING_PROFILER.stopMeasuring(content.length);
      return content;
    } else {
      // Wir haben keinen Inhalt
      HTTP_LOADING_PROFILER.abortMeasuring();

      // Check whether download failed
      if (loaderThread.getError() != null) {
        if (loaderThread.getError() instanceof RedirectException) {
          throw (RedirectException) loaderThread.getError();
        } else {
          throw new RegainException("Loading Document by HTTP failed: " + url,
                                    loaderThread.getError());
        }
      }

      // Wir haben weder einen Inhalt noch einen Fehler
      // -> Der Download läuft noch
      // -> Da jedoch mittlerweile der Timeout abgelaufen ist -> Exception werfen
      throw new RegainException("Loading Document by HTTP timed out after " +
          mHttpTimeoutSecs + " seconds: " + url);
    }
  }


  /**
   * Return the lentgth of the document (in bytes).
   *
   * @return int length of document.
   */
  public int getLength() /*throws RegainException*/ {
    int length = 0;
    
    if (mContent != null) {
      length = mContent.length;
    
    } else {
      // document still not loaded because it's a file 
      if( mContentAsFile != null ) {
        length = (int) mContentAsFile.length();
      
      } else if( mUrl.startsWith("smb://")) {
        // @todo : define a suitable way to hold different kinds of files (local fs, windows share, other share types)
        try{
          length = (int) RegainToolkit.urlToSmbFile(mUrl).length();
        } catch( Exception ex ){
          //throw new RegainException("Detection of file length failed: ", ex);
        }
      }
    }
    return length;
  }


  /**
   * Gibt zurück, wann das Dokument zuletzt geändert wurde.
   * <p>
   * Wenn die letzte Änderung nicht ermittelt werden kann (z.B. bei
   * HTTP-Dokumenten), dann wird <code>null</code> zurückgegeben.
   *
   * @return Wann das Dokument zuletzt geändert wurde.
   */
  public Date getLastModified() {
    Date date = null;
    
    if (mUrl.startsWith("file://")) {
      date = new Date(mContentAsFile.lastModified());
    } else if(mUrl.startsWith("smb://")) {
      try{
        date = new Date(RegainToolkit.urlToSmbFile(mUrl).lastModified());
      } catch( Exception ex ){
        //throw new RegainException("Detection of file length failed: ", ex);
      }
    }
    // @todo: last modified date for messages
    
    return date;
  }


  /**
   * Gibt die URL des Dokuments zurück.
   *
   * @return Die URL des Dokuments.
   */
  public String getUrl() {
    return mUrl;
  }



  /**
   * Gibt die URL jenen Dokuments zurück, in dem die URL dieses Dokuments
   * gefunden wurde.
   *
   * @return Die URL jenen Dokuments, in dem die URL dieses Dokuments gefunden
   *         wurde.
   */
  public String getSourceUrl() {
    return mSourceUrl;
  }



  /**
   * Gibt den Text des Links zurück in dem die URL gefunden wurde.
   * <p>
   * Ist <code>null</code>, falls die URL nicht in einem Link (also einem a-Tag)
   * gefunden wurde oder wenn aus sonstigen Gr�nden kein Link-Text vorhanden ist.
   *
   * @return Der Text des Links zurück in dem die URL gefunden wurde.
   */
  public String getSourceLinkText() {
    return mSourceLinkText;
  }



  /**
   * Gibt den Inhalt des Dokuments zurück.
   *
   * @return Der Inhalt des Dokuments.
   * @throws RegainException Wenn das Dokument nicht geladen werden konnte.
   */
  public byte[] getContent() throws RegainException {
    if (mContent == null) {
      // Das Dokument befindet sich in einer Datei -> Diese laden
      FILE_LOADING_PROFILER.startMeasuring();
      try {
        byte[] content = null;
        if( mUrl.startsWith("file://")) {
          content = CrawlerToolkit.loadFile(mContentAsFile);
        } else if( mUrl.startsWith("smb://")) {
          content =  loadSmbFile(mUrl); 
        } else if( mUrl.startsWith("imap://") || mUrl.startsWith("imaps://")) {
          content =  loadIMAPMessage(mUrl); 
        }
        FILE_LOADING_PROFILER.stopMeasuring(content.length);
        return content;
      }
      catch (RegainException exc) {
        FILE_LOADING_PROFILER.abortMeasuring();
        throw new RegainException("Loading Document for url: " + mUrl + " failed: "
          + mContentAsFile, exc);
      }
    }

    return mContent;
  }



  /**
   * Gibt den Inhalt des Dokuments als String zurück.
   * <p>
   * Dieser String wird erst bei der ersten Abfrage erzeugt und dann gecached.
   *
   * @return Der Inhalt des Dokuments als String.
   * @throws RegainException Wenn das Dokument nicht geladen werden konnte.
   */
  public String getContentAsString() throws RegainException {
    if (mContentAsString == null) {
      mContentAsString = new String(getContent());
    }

    return mContentAsString;
  }


  /**
   * Gets the content of the document as stream. The stream must be closed by
   * the caller.
   * 
   * @return The content of the document as stream.
   * @throws RegainException If creating the stream failed.
   */
  public InputStream getContentAsStream()
    throws RegainException
  {
    if (mContent != null) {
      return new ByteArrayInputStream(mContent);
    } else {
      // This document must be a file
      if(mUrl.startsWith("file://")){
        try {
          return new FileInputStream(mContentAsFile);
        
        } catch (Throwable thr) {
          throw new RegainException("Creating stream for file failed: " +
              mContentAsFile, thr);
        }
      } else if(mUrl.startsWith("smb://")) {
        try {
           SmbFile smbFile = RegainToolkit.urlToSmbFile(mUrl);
           return smbFile.getInputStream();
           
        } catch (Throwable thr) {
          throw new RegainException("Creating stream for file failed: " +
              mContentAsFile, thr);
        }
      } else {
        throw new RegainException("Creating stream for unknown file protocoll failed.");
        
      }
    }
  }


  /**
   * Schreibt den Inhalt des Dokuments in eine Datei.
   *
   * @param file Die Datei in die geschrieben werden soll.
   * @throws RegainException Wenn das Schreiben fehl schlug.
   */
  public void writeToFile(File file) throws RegainException {
    try {
      RegainToolkit.writeToFile(getContent(), file);

      if (mContentAsFile == null) {
        // Falls das Dokument in Dateiform ben�tigt wird, dann diese Datei
        // nutzen.
        mContentAsFile = file;
      }
    }
    catch (RegainException exc) {
      throw new RegainException("Creating file that contains the "
        + "document from '" + mUrl + "' failed", exc);
    }
  }


  /**
   * Gibt den Datei des Dokuments zurück. Falls das Dokument nicht als Datei
   * existiert, wird eine tempor�re Datei erzeugt.
   *
   * @return Die Datei des Dokuments.
   * @throws RegainException Wenn entweder keine tempor�re Datei erstellt werden
   *         konnte oder wenn nicht in die tempor�re Datei geschrieben werden
   *         konnte.
   */
  public File getContentAsFile() throws RegainException {
    return getContentAsFile(false);
  }


  /**
   * Gibt den Datei des Dokuments zurück. Falls das Dokument nicht als Datei
   * existiert, wird eine tempor�re Datei erzeugt.
   *
   * @param forceTempFile Bigt an, ob erzwungen werden soll, dass eine
   *        tempor�re Datei erzeugt wird. Auf diese Weise kann man sicher sein,
   *        dass die Datei von niemandem ge�ffnet ist.
   * @return Die Datei des Dokuments.
   * @throws RegainException Wenn entweder keine tempor�re Datei erstellt werden
   *         konnte oder wenn nicht in die tempor�re Datei geschrieben werden
   *         konnte.
   */
  public File getContentAsFile(boolean forceTempFile) throws RegainException {
    if ((mContentAsFile == null) || (forceTempFile && ! mContentAsFileIsTemporary)) {
      // Das Dokument wurde via HTTP geladen
      // -> Inhalt in eine Datei schreiben

      // Determine the file extension
      String extension = "";
      URL url;
      String path;
      if( mUrl.toLowerCase().startsWith("http") | mUrl.toLowerCase().startsWith("https") ) {
        try {
          url = new URL(mUrl);
          path = url.getPath();
          // Handles urls like http://www.thtesche.com/ an http://www.thtesche.com/blog/
          if( (path.length()==0 
                  && (url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https") ))
                  || path.endsWith("/")) {
            path = "index.html";
          }
        } catch (MalformedURLException ex) {
           mLog.debug("Couldn't create URL", ex);
           path = mUrl;
        }

        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1 || path.length()-lastDot>=6 ) {
          extension = "";
        } else {
          extension = path.substring(lastDot);
        }
      } else if( mUrl.toLowerCase().startsWith("imap") | mUrl.toLowerCase().startsWith("imaps") ) {
        // @todo: consider extensions for attachments too
        extension = ".mht";
        
      }
      
      // Get an unused file
      File tmpFile;
      try {
        tmpFile = File.createTempFile("lucenesearch_", extension);
      }
      catch (IOException exc) {
        throw new RegainException("Getting temporary File failed", exc);
      }

      // write content in temporary file
      writeToFile(tmpFile);

      mContentAsFile = tmpFile;
      mContentAsFileIsTemporary = true;
    }

    return mContentAsFile;
  }


  /**
   * Gibt alle genutzten System-Ressourcen, wie tempor�re Dateien, wieder frei.
   * <p>
   * Ressourcen der VM, wie z.B. Arrays, werden nicht freigegeben. Das soll der
   * GarbageCollector erledigen.
   */
  public void dispose() {
    if ((mContentAsFile != null) && mContentAsFileIsTemporary) {
      if (mLog.isDebugEnabled()) {
        mLog.debug("Deleting temporary file: " + mContentAsFile.getAbsolutePath());
      }
      if (! mContentAsFile.delete()) {
        mContentAsFile.deleteOnExit();
        mLog.debug("Deleting temporary file failed: " + mContentAsFile.getAbsolutePath() + 
                "File will be deleted on program exit.");
      }
    }
  }


  /**
   * Gets the String representation of this class.
   *
   * @return The String representation of this class.
   */
  @Override
  public String toString() {
    return getUrl();
  }

  /**
   * Gets the mimetype of this document.
   *
   * @return The mimetype
   */
  public String getMimeType() {
    return mMimeType;
  }

  /**
   * Sets the mimetype of this document.
   */
  public void setMimeType(String mMimeType) {
    this.mMimeType = mMimeType;
  }

  /**
   * Gets the information wether the document contains links
   * @return true if the document contains at least one link
   */
  public boolean hasLinks() {
    if( this.mLinks.isEmpty() )
      return false;
    else
      return true;
  }
  
  /** 
   * Adds a single link to the Hashmap of links
   */
  public void addLink( String url, String linkText ) {
    this.mLinks.put(url,linkText);
  }
  /**
   * Gets the links
   * @return Hashmap of links. key is the URL, value the link text
   */
  public HashMap getLinks(){
    return mLinks;
  }
}
