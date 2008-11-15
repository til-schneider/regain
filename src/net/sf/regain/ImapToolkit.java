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
package net.sf.regain;

import com.sun.mail.imap.IMAPFolder;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.URLName;

/**
 * Toolkit for handling IMAP specific functions. We label the messages/attachments with the following
 * pattern:
 * imap-URL/foldername/message_uid_(attachment_id) (attachment is optional)
 * Remark: The above URL is not a valid URL and has to be handled a specialised way
 * 
 * @author Thomas Tesche (thtesche), http://www.thtesche.com/
 */
public class ImapToolkit {

  private static Pattern messagePattern = Pattern.compile(".*((message_([0-9]+))(_attachment_([0-9]+))*)$");

  public static Pattern getMessagePattern() {
    return messagePattern;
  }

  public static boolean isMessageURL(String url) throws Exception {

    URLName urlName = new URLName(url);
    boolean result = false;

    // are there references to a message and optional to an attachment in the message
    if (urlName.getFile() != null) {
      try {
        Matcher matcher = messagePattern.matcher(url);
        matcher.find();
        if (matcher.groupCount() > 0) {
          if (matcher.group(1).startsWith("message")) {
            result = true;
          }
        }

      } catch (IllegalStateException matcherEx) {
        //System.out.println(matcherEx);
        //matcherEx.printStackTrace(System.out);
      }

    }
    return result;
  }

  /**
   * Method removes the message identifier from an IMAP-url
   * 
   * @param url the URL with message identifier (e.g. host:port/path/message_UID
   * @return the 'real' IMAP URL (e.g. host:port/path/
   * @throws java.lang.Exception
   */
  public static String cutMessageIdentifier(String url) throws Exception {

    String result = "";
    Matcher matcher = messagePattern.matcher(url);
    matcher.find();
    if (matcher.groupCount() > 0) {
      // the first group is the whole message identifier
      result = url.replaceFirst("/" + matcher.group(1), "");
    //System.out.println(matcher.group(1) + " " + matcher.group(2));
    }
    return result;
  }

  /**
   * Determines all subfolder from a start folder and the count of the messages in every folder.
   * 
   * @param entryFolder the basis folder to start from
   * @param recursive <code>true</code> if the folders and messages count should 
   *        be obtained recursivly
   * @return Map of folder name and message count for this folder
   * @throws java.lang.Exception
   */
  public static Map<String, Integer> getAllFolders(IMAPFolder entryFolder, boolean recursive)
    throws Exception {

    Map<String, Integer> result = new Hashtable<String, Integer>();

    IMAPFolder currFolder = entryFolder;

    if (currFolder.list().length > 0) {
      IMAPFolder[] folderList = (IMAPFolder[]) currFolder.list();

      // Iterate over all subfolder
      for (IMAPFolder folder : folderList) {
        if (folder.exists()) {
          try {
            folder.open(Folder.READ_ONLY);
            result.put(folder.getName(), folder.getMessageCount());

            folder.close(false);

          } catch (MessagingException messageEx) {
            // Some folders are forbidden to read but we need the name nevertheless
            // System.out.println("Folder :" + folder.getFullName() + " ließ sich nicht lesen");
            result.put(folder.getFullName(), 0);
          }
          // estimate recursive the subfolders
          if (recursive) {
            Map<String, Integer> subFolders = (Hashtable) getAllFolders(folder, true);

            for (Map.Entry<String, Integer> entry : subFolders.entrySet()) {
              result.put(entry.getKey(), entry.getValue());
            }
          }
        }
      }
    }
    return result;
  }
}
