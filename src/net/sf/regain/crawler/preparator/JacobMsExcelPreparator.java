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
 *  $RCSfile: JacobMsExcelPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/JacobMsExcelPreparator.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.RawDocument;

import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;

import de.filiadata.lucene.spider.generated.msoffice2000.excel.*;

/**
 * Präpariert ein Microsoft-Excel-Dokument für die Indizierung mit Hilfe der
 * <a href="http://danadler.com/jacob/">Jacob-API</a>, wobei
 * <a href="http://www.bigatti.it/projects/jacobgen/">Jacobgen</a>
 * genutzt wurde, um den Zugriff zu erleichtern.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class JacobMsExcelPreparator extends AbstractPreparator {

  /**
   * Die Excel-Applikation. Ist <code>null</code>, solange noch kein Dokument
   * bearbeitet wurde.
   */
  private Application mExcelApplication;


  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */  
  public void prepare(RawDocument rawDocument) throws RegainException {
    if (mExcelApplication == null) {
      // COM-Thread initialisieren
      ComThread.InitSTA();

      // Neue Excel-Applikation erstellen
      mExcelApplication = new Application();
    }
    
    try {
      // Get all workbooks
      Workbooks wbs = mExcelApplication.getWorkbooks();

      // Open the file
      java.io.File file = rawDocument.getContentAsFile();
      Workbook wb = wbs.open(file.getAbsolutePath());
  
      // Collect the content of all sheets
      Sheets sheets = wb.getWorksheets();
      int sheetCount = sheets.getCount();
      StringBuffer contentBuf = new StringBuffer();
      for (int sheetIdx = 1; sheetIdx <= sheetCount; sheetIdx++) {
        Variant sheetVariant = (Variant) sheets.getItem(new Variant(sheetIdx));
        Worksheet sheet = new Worksheet(sheetVariant.toDispatch());
        
        // Letzte Zelle mit Daten finden
        // VB code: (Quelle: http://www.vbgamer.de/cgi-bin/loadframe.pl?ID=vb/tipps/tip0342.shtml)
        //   myWorksheet.Cells.Find(What:="*", After:=.Range("A1"), _
        //                          SearchOrder:=xlByRows, _
        //                          SearchDirection:=xlPrevious).Row
        Variant what = new Variant("*");
        Variant after = new Variant(sheet.getRange(new Variant("A1")));
        Variant lookIn = new Variant(1);
        Variant lookAt = new Variant(XlLookAt.xlWhole);
        Variant searchOrder = new Variant(XlSearchOrder.xlByRows);
        int searchDirection = XlSearchDirection.xlPrevious;
        Range lastCell = sheet.getCells().find(what, after, lookIn, lookAt,
          searchOrder, searchDirection);
  
        // Get an array with all cells
        Variant rangeVariant = new Variant("A1:" + lastCell.getAddress());
        SafeArray cellArray = sheet.getRange(rangeVariant).getValue().toSafeArray();
  
        int startRow = cellArray.getLBound(1);
        int startCol = cellArray.getLBound(2);
        int endRow   = cellArray.getUBound(1);
        int endCol   = cellArray.getUBound(2);
        
        for (int row = startRow; row <= endRow; row++) {
          for (int col = startCol; col <= endCol; col++) {
            String cellValue = cellArray.getString(row, col);
            if ((cellValue != null) && (cellValue.length() != 0)) {
              contentBuf.append(cellValue);
              contentBuf.append(" ");
            }
          }
          contentBuf.append("\n");
        }
      }
      
      // Set the content
      setCleanedContent(contentBuf.toString());
      
      // Close the workbook without saving
      wb.close(new Variant(false));
    }
    catch (ComFailException exc) {
      throw new RegainException("Using COM failed. "
        + "Be sure to use Java 1.3 or older!", exc);
    }
  }


  /**
   * Gibt alle Ressourcen frei, die von diesem Präparator genutzt wurden.
   * <p>
   * Wird ganz am Ende des Crawler-Prozesses aufgerufen, nachdem alle Dokumente
   * bearbeitet wurden.
   */
  public void close() throws RegainException {
    if (mExcelApplication != null) {
      // Excel schließen
      try {
        mExcelApplication.quit();
      }
      catch (Throwable thr) {
        throw new RegainException("Using COM failed.", thr);
      }
      finally {
        // Alle Ressourcen des COM-Threads freigeben
        ComThread.Release();
      }
    }
  }

}
