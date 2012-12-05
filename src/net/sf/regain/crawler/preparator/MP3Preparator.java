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
 *     $Date: 2008-03-16 20:50:37 +0100 (So, 16 Mär 2008) $
 *   $Author: thtesche $
 * $Revision: 281 $
 */
package net.sf.regain.crawler.preparator;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;

/**
 * Prepares a mp3 file for indexing of metadata
 * <p>
 * The following information from the audio tag (ID3v2 or ID3v1) will be extracted:
 * artist, album, title, year, length, bitrate
 *
 * @author Thomas Tesche, cluster:Consult, http://www.thtesche.com/
 */
public class MP3Preparator extends AbstractPreparator {

  /**
   * Creates a new instance of MP3Preparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public MP3Preparator() throws RegainException {
    super(new String[]{"audio/mp3", "audio/x-mp3", "audio/mpeg"});
  }

  /**
   * Prepares the document for indexing
   *
   * @param rawDocument the document
   *
   * @throws RegainException if preparation goes wrong
   */
  @Override
  public void prepare(RawDocument rawDocument) throws RegainException {

    File rawFile = rawDocument.getContentAsFile(false);
    try {
    
      prepareFile(rawFile, rawDocument.getUrl());

    } catch (ReadOnlyFileException ex) {
      
      try {
        File tempFile = File.createTempFile(rawFile.getName(), ".mp3");
        RegainToolkit.copyFile(rawFile, tempFile);
        
        prepareFile(tempFile, rawDocument.getUrl());
      
      if (!tempFile.delete())
         tempFile.deleteOnExit();
      
      } catch (ReadOnlyFileException e) {
        throw new RegainException("Error handling audio file: " + rawDocument.getUrl(), e);

      } catch (IOException e) {
        throw new RegainException("Error handling audio file: " + rawDocument.getUrl(), e);

      }
    }
  }

  protected void prepareFile(File rawFile, String origUrl) throws ReadOnlyFileException, RegainException
  {
    try 
    {
      MP3File mp3file = new MP3File(rawFile);
      ArrayList<String> info = new ArrayList<String>();
  
      if (mp3file.hasID3v2Tag()) {
        ID3v24Tag id3v24tag = mp3file.getID3v2TagAsv24();
  
        info.add(id3v24tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST).trim());
        info.add(id3v24tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM).trim());
        info.add(id3v24tag.getFirst(ID3v24Frames.FRAME_ID_TITLE).trim());
        try {
          int year = new Integer(id3v24tag.getFirst(ID3v24Frames.FRAME_ID_YEAR).trim()).intValue();
          if (year > 0) {
            info.add(id3v24tag.getFirst(ID3v24Frames.FRAME_ID_YEAR).trim());
          }
        } catch (Exception ex) {
        }
        info.add(mp3file.getMP3AudioHeader().getTrackLengthAsString().trim());
        info.add(mp3file.getMP3AudioHeader().getBitRate().trim() + "kbps");
  
        setCleanedContent(concatenateStringParts(info, Integer.MAX_VALUE));
        setTitle(concatenateStringParts(info, 2));
  
      } else if (mp3file.hasID3v1Tag()) {
        ID3v1Tag tag = mp3file.getID3v1Tag();
  
        info.add(tag.getFirst(FieldKey.ARTIST).trim());
        info.add(tag.getFirst(FieldKey.ALBUM).trim());
        info.add(tag.getFirst(FieldKey.TITLE).trim());
        try {
          int year = new Integer(tag.getFirst(FieldKey.YEAR).trim()).intValue();
          if (year > 0) {
            info.add(tag.getFirst(FieldKey.YEAR).trim());
          }
        } catch (Exception ex) {
        }
        info.add(mp3file.getMP3AudioHeader().getTrackLengthAsString().trim());
        info.add(mp3file.getMP3AudioHeader().getBitRate().trim() + "kbps");
  
        setCleanedContent(concatenateStringParts(info, Integer.MAX_VALUE));
        setTitle(concatenateStringParts(info, 2));
  
      } else {
        setCleanedContent("");
      }
    } catch (TagException ex) {
      throw new RegainException("Error handling audio file: " + origUrl, ex);

    } catch (InvalidAudioFrameException ex) {
      throw new RegainException("Error handling audio file: " + origUrl, ex);

    } catch (IOException ex) {
      throw new RegainException("Error handling audio file: " + origUrl, ex);
    }

  }
}
