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
 *  $RCSfile: PreparatorTest.java,v $
 *   $Source: /cvsroot/regain/regain/test/src/net/sf/regain/test/PreparatorTest.java,v $
 *     $Date: 2005/08/13 17:15:15 $
 *   $Author: til132 $
 * $Revision: 1.8 $
 */
package net.sf.regain.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.CrawlerToolkit;
import net.sf.regain.crawler.Profiler;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.*;

/**
 * Tests all the preparators
 * 
 * @author Til Schneider, www.murfman.de
 */
public class PreparatorTest {
  
  /** The profilers that measured the work of the preparators. */
  private static ArrayList mProfilerList;
  
  /** The class prefix of the the regain preparators. */
  private static final String REGAIN_PREP_PREFIX = "net.sf.regain.crawler.preparator.";


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
    
    File docDir, outputDir;
    try {
      docDir = new File(args[0]).getCanonicalFile();
      outputDir = new File(args[1]).getCanonicalFile();
    }
    catch (IOException exc) {
      exc.printStackTrace();
      System.exit(1);
      return;
    }
    
    mProfilerList = new ArrayList();
    
    testPreparator(docDir, outputDir, "html", new HtmlPreparator());
    testPreparator(docDir, outputDir, "doc", new JacobMsWordPreparator());
    testPreparator(docDir, outputDir, "doc", new PoiMsWordPreparator());
    testPreparator(docDir, outputDir, "pdf", new PdfBoxPreparator());
    testPreparator(docDir, outputDir, "ppt", new JacobMsPowerPointPreparator());
    testPreparator(docDir, outputDir, "rtf", new SimpleRtfPreparator());
    testPreparator(docDir, outputDir, "rtf", new SwingRtfPreparator());
    testPreparator(docDir, outputDir, "txt", new PlainTextPreparator());
    testPreparator(docDir, outputDir, "xls", new JacobMsExcelPreparator());
    testPreparator(docDir, outputDir, "xls", new PoiMsExcelPreparator());
    testPreparator(docDir, outputDir, "xml", new XmlPreparator());
    
    System.out.println();
    System.out.println("Summary:");
    for (int i = 0; i < mProfilerList.size(); i++) {
      System.out.println(" " + mProfilerList.get(i));
    }
    System.out.println("Results written to: " + outputDir.getAbsolutePath());
  }

  
  /**
   * Tests one preparator
   * 
   * @param docDir The source directory where the documents are located.
   * @param outputDir The target directory where to write the extracted texts.
   * @param fileType The file type the current preperator takes.
   * @param prep The preparatos to test
   */
  private static void testPreparator(File docDir, File outputDir,
    String fileType, AbstractPreparator prep)
  {
    String prepName = prep.getClass().getName();
    if (prepName.startsWith(REGAIN_PREP_PREFIX)) {
      prepName = prepName.substring(REGAIN_PREP_PREFIX.length());
    }

    System.out.println("Testing preparator " + prepName + "...");
    
    Profiler profiler = new Profiler(prepName, "docs");
    
    File typeDir = new File(docDir, fileType);
    File prepOutputDir = new File(outputDir, prepName);
    if (! prepOutputDir.mkdir()) {
      System.out.println("Could not create output dir: "
        + prepOutputDir.getAbsolutePath());
      System.exit(1);
    }
    
    String sourceUrl = RegainToolkit.fileToUrl(typeDir);
    File[] docFileArr = typeDir.listFiles();
    if (docFileArr == null) {
      System.out.println("No test docs for preparator " + prepName
          + " found in " + typeDir.getAbsolutePath());
      return;
    }
    for (int i = 0; i < docFileArr.length; i++) {
      if (docFileArr[i].isFile()) {
        String url = RegainToolkit.fileToUrl(docFileArr[i]);
        System.out.println("Preparing document: " + url);
        try {
          RawDocument doc = new RawDocument(url, sourceUrl, null);

          profiler.startMeasuring();
          String content;
          try {
            prep.prepare(doc);
            content = prep.getCleanedContent();
            prep.cleanUp();
            profiler.stopMeasuring(docFileArr[i].length());
          }
          catch (Throwable thr) {
            profiler.abortMeasuring();
            throw thr;
          }

          File outFile = new File(prepOutputDir, docFileArr[i].getName() + ".txt");
          CrawlerToolkit.writeToFile(content, outFile);
        }
        catch (Throwable thr) {
          System.out.println("Preparing document failed: " + url);
          thr.printStackTrace();
        }
      }
    }
    
    System.out.println("Closing preparator " + prepName + "...");
    try {
      prep.close();
    }
    catch (Throwable thr) {
      System.out.println("Closing preparator failed");
      thr.printStackTrace();
    }
    
    mProfilerList.add(profiler);
  }
  
}
