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
 *     $Date: 2006/01/21 11:53:30 $
 *   $Author: til132 $
 * $Revision: 1.8 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.RawDocument;

import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
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
 * @author Til Schneider, www.murfman.de
 * @author Reinhard Balling
 */
public class JacobMsExcelPreparator extends AbstractJacobMsOfficePreparator {

  /**
   * Die Excel-Applikation. Ist <code>null</code>, solange noch kein Dokument
   * bearbeitet wurde.
   */
  private Application mExcelApplication;
  

  /**
   * Creates a new instance of JacobMsExcelPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public JacobMsExcelPreparator() throws RegainException {
    super(new String[] { "xls", "xlt" });
  }

  
  /**
   * Initializes the preparator.
   * 
   * @param config The configuration
   * @throws RegainException If the configuration has an error.
   */
  public void init(PreparatorConfig config) throws RegainException {
    // NOTE: This method is not nessesary since it only calls the super method,
    //       but I defined it to ensure that the super call is not forgotten
    //       when there should be a config some day.
    super.init(config);
  }


  /**
   * Wrapper for calling the ActiveX-Method with input-parameter(s).
   * 
   * @param row an input-parameter of type int
   * @param col an input-parameter of type int
   * @return the result is of type Range
   */
  public Range getCells(Worksheet sheet, int row, int col) {
    return new Range(Dispatch.call(sheet, "Cells", new Variant(row), 
        new Variant(col)).toDispatch());
  }


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
    //Dispatch.put(mExcelApplication, "Visible", new Variant(true));

    try {
      // Some constants needed later on
      Variant what = new Variant("*");
      Variant lookIn = new Variant(-4163);
      Variant lookAt = new Variant(XlLookAt.xlWhole);
      Variant searchOrderByRows = new Variant(XlSearchOrder.xlByRows);
      Variant searchOrderByCols = new Variant(XlSearchOrder.xlByColumns);
      int searchDirectionUp = XlSearchDirection.xlNext;
      int searchDirectionDown = XlSearchDirection.xlPrevious;
      Variant xlFalse = new Variant(false);    

      // Get all workbooks
      Workbooks wbs = mExcelApplication.getWorkbooks();

      // Open the file
      java.io.File file = rawDocument.getContentAsFile();
      // RB 20051106: Need to add second argument to open call, otherwise Excel
      // can hang here waiting for an answer to the dialogue box asking to
      // update external links
      Workbook wb = wbs.open(file.getAbsolutePath(), new Variant(0));

      // Collect the content of all sheets
      Sheets sheets = wb.getWorksheets();
      int sheetCount = sheets.getCount();

      //System.out.println("Sheetcount = "+sheetCount); 
      StringBuffer contentBuf = new StringBuffer(DEFAULT_BUFFER_SIZE);
      for (int sheetIdx = 1; sheetIdx <= sheetCount; sheetIdx++) {
        Variant sheetVariant = (Variant) sheets.getItem(new Variant(sheetIdx));
        Worksheet sheet = new Worksheet(sheetVariant.toDispatch());
        Variant after = new Variant(sheet.getRange(new Variant("IV65536")));
        Range extractRange;
        // Find the top left and bottom right corner enclosing the data in the sheet
        try {
          // The following calls may fail because the find operation returns
          // a null pointer. This exception is caught by reverting to the
          // UsedRange call which also returns the used area in a spreadsheet, 
          // even though it is often bigger than necessary.
          int firstRow = sheet.getCells().find(what, after, lookIn, lookAt, searchOrderByRows, 
                  searchDirectionUp, xlFalse, xlFalse).getRow();
          int firstCol = sheet.getCells().find(what, after, lookIn, lookAt, searchOrderByCols, 
              searchDirectionUp, xlFalse, xlFalse).getColumn();
          int lastRow = sheet.getCells().find(what, after, lookIn, lookAt, searchOrderByRows, 
                  searchDirectionDown, xlFalse, xlFalse).getRow();
          int lastCol = sheet.getCells().find(what, after, lookIn, lookAt, searchOrderByCols, 
                  searchDirectionDown, xlFalse, xlFalse).getColumn();
          extractRange = sheet.getRange(new Variant(getCells(sheet, firstRow, firstCol)), 
                                        new Variant(getCells(sheet, lastRow, lastCol)));
          //System.out.println("Sheet "+sheetIdx+"   ProperUsedRange="+extractRange.getAddress());        
        } catch(NullPointerException e) {
          extractRange = sheet.getUsedRange();
          //System.out.println("Sheet "+sheetIdx+"   UsedRange="+extractRange.getAddress());        
        }
        //RB 20051106: If the sheet is empty, getUsedRange returns $A$1 and
        // toSafeArray fails. Therefore we have to check that there is some text
        // to extract by comparing the address of the range to the string "$A$1".
        if (!extractRange.getAddress().equals("$A$1")) {
          SafeArray cellArray = extractRange.getValue().toSafeArray();

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
      }
      
      // Read the document properties
      readProperties(wb);
      
      // Set the content
      setCleanedContent(contentBuf.toString());

      // Close the workbook without saving
      wb.close(new Variant(false));
    }
    catch (ComFailException exc) {
      throw new RegainException("Using COM failed.", exc);
    }
  }


  /**
   * Frees all resources reserved by the preparator.
   * <p>
   * Is called at the end of the crawler process after all documents were
   * processed.
   * 
   * @throws RegainException If freeing the resources failed.
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
