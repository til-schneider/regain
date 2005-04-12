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
 *  $RCSfile: RegainToolkit.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/RegainToolkit.java,v $
 *     $Date: 2005/03/30 10:30:03 $
 *   $Author: til132 $
 * $Revision: 1.12 $
 */
package net.sf.regain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * Enthält Hilfsmethoden, die sowohl vom Crawler als auch von der Suchmaske
 * genutzt werden.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RegainToolkit {

  /**
   * Gibt an, ob die Worte, die der Analyzer identifiziert ausgegeben werden
   * sollen.
   */
  private static final boolean ANALYSE_ANALYZER = false;

  /** The number of bytes in a kB (kilo byte). */
  private static final int SIZE_KB = 1024;

  /** The number of bytes in a MB (mega byte). */
  private static final int SIZE_MB = 1024 * 1024;

  /** The number of bytes in a GB (giga byte). */
  private static final int SIZE_GB = 1024 * 1024 * 1024;

  /** Der gecachte, systemspeziefische Zeilenumbruch. */
  private static String mLineSeparator;



  /**
   * Löscht ein Verzeichnis mit allen Unterverzeichnissen und -dateien.
   *
   * @param dir Das zu löschende Verzeichnis.
   *
   * @throws RegainException Wenn das Löschen fehl schlug.
   */
  public static void deleteDirectory(File dir) throws RegainException {
    if (! dir.exists()) {
      return; // Nothing to do
    }

    // Delete all children
    File[] children = dir.listFiles();
    if (children != null) {
      for (int i = 0; i < children.length; i++) {
        if (children[i].isDirectory()) {
          deleteDirectory(children[i]);
        } else {
          if (! children[i].delete()) {
            throw new RegainException("Deleting " + children[i].getAbsolutePath()
              + " failed!");
          }
        }
      }
    }

    // Delete self
    if (! dir.delete()) {
      throw new RegainException("Deleting " + dir.getAbsolutePath() + " failed!");
    }
  }



  /**
   * Schreibt alle Daten, die der Reader liefert in den Writer.
   * <p>
   * Weder der Reader noch der Writer werden dabei geschlossen. Dies muss die
   * aufrufende Methode übernehmen!
   *
   * @param reader Der Reader, der die Daten liefert.
   * @param writer Der Writer auf den die Daten geschrieben werden sollen.
   *
   * @throws IOException Wenn Lesen oder Schreiben fehl schlug.
   */
  public static void pipe(Reader reader, Writer writer) throws IOException {
    char[] buffer = new char[10240]; // 10 kB (or kChars ;-) )

    int len;
    while ((len = reader.read(buffer)) != -1) {
      writer.write(buffer, 0, len);
    }
  }



  /**
   * Schreibt alle Daten, die der InputStream liefert in den OutputStream.
   * <p>
   * Weder der InputStream noch der OutputStream werden dabei geschlossen. Dies
   * muss die aufrufende Methode übernehmen!
   *
   * @param in Der InputStream, der die Daten liefert.
   * @param out Der OutputStream auf den die Daten geschrieben werden sollen.
   *
   * @throws IOException Wenn Lesen oder Schreiben fehl schlug.
   */
  public static void pipe(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[10240]; // 10 kB

    int len;
    while ((len = in.read(buffer)) != -1) {
      out.write(buffer, 0, len);
    }
  }


  /**
   * Copies a file.
   *
   * @param from The source file.
   * @param to The target file.
   * @throws RegainException If copying failed.
   */
  public static void copyFile(File from, File to) throws RegainException {
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      in = new FileInputStream(from);
      out = new FileOutputStream(to);

      RegainToolkit.pipe(in, out);
    }
    catch (IOException exc) {
      throw new RegainException("Copying file from " + from.getAbsolutePath()
        + " to " + to.getAbsolutePath() + " failed", exc);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }
  
  
  /**
   * Copies a directory.
   * 
   * @param fromDir The source directory.
   * @param toDir The target directory.
   * @param copySubDirs Specifies whether to copy sub directories.
   * @param excludeExtension The file extension to exclude.
   * @throws RegainException If copying the index failed.
   */
  public static void copyDirectory(File fromDir, File toDir,
    boolean copySubDirs, String excludeExtension)
    throws RegainException
  {
    File[] indexFiles = fromDir.listFiles();
    for (int i = 0; i < indexFiles.length; i++) {
      String fileName = indexFiles[i].getName();
      File targetFile = new File(toDir, fileName);
      if (indexFiles[i].isDirectory()) {
        if (copySubDirs) {
          targetFile.mkdir();
          copyDirectory(indexFiles[i], targetFile, copySubDirs, excludeExtension);
        }
      }
      else if ((excludeExtension == null) || (! fileName.endsWith(excludeExtension))) {
        RegainToolkit.copyFile(indexFiles[i], targetFile);
      }
    }
  }


  /**
   * Copies a directory.
   * 
   * @param fromDir The source directory.
   * @param toDir The target directory.
   * @param copySubDirs Specifies whether to copy sub directories.
   * @throws RegainException If copying the index failed.
   */
  public static void copyDirectory(File fromDir, File toDir,
    boolean copySubDirs)
    throws RegainException
  {
    copyDirectory(fromDir, toDir, copySubDirs, null);
  }
  
  
  /**
   * Reads a String from a stream.
   * 
   * @param stream The stream to read the String from
   * @param charsetName The name of the charset to use.
   * @return The stream content as String.
   * @throws RegainException If reading the String failed.
   */
  public static String readStringFromStream(InputStream stream, String charsetName)
    throws RegainException
  {
    InputStreamReader reader = null;
    try {
      if (charsetName == null) {
        reader = new InputStreamReader(stream);
      } else {
        reader = new InputStreamReader(stream, charsetName);
      }
      StringWriter writer = new StringWriter();

      RegainToolkit.pipe(reader, writer);

      reader.close();
      writer.close();

      return writer.toString();
    }
    catch (IOException exc) {
      throw new RegainException("Reading String from stream failed", exc);
    }
    finally {
      if (reader != null) {
        try { reader.close(); } catch (IOException exc) {}
      }
    }
  }

  
  /**
   * Reads a String from a stream.
   * 
   * @param stream The stream to read the String from
   * @return The stream content as String.
   * @throws RegainException If reading the String failed.
   */
  public static String readStringFromStream(InputStream stream)
    throws RegainException
  {
    return readStringFromStream(stream, null);
  }
  

  /**
   * Liest einen String aus einer Datei.
   *
   * @param file Die Datei aus der der String gelesen werden soll.
   *
   * @return Der Inhalt der Datei als String oder <code>null</code>, wenn die
   *         Datei nicht existiert.
   * @throws RegainException Wenn das Lesen fehl schlug.
   */
  public static String readStringFromFile(File file) throws RegainException {
    if (! file.exists()) {
      return null;
    }

    FileInputStream stream = null;
    try {
      stream = new FileInputStream(file);
      return readStringFromStream(stream);
    }
    catch (IOException exc) {
      throw new RegainException("Reading String from " + file.getAbsolutePath()
          + "failed", exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }

  
  /**
   * Gets the size of a directory with all files.
   * 
   * @param dir The directory to get the size for.
   * @return The size of the directory.
   */
  public static long getDirectorySize(File dir) {
    File[] childArr = dir.listFiles();
    long size = 0;
    if (childArr != null) {
      for (int i = 0; i < childArr.length; i++) {
        if (childArr[i].isDirectory()) {
          size += getDirectorySize(childArr[i]);
        } else {
          size += childArr[i].length();
        }
      }
    }
    return size;
  }
  

  /**
   * Erzeugt einen Analyzer, der sowohl vom Crawler als auch von der Suchmaske
   * genutzt wird. Es ist sehr wichtig, dass beide den gleichen Analyzer nutzen,
   * daher nutzen beide diese Methode.
   * <p>
   * Momentan werden folgende Analyzer-Typen unterstützt:
   * <ul>
   *   <li>english: Ein Analyzer für englisch</li>
   *   <li>german: Ein Analyzer für deutsch</li>
   * </ul>
   *
   * @param analyzerType Der Typ des zu erstellenden Analyzers.
   * @param stopWordList Alle Worte, die nicht indiziert werden sollen.
   * @param exclusionList Alle Worte, die bei der Indizierung nicht vom Analyzer
   *        verändert werden sollen.
   * @return Der Analyzer
   * @throws RegainException Wenn die Erzeugung fehl schlug.
   */
  public static Analyzer createAnalyzer(String analyzerType,
    String[] stopWordList, String[] exclusionList)
    throws RegainException
  {
    if (analyzerType == null) {
      throw new RegainException("No analyzer type specified!");
    }

    analyzerType = analyzerType.trim();

    Analyzer analyzer;
    if (analyzerType.equalsIgnoreCase("english")) {
      StandardAnalyzer stdAnalyzer;
      if ((stopWordList != null) && (stopWordList.length != 0)) {
        stdAnalyzer = new StandardAnalyzer(stopWordList);
      } else {
        stdAnalyzer = new StandardAnalyzer();
      }

      if ((exclusionList != null) && (exclusionList.length != 0)) {
        throw new RegainException("Analyzer type " + analyzerType
          + " does not support exclusion lists");
      }

      analyzer = stdAnalyzer;
    }
    else if (analyzerType.equalsIgnoreCase("german")) {
      GermanAnalyzer deAnalyzer;
      if ((stopWordList != null) && (stopWordList.length != 0)) {
        deAnalyzer = new GermanAnalyzer(stopWordList);
      } else {
        deAnalyzer = new GermanAnalyzer();
      }

      if ((exclusionList != null) && (exclusionList.length != 0)) {
        deAnalyzer.setStemExclusionTable(exclusionList);
      }

      analyzer = deAnalyzer;
    }
    else {
      throw new RegainException("Unkown analyzer type: '" + analyzerType + "'");
    }

    analyzer = new WrapperAnalyzer(analyzer);

    if (ANALYSE_ANALYZER) {
      return createAnalysingAnalyzer(analyzer);
    }
    return analyzer;
  }


  /**
   * Erzeugt einen Analyzer, der die Aufrufe an einen eingebetteten Analyzer
   * analysiert.
   * <p>
   * Dies ist beim Debugging hilfreich, wenn man prüfen will, was ein Analyzer
   * bei bestimmten Anfragen ausgibt.
   *
   * @param nestedAnalyzer The nested Analyzer that should
   * @return Ein Analyzer, der die Aufrufe an einen eingebetteten Analyzer
   *         analysiert.
   */
  private static Analyzer createAnalysingAnalyzer(final Analyzer nestedAnalyzer) {
    return new Analyzer() {
      public TokenStream tokenStream(String fieldName, Reader reader) {
        // NOTE: For Analyzation we have to read the reader twice:
        //       Once for the analyzation and second for the returned TokenStream
        //       -> We save the content of the Reader in a String and read this
        //          String twice.
        try {
          // Save the content of the reader in a String
          StringWriter writer = new java.io.StringWriter();
          pipe(reader, writer);
          String asString = writer.toString();

          // Anaylize the call
          TokenStream stream = nestedAnalyzer.tokenStream(fieldName,
            new StringReader(asString));
          System.out.println("Tokens for '" + asString + "':");
          Token token;
          while ((token = stream.next()) != null) {
            System.out.println("  '" + token.termText() + "'");
          }

          // Do the call a second time and return the result this time
          return nestedAnalyzer.tokenStream(fieldName, new StringReader(asString));
        }
        catch (IOException exc) {
          System.out.println("exc: " + exc);

          return null;
        }
      }
    };
  }



  /**
   * Ersetzt in einem String alle Vorkommnisse von <CODE>pattern</CODE> durch
   * <CODE>replacement</CODE>.
   * <p>
   * Hinweis: <CODE>pattern</CODE> darf auch Teil von <CODE>replacement</CODE>
   * sein!
   *
   * @param source Der zu durchsuchende String.
   * @param pattern Das zu ersetzende Muster.
   * @param replacement Der Ersatz für alle Vorkommnisse des Musters.
   *
   * @return Ein String in dem alle Vorkommnisse von <CODE>pattern</CODE> durch
   *         <CODE>replacement</CODE> ersetzt wurden.
   */
  public static String replace(String source, String pattern, String replacement) {
    // Check whether the pattern occurs in the source at all
    int firstPatternPos = source.indexOf(pattern);
    if (firstPatternPos == -1) {
      // The pattern does not occur in the source -> return the source
      return source;
    }

    // Build a new String where pattern is replaced by the replacement
    StringBuffer target = new StringBuffer(source.length());
    int start = 0;             // The start of a part without the pattern
    int end = firstPatternPos; // The end of a part without the pattern
    do {
      target.append(source.substring(start, end));
      target.append(replacement);
      start = end + pattern.length();
    } while ((end = source.indexOf(pattern, start)) != -1);
    target.append(source.substring(start, source.length()));

    // return the String
    return target.toString();
  }


  /**
   * Gibt einen Wert in Prozent mit zwei Nachkommastellen zurück.
   *
   * @param value Der Wert. (Zwischen 0 und 1)
   * @return Der Wert in Prozent.
   */
  public static String toPercentString(double value) {
    NumberFormat format = NumberFormat.getPercentInstance();
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);
    return format.format(value);
  }

  
  /**
   * Gibt einen für den Menschen gut lesbaren String für eine Anzahl Bytes
   * zurück.
   *
   * @param bytes Die Anzahl Bytes
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes) {
    return bytesToString(bytes, Locale.ENGLISH);
  }


  /**
   * Gibt einen für den Menschen gut lesbaren String für eine Anzahl Bytes
   * zurück.
   *
   * @param bytes Die Anzahl Bytes
   * @param locale The locale to use for formatting the numbers.
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes, Locale locale) {
    return bytesToString(bytes, 2, locale);
  }


  /**
   * Gibt einen für den Menschen gut lesbaren String für eine Anzahl Bytes
   * zurück.
   *
   * @param bytes Die Anzahl Bytes
   * @param fractionDigits Die Anzahl der Nachkommastellen
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes, int fractionDigits) {
    return bytesToString(bytes, fractionDigits, Locale.ENGLISH);
  }
  

  /**
   * Gibt einen für den Menschen gut lesbaren String für eine Anzahl Bytes
   * zurück.
   *
   * @param bytes Die Anzahl Bytes
   * @param fractionDigits Die Anzahl der Nachkommastellen
   * @param locale The locale to use for formatting the numbers.
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes, int fractionDigits, Locale locale) {
    int factor;
    String unit;

    if (bytes > SIZE_GB) {
      factor = SIZE_GB;
      unit = "GB";
    }
    else if (bytes > SIZE_MB) {
      factor = SIZE_MB;
      unit = "MB";
    }
    else if (bytes > SIZE_KB) {
      factor = SIZE_KB;
      unit = "kB";
    }
    else {
      return bytes + " Byte";
    }

    NumberFormat format = NumberFormat.getInstance(locale);
    format.setMinimumFractionDigits(fractionDigits);
    format.setMaximumFractionDigits(fractionDigits);

    String asString = format.format((double) bytes / (double) factor);

    return asString + " " + unit;
  }


  /**
   * Konvertiert ein Date-Objekt in einen String mit dem Format
   * "YYYY-MM-DD HH:MM". Das ist nötig, um ein eindeutiges und vom Menschen
   * lesbares Format zu haben.
   * <p>
   * Dieses Format ist mit Absicht nicht lokalisiert, um die Eindeutigkeit zu
   * gewährleisten. Die Lokalisierung muss die Suchmaske übernehmen.
   *
   * @param lastModified Das zu konvertiernende Date-Objekt
   * @return Ein String mit dem Format "YYYY-MM-DD HH:MM"
   * @see #stringToLastModified(String)
   */
  public static String lastModifiedToString(Date lastModified) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(lastModified);

    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1; // +1: In the Date class january is 0
    int day = cal.get(Calendar.DAY_OF_MONTH);

    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int minute = cal.get(Calendar.MINUTE);

    StringBuffer buffer = new StringBuffer(16);

    // "YYYY-"
    buffer.append(year);
    buffer.append('-');

    // "MM-"
    if (month < 10) {
      buffer.append('0');
    }
    buffer.append(month);
    buffer.append('-');

    // "DD "
    if (day < 10) {
      buffer.append('0');
    }
    buffer.append(day);
    buffer.append(' ');

    // "HH:"
    if (hour < 10) {
      buffer.append('0');
    }
    buffer.append(hour);
    buffer.append(':');

    // "MM"
    if (minute < 10) {
      buffer.append('0');
    }
    buffer.append(minute);

    return buffer.toString();
  }


  /**
   * Konvertiert einen String mit dem Format "YYYY-MM-DD HH:MM" in ein
   * Date-Objekt.
   *
   * @param asString Der zu konvertierende String
   * @return Das konvertierte Date-Objekt.
   * @throws RegainException Wenn der String ein falsches Format hat.
   * @see #lastModifiedToString(Date)
   */
  public static Date stringToLastModified(String asString)
    throws RegainException
  {
    Calendar cal = Calendar.getInstance();

    try {
      // Format: "YYYY-MM-DD HH:MM"

      int year   = Integer.parseInt(asString.substring(0, 4));
      cal.set(Calendar.YEAR, year);
      int month  = Integer.parseInt(asString.substring(5, 7));
      cal.set(Calendar.MONTH, month - 1); // -1: In the Date class january is 0
      int day    = Integer.parseInt(asString.substring(8, 10));
      cal.set(Calendar.DAY_OF_MONTH, day);

      int hour   = Integer.parseInt(asString.substring(11, 13));
      cal.set(Calendar.HOUR_OF_DAY, hour);
      int minute = Integer.parseInt(asString.substring(14, 16));
      cal.set(Calendar.MINUTE, minute);
      cal.set(Calendar.SECOND, 0);
    }
    catch (Throwable thr) {
      throw new RegainException("Last-modified-string has not the format" +
        "'YYYY-MM-DD HH:MM': " + asString, thr);
    }

    return cal.getTime();
  }


  /**
   * Splits a String into a string array.
   *
   * @param str The String to split.
   * @param delim The String that separates the items to split
   * @return An array the items.
   */
  public static String[] splitString(String str, String delim) {
    return splitString(str, delim, false);
  }


  /**
   * Splits a String into a string array.
   *
   * @param str The String to split.
   * @param delim The String that separates the items to split
   * @param trimSplits Specifies whether {@link String#trim()} should be called
   *        for every split.
   * @return An array the items.
   */
  public static String[] splitString(String str, String delim, boolean trimSplits) {
    StringTokenizer tokenizer = new StringTokenizer(str, delim);
    String[] searchFieldArr = new String[tokenizer.countTokens()];
    for (int i = 0; i < searchFieldArr.length; i++) {
      searchFieldArr[i] = tokenizer.nextToken();
      if (trimSplits) {
        searchFieldArr[i] = searchFieldArr[i].trim();
      }
    }
    return searchFieldArr;
  }


  /**
   * Gibt den systemspeziefischen Zeilenumbruch zurück.
   *
   * @return Der Zeilenumbruch.
   */
  public static String getLineSeparator() {
    if (mLineSeparator == null) {
      mLineSeparator = System.getProperty("line.separator");
    }

    return mLineSeparator;
  }


  /**
   * Loads a class and creates an instance.
   * 
   * @param className The name of the class to load and create an instance of.
   * @param superClass The super class the class must extend.
   * @param classLoader The class loader to use for loading the class. May be
   *        <code>null</code>
   * @return An object of the class.
   * @throws RegainException If loading the class or creating the instance
   *         failed or if the class is no instance of the given super class. 
   */
  public static Object createClassInstance(String className,
    Class superClass, ClassLoader classLoader)
    throws RegainException
  {
    // Load the class
    Class clazz;
    try {
      if (classLoader == null) {
        clazz = Class.forName(className);
      } else {
        clazz = classLoader.loadClass(className);
      }
    }
    catch (ClassNotFoundException exc) {
      throw new RegainException("The class '" + className
        + "' does not exist", exc);
    }

    // Create the instance
    Object obj;
    try {
      obj = clazz.newInstance();
    }
    catch (Exception exc) {
      throw new RegainException("Error creating instance of class "
        + className, exc);
    }

    // Check the instance
    if (! superClass.isInstance(obj)) {
      throw new RegainException("The class " + className + " does not " +
        "implement " + superClass.getName());
    }

    return obj;
  }


  /**
   * Loads a class and creates an instance.
   * 
   * @param className The name of the class to load and create an instance of.
   * @param superClass The super class the class must extend.
   * @param jarFileName The name of the jar file to load the class from.
   *        May be <code>null</code>.
   * @return An object of the class.
   * @throws RegainException If loading the class or creating the instance
   *         failed or if the class is no instance of the given super class. 
   */
  public static Object createClassInstance(String className, Class superClass,
    String jarFileName)
    throws RegainException
  {
    // Create a class loader for the jar file
    ClassLoader classLoader = null;
    if (jarFileName != null) {
      File jarFile = new File(jarFileName);
      if (! jarFile.exists()) {
        throw new RegainException("Jar file does not exist: " +
            jarFile.getAbsolutePath());
      }
      
      try {
        classLoader = new URLClassLoader(new URL[] { jarFile.toURL() }, superClass.getClassLoader());
      }
      catch (MalformedURLException exc) {
        throw new RegainException("Creating class loader for " +
            "jar file failed: " + jarFile.getAbsolutePath(),
            exc);
      }
    }
    
    // Create the instance
    return createClassInstance(className, superClass, classLoader);
  }


  /**
   * Gets the file name that is described by a URL with the <code>file://</code>
   * protocol.
   *
   * @param url The URL to get the file name for.
   * @return The file name that matches the URL.
   * @throws RegainException If the URL's protocol isn't <code>file://</code>.
   */
  public static String urlToFileName(String url) throws RegainException {
    if (! url.startsWith("file://")) {
      throw new RegainException("URL must have the file:// protocol to get a "
        + "File for it");
    }
  
    // Cut the file://
    String fileName = url.substring(7);
  
    // Replace %20 by spaces
    return replace(fileName, "%20", " ");
  }


  /**
   * Gets the file that is described by a URL with the <code>file://</code>
   * protocol.
   *
   * @param url The URL to get the file for.
   * @return The file that matches the URL.
   * @throws RegainException If the URL's protocol isn't <code>file://</code>.
   */
  public static File urlToFile(String url) throws RegainException {
    return new File(urlToFileName(url));
  }


  /**
   * Gets the URL of a file name.
   *
   * @param fileName The file name to get the URL for
   * @return The URL of the file.
   */
  public static String fileNameToUrl(String fileName) {
    // Replace spaces by %20
    fileName = replace(fileName, " ", "%20");
  
    // Replace file separators by /
    fileName = replace(fileName, File.separator, "/");
  
    return "file://" + fileName;
  }


  /**
   * Gets the URL of a file.
   *
   * @param file The file to get the URL for
   * @return The URL of the file.
   */
  public static String fileToUrl(File file) {
    return fileNameToUrl(file.getAbsolutePath());
  }


  /**
   * URL-encodes a String. 
   * 
   * @param text The String to URL-encode.
   * @return The URL-encoded String.
   * @throws RegainException If URL-encoding failed.
   */
  public static String urlEncode(String text) throws RegainException {
    try {
      return URLEncoder.encode(text, "UTF-8");
    }
    catch (UnsupportedEncodingException exc) {
      throw new RegainException("URL-encoding failed: '" + text + "'", exc);
    }
  }


  /**
   * URL-decodes a String. 
   * 
   * @param text The String to URL-decode.
   * @return The URL-decoded String.
   * @throws RegainException If URL-decoding failed.
   */
  public static String urlDecode(String text) throws RegainException {
    try {
      return URLDecoder.decode(text, "UTF-8");
    }
    catch (UnsupportedEncodingException exc) {
      throw new RegainException("URL-decoding failed: '" + text + "'", exc);
    }
  }


  // inner class WrapperAnalyzer
  
  
  /**
   * An analyzer that changes a document in lowercase before delivering
   * it to a nested analyzer. For the field "groups" an analyzer is used that
   * only tokenizes the input without stemming the tokens.
   */
  private static class WrapperAnalyzer extends Analyzer {
    
    /** The analyzer to use for the "groups" field. */
    private Analyzer mGroupsAnalyzer;
    /** The nested analyzer. */
    private Analyzer mNestedAnalyzer;
    
    
    /**
     * Creates a new instance of WrapperAnalyzer.
     * 
     * @param nestedAnalyzer The nested analyzer.
     */
    public WrapperAnalyzer(Analyzer nestedAnalyzer) {
      mGroupsAnalyzer = new SimpleAnalyzer();
      mNestedAnalyzer = nestedAnalyzer;
    }
    
    
    /**
     * Creates a TokenStream which tokenizes all the text in the provided
     * Reader.
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
      if (fieldName.equals("groups")) {
        return mGroupsAnalyzer.tokenStream(fieldName, reader);
      } else {
        Reader lowercasingReader = new LowercasingReader(reader);
        return mNestedAnalyzer.tokenStream(fieldName, lowercasingReader);
      }
    }
    
  } // inner class WrapperAnalyzer


  // inner class LowercasingReader


  /**
   * Liest alle Zeichen von einem eingebetteten Reader in Kleinschreibung.
   *
   * @author Til Schneider, www.murfman.de
   */
  private static class LowercasingReader extends Reader {

    /** Der eingebettete Reader. */
    private Reader mNestedReader;


    /**
     * Erzeugt eine neue LowercasingReader-Instanz.
     *
     * @param nestedReader Der Reader, von dem die Daten kommen, die in
     *        Kleinschreibung gewandelt werden sollen.
     */
    public LowercasingReader(Reader nestedReader) {
      mNestedReader = nestedReader;
    }


    /**
     * Schließt den eingebetteten Reader.
     *
     * @throws IOException Wenn der eingebettete Reader nicht geschlossen werden
     *         konnte.
     */
    public void close() throws IOException {
      mNestedReader.close();
    }


    /**
     * Liest Daten vom eingebetteten Reader und wandelt sie in Kleinschreibung.
     *
     * @param cbuf Der Puffer, in den die gelesenen Daten geschrieben werden
     *        sollen
     * @param off Der Offset im Puffer, ab dem geschreiben werden soll.
     * @param len Die max. Anzahl von Zeichen, die geschrieben werden soll.
     * @return Die Anzahl von Zeichen, die tatsächlich geschrieben wurde, bzw.
     *         <code>-1</code>, wenn keine Daten mehr verfügbar sind.
     * @throws IOException Wenn nicht vom eingebetteten Reader gelesen werden
     *         konnte.
     */
    public int read(char[] cbuf, int off, int len) throws IOException {
      // Read the data
      int charCount = mNestedReader.read(cbuf, off, len);

      // Make it lowercase
      if (charCount != -1) {
        for (int i = off; i < off + charCount; i++) {
          cbuf[i] = Character.toLowerCase(cbuf[i]);
        }
      }

      // Return the number of chars read
      return charCount;
    }

  } // inner class LowercasingReader

}
