package net.sf.regain.test.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

public class IntegrationTestCase extends TestCase
{
  protected static final File root = new File(System.getProperty("user.dir"));
  protected static final File runtime = new File(root, "build/runtime");
  protected static final File runtimeTest = new File(root, "test/build/runtime");
  protected static final long SLEEP_WAIT_MILLIS = 300;

  protected File environment;  
  protected DefaultExecuteResultHandler resultHandler;
  protected ExecuteWatchdog watchdog;

  /**
   * Prepare a test environment
   * @throws IOException
   */
  protected File copyDefaultConfig() throws IOException
  {
    if (fileNewerThan(new File(runtime, "desktop/"+getPlatform()+"/regain.jar"), new File(runtimeTest, "desktop/"+getPlatform()+"/regain.jar")))
    {
      FileUtils.copyDirectory(new File(runtime, "desktop/"+getPlatform()), new File(runtimeTest, "desktop/"+getPlatform()));
      FileUtils.copyDirectory(new File(root, "web/common"), new File(runtimeTest, "desktop/"+getPlatform() + "/web"));
    }
    
    // TODO : Test under Windows
    File env = new File(runtimeTest, "desktop/"+getPlatform());
    
    cleanDirectory(new File(env, "log"));
    cleanDirectory(new File(env, "searchindex"));
    
    cleanDirectoryKeepSubdirectories(new File(env, "conf"));
    FileUtils.copyDirectory(new File(env, "conf/default"), new File(env, "conf"));
    
    return env;
  }
  
  private void cleanDirectory(File dir) throws IOException
  {
    dir.mkdirs();
    FileUtils.cleanDirectory(dir);
  }

  private boolean fileNewerThan(File file, File file2)
  {
    return file.lastModified() > file2.lastModified();
  }

  private String getPlatform()
  {
    String os = System.getProperty("os.name");
    if (os.contains("indows"))
       return "windows";
    else
      return "linux";
  }

  protected void enableDebugLogging(File debugFile) throws IOException
  {
    ArrayList<String> lines = new ArrayList<String>();
    lines.add("");
    lines.add("log4j.category.net.sf.regain=DEBUG");
    lines.add("log4j.category.net.sf.regain.crawler.Crawler=DEBUG");
    FileUtils.writeLines(debugFile, null, lines, null, true);
  }
  
  protected void enableCrawlerPlugin(File crawlerConfig, String pluginName, boolean enable) throws IOException
  {
    List<String> lines = FileUtils.readLines(crawlerConfig);
    
    int i = 0;
    for (String str : lines)
    {
      if (str.contains(pluginName))
      {
        String before = lines.get(i - 1);
        if (before != null && before.contains("enabled=\""))
        {
          before = before.replaceFirst("enabled=\"[a-z]*\"", "enabled=\"" + Boolean.toString(enable) + "\"");
          lines.set(i - 1, before);
        }
      }
      i++;
    }
   FileUtils.writeLines(crawlerConfig, lines);
  }

  protected void addStartDirectoryToCrawlerConfig(File crawlerConfig, File crawlingDir) throws IOException
  {
    String fileURL = crawlingDir.toURI().toURL().toExternalForm();
    fileURL = "file://" + fileURL.substring(5); // Naughty hack. Java is too strict about RFCs, see http://stackoverflow.com/questions/1131273/java-file-touri-tourl-on-windows-file

    String line = "    <start parse=\"true\" index=\"false\">" + fileURL + "</start>";
    
    addLineAfterContains(crawlerConfig, "<startlist>", line);
  }
  
  protected void addExcludePattern(File crawlerConfig, String pattern) throws IOException
  {
    String line = "    <regex>" + pattern + "</regex>";
    addLineAfterContains(crawlerConfig, "<blacklist>", line);
  }

  private void addLineAfterContains(File crawlerConfig, String token, String line) throws IOException
  {
    // Get File
    List<String> lines = FileUtils.readLines(crawlerConfig);
    
    // Find the opening xml tag
    int i = 0;
    for (String singleLine : lines)
    {
      if (singleLine.contains(token))
        break;
      i++;
    }

    // Insert it - doesn't matter if there are several start s
    lines.add(i+1, line);
    
    // Overwrite file
    FileUtils.writeLines(crawlerConfig, lines);
  }

  protected String getUrlContent(String urlString, boolean requireStatusOK, boolean methodGet) throws IOException
  {
      URL url = new URL(urlString);

      HttpURLConnection huc = (HttpURLConnection) url.openConnection();
      
      if (methodGet)
        huc.setRequestMethod("GET");
      else
        huc.setRequestMethod("POST");
      huc.connect();

      int statusCode = huc.getResponseCode();
      if (statusCode != HttpURLConnection.HTTP_OK)
      {
        String stderr = IOUtils.toString(huc.getErrorStream());
        System.out.print(stderr);
      }
      assertEquals("Status Code was not 200 OK (URL: " + urlString + ")", HttpURLConnection.HTTP_OK, statusCode);

      String response = IOUtils.toString(huc.getInputStream());

      return response;
    }

  /**
   * Check if the process is still alive.
   * If not, the unit test has failed.
   * 
   * @param resultHandler Result handler that is keeping track of the respective process
   * @param output        Process output until now (is written to stdout if assertion failed)
   * @throws IllegalStateException  Re-Throw Process Exception
   */
  protected void assertProcessAlive(DefaultExecuteResultHandler resultHandler, String output) throws IllegalStateException
  {
    if (resultHandler.hasResult()) // Process died
    { 
      //String lastOutput = lastLines(output, 3);
      System.out.print(output);
      
      Exception ex = resultHandler.getException();
      int exitValue = resultHandler.getExitValue();
      
      if (output.contains("BindException: Address already in use"))
        fail("Process could not start, port (default: 8020) already occupied. Maybe another instance of regain desktop is running?");
      
      if (exitValue == 143)
        fail("Process timed out, was killed by unit test.");
      else if (exitValue == 1)
        fail("Process indicated an error:" /* + grep(output, "Exception")*/);
      else if (ex == null)
        fail("Process died unexpectantly. Exit Value: " + exitValue);
      else
        throw new IllegalStateException("Process died with an exception (Exit Value: " + exitValue + "):", ex);
    }
  }

 /*
  private String lastLines(String output, int nb)
  {    
    int pos = output.length();
     for (int i = 0; i < nb && pos > 0; i++)
     {
       pos = output.lastIndexOf('\n', pos);
       i++;
     }
     if (pos == -1)
        return output;
     return output.substring(pos);
  }
*/
  
  protected void assertContains(String message, String expected, String actual)
  {
    boolean cond = actual.contains(expected);
    if (!cond)
    {
      System.out.print(actual);
      fail(message);
    }
  }

  /**
   * Wrapper function for commons-exec:
   * Execute a Command as a background process.
   * 
   * @param cmd         Command to execute
   * @param workingDir  Working directory
   * @return  An outputstream that contains the output of the process into stdout/stderr
   * @throws ExecuteException Error during execution
   * @throws IOException    File does not exist, and so could not be executed.
   */
  protected ByteArrayOutputStream startUp(String cmd, File workingDir) throws ExecuteException, IOException
  {
    return startUp(cmd, workingDir, 0);
  }

  /**
   * Wrapper function for commons-exec:
   * Execute a Command as a background process.
   * 
   * @param cmd         Command to execute
   * @param workingDir  Working directory
   * @param timeout     Kill process after this time (in sec) (0: no timeout)
   * @return  An outputstream that contains the output of the process into stdout/stderr
   * @throws ExecuteException Error during execution
   * @throws IOException    File does not exist, and so could not be executed.
   */
  protected ByteArrayOutputStream startUp(String cmd, File workingDir, int timeout) throws ExecuteException, IOException
  {
    CommandLine cmdLine = CommandLine.parse(cmd);
    Executor executor = new DefaultExecutor();
    resultHandler = new DefaultExecuteResultHandler();
    
    if (timeout > 0)
    {
      watchdog = new ExecuteWatchdog(1000 * timeout);
      executor.setWatchdog(watchdog);
    }
    
    /* No live-streaming needed
    PipedOutputStream os = new PipedOutputStream();
    InputStream is = new PipedInputStream(os);
    executor.setStreamHandler(new PumpStreamHandler(os));
    */
    
    ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
    executor.setStreamHandler(new PumpStreamHandler(os));
    
    executor.setWorkingDirectory(workingDir);
    executor.execute(cmdLine, resultHandler);
    
    return os;
  }

  /**
   * Idle until outputstream contains a magic String.
   * 
   * @param os          Outputstream to check periodically.
   * @param waitForStr  Magic string to wait for
   * @param maxMillisWait Abort waiting if we waited longer than this.
   *                      If 0, wait infinitely.
   * @return  Content of output stream.
   * @see SLEEP_WAIT_MILLIS Duration of sleeping period in ms.
   */
  protected String waitForContains(OutputStream os, String waitForStr, long maxMillisWait)
  {
    String str;
    long begin = System.currentTimeMillis();
    
    do
    {
      try {
        Thread.sleep(SLEEP_WAIT_MILLIS);
      } catch (InterruptedException e) { /* ignore */ }
      str = os.toString();
  
      if (maxMillisWait > 0)
      {
        long now = System.currentTimeMillis();
        if (now - begin > maxMillisWait)
          return str;
      }
      
      assertProcessAlive(resultHandler, str);
    } 
    while (!str.contains(waitForStr));
    
    return str;
  }

  /**
   * Idle until outputstream contains a magic String.
   * 
   * @param os          Outputstream to check periodically.
   * @param waitForStr  Magic string to wait for
   * @return  Content of output stream.
   * @see SLEEP_WAIT_MILLIS Duration of sleeping period in ms.
   */
  protected String waitForContains(OutputStream os, String waitForStr)
  {
    return waitForContains(os, waitForStr, 0);
  }

  /**
   * Clean a directoy without deleting it,
   * but don't delete subdirectories and their content.
   * 
   * @param directory directory to clean
   * @throws IOException in case cleaning is unsuccessful
   * @see FileUtils.cleanDirectory(File)
   */
  protected static void cleanDirectoryKeepSubdirectories(File directory) throws IOException
  {
    directory.mkdirs();
    
    File[] files = directory.listFiles();
    if (files == null) {  // null if security restricted
        throw new IOException("Failed to list contents of " + directory);
    }
  
    IOException exception = null;
    for (File file : files) {
      // ADD Check:
        if (file.isDirectory())
          continue;
      // End ADD (The rest is copied from commons-io)

        try {
            FileUtils.forceDelete(file);
        } catch (IOException ioe) {
            exception = ioe;
        }
    }
    
    if (null != exception) {
      throw exception;
    }
  }
}
