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
 *  $RCSfile$
 *   $Source$
 *     $Date: 2012-08-20 10:58:50 +0200 (Mo, 20 Aug 2012) $
 *   $Author: benjaminpick $
 * $Revision: 619 $
 */
package net.sf.regain.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.Profiler;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.HtmlPreparator;
import net.sf.regain.crawler.preparator.IfilterPreparator;
import net.sf.regain.crawler.preparator.JacobMsExcelPreparator;
import net.sf.regain.crawler.preparator.JacobMsPowerPointPreparator;
import net.sf.regain.crawler.preparator.JacobMsWordPreparator;
import net.sf.regain.crawler.preparator.JavaPreparator;
import net.sf.regain.crawler.preparator.OpenOfficePreparator;
import net.sf.regain.crawler.preparator.PdfBoxPreparator;
import net.sf.regain.crawler.preparator.PlainTextPreparator;
import net.sf.regain.crawler.preparator.PoiMsOfficePreparator;
import net.sf.regain.crawler.preparator.SimpleRtfPreparator;
import net.sf.regain.crawler.preparator.SwingRtfPreparator;
import net.sf.regain.crawler.preparator.XmlPreparator;
import net.sf.regain.crawler.preparator.ZipPreparator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Tests all the preparators
 *
 * @author Til Schneider, www.murfman.de
 */
public class PreparatorTest {

  /**
   * The logger for this class
   */
  private static Logger mLog = Logger.getLogger(PreparatorTest.class);
  /**
   * The profilers that measured the work of the preparators.
   */
  private static ArrayList<Profiler> mProfilerList;
  /**
   * The class prefix of the the regain preparators.
   */
  private static final String REGAIN_PREP_PREFIX = "net.sf.regain.crawler.preparator.";
  /**
   * Der Dateiname der Log4J-Properties-Datei.
   */
  private static final String LOG4J_PROP_FILE_NAME = "log4j.properties";

  /**
   * Does the test
   *
   * @param args The Command line arguments
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: [test document directory] [output directory]");
      System.exit(1);
    }

    // Initialize Logging
    String logConfigFileName = LOG4J_PROP_FILE_NAME;
    File logConfigFile = new File(logConfigFileName);
    if (!logConfigFile.exists()) {
      System.out.println("ERROR: Logging configuration file not found: " + logConfigFile.getAbsolutePath());
      return; // Abort
    }

    PropertyConfigurator.configure(logConfigFile.getAbsolutePath());
    mLog.info("Logging initialized");

    File docDir, outputDir;
    try {
      docDir = new File(args[0]).getCanonicalFile();
      outputDir = new File(args[1]).getCanonicalFile();
    } catch (IOException exc) {
      exc.printStackTrace();
      System.exit(1);
      return;
    }
    if (!docDir.canRead())
    {
      System.err.println("The input document directory cannot be read: " + docDir.getAbsolutePath());
      System.exit(1);
    }
    if (!outputDir.canWrite())
    {
      System.err.println("The output document directory cannot be written: " + outputDir.getAbsolutePath());
      System.exit(1);
    }
    

    mProfilerList = new ArrayList<Profiler>();

    try {
      IfilterPreparator ifilterPreparator = new IfilterPreparator();
      testPreparator(docDir, outputDir, "html", ifilterPreparator);
      testPreparator(docDir, outputDir, "doc", ifilterPreparator);
      testPreparator(docDir, outputDir, "pdf", ifilterPreparator);
      testPreparator(docDir, outputDir, "ppt", ifilterPreparator);
      testPreparator(docDir, outputDir, "rtf", ifilterPreparator);
      testPreparator(docDir, outputDir, "xls", ifilterPreparator);
      testPreparator(docDir, outputDir, "doc", new JacobMsWordPreparator());
      testPreparator(docDir, outputDir, "ppt", new JacobMsPowerPointPreparator());
      testPreparator(docDir, outputDir, "xls", new JacobMsExcelPreparator());
    } catch (RegainException exc) {
      mLog.error("Creating Windows-only preparator failed", exc);
    }

    try {
      testPreparator(docDir, outputDir, "html", new HtmlPreparator());
      testPreparator(docDir, outputDir, "doc", new PoiMsOfficePreparator());
      testPreparator(docDir, outputDir, "pdf", new PdfBoxPreparator());
      testPreparator(docDir, outputDir, "ppt", new PoiMsOfficePreparator());
      testPreparator(docDir, outputDir, "rtf", new SimpleRtfPreparator());
      testPreparator(docDir, outputDir, "rtf", new SwingRtfPreparator());
      testPreparator(docDir, outputDir, "txt", new PlainTextPreparator());
      testPreparator(docDir, outputDir, "xls", new PoiMsOfficePreparator());
      testPreparator(docDir, outputDir, "xml", new XmlPreparator());
      testPreparator(docDir, outputDir, "vsd", new PoiMsOfficePreparator());
      testPreparator(docDir, outputDir, "java", new JavaPreparator());
      testPreparator(docDir, outputDir, "ooo", new OpenOfficePreparator());
      testPreparator(docDir, outputDir, "zip", new ZipPreparator());
    } catch (RegainException exc) {
      mLog.error("Creating preparator failed", exc);
    }

    mLog.info("\nSummary:");
    for (int i = 0; i < mProfilerList.size(); i++) {
      mLog.info(" " + mProfilerList.get(i));
    }
    mLog.info("Results written to: " + outputDir.getAbsolutePath());
  }

  /**
   * Tests one preparator
   *
   * @param docDir The source directory where the documents are located.
   * @param outputDir The target directory where to write the extracted texts.
   * @param fileType The file type the current preperator takes.
   * @param prep The preparatos to test
   * @throws RegainException If URL-encoding failed
   */
  private static void testPreparator(File docDir, File outputDir,
          String fileType, AbstractPreparator prep)
          throws RegainException {
    String prepName = prep.getClass().getName();
    if (prepName.startsWith(REGAIN_PREP_PREFIX)) {
      prepName = prepName.substring(REGAIN_PREP_PREFIX.length());
    }

    mLog.info("Initializing preparator " + prepName + "...");
    try {
      prep.init(new PreparatorConfig());
    } catch (Throwable thr) {
      mLog.error("Initializing preparator failed", thr);
    }

    mLog.info("Testing preparator " + prepName + "...");
    Profiler profiler = new Profiler(prepName, "docs");

    File typeDir = new File(docDir, fileType);
    File prepOutputDir = new File(outputDir, prepName);
    if (!prepOutputDir.exists()) {
      if (!prepOutputDir.mkdir()) {
        mLog.error("Could not create output dir: " + prepOutputDir.getAbsolutePath());
        System.exit(1);
      }
    }

    String sourceUrl = RegainToolkit.fileToUrl(typeDir);
    File[] docFileArr = typeDir.listFiles();
    if (docFileArr == null) {
      mLog.info("No test docs for preparator " + prepName + " found in " + typeDir.getAbsolutePath());
      return;
    }
    for (int i = 0; i < docFileArr.length; i++) {
      if (docFileArr[i].isFile()) {
        String url = RegainToolkit.fileToUrl(docFileArr[i]);
        mLog.info("Preparing document: " + url);
        try {
          RawDocument doc = new RawDocument(url, sourceUrl, null, null);

          profiler.startMeasuring();
          String content;
          try {
            prep.prepare(doc);
            content = prep.getCleanedContent();
            prep.cleanUp();
            profiler.stopMeasuring(docFileArr[i].length());
            
            HashMap<String, String> links = doc.getLinks();
            if (links != null && links.size() > 0)
            {
              content += "\n\nLinks in document:\n\n";
              for (Entry<String, String> link : links.entrySet())
              {
                content += link.getValue() + ": " + link.getKey() + "\n";
              }
            }

          } catch (Throwable thr) {
            profiler.abortMeasuring();
            throw thr;
          }

          File outFile = new File(prepOutputDir, docFileArr[i].getName() + ".txt");
          RegainToolkit.writeToFile(content, outFile);
        } catch (Throwable thr) {
          mLog.error("Preparing document failed: " + url, thr);
        }
      }
    }

    mLog.info("Closing preparator " + prepName + "...");
    try {
      prep.close();
    } catch (Throwable thr) {
      mLog.error("Closing preparator failed", thr);
    }

    mProfilerList.add(profiler);
  }
}
