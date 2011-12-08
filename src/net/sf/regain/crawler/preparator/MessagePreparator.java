/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004-2010  Til Schneider, Thomas Tesche
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.preparator.util.StripEntities;

import org.apache.log4j.Logger;

/**
 * This class prepares  messages (MIME, rfc822), specifically spoof email messages.
 * <p>
 * The document contains the message text and the file names of the attachments.
 *
 * @author Thomas Tesche, www.thtesche.com, Kevin Black (KJB)
 * @see MessagePreparator
 */
public class MessagePreparator extends AbstractPreparator {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(MessagePreparator.class);
  /** Regex Compilation to match URLs in body. */
  private static Pattern mURLPattern = Pattern.compile("(?im)((?:http|https|ftp|mailto):[^\\s\"'<>]*)");

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
      MimeMessage message = new MimeMessage(session, mimeInput);

      resultText.append("Subject: " + message.getSubject()).append("\n");
      if (message.getSentDate() != null) {
        resultText.append("Sent: " + simpleFormat.format(message.getSentDate())).append("\n");
      }

      if (message.getReceivedDate() != null) {
        resultText.append("Received: " + simpleFormat.format(message.getReceivedDate())).append("\n");
      }
      Address[] recipientsArray = null;
      try {
        recipientsArray = message.getAllRecipients();
      } catch (AddressException ae) {
        // KJB: an issue with getAllRecipients if To: contains a semi-colon rather than comma.
        recipientsArray = fixAddress(ae, message, "To");
      }
      if (recipientsArray != null && recipientsArray.length > 0) {
        String recipients = "";
        for (int i = 0; i < recipientsArray.length; i++) {
          recipients += recipientsArray[i].toString() + ", ";
          keyText.append(stripNoneWordChars(recipientsArray[i].toString()) + " ");
        }
        recipients = recipients.substring(0, recipients.length() - 2);
        resultText.append("Recipient(s): " + recipients).append("\n");
      }

      Address[] repliesArray = null;
      try {
        repliesArray = message.getReplyTo();
      } catch (AddressException ae) {
        repliesArray = fixAddress(ae, message, "Reply-to");
      }
      if (repliesArray != null && repliesArray.length > 0) {
        String replies = "";
        for (int i = 0; i < repliesArray.length; i++) {
          replies += repliesArray[i].toString() + ", ";
          keyText.append(stripNoneWordChars(repliesArray[i].toString()) + " ");
        }
        replies = replies.substring(0, replies.length() - 2);
        resultText.append("Reply to: " + replies).append("\n");
      }

      Address[] senderArray = null;
      try {
        senderArray = message.getFrom();
      } catch (AddressException ae) {
        senderArray = fixAddress(ae, message, "From");
      }
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

          if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)))) {
            attachments.add("attachment: " + bp.getFileName());
            if (bp.isMimeType("text/*")) {
              textParts.add((String) bp.getContent());
              mLog.debug("added txt from attachment: " + bp.getFileName() + " : " + bp.getContentType());
            }

          } else if (disposition == null || disposition.equals(Part.INLINE)) {

            // Plain Text oder HTML
            if (bp.isMimeType("text/*")) {
              textParts.add((String) bp.getContent());

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

                } // end of bodypart which are not attachments
              } // end of iterate over all inner bodyparts
            } // MultipartContainer in a MultipartContainer
            else if (bp.isMimeType("message/*")) {
              Object bpContent = bp.getContent();
              if (bpContent instanceof MimeMessage) {

                // Added by KJB.
                // Message container.
                MimeMessage mmInner = (MimeMessage) bp.getContent();
                // tack on the headers.
                for (Enumeration<String> e = mmInner.getAllHeaderLines(); e.hasMoreElements();) {
                  textParts.add(e.nextElement());
                }

                Object mmContent = mmInner.getContent();
                if (mmContent instanceof String) {
                  textParts.add((String) mmContent);
                } else if (mmContent instanceof InputStream) {
                  textParts.add(RegainToolkit.readStringFromStream((InputStream) mmContent));
                } else {
                  mLog.error("Message Content is of unknown Class: " + mmContent.getClass().getName());
                }
              } else if (bpContent instanceof InputStream) {
                textParts.add(RegainToolkit.readStringFromStream((InputStream) bpContent));
              } else {
                mLog.error("Body Part Inner Message Content is of unknown Class: " + bpContent.getClass().getName());
              }

            } // Message container

          } // consider all none attachment parts
          // outmost container

        } // iterate over all bodyparts (a bodypart could be multipart container for itself)

      } else {
        // This is a plain text mail.
        Object content = message.getContent();
        if (content instanceof String) {
          textParts.add((String) content);
        } else if (content instanceof InputStream) {
          textParts.add(RegainToolkit.readStringFromStream((InputStream) content));
        } else {
          mLog.error("Message content is of unknown class: " + content.getClass().getName());
        }
      }

    } catch (MessagingException ex) {
      mLog.error("Could not instantiate mime message for parsing.", ex);

    } catch (IOException ex) {
      mLog.error("Could not parse mime message for parsing.", ex);
    }

    if (textParts.size() > 0) {
      // Iterate over all text parts of the mail and aggregate the content
      Iterator<String> iter = textParts.iterator();
      while (iter.hasNext()) {
        String current = (String) iter.next();
        resultText.append(StripEntities.stripHTMLTags(((String) current)) + " ").append("\n");
        Collection<String> urls = this.extractURLs(current);
        if (urls != null && !urls.isEmpty()) {
          Iterator<String> uIter = urls.iterator();
          while (uIter.hasNext()) {
            resultText.append(uIter.next()).append("\n");
          }
        }
      }
    }

    if (attachments.size() > 0) {
      Iterator<String> iter = attachments.iterator();
      // Note, attachments are simply the name of the attachment file.
      while (iter.hasNext()) {
        resultText.append(StripEntities.stripHTMLTags(((String) iter.next()))).append("\n");
      }
    }

    setCleanedContent(resultText.toString());

  }

  /** Occasionally see Addresses that have semi-colons rather than commas, which
   * cause "Illegal semicolon, not in group" AddressException.
   * This helper function attempts to change semi-colons to commas and return the
   * Address Array.
   *
   * @param ae          Address Exception object
   * @param message     MIME Message object
   * @param headerName  Name of header, e.g. To, From, Reply-To
   * @return if available, array of Addresses; otherwise, null
   */
  private Address[] fixAddress(AddressException ae, MimeMessage message, String headerName) {
    Address[] addresses = null;
    // "Illegal semicolon, not in group in string".?
    try {
      String[] toArray = message.getHeader(headerName);
      if (toArray != null && ae.getMessage().contains("Illegal semicolon, not in group")) {
        // replace semi-colon with comma, and try to build a From Address[]
        toArray[0] = toArray[0].replace(';', ',');
        message.setHeader(headerName, toArray[0]);
        try {
          addresses = message.getAllRecipients();
        } catch (Exception e) {
          addresses = null;
        }
      }
    } catch (MessagingException me) {
      addresses = null;
      mLog.error("Could not parse Header: " + headerName);
    }
    return addresses;
  }

  /** Extract URLs from text source.
   * Tried org.htmlparser functions here (eg. http://notetodogself.blogspot.com/2007/11/extract-links-using-htmlparser.html)
   * but that technique missed quite a few URLs.
   *  Using the RegEx technique.
   *
   * @param text input string of text or HTML
   * @return Collection of strings matching https?|ftp|mailto
   */
  private Collection<String> extractURLs(String text) {
    if (text == null || text.isEmpty()) {
      return null;
    }

    Collection<String> result = new HashSet<String>();

    Matcher matcher = mURLPattern.matcher(text);
    while (matcher.find()) {
      String url = matcher.group(1);
      result.add(url);
    }

    return result;
  }

  /**
   * Removes unwanted chars from a given string.
   * @param uncleanString
   * @return
   */
  private String stripNoneWordChars(String uncleanString) {
    return uncleanString.replace(".", " ").replace(":", " ").replace("@", " ").replace("-", " ").replace("_", " ").replace("<", " ").replace(">", " ");

  }

  /**
   * Get the content of an InputStream as String.
   *
   * @param stream the InputStream
   * @return the convertet content as String
   * @throws IOException
   */
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
