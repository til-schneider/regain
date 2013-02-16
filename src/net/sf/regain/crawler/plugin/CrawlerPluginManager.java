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
 */

package net.sf.regain.crawler.plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import net.sf.regain.crawler.Crawler;
import net.sf.regain.crawler.CrawlerJob;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.document.WriteablePreparator;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

/**
 * Guarantees:
 * - If one plugin throws an exception, the other plugins will be executed none-the-less
 * - Every argument of a plugin call is non-null
 *
 * Singleton pattern: get the only instance by calling getInstance().
 *
 * @author Benjamin
 */
public class CrawlerPluginManager {
	/**
	 * Guessed maximum number of plugins.
	 * Note that this is not a hard limit: there can be more plugins,
	 * however, inserting plugins with the same "order" may not be inserted at the end.
	 */
	private static final int MAX_PLUGINS = 100;

	/**
	 * List of registered Plugins (in order of call)
	 * (Dev note: Priority Queue didn't work out: iterator is not ordered, only poll is)
	 */
	private SortedMap<Integer,CrawlerPlugin> plugins = null;

	/**
	 * The single Manager Instance.
	 */
	private static CrawlerPluginManager instance = null;

	/**
	 * Logger instance
	 */
	private static Logger mLog = Logger.getLogger(CrawlerPluginManager.class);

	/**
	 * Keep a record of the next value "order" so that the plugin is inserted at the end of queue
	 */
	private int nextOrder = 1;

	/**
	 * Count up for every inserted Plugin.
	 */
	private int insertIndex = 0;

	/**
	 * Instead of Constructor: get a singleton instance of the Manager,
	 * so that only one manager exists at a time.
	 *
	 * @return	The Plugin Manager
	 */
	public static CrawlerPluginManager getInstance()
	{
		if (instance == null)
		{
			instance = new CrawlerPluginManager();
		}
		return instance;
	}

	protected CrawlerPluginManager()
	{
		plugins = new TreeMap<Integer,CrawlerPlugin>();
	}

	/**
	 * Register a Plugin at the end of the current queue.
	 *
	 * @param plugin	Plugin to register
	 */
	public void registerPlugin(CrawlerPlugin plugin)
	{
		registerPlugin(plugin, nextOrder);
	}

	/**
	 * Register a Plugin at a certain position
	 *
	 * @param plugin	Plugin to register
	 * @param order		Place where to insert the plugin
	 * 					(The lower the order, the earlier the plugin is called
	 * 					relatively to other plugins)
	 * @throws NullPointerException	if plugin is null
	 */
	public void registerPlugin(CrawlerPlugin plugin, int order)
	{
		plugins.put(MAX_PLUGINS * order + insertIndex, plugin);

		if (order + 1 > nextOrder) {
      nextOrder = order + 1;
    }
		insertIndex ++;
	}

	/**
	 * Unregister an already registered plugin.
	 *
	 * Note: you need to keep the reference of the plugin instance you registered, if you plan to unregister it later on.
	 * Alternatively, configure your plugin's equal()-Function so that it returns true if only the Classname is the same.
	 *
	 * @param plugin
	 */
	public void unregisterPlugin(CrawlerPlugin plugin)
	{
		plugins.values().remove(plugin);
	}

	/**
	 * Unregister all Plugins
	 */
	public void clear() {
		plugins.clear();
		nextOrder = 1;
		insertIndex = 0;
	}


	/**
	 * Trigger an event: call the corresponding plugins.
	 * Collect return values.
	 * TODO : Profiling?
	 *
	 * (This is done via Reflection API to avoid code duplication)
	 *
	 * @param methodName	Name of Event (as in the interface: onEvent)
	 * @param args		Args of Event (as in the interface)
	 * @return Return Values of the called methods. Null if the called method threw an exception.
	 */
	protected List<Object> triggerEvent(String methodName, Class<?>[] argTypes, Object... args)
	{
		List<Object> returns = new ArrayList<Object>();

	  checkIfEventExists(methodName, argTypes);

		for (Map.Entry<Integer, CrawlerPlugin> entry : plugins.entrySet())
		{
		  Object ret = null;

			CrawlerPlugin plugin = entry.getValue();
			String pluginName = plugin.getClass().getName();

			mLog.debug("Send " + methodName + "-Event to " + pluginName);

			try {
				ret = MethodUtils.invokeMethod(plugin, methodName, args, argTypes);
			} catch (IllegalAccessException e) 	{ mLog.error("Reflection Error:", e);
			} catch (SecurityException e) 		{ mLog.error("Reflection Error:", e);
			} catch (NoSuchMethodException e)	{ mLog.error("Reflection Error:", e);
			} catch (InvocationTargetException e) {
				mLog.error(pluginName + " has thrown an exception:", e.getCause());
			} catch (Throwable e) {
				mLog.error(pluginName + " has thrown an exception:", e);
			}
			returns.add(ret);
		}

		return returns;
	}

	/**
	 * Check if the array does not contain any null value.
	 * @param args	Array of arguments
	 * @throws IllegalArgumentException if a null value is detected
	 */
	private void checkArgsNotNull(Object[] args) {
		for (int i = 0; i < args.length; i++)
		{
			if (args[i] == null) {
        throw new IllegalArgumentException("Plugin manager was called with null parameter (param nb " + (i + 1) + ")");
      }
		}
	}

	/**
	 * Check if a certain eventName exists in the CrawlerPlugin Interface
	 *
	 * @param methodName	"on" + eventName
	 * @param argTypes		Types of the arguments
	 */
	protected void checkIfEventExists(String methodName, Class<?>[] argTypes) {
		try {
			CrawlerPlugin.class.getMethod(methodName, argTypes);
		} catch (SecurityException e) {
			mLog.error("Reflection Error:", e);
		} catch (NoSuchMethodException e1) {
			String methodSignature = methodName + "(" + argTypesToString(argTypes) + ")";
			throw new RuntimeException("There is no event with this name (or different arguments): " + methodSignature + " declared in the CrawlerPlugin-Interface");
		}
	}

	/**
	 * Convert argument Types into a string represantation
	 * @param argTypes	Types of the arguments
	 * @return			Stringified types (e.g. java.lang.String, java.lang.String)
	 */
	private String argTypesToString(Class<?>[] argTypes) {
		StringBuilder ret = new StringBuilder(100);
		for (int i = 0; i < argTypes.length; i++)
		{
			ret.append(argTypes[i].getCanonicalName());
			if (i < argTypes.length - 1) {
        ret.append(", ");
      }
		}
		return ret.toString();
	}

	/**
	 * Get Parameter types for Reflection API
	 * (Little helper function, currently not used)
	 *
	 * @param params	Parameters to give
	 * @return Their respective classes
	 *
	 * currently not used
	private Class<?>[] getTypes(Object[] params) {
		Class<?> types[] = new Class<?>[params.length];

		for (int i = 0; i < params.length; i++)
			types[i] = params[i].getClass();

		return types;
	}
*/

	// --------------------- Event Triggers -------------------------

	/**
	 * Trigger Event: onStartCrawling
	 * @see CrawlerPlugin#onStartCrawling(Crawler)
	 * @param crawler	Crawler instance (caller)
	 */
	public void eventStartCrawling(Crawler crawler)
	{
		triggerEvent("onStartCrawling",
				new Class[]{Crawler.class},
				crawler);
	}

	/**
	 * Trigger Event: onFinishCrawling
	 * @see CrawlerPlugin#onFinishCrawling(Crawler)
	 * @param crawler	Crawler instance (caller)
	 */
	public void eventFinishCrawling(Crawler crawler) {
		triggerEvent("onFinishCrawling",
				new Class[]{Crawler.class},
				crawler);
	}

	/**
	 * Trigger Event: onBeforePrepare
	 * @see CrawlerPlugin#onBeforePrepare(RawDocument, WriteablePreparator)
	 * @param	document	Document to prepare
	 * @param	preparator	Preparator that will prepare
	 */
	public void eventBeforePrepare(RawDocument document, WriteablePreparator preparator) {
		triggerEvent("onBeforePrepare",
				new Class[]{RawDocument.class, WriteablePreparator.class},
				document, preparator);
	}

	/**
	 * Trigger Event: onAfterPrepare
	 * @see CrawlerPlugin#onAfterPrepare(RawDocument, WriteablePreparator)
	 * @param document		Document to prepare
	 * @param preparator	Preparator that prepared
	 */
	public void eventAfterPrepare(RawDocument document, WriteablePreparator preparator) {
		triggerEvent("onAfterPrepare",
				new Class[]{RawDocument.class, WriteablePreparator.class},
				document, preparator);
	}

	/**
	 * Trigger Event: onCreateIndexEntry
	 * @see CrawlerPlugin#onCreateIndexEntry(Document, IndexWriter)
	 * @param doc	Document to add
	 * @param index	Index where it will be added
	 */
	public void eventCreateIndexEntry(Document doc, IndexWriter index) {
		triggerEvent("onCreateIndexEntry",
				new Class[]{Document.class, IndexWriter.class},
				doc, index);
	}

	/**
	 * Trigger Event: onDeleteIndexEntry
	 * @see CrawlerPlugin#onDeleteIndexEntry(Document, IndexReader)
	 * @param doc	Document to delete
	 * @param index	Index where it will be deleted
	 */
	public void eventDeleteIndexEntry(Document doc, IndexReader index) {
		triggerEvent("onDeleteIndexEntry",
				new Class[]{Document.class, IndexReader.class},
				doc, index);
	}

	/**
	 * Trigger Event: onAcceptURL
	 * @see CrawlerPlugin#onAcceptURL(String, CrawlerJob)
	 * @param url	URL that was accepted
	 * @param job	Resulting Job
	 */
	public void eventAcceptURL(String url, CrawlerJob job) {
		triggerEvent("onAcceptURL",
				new Class[]{String.class, CrawlerJob.class},
				url, job);
	}

	/**
	 * Trigger Event: onDeclineURL
	 * @see CrawlerPlugin#onDeclineURL(String)
	 * @param url	URL that was declined
	 */
	public void eventDeclineURL(String url) {
		triggerEvent("onDeclineURL",
				new Class[]{String.class},
				url);
	}

	/**
	 * Trigger Event: checkDynamicBlacklist
	 * (This is not lazy: all plugins are called even if the first returns true.)
	 *
	 * @param url
	 * @param sourceUrl
	 * @param sourceLinkText
	 * @see CrawlerPlugin#checkDynamicBlacklist(String, String, String)
	 * @return True if blacklisted by at least one of the plugins.
	 */
  public boolean eventAskDynamicBlacklist(String url, String sourceUrl, String sourceLinkText)
  {
    List<Object> returns;

    returns = triggerEvent("checkDynamicBlacklist",
        new Class[]{String.class, String.class, String.class},
        url, sourceUrl, sourceLinkText);

    int i = 0;
    for (Object ret : returns)
    {
      if (! (ret instanceof Boolean)) {
        continue;
      }

      boolean blacklist = ((Boolean) ret).booleanValue();

      if (blacklist)
      {
        if (mLog.isDebugEnabled())
        {
          // Find out which plugin was the cause
          CrawlerPlugin cp = null;
          for (Map.Entry<Integer, CrawlerPlugin> entry : plugins.entrySet())
          {
            cp = entry.getValue();
            if (i == 0) {
              break;
            }
            i--;
          }
          mLog.debug("URL dynamically blacklisted by CrawlerPlugin " + cp.getClass().getName());
        }
        return true;
      }
      i++;
    }

    return false;
  }

	/**
	 * Lists contained plugins for debugging purposes
	 * @return Debugging output: contained plugins.
	 */
  @Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Contains ").append(plugins.size()).append(" Plugins: \n");

		for (Map.Entry<Integer, CrawlerPlugin> entry : plugins.entrySet())
		{
			str.append("Plugin ").append(entry.getValue().getClass().getName());
			str.append(" with order ").append(entry.getKey()).append("\n");
		}

		return str.toString();
	}

}
