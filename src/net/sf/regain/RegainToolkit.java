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
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain;

import java.io.*;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * Enthält Hilfsmethoden, die sowohl vom Crawler als auch von der Suchmaske
 * genutzt werden.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class RegainToolkit {

  /**
   * Gibt an, ob die Worte, die der Analyzer identifiziert ausgegeben werden
   * sollen.
   */
  private static final boolean ANALYSE_ANALYZER = false;

  /** Die Anzahl Bytes in einem kB (Kilobyte). */
  private static final int SIZE_KB = 1024;

  /** Die Anzahl Bytes in einem MB (Megabyte). */
  private static final int SIZE_MB = 1024 * 1024;

  /** Die Anzahl Bytes in einem GB (Gigabyte). */
  private static final int SIZE_GB = 1024 * 1024 * 1024;
  
  /** Der gecachte, systemspeziefische Zeilenumbruch. */
  private static String lineSeparator;


  
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
    
    FileReader reader = null;
    try {
      reader = new FileReader(file);
      StringWriter writer = new StringWriter();
      
      RegainToolkit.pipe(reader, writer);
      
      reader.close();
      writer.close();
      
      return writer.toString();
    }
    catch (IOException exc) {
      throw new RegainException("Reading word list from " + file.getAbsolutePath()
          + "failed", exc);
    }
    finally {
      if (reader != null) {
        try { reader.close(); } catch (IOException exc) {}
      }
    }
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

    analyzer = createCaseInsensitiveAnalyzer(analyzer);
    
    if (ANALYSE_ANALYZER) {
      return createAnalysingAnalyzer(analyzer);
    }
    return analyzer;
  }
  
  
  /**
   * Erzeugt einen Analyzer, der ein Dokument in Kleinschreibung umwandelt,
   * bevor es dem einem eingebetteten Analyzer übergeben wird.
   * 
   * @param nestedAnalyzer Der eingebettete Analyzer.
   * @return Der Analyzer.
   */
  private static Analyzer createCaseInsensitiveAnalyzer(
    final Analyzer nestedAnalyzer)
  {
    return new Analyzer() {
      public TokenStream tokenStream(String fieldName, Reader reader) {
        Reader lowercasingReader = new LowercasingReader(reader);
        
        return nestedAnalyzer.tokenStream(fieldName, lowercasingReader);
      }
    };
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
   * Gibt einen für den Menschen gut lesbaren String für eine Anzahl Bytes
   * zurück.
   * 
   * @param bytes Die Anzahl Bytes
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes) {
    return bytesToString(bytes, 2);
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
   * @param fractionDigits Die Anzahl der Nachkommastellen
   * @return Ein String, der sie Anzahl Bytes wiedergibt
   */
  public static String bytesToString(long bytes, int fractionDigits) {
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
    
    NumberFormat format = NumberFormat.getInstance();
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
    StringTokenizer tokenizer = new StringTokenizer(str, delim);
    String[] searchFieldArr = new String[tokenizer.countTokens()];
    for (int i = 0; i < searchFieldArr.length; i++) {
      searchFieldArr[i] = tokenizer.nextToken();
    }
    return searchFieldArr;
  }
  
  
  /**
   * Gibt den systemspeziefischen Zeilenumbruch zurück.
   * 
   * @return Der Zeilenumbruch.
   */
  public static String getLineSeparator() {
    if (lineSeparator == null) {
      lineSeparator = System.getProperty("line.separator");
    }
    
    return lineSeparator;
  }

  
  // inner class LowercasingReader
  
  
  /**
   * Liest alle Zeichen von einem eingebetteten Reader in Kleinschreibung.
   * 
   * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
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
