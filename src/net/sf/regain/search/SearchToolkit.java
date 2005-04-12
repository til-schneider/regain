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
 *  $RCSfile: SearchToolkit.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/SearchToolkit.java,v $
 *     $Date: 2005/04/11 08:16:25 $
 *   $Author: til132 $
 * $Revision: 1.16 $
 */
package net.sf.regain.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.access.SearchAccessController;
import net.sf.regain.search.config.DefaultSearchConfigFactory;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.search.config.SearchConfig;
import net.sf.regain.search.config.SearchConfigFactory;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * A toolkit for the search JSPs containing helper methods.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchToolkit {

  /** The name of the page context attribute that holds the search query. */
  private static final String SEARCH_QUERY_CONTEXT_ATTR_NAME = "SearchQuery";
  
  /** The name of the page context attribute that holds the SearchContext. */
  private static final String SEARCH_CONTEXT_ATTR_NAME = "SearchContext";

  /** The name of the page context attribute that holds the IndexConfig. */
  private static final String INDEX_CONFIG_CONTEXT_ATTR_NAME = "IndexConfig";
  
  /** The prefix for request parameters that contain additional field values. */
  private static final String FIELD_PREFIX = "field.";
  
  /** The configuration of the search mask. */
  private static SearchConfig mConfig;
  
  /** Holds for an extension the mime type. */
  private static HashMap mMimeTypeHash;

  
  /**
   * Gets the IndexConfig from the PageContext.
   * <p>
   * If there is no IndexConfig in the PageContext it is put in the PageContext,
   * so the next call will find it.
   * 
   * @param request The page request where the IndexConfig will be taken
   *        from or put to.
   * @return The IndexConfig for the page the context is for.
   * @throws RegainException If there is no IndexConfig for the specified index.
   */
  public static IndexConfig getIndexConfig(PageRequest request)
    throws RegainException
  {
    IndexConfig config = (IndexConfig) request.getContextAttribute(INDEX_CONFIG_CONTEXT_ATTR_NAME);
    if (config == null) {
      // Load the config (if not yet done)
      loadConfiguration(request);

      // Get the name of the index
      String indexName = request.getParameter("index");
      if (indexName == null) {
        indexName = mConfig.getDefaultIndexName();
      }
      if (indexName == null) {
        throw new RegainException("Request parameter 'index' not specified and " +
            "no default index configured");
      }
      
      // Get the configuration for that index
      config = mConfig.getIndexConfig(indexName);
      if (config == null) {
        throw new RegainException("The configuration does not contain the index '"
            + indexName + "'");
      }

      // Store the IndexConfig in the page context
      request.setContextAttribute(INDEX_CONFIG_CONTEXT_ATTR_NAME, config);
    }
    return config;
  }
  
  
  /**
   * Gets the search query.
   * 
   * @param request The request to get the query from.
   * @return The search query.
   * @throws RegainException If getting the query failed.
   */
  public static String getSearchQuery(PageRequest request)
    throws RegainException
  {
    String queryString = (String) request.getContextAttribute(SEARCH_QUERY_CONTEXT_ATTR_NAME);
    if (queryString == null) {
      // Get the query parameter
      StringBuffer query = new StringBuffer();
      String queryParam = request.getParameter("query");
      if (queryParam != null) {
        query.append(queryParam);
      }
      
      // Append the additional fields to the query
      Enumeration enum = request.getParameterNames();
      while (enum.hasMoreElements()) {
        String paramName = (String) enum.nextElement();
        if (paramName.startsWith(FIELD_PREFIX)) {
          // This is an additional field -> Append it to the query
          String fieldName = paramName.substring(FIELD_PREFIX.length());
          String fieldValue = request.getParameter(paramName);
          
          if (fieldValue != null) {
            fieldValue = fieldValue.trim();
            if (fieldValue.length() != 0) {
              query.append(" " + fieldName + ":\"" + fieldValue + "\"");
            }
          }
        }
      }
      
      queryString = query.toString().trim();
      request.setContextAttribute(SEARCH_QUERY_CONTEXT_ATTR_NAME, queryString);
    }
    
    return queryString;
  }
  

  /**
   * Gets the SearchContext from the PageContext.
   * <p>
   * If there is no SearchContext in the PageContext it is created and put in the
   * PageContext, so the next call will find it.
   *
   * @param request The page request where the SearchContext will be taken
   *        from or put to.
   * @return The SearchContext for the page the context is for.
   * @throws RegainException If the SearchContext could not be created.
   * @see SearchContext
   */
  public static SearchContext getSearchContext(PageRequest request)
    throws RegainException
  {
    SearchContext context = (SearchContext) request.getContextAttribute(SEARCH_CONTEXT_ATTR_NAME);
    if (context == null) {
      // Get the index config
      IndexConfig indexConfig = getIndexConfig(request);

      // Get the query
      String query = getSearchQuery(request);
      
      // Get the groups the current user has reading rights for
      String[] groupArr = null;
      SearchAccessController accessController = indexConfig.getSearchAccessController();
      if (accessController != null) {
        groupArr = accessController.getUserGroups(request);
        
        if (groupArr == null) {
          // NOTE: The SearchAccessController should never return null, but for
          //       security reasons we check it. Because if the groupArr is
          //       null, the access control is disabled.
          groupArr = new String[0];
        }
      }
      
      // Create the SearchContext and store it in the page context
      context = new SearchContext(indexConfig, query, groupArr);
      request.setContextAttribute(SEARCH_CONTEXT_ATTR_NAME, context);
    }

    return context;
  }


  /**
   * Extracts the file URL from a request path.
   * 
   * @param requestPath The request path to extract the file URL from.
   * @return The extracted file URL.
   * @throws RegainException If extracting the file URL failed.
   * 
   * @see net.sf.regain.search.sharedlib.hit.LinkTag
   */
  public static String extractFileUrl(String requestPath)
    throws RegainException
  {
    int filePos = requestPath.indexOf("file/");
    String filename = RegainToolkit.urlDecode(requestPath.substring(filePos + 5));
    
    // Restore the double slashes
    filename = RegainToolkit.replace(filename, "\\", "/");
    
    // Assemble the file URL
    return RegainToolkit.fileNameToUrl(filename);
  }


  /**
   * Decides whether the remote access to a file should be allowed.
   * <p>
   * The access is granted if the file is in the index.
   * 
   * @param request The request that holds the used index.
   * @param fileUrl The URL to file to check.
   * @return Whether the remote access to a file should be allowed.
   * @throws RegainException If checking the file failed.
   */
  public static boolean allowFileAccess(PageRequest request, String fileUrl)
    throws RegainException
  {
    IndexConfig config = getIndexConfig(request);
    
    IndexSearcherManager manager = IndexSearcherManager.getInstance(config.getDirectory());
    
    // Check whether the document is in the index
    Term urlTerm = new Term("url", fileUrl);
    Query query = new TermQuery(urlTerm);
    Hits hits = manager.search(query);
    
    // Allow the access if we found the file in the index
    return hits.length() > 0;
  }


  /**
   * Sends a file to the client.
   *
   * @param request The request.
   * @param response The response.
   * @param file The file to send.
   * @throws RegainException If sending the file failed.
   */
  public static void sendFile(PageRequest request, PageResponse response, File file)
    throws RegainException
  {
    long lastModified = file.lastModified();
    if (lastModified < request.getHeaderAsDate("If-Modified-Since")) {
      // The browser can use the cached file
      response.sendError(304);
    } else {
      response.setHeaderAsDate("Date", System.currentTimeMillis());
      response.setHeaderAsDate("Last-Modified", lastModified);
    
      // TODO: Make this configurable
      if (mMimeTypeHash == null) {
        // Source: http://de.selfhtml.org/diverses/mimetypen.htm
        mMimeTypeHash = new HashMap();
        mMimeTypeHash.put("html", "text/html");
        mMimeTypeHash.put("htm",  "text/html");
        mMimeTypeHash.put("gif",  "image/gif");
        mMimeTypeHash.put("jpg",  "image/jpeg");
        mMimeTypeHash.put("jpeg", "image/jpeg");
        mMimeTypeHash.put("png",  "image/png");
        mMimeTypeHash.put("js",   "text/javascript");
        mMimeTypeHash.put("txt",  "text/plain");
        mMimeTypeHash.put("pdf",  "application/pdf");
        mMimeTypeHash.put("xls",  "application/msexcel");
        mMimeTypeHash.put("doc",  "application/msword");
        mMimeTypeHash.put("ppt",  "application/mspowerpoint");
        mMimeTypeHash.put("rtf",  "text/rtf");
        
        // Source: http://framework.openoffice.org/documentation/mimetypes/mimetypes.html
        mMimeTypeHash.put("sds",  "application/vnd.stardivision.chart");
        mMimeTypeHash.put("sdc",  "application/vnd.stardivision.calc");
        mMimeTypeHash.put("sdw",  "application/vnd.stardivision.writer");
        mMimeTypeHash.put("sgl",  "application/vnd.stardivision.writer-global");
        mMimeTypeHash.put("sda",  "application/vnd.stardivision.draw");
        mMimeTypeHash.put("sdd",  "application/vnd.stardivision.impress");
        mMimeTypeHash.put("sdf",  "application/vnd.stardivision.math");
        mMimeTypeHash.put("sxw",  "application/vnd.sun.xml.writer");
        mMimeTypeHash.put("stw",  "application/vnd.sun.xml.writer.template");
        mMimeTypeHash.put("sxg",  "application/vnd.sun.xml.writer.global");
        mMimeTypeHash.put("sxc",  "application/vnd.sun.xml.calc");
        mMimeTypeHash.put("stc",  "application/vnd.sun.xml.calc.template");
        mMimeTypeHash.put("sxi",  "application/vnd.sun.xml.impress");
        mMimeTypeHash.put("sti",  "application/vnd.sun.xml.impress.template");
        mMimeTypeHash.put("sxd",  "application/vnd.sun.xml.draw");
        mMimeTypeHash.put("std",  "application/vnd.sun.xml.draw.template");
        mMimeTypeHash.put("sxm",  "application/vnd.sun.xml.math");
        mMimeTypeHash.put("odt",  "application/vnd.oasis.opendocument.text");
        mMimeTypeHash.put("ott",  "application/vnd.oasis.opendocument.text-template");
        mMimeTypeHash.put("oth",  "application/vnd.oasis.opendocument.text-web");
        mMimeTypeHash.put("odm",  "application/vnd.oasis.opendocument.text-master");
        mMimeTypeHash.put("odg",  "application/vnd.oasis.opendocument.graphics");
        mMimeTypeHash.put("otg",  "application/vnd.oasis.opendocument.graphics-template");
        mMimeTypeHash.put("odp",  "application/vnd.oasis.opendocument.presentation");
        mMimeTypeHash.put("otp",  "application/vnd.oasis.opendocument.presentation-template");
        mMimeTypeHash.put("ods",  "application/vnd.oasis.opendocument.spreadsheet");
        mMimeTypeHash.put("ots",  "application/vnd.oasis.opendocument.spreadsheet-template");
        mMimeTypeHash.put("odc",  "application/vnd.oasis.opendocument.chart");
        mMimeTypeHash.put("odf",  "application/vnd.oasis.opendocument.formula");
        mMimeTypeHash.put("odb",  "application/vnd.oasis.opendocument.database");
        mMimeTypeHash.put("odi",  "application/vnd.oasis.opendocument.image");
      }
      
      // Set the MIME type
      String filename = file.getName();
      int lastDot = filename.lastIndexOf('.');
      if (lastDot != -1) {
        String extension = filename.substring(lastDot + 1);
        String mimeType = (String) mMimeTypeHash.get(extension);
        if (mimeType != null) {
          response.setHeader("Content-Type", mimeType);
        }
      }
      
      // Send the file
      OutputStream out = null;
      FileInputStream in = null;
      try {
        out = response.getOutputStream();
        in = new FileInputStream(file);
        RegainToolkit.pipe(in, out);
      }
      catch (IOException exc) {
        throw new RegainException("Sending file failed: " + file.getAbsolutePath(), exc);
      }
      finally {
        if (in != null) {
          try { in.close(); } catch (IOException exc) {}
        }
        if (out != null) {
          try { out.close(); } catch (IOException exc) {}
        }
      }
    }
  }


  /**
   * Loads the configuration of the search mask.
   * <p>
   * If the configuration is already loaded, nothing is done.
   * 
   * @param request The page request. Used to get the "configFile" init
   *        parameter, which holds the name of the configuration file.
   * @throws RegainException If loading failed.
   */
  private static void loadConfiguration(PageRequest request)
    throws RegainException
  {
    if (mConfig == null) {
      // Create the factory
      String factoryClassname = request.getInitParameter("searchConfigFactoryClass");
      String factoryJarfile   = request.getInitParameter("searchConfigFactoryJar");
      if (factoryClassname == null) {
        factoryClassname = DefaultSearchConfigFactory.class.getName();
      }
      SearchConfigFactory factory = (SearchConfigFactory)
        RegainToolkit.createClassInstance(factoryClassname, SearchConfigFactory.class, factoryJarfile);
      
      // Create the config
      mConfig = factory.createSearchConfig(request);
    }
  }
  
}
