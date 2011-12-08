package net.sf.regain.test.integration;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;


public class StartDesktopTest extends IntegrationTestCase
{
  private static final long SLEEP_WAIT_MILLIS = 300;
  public void setUp() throws Exception
  {
    environment = copyDefaultConfig();
    
    // -------- Configure Test Version: --------------
    
    addStartDirectoryToCrawlerConfig(new File(environment, "conf/CrawlerConfiguration.xml"), new File(root, "test/testfiles"));
    addExcludePattern(new File(environment, "conf/CrawlerConfiguration.xml"), ".*\\.svn.*"); // TODO: Bug or documentation error? Only works with .*, $ is superflous
    enableDebugLogging(new File(environment, "conf/log4j.properties"));
  }
  
  public void tearDown()
  {
    if (watchdog != null)
      watchdog.destroyProcess();
    
    FileUtils.deleteQuietly(new File(root, "test/testfiles/test2.txt"));
  }
  
  // Given a default configuration
  public void testFirstStart() throws Exception
  {
    // When starting the desktop version
    ByteArrayOutputStream os = startUp("java -jar regain.jar", environment, 20);
    
    // Then it's getting ready within 20 seconds
    String str = waitForContains(os, "Finished crawling");
    
    // Then all documents are indexed successfully (TODO: Sometimes statistics are not shown. Why??)
    assertContains("Crawling had some errors (or statistics not shown)", "Errors:             0", str);
    
    // When querying for "test.doc"
    String response = query("test.doc");
    // Then a document with name "test.doc" is found
    assertContains("There was no URL containing /test.doc", "/test.doc", response);
    // Then a document URL contains the URL Rewriting $$
    assertContains("URL Rewriting didn't seam to work", "file/%24/%24", response);
    
    // When querying for "test.txt"
    response = query("test.txt");
    // Then test.txt can be download
    String regex = "href=\"(.*?txt)\"";
    Matcher matcher = Pattern.compile(regex).matcher(response);
    assertTrue("Did not find any URL for test.txt", matcher.find());
    String urlTestTxt = matcher.group(1);
    
    if (!urlTestTxt.startsWith("http://"))
      urlTestTxt = "http://localhost:8020/" + urlTestTxt;
    String file = getUrlContent(urlTestTxt, true, true);
    assertContains("File contains garbage", "so weiter.Das ist ein Testsatz un", file);
    
    // When querying for "test.txt.svn-base" via XML
    response = queryXML("filename:test.txt.svn-base");
    // Then no files are found
    assertContains("Exclude .svn didn't seem to work", "<count_hits>0</count_hits>", response);
    
    // When adding a file to the index
    FileUtils.copyFile(new File(root, "test/testfiles/test.txt"), new File(root, "test/testfiles/test2.txt"));
    
    getUrlContent("http://localhost:8020/status.jsp?indexaction=start", true, true);
    os.reset();
    str = waitForContains(os, "Finished loading new index.");
    // Then a file was indexed
    assertContains("test2.txt was not index", "Indexed documents:\n  Completed docs: 1 docs" , str);
    // Then this file can be found
    response = queryXML("filename:test2.txt");
    assertContains("test2.txt could not found", "<count_hits>1</count_hits>", response);
  }
  
  private String waitForContains(OutputStream os, String waitForStr)
  {
    String str;
    do
    {
      try {
        Thread.sleep(SLEEP_WAIT_MILLIS);
      } catch (InterruptedException e) { /* ignore */ }
      
      str = os.toString();
      assertProcessAlive(resultHandler, str);
    } 
    while (!str.contains(waitForStr));
    
    return str;
  }

  private String query(String query) throws IOException
  {
    return getUrlContent("http://localhost:8020/search.jsp?query=" + query, true, true); // Urlencode
  }
  private String queryXML(String query) throws IOException
  {
    return getUrlContent("http://localhost:8020/search_xml.jsp?query=" + query, true, true); // Urlencode
  }

}
