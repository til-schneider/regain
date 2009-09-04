/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2008  Til Schneider, Thomas Tesche
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
 * Contact: Til Schneider, info@murfman.de, Thomas Tesche, regain@thtesche.com
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-10-25 18:35:21 +0200 (Sat, 25 Oct 2008) $
 *   $Author: thtesche $
 * $Revision: 349 $
 */
package net.sf.regain.crawler.preparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.CRC32;
import javax.mail.Address;
import javax.mail.BodyPart;
import org.apache.log4j.Logger;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;
import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.util.StripEntities;

/**
 * This class prepares  messages (MIME, rfc822).
 * <p>
 * The document contains the message text and the file names of the attachments.
 *
 * @author Thomas Tesche, www.thtesche.com
 */
public class MessagePreparator extends AbstractPreparator {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(MessagePreparator.class);

  /**
   * Creates a new instance of MessagePreparator.
   *
   * @throws RegainException If creating of the preparator failed.
   */
  public MessagePreparator() throws RegainException {
    super("message/rfc822");
  }

  /**
   * Prepares the document for indexing.
   *
   * @param rawDocument The document to prepare.
   *
   * @throws RegainException If the preparation fails.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {

    Properties mailProperties = System.getProperties();
    Session session = Session.getInstance(mailProperties, null);
    SharedByteArrayInputStream mimeInput = new SharedByteArrayInputStream(rawDocument.getContent());

    Collection<String> textParts = new ArrayList<String>();
    Collection<String> attachments = new ArrayList<String>();
    SimpleDateFormat simpleFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    StringBuffer resultText = new StringBuffer();
    StringBuffer keyText = new StringBuffer();

    try {
      CRC32 crc = new CRC32();
      crc.update(rawDocument.getContent());

      mLog.debug("prepare MIME message crc: " + crc.getValue() + " for IMAP url: " + rawDocument.getUrl());
      MimeMessage message = new MimeMessage(session, mimeInput);

      resultText.append("Subject: " + message.getSubject()).append("\n");
      resultText.append("Sent: " + simpleFormat.format(message.getSentDate())).append("\n");

      if (message.getReceivedDate() != null) {
        resultText.append("Recieved: " + simpleFormat.format(message.getReceivedDate())).append("\n");
      }
      Address[] recipientsArray = message.getAllRecipients();
      String recipients = "";
      for (int i = 0; i < recipientsArray.length; i++) {
        recipients += recipientsArray[i].toString() + ", ";
        keyText.append(stripNoneWordChars(recipientsArray[i].toString()) + " ");
      }
      recipients = recipients.substring(0, recipients.length() - 2);
      resultText.append("Recipient(s): " + recipients).append("\n");

      Address[] repliesArray = message.getReplyTo();
      if (repliesArray != null && repliesArray.length > 0) {
        String replies = "";
        for (int i = 0; i < repliesArray.length; i++) {
          replies += repliesArray[i].toString() + ", ";
          keyText.append(stripNoneWordChars(repliesArray[i].toString()) + " ");
        }
        replies = replies.substring(0, replies.length() - 2);
        resultText.append("Reply to: " + replies).append("\n");
      }

      Address[] senderArray = message.getFrom();
      if (senderArray != null && senderArray.length > 0) {
        String sender = "";
        for (int i = 0; i < senderArray.length; i++) {
          sender += senderArray[i].toString() + ", ";
          keyText.append(stripNoneWordChars(senderArray[i].toString()) + " ");
        }
        sender = sender.substring(0, sender.length() - 2);
        setTitle(message.getSubject() + " from " + sender);
        resultText.append("Sender: " + sender).append("\n");
      }

      resultText.append("Header key words: " + keyText.toString()).append("\n");
      resultText.append("-------------------------------------------------------------").append("\n");

      // multipart or not multipart
      if (message.getContent() instanceof Multipart) {
        //contentType = "multipart";
        Multipart mp = (Multipart) message.getContent();

        for (int i = 0; i < mp.getCount(); i++) {
          BodyPart bp = mp.getBodyPart(i);
          String disposition = bp.getDisposition();

          if ((disposition != null) &&
            ((disposition.equals(Part.ATTACHMENT)))) {
            attachments.add("attachment: " + bp.getFileName());

          } else if (disposition == null || disposition.equals(Part.INLINE)) {

            // Plain Text oder HTML
            if (bp.isMimeType("text/*")) {
              textParts.add((String) bp.getContent());
            //textParts.add( inputStreamAsString(bp.getInputStream()));

            } else if (bp.isMimeType("multipart/*")) {
              // another bodypart container
              // we process only depth = 1 and not deeper and only text.
              Multipart mpInner = (Multipart) bp.getContent();

              for (int k = 0; k < mpInner.getCount(); k++) {
                BodyPart bpInner = mpInner.getBodyPart(k);

                if (bpInner != null && (bpInner.getDisposition() == null ||
                  bpInner.getDisposition().equals(Part.INLINE))) {

                  if (bpInner.isMimeType("text/*")) {
                    textParts.add((String) bpInner.getContent());
                  }

                } //Auswerten auf BodyParts, welche keine Attachments sind
              } // über alle inneren BodyParts iterieren
            } // MultipartContainer in einem MultipartContainer auswerten

          } // berücksichtige alle als 'nicht'-Attachment gekennzeichete Teile
        // im äußeren Container

        } // iteriere über alle Bodyparts, wobei ein Bodypart wieder ein
      // MultipartContainer sein kann

      } else {
        // This is a plain text mail.
        //contentType = "text";
        Object content = message.getContent();
        if (content instanceof String) {
          textParts.add((String) content);
        } else {
          // This is an SharedByteArrayInputstream
          textParts.add(RegainToolkit.readStringFromStream((SharedByteArrayInputStream) content));
        }
      }

    } catch (MessagingException ex) {
      mLog.error("Could not instanciate mime message for parsing.", ex);

    } catch (IOException ex) {
      mLog.error("Could not parse mime message for parsing.", ex);
    }

    if (textParts.size() > 0) {
      // Iteriere über alle Textteile der Mail und aggregiere diese
      Iterator iter = textParts.iterator();
      while (iter.hasNext()) {
        String current = (String) iter.next();
        resultText.append(StripEntities.stripHTMLTags(((String) current)) + " ").append("\n");
      }
    }

    if (attachments.size() > 0) {
      // Iteriere über alle Textteile der Mail und aggregiere diese
      Iterator iter = textParts.iterator();
      while (iter.hasNext()) {
        resultText.append(StripEntities.stripHTMLTags(((String) iter.next()))).append("\n");
      }
    }

    setCleanedContent(resultText.toString());

  }

  /** 
   * Removes unwanted chars from a given string.
   * @param uncleanString
   * @return
   */
  private String stripNoneWordChars(String uncleanString) {
    return uncleanString.replace(".", " ").replace(":", " ").replace("@", " ").replace("-", " ").replace("_", " ").replace("<", " ").replace(">", " ");

  }

  public static String inputStreamAsString(InputStream stream)
    throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
    StringBuilder sb = new StringBuilder();
    String line = null;

    while ((line = br.readLine()) != null) {
      sb.append(line + "\n");
    }

    br.close();
    return sb.toString();
  }
}
