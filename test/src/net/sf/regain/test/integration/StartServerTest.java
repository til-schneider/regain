package net.sf.regain.test.integration;


import java.io.*;

import org.apache.commons.io.FileUtils;


public class StartServerTest extends IntegrationTestCase
{
  public void setUp() throws Exception
  {
    String destFolder = "crawler";
    FileUtils.copyDirectory(new File(runtime, destFolder), new File(runtimeTest, destFolder));
    environment = prepareTestEnvironment(destFolder);

    // -------- Configure Test Version: --------------

    File crawlerConfig = new File(environment, "CrawlerConfiguration.xml");
    addStartDirectoryToCrawlerConfig(crawlerConfig, new File(root, "test/testfiles"));
    addExcludePattern(crawlerConfig, ".*/\\.svn/.*");
    enableCrawlerPlugin(crawlerConfig, "FilesizeFilterPlugin", true);
    enableDebugLogging(new File(environment, "log4j.properties"));
  }

  public void tearDown()
  {
    if (watchdog != null)
      watchdog.destroyProcess();
  }

  // Given a default configuration
  public void testCrawlerStart() throws Exception
  {
    // When running the crawler
    ByteArrayOutputStream os = startUp("java -jar regain-crawler.jar", environment, 40, true);

    // Then it's getting ready within 20 seconds
    String str = os.toString();
    assertContains("Crawling did not finish correctly", "Finished crawling", str);

    // Then all documents are indexed successfully
    assertContains("Crawling had some errors (or statistics not shown)", "Errors:             0", str);
  }

  public void testTomcat() throws Exception
  {
    // TODO: Test
  }

}
