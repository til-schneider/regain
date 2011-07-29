package net.sf.regain.test;

import java.io.File;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import net.sf.regain.crawler.plugin.AbstractCrawlerPlugin;
import net.sf.regain.crawler.plugin.CrawlerPluginManager;
import junit.framework.TestCase;

public class CrawlerPluginTest extends TestCase {

	protected static Logger mLog = Logger.getLogger(CrawlerPluginTest.class);
	
	private static final String LOG4J_CONFIG_FILE = "test/log4j.properties";
	static
	{
		System.setProperty("log4j.configuration", LOG4J_CONFIG_FILE);

		File logConfigFile = new File(LOG4J_CONFIG_FILE);
		if (!logConfigFile.exists()) {
			System.out.println("ERROR: Logging configuration file not found: " + logConfigFile.getAbsolutePath());
			System.exit(1); // Abort
		}

		PropertyConfigurator.configureAndWatch(logConfigFile.getAbsolutePath(), 10 * 1000);
		mLog.info("Logging initialized");
	}
	
	private CrawlerPluginManager pluginManager;

	public void setUp()
	{
		pluginManager = CrawlerPluginManager.getInstance();
		pluginManager.clear();
		TestPlugin.called = new int[10];
		TestPlugin.nbCalled = 0;
	}
	
	// ------------- Tests -------------------
	
	public void testArgumentNull()
	{
		try {
			pluginManager.eventDeclineURL(null);
			fail("Null may not be passed as argument to the plugin manager, yet no exception was thrown");
		} catch (IllegalArgumentException e) {
			
		}
	}
	
	public void testOrderDefault()
	{
		/*
		1 x
		2 x 
		3 x
		 */
		pluginManager.registerPlugin(new TestPlugin(1));
		pluginManager.registerPlugin(new TestPlugin(2));
		pluginManager.registerPlugin(new TestPlugin(3));

		callPlugins();
		
		assertArrayEquals("The 3 plugins are not called in the right order", new int[]{1,2,3,0,0,0,0,0,0,0}, TestPlugin.called);
	}
	
	public void testOrderPriority()
	{
		/*
		1 1
		2 2
		3 3
		*/
		pluginManager.registerPlugin(new TestPlugin(3), 3);
		pluginManager.registerPlugin(new TestPlugin(1), 1);
		pluginManager.registerPlugin(new TestPlugin(2), 2); // Should be inserted in between
		
		callPlugins();
		
		assertArrayEquals("The 3 plugins are not called in the right order", new int[]{1,2,3,0,0,0,0,0,0,0}, TestPlugin.called);
	}
	
	public void testOrderBoth()
	{
/*
1 1
2 1
3 2
4 3
5 3
6 x
 */
		pluginManager.registerPlugin(new TestPlugin(4), 3);
		pluginManager.registerPlugin(new TestPlugin(5), 3);
		pluginManager.registerPlugin(new TestPlugin(1), 1);
		pluginManager.registerPlugin(new TestPlugin(2), 1);
		pluginManager.registerPlugin(new TestPlugin(3), 2);
		pluginManager.registerPlugin(new TestPlugin(6));

		callPlugins();
		
		assertArrayEquals("The 6 plugins are not called in the right order", new int[]{1,2,3,4,5,6,0,0,0,0}, TestPlugin.called);
	}


	public void testThrowException()
	{
/*
1 1
2 2 -> Exception
3 3
 */
		pluginManager.registerPlugin(new TestPlugin(1), 1);
		pluginManager.registerPlugin(new TestPlugin(2, true), 2);
		pluginManager.registerPlugin(new TestPlugin(3), 3);

		callPlugins();
		
		assertArrayEquals("The 3 plugins are not all called when throwing exceptions", new int[]{1,2,3,0,0,0,0,0,0,0}, TestPlugin.called);
	}
	
	public void testAllEventsWorking()
	{
		try { pluginManager.eventAcceptURL(null, null); 		} catch (IllegalArgumentException e) {}
		try { pluginManager.eventDeclineURL(null); 				} catch (IllegalArgumentException e) {}
		try { pluginManager.eventAfterPrepare(null, null); 		} catch (IllegalArgumentException e) {}
		try { pluginManager.eventBeforePrepare(null, null); 	} catch (IllegalArgumentException e) {}
		try { pluginManager.eventCreateIndexEntry(null, null);	} catch (IllegalArgumentException e) {}
		try { pluginManager.eventDeleteIndexEntry(null, null);	} catch (IllegalArgumentException e) {}
		try { pluginManager.eventStartCrawling(null); 			} catch (IllegalArgumentException e) {}
		try { pluginManager.eventFinishCrawling(null);			} catch (IllegalArgumentException e) {}
	}

	// ----------- Helper functions ---------------
	
	private void callPlugins() {
		mLog.info(pluginManager.toString());
		pluginManager.eventDeclineURL("");
	}
	
	
	private void assertArrayEquals(String message, int[] expected, int[] actual) {
		if (!arrayIsEqual(expected, actual))
		{
			throw new AssertionError(message +
					"\nExpected: " + array2String(expected) + 
					"\nActual:   " + array2String(actual));
		}
	}

	private boolean arrayIsEqual(int[] a, int[] b)
	{
		if (a.length != b.length)
			return false;
		
		for (int i = 0; i < a.length; i++)
		{
			if (a[i] != b[i])
				return false;
		}
		return true;		
	}
	
	private String array2String(int[] array)
	{
		StringBuilder sb = new StringBuilder(100);
		sb.append("[");
		for (int i = 0; i < array.length; i++)
		{
			sb.append(array[i]);
			if (i + 1 < array.length)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	/*
	private void assertIsSortedAscending(String message, int[] data) {
		if (!isSortedAscending(data))
			
		
	}
	
	private boolean isSortedAscending(int[] data)
	{
		for (int i = 0; i < data.length - 1; i++)
		{
			if (data[i+1] == 0) // End of array
				return true;
			if (data[i] > data[i + 1])
				return false;
		}
		return true;
	}
*/
}

class TestPlugin extends AbstractCrawlerPlugin
{
	private int content;

	private boolean throwException;

	public static int called[];
	public static int nbCalled = 0;
	
	public TestPlugin(int content)
	{
		this(content, false);
	}
	
	public TestPlugin(int content, boolean throwException)
	{
		super();
		this.content = content;
		this.throwException = throwException;
	}
	public String toString()
	{
		return content + "";
	}
	
	public void onDeclineURL(String str)
	{
		called[nbCalled] = content;
		nbCalled++;
		
		if (throwException)
			throw new RuntimeException("Test Exception");
	}
}