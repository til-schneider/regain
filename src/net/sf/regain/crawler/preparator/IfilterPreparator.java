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
 */
package net.sf.regain.crawler.preparator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.ifilter.IfilterWrapper;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * A preparator that uses Microsoft's IFilter interface for preparing various
 * kinds of documents.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IfilterPreparator extends AbstractPreparator {

  /** The logger for this class. */
  private static Logger mLog = Logger.getLogger(IfilterPreparator.class);

  /** The extensions covered by this preparator. They are retrieved once from the registry */
  private static String[] mExtensionArr;

  /** Contains a IfilterWrapper for a file extension (String, e.g. ".doc") */
  private HashMap<String, IfilterWrapper> mExtensionToIFilterHash;

  /**
   * Contains a IfilterWrapper for a GUID (String,
   * e.g. "clsid:f07f3920-7b8c-11cf-9be8-00aa004b9986")
   */
  private HashMap<String, IfilterWrapper> mGuidToIFilterHash;

  /** The regex that matches a registry value. */
  private static RE mValueRegex;



  /**
   * Creates a new instance of IfilterPreparator.
   *
   * @throws RegainException If getting the supported extensions failed.
   */
  public IfilterPreparator() throws RegainException {
    // Read the possible extensions from the Windows registry
    super(getExtensionArr());
  }


  /**
   * Gets the extensions covered by this preparator.
   *
   * @return The extensions covered by this preparator.
   * @throws RegainException If getting the extensions failed.
   */
  private static String[] getExtensionArr()
    throws RegainException
  {
    if (mExtensionArr == null) {
      long startTime = 0;
      if (mLog.isDebugEnabled()) {
        startTime = System.currentTimeMillis();
      }

      String regKey = "HKEY_LOCAL_MACHINE\\Software\\Classes";
      String[] classChildren = getRegistryKeyChildren(regKey);
      if (classChildren == null) {
        throw new RegainException("Reading windows registry failed");
      }

      ArrayList<String> list = new ArrayList<String>();
      for (int i = 0; i < classChildren.length; i++) {
        if (classChildren[i].startsWith(".")) {
          // This is a definition for an extension -> Check whether it has a
          // PersistentHandler
          regKey = "HKEY_LOCAL_MACHINE\\Software\\Classes\\" + classChildren[i]
            + "\\PersistentHandler";
          String persistentHandlerGuid = getRegistryKeyValue(regKey);
          if (persistentHandlerGuid != null) {
            // It has one -> add the extension
            list.add(classChildren[i].substring(1));
          }
        }
      }

      mExtensionArr = new String[list.size()];
      list.toArray(mExtensionArr);

      if (mLog.isDebugEnabled()) {
        double duration = (double) (System.currentTimeMillis() - startTime) / 1000.0;

        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        mLog.debug("Getting the supported extensions of the IfilterPreparator took "
            + format.format(duration) + " secs");
      }
    }

    return mExtensionArr;
  }


  // overridden
  public void init(PreparatorConfig config) throws RegainException {
    IfilterWrapper.initCom();

    mExtensionToIFilterHash = new HashMap<String, IfilterWrapper>();
    mGuidToIFilterHash = new HashMap<String, IfilterWrapper>();
  }


  // overridden
  public void prepare(RawDocument rawDocument) throws RegainException {
    String url = rawDocument.getUrl();
    int dotPos = url.lastIndexOf('.');
    if (dotPos == -1) {
      throw new RegainException("Can't detect file extension: " + url);
    }
    String extension = url.substring(dotPos);

    IfilterWrapper ifilter = getIfilterWrapperForExtension(extension);

    String fileName = rawDocument.getContentAsFile().getAbsolutePath();
    StringBuffer buffer = new StringBuffer(DEFAULT_BUFFER_SIZE);

    ifilter.getText(fileName, buffer);

    setCleanedContent(buffer.toString());
  }


  /**
   * Gets the apropriate IfilterWrapper for a file extension.
   *
   * @param extension The file extension to get the IfilterWrapper for,
   *        e.g.
   * @return The IfilterWrapper for the extension.
   * @throws RegainException If getting the IfilterWrapper failed.
   */
  private IfilterWrapper getIfilterWrapperForExtension(String extension)
    throws RegainException
  {
    IfilterWrapper ifilter = mExtensionToIFilterHash.get(extension);
    if (ifilter != null) {
      // We already have a cached one -> Return it
      return ifilter;
    }

    // We don't have a ifilter for that extension yet -> Get the GUID of the
    // ifilter from the Windows registry, then get the ifilter

    // The following description comes from:
    // http://www.codeproject.com/csharp/FullTextSearchingIFinters.asp

    // # Step 1: Determine if there is a PersistentHandler associated with the file
    //           extension. This can be found in the registry under
    //           HKEY_LOCAL_MACHINE\Software\Classes\FileExtension, e.g.
    //           HKLM\Software\Classes\.htm. The default value of the sub key
    //           called PersistentHandler gives you the GUID of the
    //           PersistentHandler. If present skip to step four otherwise continue
    //           with step two.
    String regKey = "HKEY_LOCAL_MACHINE\\Software\\Classes\\" + extension
      + "\\PersistentHandler";
    String persistentHandlerGuid = getRegistryKeyValue(regKey);

    if (persistentHandlerGuid == null) {
      // # Step 2: Determine the CLSID associated with the file extension. Take the
      //           default value which is associated with the extension, for example
      //           "htmlfile" for the key HKLM\Software\Classes\.htm. Next search for
      //           that entry, e.g. "hmtlfile", under HKLM\Software\Classes. The
      //           default value of the sub key CLSID contains the CLSID associated
      //           with that file extension.
      regKey = "HKEY_LOCAL_MACHINE\\Software\\Classes\\" + extension;
      String extensionClass = getRegistryKeyValue(regKey);
      if (extensionClass == null) {
        throw new RegainException("Unknown file extension: " + extension);
      }

      regKey = "HKEY_LOCAL_MACHINE\\Software\\Classes\\" + extensionClass + "\\CLSID";
      String extensionClsid = getRegistryKeyValue(regKey);
      if (extensionClsid == null) {
        throw new RegainException("CLSID of extension class " + extensionClass
            + " not found");
      }

      // # Step 3: Next search for that CLSID under HKLM\Software\Classes\CLSID. The
      //           default value of the sub key called PersistentHandler gives you
      //           the GUID of the PersistentHandler.
      regKey = "HKEY_LOCAL_MACHINE\\Software\\Classes\\CLSID\\" + extensionClsid
        + "\\PersistentHandler";
      persistentHandlerGuid = getRegistryKeyValue(regKey);
      if (persistentHandlerGuid == null) {
        throw new RegainException("PersistentHandler of extension class "
            + extensionClass + " not found");
      }
    }

    // # Step 4: Search for that GUID under HKLM\Software\Classes\CLSID. Under it
    //           you will find a sub key PersistentAddinsRegistered which always
    //           has a sub key {89BCB740-6119-101A-BCB7-00DD010655AF} (this is the
    //           GUID of the IFilter interface). The default value of this key has
    //           the IFilter PersistentHandler GUID.
    regKey = "HKEY_LOCAL_MACHINE\\Software\\Classes\\CLSID\\"
      + persistentHandlerGuid
      + "\\PersistentAddinsRegistered\\{89BCB740-6119-101A-BCB7-00DD010655AF}";
    String ifilterGuid = getRegistryKeyValue(regKey);
    if (ifilterGuid == null) {
      throw new RegainException("GUIF of PersistentHandler not found for "
            + "extension " + extension);
    }

    // Strip the "{" and "}"
    ifilterGuid = "clsid:" + ifilterGuid.substring(1, ifilterGuid.length() - 1);
    if (mLog.isDebugEnabled()) {
      mLog.debug("# ifilterGuid for " + extension + " is " + ifilterGuid);
    }

    // # Step 5: Search for this GUID once more under HKLM\Software\Classes\CLSID.
    //           Under its key you will find the InProcServer32 sub key and its
    //           default value contains the name of the DLL which provides the
    //           IFilter interface to use for this extension. For example for the
    //           .htm and .html extension this is the DLL nlhtml.dll.
    // NOTE: We don't need this, the GUID is enough

    ifilter = getIfilterWrapperForGuid(ifilterGuid);
    mExtensionToIFilterHash.put(extension, ifilter);

    return ifilter;
  }


  /**
   * Gets a IfilterWrapper for a GUID.
   *
   * @param ifilterGuid The GUID to get the IfilterWrapper for.
   * @return The IfilterWrapper for the GUID.
   * @throws RegainException If getting the IfilterWrapper failed.
   */
  private IfilterWrapper getIfilterWrapperForGuid(String ifilterGuid)
    throws RegainException
  {
    IfilterWrapper ifilter = mGuidToIFilterHash.get(ifilterGuid);
    if (ifilter == null) {
      ifilter = new IfilterWrapper(ifilterGuid);
      mGuidToIFilterHash.put(ifilterGuid, ifilter);
    }

    return ifilter;
  }


  // overridden
  public void close() throws RegainException {
    // Close all ifilters
    Iterator<IfilterWrapper> ifilterIter = mGuidToIFilterHash.values().iterator();
    while (ifilterIter.hasNext()) {
      IfilterWrapper ifilter = ifilterIter.next();
      ifilter.close();
    }

    // Clean the hashes
    mExtensionToIFilterHash = null;
    mGuidToIFilterHash = null;

    // Uninitialize COM
    IfilterWrapper.closeCom();
  }


  /**
   * Gets the default value from a Windows registry key.
   *
   * @param regKey The Windows registry key to get the default value for.
   * @return The default value or null if the value couldn't be retreived.
   * @throws RegainException if initializing the value regex failed.
   */
  private static String getRegistryKeyValue(String regKey)
    throws RegainException
  {
    return getRegistryKeyValue(regKey, null);
  }


  /**
   * Gets a value from a Windows registry key.
   *
   * @param regKey The Windows registry key to get the value for.
   * @param valueName The name of the value to get.
   * @return The default value or null if the value couldn't be retreived.
   * @throws RegainException if initializing the value regex failed.
   */
  private static String getRegistryKeyValue(String regKey, String valueName)
    throws RegainException
  {
    String[] cmdArr = { "reg", "query", regKey };
    String[] output;
    try {
      output = CrawlerToolkit.executeNativeCommand(cmdArr);
    }
    catch (RegainException exc) {
      // Probably the regKey doesn't exist
      return null;
    }

    int valueStartIdx = -1;
    for (int i = 0; i < output.length; i++) {
      if (output[i].equals(regKey)) {
        // We found the start of the value output
        valueStartIdx = i + 1;
      }
    }

    if (valueStartIdx == -1) {
      // Start not found
      return null;
    }

    if (mValueRegex == null) {
      try {
        mValueRegex = new RE("^\\s+(.*)\\s+REG_SZ\\s+(.*)$");
      } catch (RESyntaxException exc) {
        throw new RegainException("Creating registry value regex failed", exc);
      }
    }

    synchronized (mValueRegex) {
      for (int i = valueStartIdx; i < output.length; i++) {
        if (mValueRegex.match(output[i])) {
          String name = mValueRegex.getParen(1).trim();
          // NOTE: The name of the default value is "<NO NAME>" on Windows NT, 2000 and XP
          //       and "(Stardard)" or "(Default)" or some other localized stuff
          //       on Windows Server 2003.
          if (valueName != null ? name.equals(valueName)
                                : (name.equals("<NO NAME>") || name.startsWith("(")))
          {
            // We found the default value -> return it
            return mValueRegex.getParen(2);
          }
        } else {
          // This is the end of the value output
          break;
        }
      }
    }

    // Nothing found
    return null;
  }


  /**
   * Gets the child key from a Windows registry key.
   *
   * @param regKey The Windows registry key to get the child keys for.
   * @return The child keys or null if the children could not be read.
   */
  private static String[] getRegistryKeyChildren(String regKey) {
    String[] cmdArr = { "reg", "query", regKey };
    String[] output;
    try {
      output = CrawlerToolkit.executeNativeCommand(cmdArr);
    }
    catch (RegainException exc) {
      // Probably the regKey doesn't exist
      return null;
    }

    // Get the children
    ArrayList<String> list = new ArrayList<String>();
    String childPrefix = regKey + "\\";
    for (int i = 0; i < output.length; i++) {
      if (output[i].startsWith(childPrefix)) {
        // We found a child
        list.add(output[i].substring(childPrefix.length()));
      }
    }

    // Convert the list to an array
    String[] asArr = new String[list.size()];
    list.toArray(asArr);
    return asArr;
  }

}
