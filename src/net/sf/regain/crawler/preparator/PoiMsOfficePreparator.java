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
 * Contact: Til Schneider, info@murfman.de, Thomas Tesche (www.thtesche.com)
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-10-25 18:35:21 +0200 (Sat, 25 Oct 2008) $
 *   $Author: thtesche $
 * $Revision: 349 $
 */
package net.sf.regain.crawler.preparator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.log4j.Logger;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Prepares all MS*-documents using POI
 * <a href="http://jakarta.apache.org/poi/">POI-API</a>.
 * <p>
 * The preparator use the generic extractor possibilities of POI.
 * Contributions from Jorge Corona.
 *
 * @author Thomas Tesche, www.thtesche.com
 */
public class PoiMsOfficePreparator extends AbstractPreparator {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(PoiMsOfficePreparator.class);

  /**
   * Creates a new instance of PoiMsOfficePreparator.
   *
   * @throws RegainException If creation of the preparator failed.
   */
  public PoiMsOfficePreparator() throws RegainException {
    super(new String[]{
              "application/msexcel", "application/vnd.ms-excel",
              "application/vnd.openxmlformats-officedocument.spreadsheetml",
              "application/msword", "application/vnd.ms-word",
              "application/vnd.openxmlformats-officedocument.wordprocessingml",
              "application/msvisio", "application/vnd.visio",
              "application/mspowerpoint", "application/vnd.ms-powerpoint",
              "application/vnd.openxmlformats-officedocument.presentationml",
              "application/vnd.ms-office"});
  }

  /**
   * Prepares the document.
   *
   * @param rawDocument the document to prepare
   *
   * @throws RegainException thrown in case of errors
   */
  @Override
  public void prepare(RawDocument rawDocument) throws RegainException {

    InputStream stream = null;

    try {
      stream = rawDocument.getContentAsStream();
      POITextExtractor contentExtractor = ExtractorFactory.createExtractor(stream);
      setCleanedContent(contentExtractor.getText());
      POITextExtractor metadataExtractor = contentExtractor.getMetadataTextExtractor();

      Map<String, String> metaDataMap = createMetaDataMap(metadataExtractor.getText());
//      if (mLog.isDebugEnabled()) {
//      mLog.info("Found meta data ::" + metadataExtractor.getText()
//              + ":: in " + rawDocument.getUrl());
//      }

      StringBuilder metaData = new StringBuilder();
      metaData.append(" ");

      ArrayList<String> fields = new ArrayList(Arrays.asList("Title", "Creator", "Company",
              "Keywords", "LastModifiedBy", "Description", "Subject", "PID_TITLE", "PID_AUTHOR",
              "PID_COMMENTS", "PID_KEYWORDS", "PID_SUBJECT", "PID_COMPANY"));
//      // Possible field values for the metadata extractor (and more from ms oxxx docs):
//      //PID_TITLE, PID_AUTHOR, PID_COMMENTS, PID_TEMPLATE, PID_LASTAUTHOR, PID_REVNUMBER
//      //PID_APPNAME, PID_EDITTIME, PID_CREATE_DTM, PID_LASTSAVE_DTM, PID_PAGECOUNT, PID_WORDCOUNT
//      //PID_CHARCOUNT, PID_SECURITY, PID_KEYWORDS, PID_SUBJECT, PID_CODEPAGE, PID_COMPANY
//      //PID_LINECOUNT, PID_PARCOUNT, PID_SCALE, PID_LINKSDIRTY, PID_DOCPARTS PID_HEADINGPAIR
      for (String field : fields) {
        if (metaDataMap.containsKey(field)) {
          metaData.append(metaDataMap.get(field));
          metaData.append(" ");
        }
      }
      setCleanedMetaData(metaData.toString());
      if (mLog.isDebugEnabled()) {
        mLog.debug("Extracted meta data ::" + getCleanedMetaData()
                + ":: from " + rawDocument.getUrl());
      }

      if (metaDataMap.containsKey("Title")) {
        setTitle(metaDataMap.get("Title"));
      } else if (metaDataMap.containsKey("PID_TITLE")) {
        setTitle(metaDataMap.get("PID_TITLE"));
      }

    } catch (InvalidFormatException invalidFormatEx) {
      throw new RegainException("Invalid format while reading MS* (OpenXML) document. URL: "
              + rawDocument.getUrl(), invalidFormatEx);

    } catch (Exception e) {
      throw new RegainException("Reading MS* (OpenXML) document failed : " + rawDocument.getUrl(), e);

    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (Exception exc) {
        }
      }
    }
  }

  private Map createMetaDataMap(String rawLine) {
    Map<String, String> metaDataMap = new HashMap<String, String>();

    if (rawLine != null && !rawLine.isEmpty()) {
      String[] singleLines = rawLine.split("\n");

      if (singleLines != null) {
        for (int i = 0; i < singleLines.length; i++) {

          String[] key_valuePair = singleLines[i].split("=");

          if (key_valuePair != null && key_valuePair.length == 2) {
            if ((key_valuePair[0] != null && !key_valuePair[0].trim().isEmpty())
                    && (key_valuePair[1] != null && !key_valuePair[1].trim().isEmpty())) {
              metaDataMap.put(key_valuePair[0].trim(), key_valuePair[1].trim());
            }
          }
        }
      }
    }

    return metaDataMap;
  }
}
