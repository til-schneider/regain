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
 *  $RCSfile: SharedTagResource.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/util/sharedtag/simple/SharedTagResource.java,v $
 *     $Date: 2005/04/02 17:29:03 $
 *   $Author: til132 $
 * $Revision: 1.9 $
 */
package net.sf.regain.util.sharedtag.simple;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.log4j.Logger;

import simple.http.Request;
import simple.http.Response;
import simple.http.serve.BasicResource;
import simple.http.serve.Context;

/**
 * A simpleweb resource representing one JSP page.
 *
 * @see simple.http.serve.Resource
 * @author Til Schneider, www.murfman.de
 */
public class SharedTagResource extends BasicResource {
  
  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(SharedTagResource.class);
  
  /** The base directory where the provided files are located. */
  private static File mBaseDir;
  
  /** The root executer holding the parsed JSP page. */
  private Executer mRootTagExecuter;
  
  
  /**
   * Creates a new instance of SharedTagResource.
   * 
   * @param context The context of this resource.
   * @param root The root executer holding the parsed JSP page.
   * @throws RegainException If parsing the JSP file failed.
   */
  public SharedTagResource(Context context, Executer root) throws RegainException {
    super(context);
    
    mRootTagExecuter = root;
  }


  /**
   * Processes a request.
   * 
   * @param req The request.
   * @param resp The response.
   * @throws Exception If executing the JSP page failed.
   */
  protected synchronized void process(Request req, Response resp) throws Exception {
    process(req, resp, mRootTagExecuter, null);
  }
  
  
  /**
   * Processes a request.
   * 
   * @param req The request.
   * @param resp The response.
   * @param executer The executer to use.
   * @param error The error to show. Is <code>null</code> if no error page is
   *        shown.
   * @throws Exception If executing the JSP page failed.
   */
  private synchronized void process(Request req, Response resp,
    Executer executer, Throwable error)
    throws Exception
  {
    String encoding = "utf-8";
        
    // Write the page to a buffer first
    // If an exception should be thrown the user gets a clear error message
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(stream, false, encoding);

    PageRequest request = new SimplePageRequest(req);
    PageResponse response = new SimplePageResponse(this, req, resp, printStream, encoding);

    // Add the error to the page attributes
    if (error != null) {
      request.setContextAttribute("page.exception", error);
    }
    
    try {
      executer.execute(request, response);
    }
    catch (RedirectException exc) {
      // Send a redirect
      resp.set("Location", exc.getUrl());
      handle(req, resp, 303);
      return;
    }
    catch (Exception exc) {
      mLog.error("Processing page failed", exc);
      if (error == null) {
        // This is the normal page -> Show the error page
        try {
          Executer errorExecuter = loadErrorPage();
          process(req, resp, errorExecuter, exc);
        }
        catch (RegainException loadingExc) {
          mLog.error("Processing error page failed", loadingExc);
          
          // Throw the original error, so a simple error page is shown
          throw exc;
        }
      } else {
        // This already is the error page -> Show a simple error
        throw exc;
      }
    }
    finally {
      printStream.close();
      stream.close();
    }
    
    // The page has been generated without exception -> Send it to the user
    resp.set("Content-Type", "text/html; charset=" + encoding);
    PrintStream pageStream = resp.getPrintStream();
    try {
      stream.writeTo(pageStream);
    }
    finally {
      pageStream.close();
    }
  }


  /**
   * Loads the error page executer.
   * 
   * @return The error page executer.
   * @throws RegainException If loading the error page executer failed.
   */
  private Executer loadErrorPage() throws RegainException {
    if (mBaseDir == null) {
      mBaseDir = new File(context.getBasePath());
    }
    
    return new ExecuterParser().parse(mBaseDir, "errorpage.jsp");
  }
  
}
