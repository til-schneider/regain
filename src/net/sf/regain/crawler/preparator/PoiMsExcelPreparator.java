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
 *  $RCSfile: PoiMsExcelPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/PoiMsExcelPreparator.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


/**
 * Präpariert ein Microsoft-Excel-Dokument für die Indizierung mit Hilfe der
 * <a href="http://jakarta.apache.org/poi/">POI-API</a>.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class PoiMsExcelPreparator extends AbstractPreparator {

  /** The currently preparing Excel workbook. */  
  private HSSFWorkbook mWorkbook;
  /** Contains all data formats used in the currently preparing Excel workbook. */  
  private HSSFDataFormat mDataFormat;

  
  
  /**
   * Erzeugt eine neue MsExcelPreparator-Instanz.
   */
  public PoiMsExcelPreparator() {
  }


  
  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */  
  public void prepare(RawDocument rawDocument) throws RegainException {
    ByteArrayInputStream stream = null;
    try {
      stream = new ByteArrayInputStream(rawDocument.getContent());
      POIFSFileSystem poiFs = new POIFSFileSystem(stream);
      mWorkbook = new HSSFWorkbook(poiFs);
      mDataFormat = mWorkbook.createDataFormat();
      
      StringBuffer cleanBuffer = new StringBuffer();
      for (int sheetIdx = 0; sheetIdx < mWorkbook.getNumberOfSheets(); sheetIdx++) {
        HSSFSheet sheet = mWorkbook.getSheetAt(sheetIdx);
        
        if (sheet != null) {
          parseSheet(sheet, cleanBuffer);
        }
      }
      
      setCleanedContent(cleanBuffer.toString());
    }
    catch (IOException exc) {
      throw new RegainException("Reading MS Excel dokument failed: "
        + rawDocument.getUrl(), exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
  }

  
  
  /**
   * Durchsucht ein Excel-Sheet nach Text.
   *
   * @param sheet Das zu durchsuchende Excel-Sheet.
   * @param cleanBuffer Der StringBuffer, an den der gefundene Text angefügt
   *        werden soll.
   */  
  private void parseSheet(HSSFSheet sheet, StringBuffer cleanBuffer) {
    int firstRow = sheet.getFirstRowNum();
    int lastRow = sheet.getLastRowNum();
    for (int rowIdx = firstRow; rowIdx <= lastRow; rowIdx++) {
      HSSFRow row = sheet.getRow(rowIdx);
      
      if (row != null) {
        parseRow(row, cleanBuffer);
      }
    }
  }



  /**
   * Durchsucht eine Excel-Zeile nach Text.
   *
   * @param row Das zu durchsuchende Excel-Zeile.
   * @param cleanBuffer Der StringBuffer, an den der gefundene Text angefügt
   *        werden soll.
   */  
  private void parseRow(HSSFRow row, StringBuffer cleanBuffer) {
    short firstCell = row.getFirstCellNum();
    short lastCell = row.getLastCellNum();
    for (short cellIdx = firstCell; cellIdx <= lastCell; cellIdx++) {
      HSSFCell cell = row.getCell(cellIdx);
      
      if (cell != null) {
        parseCell(cell, cleanBuffer);
      }
    }
  }

  
  
  /**
   * Durchsucht eine Excel-Zelle nach Text.
   *
   * @param cell Das zu durchsuchende Excel-Zelle.
   * @param cleanBuffer Der StringBuffer, an den der gefundene Text angefügt
   *        werden soll.
   */  
  private void parseCell(HSSFCell cell, StringBuffer cleanBuffer) {
    String cellValue = null;

    if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
      cellValue = cell.getStringCellValue();
    }
    else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
      HSSFCellStyle style = cell.getCellStyle();
      short formatId = style.getDataFormat();
      String format = mDataFormat.getFormat(formatId);
      format = RegainToolkit.replace(format, "\\ ", " ");

      if (isCellDateFormatted(cell)) {
        // This is a date
        format = RegainToolkit.replace(format, "mmmm", "MMMM");
        format = RegainToolkit.replace(format, "/", ".");

        double numberValue = cell.getNumericCellValue();
        java.util.Date date = HSSFDateUtil.getJavaDate(numberValue);
        cellValue = new java.text.SimpleDateFormat(format).format(date);
      } else {
        // This is a Number
        double numberValue = cell.getNumericCellValue();
        cellValue = new java.text.DecimalFormat(format).format(numberValue);
      }
    }
    
    if (cellValue != null) {
      cellValue = cellValue.trim();
      if (cellValue.length() != 0) {
        cleanBuffer.append(cellValue);
        cleanBuffer.append(" ");
        
        if (getTitle() == null) {
          setTitle(cellValue);
        }
      }
    }
  }
  
  
  
  /**
   * Prüft, ob die gegebene Excel-Zelle ein Datum enthält.
   *
   * @param cell Die zu prüfende Excel-Zelle.
   * @return Ob die gegebene Excel-Zelle ein Datum enthält.
   */  
  private boolean isCellDateFormatted(HSSFCell cell) {
    short format = cell.getCellStyle().getDataFormat();
    
    if (HSSFDateUtil.isValidExcelDate(cell.getNumericCellValue())) {
      if (HSSFDateUtil.isCellDateFormatted(cell)) {
        return true;
      } else {
        String fmtText = mDataFormat.getFormat(format);
        
        if (fmtText != null) {
          fmtText = fmtText.toLowerCase();
          
          if (fmtText.indexOf("d") >= 0
            || fmtText.indexOf("m") >= 0
            || fmtText.indexOf("y") >= 0
            || fmtText.indexOf("h") >= 0
            || fmtText.indexOf("s") >= 0)
          {
            return true;
          }
        }
      }
    }
    
    return false;
  }

}
