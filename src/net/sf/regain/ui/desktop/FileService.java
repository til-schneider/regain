package net.sf.regain.ui.desktop;

import java.io.File;

import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.simple.SimplePageRequest;
import net.sf.regain.util.sharedtag.simple.SimplePageResponse;
import simple.http.Request;
import simple.http.Response;
import simple.http.load.BasicService;
import simple.http.serve.Context;

/**
 * A simpleweb service providing files. For security reasons this service only
 * provides files that are in the index.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FileService extends BasicService {

  /**
   * Creates a new instance of FileService.
   * 
   * @param context The context of this service.
   */
  public FileService(Context context) {
    super(context);
  }


  /**
   * Processes a request.
   * 
   * @param req The request.
   * @param resp The response.
   * @throws Exception If executing the JSP page failed.
   */
  public void process(Request req, Response resp) throws Exception {
    // Check whether this request comes from localhost
    boolean localhost = req.getInetAddress().isLoopbackAddress();
    if (! localhost) {
      // This request does not come from localhost -> Send 403 Forbidden
      handle(req, resp, 403);
    }

    // Extract the file name
    String filename = context.getRequestPath(req.getURI());
    int filePos = filename.indexOf("file/");
    filename = RegainToolkit.urlDecode(filename.substring(filePos + 5));
    
    // Restore the double slashes
    filename = RegainToolkit.replace(filename, "\\", "/");
    
    // Assemble the file URL
    String fileUrl = RegainToolkit.fileNameToUrl(filename);
    
    // Check the filename
    if (SearchToolkit.allowFileAccess(new SimplePageRequest(req), fileUrl)) {
      // This file is allowed -> Send it
      processFile(req, resp, RegainToolkit.urlToFile(fileUrl));
    } else {
      // This file is not allowed -> Send 403 Forbidden
      handle(req, resp, 403);
    }
  }


  /**
   * Processes a file request.
   * 
   * @param req The request.
   * @param resp The response.
   * @param file The to send.
   * @throws Exception If executing the JSP page failed.
   */
  private void processFile(Request req, Response resp, File file)
    throws Exception
  {
    PageRequest request = new SimplePageRequest(req);
    PageResponse response = new SimplePageResponse(this, req, resp, null, null);
    
    SearchToolkit.sendFile(request, response, file);
  }

}
