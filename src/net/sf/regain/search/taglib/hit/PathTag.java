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
 *  $RCSfile: PathTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/taglib/hit/PathTag.java,v $
 *     $Date: 2004/07/28 20:26:02 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.search.taglib.hit;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspWriter;

import org.apache.lucene.document.Document;

/**
 * Generiert den Pfad zum aktuellen Trefferdokument.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class PathTag extends AbstractHitTag {

  /** Die zu verwendende Stylesheet-Klasse. (Kann null sein) */
  private String mStyleSheetClass;
  
  /** Gibt an, ob Links (also a-Tags) erzeugt werden sollen. */
  private boolean mCreateLinks = true;

  /**
   * Der HTML-Code, der vor dem Tag eingefügt wird, sofern er nicht leer ist.
   * (Kann null sein)
   */
  private String mHtmlBefore;

  /**
   * Der HTML-Code, der vor dem Tag eingefügt wird, sofern er nicht leer ist.
   * (Kann null sein)
   */
  private String mHtmlAfter;
  

  /**
   * Setzt die zu verwendende Stylesheet-Klasse.
   *   
   * @param styleSheetClass Die zu verwendende Stylesheet-Klasse.
   */
  public void setClass(String styleSheetClass) {
    mStyleSheetClass = styleSheetClass;
  }


  /**
   * Gibt an, ob Links (also a-Tags) erzeugt werden sollen.
   *   
   * @param createLinks Ob Links erzeugt werden sollen.
   */
  public void setCreateLinks(boolean createLinks) {
    mCreateLinks = createLinks;
  }


  /**
   * Setzt den HTML-Code, der vor dem Tag eingefügt werden soll, sofern dieser
   * nicht leer ist.
   *   
   * @param htmlBefore Der HTML-Code vor dem Tag.
   */
  public void setBefore(String htmlBefore) {
    mHtmlBefore = htmlBefore;
  }


  /**
   * Setzt den HTML-Code, der nach dem Tag eingefügt werden soll, sofern dieser
   * nicht leer ist.
   *   
   * @param htmlAfter Der HTML-Code nach dem Tag.
   */
  public void setAfter(String htmlAfter) {
    mHtmlAfter = htmlAfter;
  }
  

  /**
   * Generiert den Tag.
   *
   * @param out Der JspWriter auf den der Taginhalt geschrieben werden soll.
   * @param hit Der aktuelle Suchtreffer.
   * @throws IOException Wenn der Tag nicht geschrieben werden konnte.
   */
  protected void printEndTag(JspWriter out, Document hit)
    throws IOException
  {
    String path = hit.get("path");
    
    if (path != null) {
      if (mHtmlBefore != null) {
        out.print(mHtmlBefore);
      }
      
      // NOTE: The path is formatted as follows:
      //       For each path element there is a line ending with \n
      //       A line constists of the URL, a blank and the title
      
      StringTokenizer tokenizer = new StringTokenizer(path, "\n");
      boolean firstPathElement = true;
      while (tokenizer.hasMoreTokens()) {
        String line = tokenizer.nextToken();
        
        int blankPos = line.indexOf(' ');
        if (blankPos != -1) {
          String url = line.substring(0, blankPos);
          String title = line.substring(blankPos + 1, line.length());
          
          if (! firstPathElement) {
            out.print(" &gt; "); // >
          }
          if (mCreateLinks) {
            out.print("<a href=\"" + url + "\"");
            if (mStyleSheetClass != null) {
              out.print(" class=\"" + mStyleSheetClass + "\"");
            }
            out.print(">" + title + "</a>");
          } else {
            out.print(title);
          }
            
          firstPathElement = false;
        }
      }
      
      if (mHtmlAfter != null) {
        out.print(mHtmlAfter);
      }
    }
  }


  /**
   * Gibt die von diesem Tag genutzten Ressourcen wieder frei.
   */
  public void release() {
    super.release();

    mStyleSheetClass = null;
    mCreateLinks = true;
    mHtmlBefore = null;
    mHtmlAfter = null;
   }

}
