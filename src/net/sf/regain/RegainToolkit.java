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
 *     $Date: 2011-04-27 19:58:02 +0200 (Mi, 27 Apr 2011) $
 *   $Author: thtesche $
 * $Revision: 488 $
 */
package net.sf.regain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import jcifs.smb.SmbFile;
import net.sf.regain.util.io.PathFilenamePair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.Version;

/**
 * Enthält Hilfsmethoden, die sowohl vom Crawler als auch von der Suchmaske
 * genutzt werden.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RegainToolkit {

  /** The encoding used for storing URLs in the index */
  public static final String INDEX_ENCODING = "UTF-8";
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
  /** The cached system's default encoding. */
  private static String mSystemDefaultEncoding;
  /** Der gecachte, systemspeziefische Zeilenumbruch. */
  private static String mLineSeparator;

  /**
   * Löscht ein Verzeichnis mit allen Unterverzeichnissen und -dateien.
   *
   * @param dir Das zu löschende Verzeichnis.
   *
   * @throws RegainException Wenn das Lï¿½schen fehl schlug.
   */
  public static void deleteDirectory(File dir) throws RegainException {
    if (!dir.exists()) {
      return; // Nothing to do
    }

    // Delete all children
    File[] children = dir.listFiles();
    if (children != null) {
      for (int i = 0; i < children.length; i++) {
        if (children[i].isDirectory()) {
          deleteDirectory(children[i]);
        } else {
          if (!children[i].delete()) {
            throw new RegainException("Deleting " + children[i].getAbsolutePath()
                    + " failed!");
          }
        }
      }
    }

    // Delete self
    if (!dir.delete()) {
      throw new RegainException("Deleting " + dir.getAbsolutePath() + " failed!");
    }
  }

  /**
   * Writes all data from the reader to the writer.
   * <p>
   * Neither the reader nor the writer will be closed. This has to be done by
   * the caller!
   *
   * @param reader The reader that provides the data.
   * @param writer The writer where to write the data.
   *
   * @throws IOException If reading or writing failed.
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
   * muss die aufrufende Methode ï¿½bernehmen!
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
    } catch (IOException exc) {
      throw new RegainException("Copying file from " + from.getAbsolutePath()
              + " to " + to.getAbsolutePath() + " failed", exc);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException exc) {
        }
      }
      if (in != null) {
        try {
          in.close();
        } catch (IOException exc) {
        }
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
          throws RegainException {
    File[] indexFiles = fromDir.listFiles();
    for (int i = 0; i < indexFiles.length; i++) {
      String fileName = indexFiles[i].getName();
      File targetFile = new File(toDir, fileName);
      if (indexFiles[i].isDirectory()) {
        if (copySubDirs) {
          targetFile.mkdir();
          copyDirectory(indexFiles[i], targetFile, copySubDirs, excludeExtension);
        }
      } else if ((excludeExtension == null) || (!fileName.endsWith(excludeExtension))) {
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
          throws RegainException {
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
          throws RegainException {
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
    } catch (IOException exc) {
      throw new RegainException("Reading String from stream failed", exc);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException exc) {
        }
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
          throws RegainException {
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
    if (!file.exists()) {
      return null;
    }

    FileInputStream stream = null;
    try {
      stream = new FileInputStream(file);
      return readStringFromStream(stream);
    } catch (IOException exc) {
      throw new RegainException("Reading String from " + file.getAbsolutePath()
              + "failed", exc);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }

  /**
   * Reads a word list from a file.
   *
   * @param file The file to read the list from.
   *
   * @return The lines of the file.
   * @throws RegainException If reading failed.
   */
  public static String[] readListFromFile(File file) throws RegainException {
    if (!file.exists()) {
      return null;
    }

    FileReader reader = null;
    BufferedReader buffReader = null;
    try {
      reader = new FileReader(file);
      buffReader = new BufferedReader(reader);

      ArrayList list = new ArrayList();
      String line;
      while ((line = buffReader.readLine()) != null) {
        list.add(line);
      }

      String[] asArr = new String[list.size()];
      list.toArray(asArr);

      return asArr;
    } catch (IOException exc) {
      throw new RegainException("Reading word list from " + file.getAbsolutePath()
              + "failed", exc);
    } finally {
      if (buffReader != null) {
        try {
          buffReader.close();
        } catch (IOException exc) {
        }
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException exc) {
        }
      }
    }
  }

  /**
   * Writes data to a file
   *
   * @param data The data
   * @param file The file to write to
   *
   * @throws RegainException When writing failed
   */
  public static void writeToFile(byte[] data, File file)
          throws RegainException {
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(file);
      stream.write(data);
      stream.close();
    } catch (IOException exc) {
      throw new RegainException("Writing file failed: " + file.getAbsolutePath(), exc);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }

  /**
   * Writes a String into a file.
   *
   * @param text The string.
   * @param file The file to write to.
   *
   * @throws RegainException If writing failed.
   */
  public static void writeToFile(String text, File file)
          throws RegainException {
    writeListToFile(new String[]{text}, file);
  }

  /**
   * Writes a word list in a file. Each item of the list will be written in a
   * line.
   *
   * @param wordList The word list.
   * @param file The file to write to.
   *
   * @throws RegainException If writing failed.
   */
  public static void writeListToFile(String[] wordList, File file)
          throws RegainException {
    if ((wordList == null) || (wordList.length == 0)) {
      // Nothing to do
      return;
    }

    FileOutputStream stream = null;
    PrintStream printer = null;
    try {
      stream = new FileOutputStream(file);
      printer = new PrintStream(stream);

      for (int i = 0; i < wordList.length; i++) {
        printer.println(wordList[i]);
      }
    } catch (IOException exc) {
      throw new RegainException("Writing word list to " + file.getAbsolutePath()
              + " failed", exc);
    } finally {
      if (printer != null) {
        printer.close();
      }
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
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
   * Returns the destinct values of one or more fields.
   * <p>
   * If an index directory is provided, then the values will be read from there.
   * They will be extracted from the search index if there are no matching
   * cache files. After extracting the cache files will be created, so the next
   * call will be faster.
   *
   * @param indexReader The index reader to use for reading the field values.
   * @param fieldNameArr The names of the fields to read the destinct values for.
   * @param indexDir The index directory where to read or write the cached
   *        destinct values. May be null.
   * @return A hashmap containing for a field name (key, String) the sorted
   *         array of destinct values (value, String[]).
   * @throws RegainException If reading from the index failed. Or if reading or
   *         writing a cache file failed.
   */
  public static HashMap readFieldValues(IndexReader indexReader,
          String[] fieldNameArr, File indexDir)
          throws RegainException {
    // Create the result map
    HashMap resultMap = new HashMap();

    // Try to read the field values from the cache files
    // and remember the failed field names in a set
    HashSet fieldsToReadSet = new HashSet();
    for (int i = 0; i < fieldNameArr.length; i++) {
      String field = fieldNameArr[i];

      String[] fieldValueArr = null;
      if (indexDir != null) {
        File fieldFile = new File(indexDir, "field_values_" + field + ".txt");

        // NOTE: fieldValueArr stays null if the file does not exist
        fieldValueArr = readListFromFile(fieldFile);
      }

      if (fieldValueArr != null) {
        resultMap.put(field, fieldValueArr);
      } else {
        // There is no cache file -> We have to read the values from the index
        fieldsToReadSet.add(field);

        // Add an empty ArrayList that can hold the values
        resultMap.put(field, new ArrayList());
      }
    }

    // For bug-prevention: Enforce the usage of the fieldsToReadSet
    // (There may be some field names removed)
    fieldNameArr = null;

    // Read the terms
    if (!fieldsToReadSet.isEmpty()) {
      try {
        TermEnum termEnum = indexReader.terms();
        while (termEnum.next()) {
          Term term = termEnum.term();
          String field = term.field();
          if (fieldsToReadSet.contains(field)) {
            // This is a value of a wanted field
            ArrayList valueList = (ArrayList) resultMap.get(field);
            valueList.add(term.text());
          }
        }
      } catch (IOException exc) {
        throw new RegainException("Reading terms from index failed", exc);
      }
    }

    // Convert the lists into arrays.
    Iterator readFieldIter = fieldsToReadSet.iterator();
    while (readFieldIter.hasNext()) {
      String field = (String) readFieldIter.next();

      ArrayList valueList = (ArrayList) resultMap.get(field);
      String[] valueArr = new String[valueList.size()];
      valueList.toArray(valueArr);

      // Sort the array
      Arrays.sort(valueArr);

      // Overwrite the list with the array
      resultMap.put(field, valueArr);

      // Write the results to a file
      if (indexDir != null) {
        File fieldFile = new File(indexDir, "field_values_" + field + ".txt");
        writeListToFile(valueArr, fieldFile);
      }
    }

    return resultMap;
  }

  /**
   * Creates an analyzer that is used both from the crawler and the search mask.
   * It is important that both use the same analyzer which is the reason for
   * this method.
   *
   * @param analyzerType The type of the analyzer to create. Either a classname
   *        or "english" or "german".
   * @param stopWordList All words that should not be indexed.
   * @param exclusionList All words that shouldn't be changed by the analyzer.
   * @param untokenizedFieldNames The names of the fields that should not be
   *        tokenized.
   * @return The analyzer.
   * @throws RegainException If the creation failed.
   */
  public static Analyzer createAnalyzer(String analyzerType,
          String[] stopWordList, String[] exclusionList, String[] untokenizedFieldNames)
          throws RegainException {
    if (analyzerType == null) {
      throw new RegainException("No analyzer type specified!");
    }

    // Get the analyzer class name
    analyzerType = analyzerType.trim();
    String analyzerClassName = analyzerType;
    if (analyzerType.equalsIgnoreCase("english")) {
      analyzerClassName = EnglishAnalyzer.class.getName();
    } else if (analyzerType.equalsIgnoreCase("german")
            || analyzerType.equalsIgnoreCase("deutsch")) {
      analyzerClassName = GermanAnalyzer.class.getName();
    } else if (analyzerType.equalsIgnoreCase("french")
            || analyzerType.equalsIgnoreCase("francais")) {
      analyzerClassName = FrenchAnalyzer.class.getName();
    } else if (analyzerType.equalsIgnoreCase("italian")
            || analyzerType.equalsIgnoreCase("italiano")) {
      analyzerClassName = ItalianAnalyzer.class.getName();
    }

    // Get the analyzer class
    Class analyzerClass;
    try {
      analyzerClass = Class.forName(analyzerClassName);
    } catch (ClassNotFoundException exc) {
      throw new RegainException("Analyzer class not found: " + analyzerClassName, exc);
    }

    // Extract stopword 'Set'. Since lucene 3.x
    Set<String> stopWordSet = new HashSet<String>();
    stopWordSet.addAll(Arrays.asList(stopWordList));

    // Create an instance
    Analyzer analyzer;
    if ((stopWordList != null) && (stopWordList.length != 0)) {
      // Copy
      Constructor ctor;
      try {
        ctor = analyzerClass.getConstructor(
                new Class[]{Version.class, Set.class});
      } catch (Throwable thr) {
        throw new RegainException("Analyzer " + analyzerType
                + " does not support stop words", thr);
      }
      try {
        analyzer = (Analyzer) ctor.newInstance(new Object[]{Version.LUCENE_30, stopWordSet});
      } catch (Throwable thr) {
        throw new RegainException("Creating analyzer instance failed", thr);
      }

    } else {
      // instantiate analyser whitout stopwords.
      try {
        Constructor analyzerWithoutStopWords;
        try {
          analyzerWithoutStopWords = analyzerClass.getConstructor(
                  new Class[]{Version.class});
        } catch (Throwable thr) {
          throw new RegainException("Analyzer " + analyzerType
                  + " is not supported.", thr);
        }

        analyzer = (Analyzer) analyzerWithoutStopWords.newInstance(new Object[]{Version.LUCENE_30});
      } catch (Throwable thr) {
        throw new RegainException("Creating analyzer instance failed", thr);
      }
    }

    // Try to apply the exclusion list
    if ((exclusionList != null) && (exclusionList.length != 0)) {
      // NOTE: This is supported by the GermanAnalyzer for instance
      Method setter;
      try {
        setter = analyzerClass.getMethod("setStemExclusionTable",
                new Class[]{exclusionList.getClass()});
      } catch (Throwable thr) {
        throw new RegainException("Analyzer " + analyzerType
                + " does not support exclusion lists");
      }

      try {
        setter.invoke(analyzer, new Object[]{exclusionList});
      } catch (Throwable thr) {
        throw new RegainException("Applying exclusion list failed.", thr);
      }
    }

    analyzer = new WrapperAnalyzer(analyzer, untokenizedFieldNames);

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
   * @param nestedAnalyzer The nested Analyzer that should be analysed
   * @return Ein Analyzer, der die Aufrufe an einen eingebetteten Analyzer
   *         analysiert.
   */
  private static Analyzer createAnalysingAnalyzer(final Analyzer nestedAnalyzer) {
    return new Analyzer() {

      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        // NOTE: For Analyzation we have to read the reader twice:
        //       Once for the analyzation and second for the returned TokenStream
        //       -> We save the content of the Reader in a String and read this
        //          String twice.
        //       -> Old behaviour!
        try {
          // Save the content of the reader in a String
          StringWriter writer = new java.io.StringWriter();
          pipe(reader, writer);
          String asString = writer.toString();

          // Analyze the call
          TokenStream stream = nestedAnalyzer.tokenStream(fieldName,
                  new StringReader(asString));
          TermAttribute termAtt = (TermAttribute) stream.addAttribute(TermAttribute.class);

          System.out.println("Tokens for '" + asString + "':");
          while (stream.incrementToken()) {
            System.out.println(" '" + termAtt.term() + "'");
          }
          stream.reset();
          return stream;
          // Do the call a second time and return the result this time
          // Old behaviour
          // return nestedAnalyzer.tokenStream(fieldName, new StringReader(asString));
        } catch (IOException exc) {
          System.out.println("exc: " + exc);

          return null;
        }
      }
    };
  }

  /**
   * Replaces in a string all occurences of <code>pattern</code> with
   * <code>replacement</code>.
   * <p>
   * Note: <code>pattern</code> may be a substring of <code>replacement</code>.
   *
   * @param source The string to search in
   * @param pattern The pattern to be replaced
   * @param replacement The replacement for each occurence of the pattern.
   *
   * @return A string where all occurences of <code>pattern</code> are replaced
   *         by <code>replacement</code>.
   */
  public static String replace(String source, String pattern, String replacement) {
    // Check whether the pattern occurs in the source at all
    int pos = source.indexOf(pattern);
    if (pos == -1) {
      // The pattern does not occur in the source -> return the source
      return source;
    }

    // Build a new String where pattern is replaced by the replacement
    StringBuilder target = new StringBuilder(source.length());
    int start = 0; // The start of a part without the pattern
    do {
      target.append(source.substring(start, pos));
      target.append(replacement);
      start = pos + pattern.length();
    } while ((pos = source.indexOf(pattern, start)) != -1);
    target.append(source.substring(start, source.length()));

    // return the String
    return target.toString();
  }

  /**
   * Replaces in a string all occurences of a list of patterns with replacements.
   * <p>
   * Note: The string is searched left to right. So any pattern matching earlier
   * in the string will be replaced.
   * Example: replace("abcd", { "bc", "ab", "cd" }, { "x", "1", "2" }) will
   * return "12" (the pattern "bc" won't be applied, since "ab" matches before).
   * <p>
   * Note: If two patterns match at the same position, then the first one
   * defined will be applied.
   * Example: replace("abcd", { "ab", "abc" }, { "1", "2" }) will return "1cd".
   *
   * @param source The string to search in
   * @param patternArr The pattern to be replaced
   * @param replacementArr The replacement for each occurence of the pattern.
   *
   * @return A string where all occurences of <code>pattern</code> are replaced
   *         by <code>replacement</code>.
   */
  public static String replace(String source, String[] patternArr,
          String[] replacementArr) {
    if (patternArr.length != replacementArr.length) {
      throw new IllegalArgumentException("patternArr and replacementArr must "
              + "have the same length: " + patternArr.length + " != "
              + replacementArr.length);
    }

    // Check whether the patterns occurs in the source at all
    int[] posArr = new int[patternArr.length];
    int minPos = Integer.MAX_VALUE;
    int minPosIdx = -1;
    for (int i = 0; i < posArr.length; i++) {
      posArr[i] = source.indexOf(patternArr[i]);
      if (posArr[i] != -1 && posArr[i] < minPos) {
        minPos = posArr[i];
        minPosIdx = i;
      }
    }
    if (minPosIdx == -1) {
      // The patterns do not occur in the source -> return the source
      return source;
    }

    // Build a new String where patterns are replaced by the replacements
    StringBuilder target = new StringBuilder(source.length());
    int start = 0;    // The start of a part without the pattern
    do {
      target.append(source.substring(start, minPos));
      target.append(replacementArr[minPosIdx]);
      start = minPos + patternArr[minPosIdx].length();

      // Find the next matching pattern
      minPos = Integer.MAX_VALUE;
      minPosIdx = -1;
      for (int i = 0; i < posArr.length; i++) {
        if (posArr[i] < start) {
          // The last match was before the current position
          // -> Find the next match for that pattern
          posArr[i] = source.indexOf(patternArr[i], start);
        }
        if (posArr[i] != -1 && posArr[i] < minPos) {
          minPos = posArr[i];
          minPosIdx = i;
        }
      }
    } while (minPosIdx != -1);
    target.append(source.substring(start, source.length()));

    // return the String
    return target.toString();
  }

  /**
   * Gibt einen Wert in Prozent mit zwei Nachkommastellen zurï¿½ck.
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
   * Gibt einen fï¿½r den Menschen gut lesbaren String fï¿½r eine Anzahl Bytes
   * zurï¿½ck.
   *
   * @param bytes Die Anzahl Bytes
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes) {
    return bytesToString(bytes, Locale.ENGLISH);
  }

  /**
   * Gibt einen fï¿½r den Menschen gut lesbaren String fï¿½r eine Anzahl Bytes
   * zurï¿½ck.
   *
   * @param bytes Die Anzahl Bytes
   * @param locale The locale to use for formatting the numbers.
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes, Locale locale) {
    return bytesToString(bytes, 2, locale);
  }

  /**
   * Gibt einen fï¿½r den Menschen gut lesbaren String fï¿½r eine Anzahl Bytes
   * zurï¿½ck.
   *
   * @param bytes Die Anzahl Bytes
   * @param fractionDigits Die Anzahl der Nachkommastellen
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes, int fractionDigits) {
    return bytesToString(bytes, fractionDigits, Locale.ENGLISH);
  }

  /**
   * Gibt einen fï¿½r den Menschen gut lesbaren String fï¿½r eine Anzahl Bytes
   * zurï¿½ck.
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
    } else if (bytes > SIZE_MB) {
      factor = SIZE_MB;
      unit = "MB";
    } else if (bytes > SIZE_KB) {
      factor = SIZE_KB;
      unit = "kB";
    } else {
      return bytes + " Byte";
    }

    NumberFormat format = NumberFormat.getInstance(locale);
    format.setMinimumFractionDigits(fractionDigits);
    format.setMaximumFractionDigits(fractionDigits);

    String asString = format.format((double) bytes / (double) factor);

    return asString + " " + unit;
  }

  /**
   * Gets a human readable String for a time.
   *
   * @param time The time in milliseconds.
   * @return The time as String.
   */
  public static String toTimeString(long time) {
    if (time == -1) {
      // This is no time
      return "?";
    }

    long millis = time % 1000;
    time /= 1000;
    long secs = time % 60;
    time /= 60;
    long mins = time % 60;
    time /= 60;
    long hours = time;

    if (hours != 0) {
      return hours + ":"
              + ((mins > 9) ? "" : "0") + mins + ":"
              + ((secs > 9) ? "" : "0") + secs + " h";
    } else if (mins != 0) {
      return mins + ":"
              + ((secs > 9) ? "" : "0") + secs + " min";
    } else if (secs != 0) {
      NumberFormat format = NumberFormat.getInstance();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);

      String asString = format.format(secs + millis / 1000.0);

      return asString + " sec";
    } else {
      return millis + " millis";
    }
  }

  /**
   * Konvertiert ein Date-Objekt in einen String mit dem Format
   * "YYYY-MM-DD HH:MM". Das ist nötig, um ein eindeutiges und vom Menschen
   * lesbares Format zu haben.
   * <p>
   * Dieses Format ist mit Absicht nicht lokalisiert, um die Eindeutigkeit zu
   * gewï¿½hrleisten. Die Lokalisierung muss die Suchmaske ï¿½bernehmen.
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

    StringBuilder buffer = new StringBuilder(16);

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
   * Konvertiert einen String mit dem Format "YYYY-MM-DD HH:MM" in ein Date-Objekt.
   *
   * @param asString Der zu konvertierende String
   * @return Das konvertierte Date-Objekt.
   * @throws RegainException Wenn der String ein falsches Format hat.
   * @see #lastModifiedToString(Date)
   */
  public static Date stringToLastModified(String asString)
          throws RegainException {
    Calendar cal = Calendar.getInstance();

    try {
      // Format: "YYYY-MM-DD HH:MM"

      int year = Integer.parseInt(asString.substring(0, 4));
      cal.set(Calendar.YEAR, year);
      int month = Integer.parseInt(asString.substring(5, 7));
      cal.set(Calendar.MONTH, month - 1); // -1: In the Date class january is 0
      int day = Integer.parseInt(asString.substring(8, 10));
      cal.set(Calendar.DAY_OF_MONTH, day);

      int hour = Integer.parseInt(asString.substring(11, 13));
      cal.set(Calendar.HOUR_OF_DAY, hour);
      int minute = Integer.parseInt(asString.substring(14, 16));
      cal.set(Calendar.MINUTE, minute);
      cal.set(Calendar.SECOND, 0);
    } catch (Throwable thr) {
      throw new RegainException("Last-modified-string has not the format"
              + "'YYYY-MM-DD HH:MM': " + asString, thr);
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
   * Gibt den systemspeziefischen Zeilenumbruch zurï¿½ck.
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
   * Returns the system's default encoding.
   *
   * @return the system's default encoding.
   */
  public static String getSystemDefaultEncoding() {
    if (mSystemDefaultEncoding == null) {
      mSystemDefaultEncoding = new InputStreamReader(System.in).getEncoding();
    }

    return mSystemDefaultEncoding;
  }

  /**
   * Checks whether the given String contains whitespace.
   * 
   * @param str The String to check.
   * @return Whether the given String contains whitespace.
   */
  public static boolean containsWhitespace(String str) {
    for (int i = 0; i < str.length(); i++) {
      if (Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks an array of group names.
   * 
   * @param accessController The access controller that returned the array of
   *        group names.
   * @param groupArr The array of group names to check.
   * @throws RegainException If the array of group names is not valid.
   */
  public static void checkGroupArray(Object accessController, String[] groupArr)
          throws RegainException {
    if (groupArr == null) {
      // Check for null
      throw new RegainException("Access controller "
              + accessController.getClass().getName() + " returned illegal "
              + "group array: null");
    } else {
      // Check for whitespace
      for (int i = 0; i < groupArr.length; i++) {
        if (RegainToolkit.containsWhitespace(groupArr[i])) {
          throw new RegainException("Access controller "
                  + accessController.getClass().getName() + " returned illegal "
                  + "group name containing whitespace: '" + groupArr[i] + "'");
        }
      }
    }
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
          throws RegainException {
    // Load the class
    Class clazz;
    try {
      if (classLoader == null) {
        clazz = Class.forName(className);
      } else {
        clazz = classLoader.loadClass(className);
      }
    } catch (ClassNotFoundException exc) {
      throw new RegainException("The class '" + className
              + "' does not exist", exc);
    }

    // Create the instance
    Object obj;
    try {
      obj = clazz.newInstance();
    } catch (Exception exc) {
      throw new RegainException("Error creating instance of class "
              + className, exc);
    }

    // Check the instance
    if (!superClass.isInstance(obj)) {
      throw new RegainException("The class " + className + " does not "
              + "implement " + superClass.getName());
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
          throws RegainException {
    // Create a class loader for the jar file
    ClassLoader classLoader = null;
    if (jarFileName != null) {
      File jarFile = new File(jarFileName);
      if (!jarFile.exists()) {
        throw new RegainException("Jar file does not exist: "
                + jarFile.getAbsolutePath());
      }

      try {
        classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, superClass.getClassLoader());
      } catch (MalformedURLException exc) {
        throw new RegainException("Creating class loader for "
                + "jar file failed: " + jarFile.getAbsolutePath(),
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
    if (!url.startsWith("file://")) {
      throw new RegainException("URL must have the file:// protocol to get a "
              + "File for it");
    }

    // Cut the file://
    String fileName = url.substring(7);

    // Replace URL-encoded special characters
    return urlDecode(fileName, INDEX_ENCODING);
  }

  /**
   * Gets the 'real' file name that is described by a URL with the <code>file://</code>
   * protocol. This file name does not contain a path, protocol and drive-letter
   *
   * @param url The URL to extract the file name from.
   * @return The file name that matches the URL.
   * @throws RegainException.
   */
  public static String urlToWhitespacedFileName(String url) throws RegainException {

    // Cut file name from path
    PathFilenamePair pfPair = fragmentUrl(url);
    String fileName = pfPair.getFilename();
    if (fileName != null) {
      int lastDot = fileName.lastIndexOf(".");
      // Remove the extension
      String fileNameWithoutExtension = "";
      if (lastDot > 0 && lastDot < fileName.length()) {
        fileNameWithoutExtension = fileName.substring(0, lastDot);
      }
      String fileNameWhitespaced = fileNameWithoutExtension.replaceAll("\\.", " ").replaceAll("-", " ").replaceAll("_", " ");
      // Reset fileNameWhitespaced in case we couldn't expand the filename
      // to a whitespaced version
      if (fileNameWithoutExtension.equals(fileNameWhitespaced)) {
        fileNameWhitespaced = "";
      }
      // Replace URL-encoded special characters
      return urlDecode(fileName + " "
              + fileNameWithoutExtension + " "
              + fileNameWhitespaced, INDEX_ENCODING).trim();
    } else {
      return "";
    }
  }

  /**
   * Constructs a path-filename pair from a given URL.
   *
   * @param url the url
   * @return a path-filename pair
   *
   * @throws RegainException
   */
  public static PathFilenamePair fragmentUrl(String url) throws RegainException {

    PathFilenamePair pfPair = new PathFilenamePair();
    int lastSlash = url.lastIndexOf("/");
    // Cut file name from path
    if (lastSlash > 0 && lastSlash + 1 < url.length()) {
      String fileName = url.substring(lastSlash + 1);
      String path = url.substring(0, lastSlash + 1);
      path = removeProtocol(path);
      pfPair.setFilename(fileName);
      pfPair.setPath(path);
    } else {
      pfPair.setPath("");
      pfPair.setFilename("");
    }

    return pfPair;
  }

  /**
   * Removes the protocol from a given path.
   *
   * @param path the path
   * @return a path without a protocol
   */
  public static String removeProtocol(String path) {
    String newPath = "";
    if (path != null) {
      newPath = path.replace("file://", "");
      newPath = newPath.replace("http://", "");
      newPath = newPath.replace("https://", "");
      newPath = newPath.replace("imap://", "");
      newPath = newPath.replace("imaps://", "");
      newPath = newPath.replace("smb://", "");
    }

    return newPath;
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
   * Gets the smbfile that is described by a URL with the <code>smb://</code>
   * protocol.
   *
   * @param url The URL to get the smbfile for.
   * @return The smbfile that matches the URL.
   * @throws RegainException If the URL's protocol isn't <code>smb://</code>.
   */
  public static SmbFile urlToSmbFile(String url) throws RegainException {

    try {
      return new SmbFile(urlToSmbFileName(url));
    } catch (MalformedURLException urlEx) {
      throw new RegainException(urlEx.getMessage(), urlEx);
    }
  }

  /**
   * Gets the smb file name that is described by a URL with the <code>smb://</code>
   * protocol.
   *
   * @param url The URL to get the file name for.
   * @return The smb file name that matches the URL.
   * @throws RegainException If the URL's protocol isn't <code>smb://</code>.
   */
  public static String urlToSmbFileName(String url) throws RegainException {
    if (!url.startsWith("smb://")) {
      throw new RegainException("URL must have the smb:// protocol to get a "
              + "File for it");
    }
    // Replace URL-encoded special characters
    return urlDecode(url, INDEX_ENCODING);
  }

  /**
   * Returns the URL of a file name.
   *
   * @param fileName The file name to get the URL for
   * @return The URL of the file.
   * @throws RegainException If URL-encoding failed. 
   */
  public static String fileNameToUrl(String fileName)
          throws RegainException {
    // Replace special characters
    fileName = urlEncode(fileName, INDEX_ENCODING);

    // Replace file separators by /
    // NOTE: "/" is "%2F", "\" is "%5C"
    fileName = replace(fileName, "%2F", "/");
    fileName = replace(fileName, "%5C", "/"); // Yes: "\" should become "/"
    fileName = replace(fileName, "\\", "/");

    return "file://" + fileName;
  }

  /**
   * Returns the URL of a file.
   *
   * @param file The file to get the URL for
   * @return The URL of the file.
   * @throws RegainException If URL-encoding failed. 
   */
  public static String fileToUrl(File file)
          throws RegainException {
    return fileNameToUrl(file.getAbsolutePath());
  }

  /**
   * Gets the canonical URL of a file (no symbolic links, normalised names etc).
   * Symbolic link detection may fail in certain situations, like for NFS file systems
   *
   * @param file The file to get the canonical URL for
   * @return The URL of the file.
   * @throws RegainException If URL-encoding failed. 
   */
  public static String fileToCanonicalUrl(File file)
          throws RegainException {
    String canUrl = null;
    try {
      //This may throw SecurityException
      canUrl = file.getCanonicalPath();
    } catch (Exception e) {
      return null;
    }
    //Canonical url returns "current dir:parh"
    int pos = canUrl.indexOf(':') + 1;
    if (pos > 0 && pos < canUrl.length()) {
      canUrl = canUrl.substring(pos);
    }

    return fileNameToUrl(canUrl);
  }

  /**
   * URL-encodes a String. 
   * 
   * @param text The String to URL-encode.
   * @param encoding The encoding to use. 
   * @return The URL-encoded String.
   * @throws RegainException If URL-encoding failed.
   */
  public static String urlEncode(String text, String encoding) throws RegainException {
    try {
      return URLEncoder.encode(text, encoding);
    } catch (UnsupportedEncodingException exc) {
      throw new RegainException("URL-encoding failed: '" + text + "'", exc);
    }
  }

  /**
   * URL-decodes a String. 
   * 
   * @param text The String to URL-decode.
   * @param encoding The encoding to use. 
   * @return The URL-decoded String.
   * @throws RegainException If URL-decoding failed.
   */
  public static String urlDecode(String text, String encoding) throws RegainException {
    try {
      return URLDecoder.decode(text, encoding);
    } catch (UnsupportedEncodingException exc) {
      throw new RegainException("URL-decoding failed: '" + text + "'", exc);
    }
  }

  /**
   * Creates a summary from given content
   * <p>
   * The method returns <code>null</code> if no summary could created
   *
   * @param content The content for which the summary is referring to
   * @param maxLength The maximum length of the created summary
   * @return The summary (first n characters of content
   */
  public static String createSummaryFromContent(String content, int maxLength) {

    if (content.length() > maxLength) {
      // cut the content only if it exceeds the max size for the summary
      int lastSpacePos = content.lastIndexOf(' ', maxLength);

      if (lastSpacePos == -1) {
        return null;
      } else {
        return content.substring(0, lastSpacePos) + " ...";
      }
    } else {
      return content;
    }
  }

  /**
   * Creates a field identifier for fields with highlighted content. All high-
   * lighted content will be stored in a field named 'highlightedOldfieldname' where
   * oldfieldname was in lowercase before renaming.
   * <p>
   * The method returns <code>null</code> if no field identifier could created
   *
   * @param field The content for which the summary is referring to
   * @return the new field identifier
   */
  public static String createHighlightedFieldIdent(String fieldName) {

    if (fieldName != null && fieldName.length() > 1) {
      return "highlighted" + fieldName.substring(0, 1).toUpperCase()
              + fieldName.substring(1, fieldName.length());
    } else {
      return null;
    }
  }

  // inner class WrapperAnalyzer
  /**
   * An analyzer that changes a document in lowercase before delivering
   * it to a nested analyzer. For the field "groups" an analyzer is used that
   * only tokenizes the input without stemming the tokens.
   */
  private static class WrapperAnalyzer extends Analyzer {

    /** The analyzer to use for a field that shouldn't be stemmed. */
    private Analyzer mNoStemmingAnalyzer;
    /** The nested analyzer. */
    private Analyzer mNestedAnalyzer;
    /** The names of the fields that should not be tokenized. */
    private HashSet mUntokenizedFieldNames;

    /**
     * Creates a new instance of WrapperAnalyzer.
     * 
     * @param nestedAnalyzer The nested analyzer.
     * @param untokenizedFieldNames The names of the fields that should not be
     *        tokenized.
     */
    public WrapperAnalyzer(Analyzer nestedAnalyzer, String[] untokenizedFieldNames) {
      mNoStemmingAnalyzer = new WhitespaceAnalyzer();
      mNestedAnalyzer = nestedAnalyzer;

      mUntokenizedFieldNames = new HashSet();
      mUntokenizedFieldNames.addAll(Arrays.asList(untokenizedFieldNames));
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the provided
     * Reader.
     */
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      boolean useStemming = true;
      // NOTE: For security reasons we explicitely check for the groups field
      //       and don't use the mUntokenizedFieldNames for this implicitely
      if (fieldName.equals("groups") || mUntokenizedFieldNames.contains(fieldName)) {
        useStemming = false;
      }

      if (useStemming) {
        Reader lowercasingReader = new LowercasingReader(reader);
        return mNestedAnalyzer.tokenStream(fieldName, lowercasingReader);
      } else {
        return mNoStemmingAnalyzer.tokenStream(fieldName, reader);
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
     * Schlieï¿½t den eingebetteten Reader.
     *
     * @throws IOException Wenn der eingebettete Reader nicht geschlossen werden
     *         konnte.
     */
    @Override
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
     * @return Die Anzahl von Zeichen, die tatsï¿½chlich geschrieben wurde, bzw.
     *         <code>-1</code>, wenn keine Daten mehr verfï¿½gbar sind.
     * @throws IOException Wenn nicht vom eingebetteten Reader gelesen werden
     *         konnte.
     */
    @Override
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
