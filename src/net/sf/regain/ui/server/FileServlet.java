/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2012-01-07 12:22:45 +0100 (Sa, 07 Jan 2012) $
 *   $Author: benjaminpick $
 * $Revision: 557 $
 */
package net.sf.regain.ui.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.taglib.JspPageRequest;
import net.sf.regain.util.sharedtag.taglib.JspPageResponse;

/**
 * A servlet providing files. For security reasons this service only provides
 * files that are in the index.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class FileServlet extends HttpServlet {

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    handleRequest(req, resp);
  }


  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    handleRequest(req, resp);
  }


  /**
   * Handles a HTTP request.
   * 
   * @param req The request.
   * @param resp The response.
   * @throws ServletException If handling the request failed.
   * @throws IOException If writing to the result page failed.
   */
  private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    // Create a page context
    JspFactory factory = JspFactory.getDefaultFactory();
    String errorPageURL = null;
    int bufferSize = (JspWriter.DEFAULT_BUFFER <= 0) ? 1024 : JspWriter.DEFAULT_BUFFER;
    boolean needsSession = false;
    boolean autoFlush = true;
    PageContext pageContext = null;
    try {
      req.setCharacterEncoding("UTF-8");
      pageContext = factory.getPageContext(this, req, resp,
        errorPageURL, needsSession, bufferSize, autoFlush);
    
      // Create a shared wrapper
      PageRequest request = new JspPageRequest(pageContext);
      PageResponse response = new JspPageResponse(pageContext);

      // Extract the file name
      String encoding = "utf-8"; // We use utf-8 in our JSPs
      String fileUrl = SearchToolkit.extractFileUrl(req.getRequestURI(), encoding);

      // Check the file
      try {
        if (SearchToolkit.allowFileAccess(request, fileUrl)) {
          // Access allowed -> Send the file
          SearchToolkit.sendFile(request, response, RegainToolkit.urlToFile(fileUrl));
        } else {
          // Access not allowed -> Send 403 Forbidden
          resp.sendError(403);
        }
      }
      catch (RegainException exc) {
        throw new ServletException("Checking file access failed: " + fileUrl, exc);
      }
    }
    catch (Exception exc) {
      if (pageContext != null)
        pageContext.handlePageException(exc);
    } finally {
      factory.releasePageContext(pageContext);
    }
  }

}
