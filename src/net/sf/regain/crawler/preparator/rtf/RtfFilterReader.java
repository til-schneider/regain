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
 *  $RCSfile: RtfFilterReader.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/rtf/RtfFilterReader.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator.rtf;

import java.io.*;

/**
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
/**
 * List nur die reine Textinformation aus einem Reader, der rohe RTF-Daten liest.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class RtfFilterReader extends FilterReader {

  /** Die Schachtelungstiefe in der der Text liegt. */
  private static final int TEXT_DEPTH = 2;
  /** Die aktuelle Schachtelungstiefe */
  private int mDepth;
  /**
   * Gibt an, ob das zuletzt gelesene Zeichen in der Schachtelungstiefe war,
   * in der der Text liegt. Dieser Flag wird genutzt, um jedesmal ein
   * Leerzeichen einzufügen, wenn die Schachtelungstiefe verlassen wurde.
   * Dadurch wird das 'Zusammenkleben' von Worten vermieden.
   */
  private boolean mLastCharWasInTextDepth;


  /**
   * Erzeugt eine neue RtfFilterReader-Instanz.
   * 
   * @param in Der Reader, von dem die rohen RTF-Daten kommen.
   */
  public RtfFilterReader(Reader in) {
    super(in);
    
    mDepth = 0;
    mLastCharWasInTextDepth = true;
  }


  /**
   * Gibt an, ob die {@link #mark(int)}-Methode unterstützt wird.
   * <p>
   * Dieser Reader unterstützt das nicht.
   * 
   * @return <code>false</code>
   */
  public boolean markSupported() {
    return false;
  }


  /**
   * Liest einige Zeichen in einen Puffer.
   * 
   * @param cbuf Der Puffer, in den die Zeichen geschrieben werden sollen.
   * @param off Der Offset im Puffer, ab dem geschrieben werden darf.
   * @param len Die max. Anzahl Zeichen, die geschrieben werden darf.
   * @return Die Anzahl Zeichen, die tatsächlich geschrieben wurde oder
   *         <code>-1</code>, wenn das Ende erreicht wurde.
   * @throws IOException Wenn vom Quell-Reader nicht gelesen werden konnte.
   */
  public int read(char[] cbuf, int off, int len) throws IOException {
    int ch = read();
    if (ch == -1) {
      return -1;
    }
    cbuf[off] = (char) ch;
    for (int i = 1; i < len; i++) {
      ch = read();
      cbuf[off + i] = (char) ch;
      if (ch == -1) {
        return i;
      }
    }
    return len;
  }


  /**
   * Liest das nächste Zeichen.
   * 
   * @return Das nächste Zeichen oder <code>-1</code>, wenn das Ende erreicht
   *         wurde.
   * @throws IOException Wenn vom Quell-Reader nicht gelesen werden konnte.
   */
  public int read() throws IOException {
    int ch = getNext();

    while ((ch == '\\') && (ch != -1)) {
      ch = in.read();

      if ((ch == '{') || (ch == '}') || (ch == '\\')) {
        // An escaped char
        return ch;
      }
      else if (ch == '*') {
        // Ignore the whole group
        do {
          ch = in.read();

          if (ch == '{') {
            mDepth++;
          } else if (ch == '}') {
            mDepth--;
          }
        } while ((mDepth != (TEXT_DEPTH - 1)) && (ch != '}') && (ch != -1));          
      }
      else if (ch == '\'') {
        StringBuffer buf = new StringBuffer(2);
        buf.append((char) getNext());
        buf.append((char) getNext());
        String specChar = buf.toString();

        if (specChar.equals("c4")) {
          return '\u00c4';
        } else if (specChar.equals("d6")) {
          return '\u00d6';
        } else if (specChar.equals("dc")) {
          return '\u00dc';
        } else if (specChar.equals("e4")) {
          return '\u00e4';
        } else if (specChar.equals("f6")) {
          return '\u00f6';
        } else if (specChar.equals("fc")) {
          return '\u00fc';
        } else if (specChar.equals("df")) {
          return '\u00df';
        }
      }
      else {
        StringBuffer buf = new StringBuffer();
        do {
          buf.append((char) ch);
          ch = getNext();
        } while ((ch != -1) && (ch != ' ') && (ch != '\\'));

        String controlWord = buf.toString();
        
        int translated = translateControlWord(controlWord);
        if (translated != 0) {
          return translated;
        }
      }

      if ((ch != '\\') && (ch != ' ')) {
        ch = getNext();
      }
    }
    return ch;
  }


  /**
   * Wandelt ein RTF-control-word in das entsprechende char um.
   * <p>
   * Falls es keine Entsprechung gibt, wird <code>0</code> zurückgegeben.
   * 
   * @param controlWord Das umzuwandelnde RTF-control-word
   * @return Die Entsprechung zu <code>controlWord</code> oder <code>0</code>,
   *         wenn es keine Entsprechung gibt.
   */
  private char translateControlWord(String controlWord) {
    if (controlWord.equals("tab")) {
      return '\t';
    }
    if (controlWord.equals("par")) {
      return '\n';
    }
    if (controlWord.equals("line")) {
      return '\n';
    }

    return 0;
  }


  /**
   * Gets the next char in the text depth that is no newline or carage return.
   * 
   * @return The next char in the text depth.
   * @throws IOException When reading from the nested reader failed.
   */
  private int getNext() throws IOException {
    int ch;
    do {
      do {
        ch = in.read();

        // Ignore newlines and carage returns
        while ((ch == '\n') || (ch == '\r')) {
          ch = in.read();
        }

        if (ch == '{') {
          mDepth++;
        } else if (ch == '}') {
          mDepth--;
        }
        
        if (mLastCharWasInTextDepth && (mDepth != TEXT_DEPTH)) {
          // Return a blank every time when skipping a format group to avoid
          // two words being concatinated
          // Example: 'little{\someGroup}cat' should not become 'littlecat',
          //          but 'little cat'
          mLastCharWasInTextDepth = false;

          if (ch == -1) {
            return -1;
          } else {
            return ' ';
          }
        }
      }
      while (ch == '{' || ch == '}');
    }
    while ((mDepth != TEXT_DEPTH) && (ch != -1));
    
    mLastCharWasInTextDepth = true;
    
    return ch;
  }

}