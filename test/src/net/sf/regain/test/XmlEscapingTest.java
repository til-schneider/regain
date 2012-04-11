package net.sf.regain.test;

import java.io.File;
import java.io.OutputStream;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class XmlEscapingTest extends TestCase
{
  protected static Logger mLog = Logger.getLogger(CrawlerPluginTest.class);
  private static final String LOG4J_CONFIG_FILE = "test/log4j.properties";

  static {
    System.setProperty("log4j.configuration", LOG4J_CONFIG_FILE);

    File logConfigFile = new File(LOG4J_CONFIG_FILE);
    if (!logConfigFile.exists()) {
      System.out.println("ERROR: Logging configuration file not found: " + logConfigFile.getAbsolutePath());
      System.exit(1); // Abort
    }

    PropertyConfigurator.configureAndWatch(logConfigFile.getAbsolutePath(), 10 * 1000);
  }

  private PseudoPageResponse resp;

  @Override
  public void setUp()
  {
    resp = new PseudoPageResponse();
  }
  
  public void testXmlEscaping() throws Exception
  {
    resp.setEscapeType("xml");
  
    resp.print("&");
    assertEquals("&amp;", resp.write);
    resp.print("<");
    assertEquals("&lt;", resp.write);
    resp.print(">");
    assertEquals("&gt;", resp.write);
    resp.print("\"");
    assertEquals("&quot;", resp.write);

    resp.print("\u0008");
    assertEquals("", resp.write);
    
    // Do not escape HTML entities!
    resp.print("ß");
    assertEquals("ß", resp.write);
  }
  
  public void testHtmlEscaping() throws Exception
  {
    resp.setEscapeType("html");
    
    resp.print(">");
    assertEquals("&gt;", resp.write);
    resp.print("ß");
    assertEquals("&szlig;", resp.write);

    resp.print("\u0008");
    assertEquals("", resp.write);
}
  
  public class PseudoPageResponse extends PageResponse
  {
    public String write = null;
    
    public void rawPrint(String text) throws RegainException
    {
        write = text;
    }

    /* Stub methods */
    
    public String getEncoding() throws RegainException
    {
      return null;
    }

    public void setHeader(String name, String value) throws RegainException
    {
    }

    public void setHeaderAsDate(String name, long value) throws RegainException
    {
    }

    public OutputStream getOutputStream() throws RegainException
    {
      return null;
    }

    public void sendRedirect(String url) throws RegainException
    {
    }

    public void sendError(int errorCode) throws RegainException
    {
    }
  }
}