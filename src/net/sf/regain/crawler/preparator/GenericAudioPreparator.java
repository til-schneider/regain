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
 */
package net.sf.regain.crawler.preparator;

import java.io.File;
import java.io.IOException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/**
 * Prepares mp4 (iTunes), ogg (Ogg-Vorbis) and flac files for indexing of metadata
 * <p>
 * The following information from the audio tag will be extracted:
 * artist, album, title, year, length, bitrate
 *
 * @author Thomas Tesche, cluster:Consult, http://www.thtesche.com/
 */
public class GenericAudioPreparator extends AbstractPreparator {

  /**
   * Creates a new instance of GenericAudioPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public GenericAudioPreparator() throws RegainException {
    super(new String[]{"audio/mp4", "application/x-ogg", "audio/flac"});
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

  protected void prepareFile(File rawFile, String origUrl) throws RegainException, ReadOnlyFileException
  {
    try {
      AudioFile audioFile = AudioFileIO.read(rawFile);
      ArrayList<String> info = new ArrayList<String>();

      Tag tag = audioFile.getTag();

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
      int length = audioFile.getAudioHeader().getTrackLength();
      int hours, minutes, seconds;
      hours = length / 3600;
      length = length - (hours * 3600);
      minutes = length / 60;
      length = length - (minutes * 60);
      seconds = length;
      DecimalFormat doubleDigit = new DecimalFormat("00");
      String res = "";
      if (hours > 0) {
        res = doubleDigit.format(hours) + ":";
      }
      res += doubleDigit.format(minutes) + ":";
      res += doubleDigit.format(seconds);

      info.add(res);
      info.add(audioFile.getAudioHeader().getBitRate().trim() + "kbps");

      setCleanedContent(concatenateStringParts(info, Integer.MAX_VALUE));
      setTitle(concatenateStringParts(info, 2));

    } catch (CannotReadException ex) {
      throw new RegainException("Error handling audio file: " + origUrl, ex);

    } catch (TagException ex) {
      throw new RegainException("Error handling audio file: " + origUrl, ex);

    } catch (InvalidAudioFrameException ex) {
      throw new RegainException("Error handling audio file: " + origUrl, ex);

    } catch (IOException ex) {
      throw new RegainException("Error handling audio file: " + origUrl, ex);
    }
  }
}
