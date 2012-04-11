package net.sf.regain.ui.desktop;

import net.sf.regain.RegainToolkit;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.simple.SharedTagResource;
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
    if (!localhost && !DesktopToolkit.getDesktopConfig().getExternalAccessAllowed() ) {
      // This request does not come from localhost -> Send 403 Forbidden
      handle(req, resp, 403);
    }

    // Create a shared wrapper
    PageRequest request = new SimplePageRequest(req);
    PageResponse response = new SimplePageResponse(this, req, resp, null, null);

    // Get the request path (Without GET-Parameters)
    // NOTE: We don't use context.getRequestPath for this, because it decodes
    //       the URL, but we want to decode it ourselves using our encoding
    String requestPath = req.getURI();
    int paramsStart = requestPath.indexOf('?');
    if (paramsStart != -1) {
      requestPath = requestPath.substring(0, paramsStart);
    }

    // Extract the file URL
    String fileUrl = SearchToolkit.extractFileUrl(requestPath,
            SharedTagResource.SIMPLE_TAG_ENCODING);
        
    // Check the file URL
    boolean allow = SearchToolkit.allowFileAccess(request, fileUrl);
    
    if ("1".equals(request.getParameter("askPermission")))
    {
      response.setHeader("Content-Type", "text/plain");
      response.printNoHtml(Boolean.toString(allow));
      handle(req, resp, 200);
      return;
    }
    
    if (allow) {
      // This file is allowed -> Send it
      SearchToolkit.sendFile(request, response, RegainToolkit.urlToFile(fileUrl));
    } else {
      // This file is not allowed -> Send 403 Forbidden
      handle(req, resp, 403);
    }
  }

}
