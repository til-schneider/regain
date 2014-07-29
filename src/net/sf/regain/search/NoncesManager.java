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
package net.sf.regain.search;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.Random;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;

/**
 * Create an check nonces for web requests
 * @author Benjamin Pick
 */
public class NoncesManager
{
  public static final int NONCE_TIMEOUT_SEC = 15 * 60;
  private static final String NONCE_STORE_CONTEXTATTRIBUTE_NAME = "nonces.used.";

  /**
   * @var Current system time
   */
  private long now;

  /**
   * @var System-specific salt.
   * We can't put in a static string here, as this wouldn't be secret anymore.
   */
  private String salt;

  /**
   * @var Request for nonceStore
   */
  private PageRequest mRequest;

  public NoncesManager()
  {
    now = System.currentTimeMillis();
    salt = NonceHelper.getSystemSpecificHashValue();
  }

  /**
   * Create a nonce for usage in a form
   *
   * @param action      action-specific constant
   * @param timestamp   timestamp to use
   * @return  Hash-Value
   */
  public String generateNonce(String action, String timestamp)
  {
    StringBuilder information = new StringBuilder(100);

    information.append(action);
    information.append(timestamp);
    information.append(salt);

    return NonceHelper.hash(information.toString(), null);
  }

  /**
   * Check if the nonce is correct.
   *
   * @param nonce      Nonce from request
   * @param action     Action specific to this request
   * @param timestamp  Timestamp that was used to create this nonce
   * @return
   */
  public boolean checkNonce(String nonce, String action, String timestamp)
  {
    long nonceTime;
    try {
      nonceTime = Long.parseLong(timestamp);
    } catch (NumberFormatException e) {
      return false;
    }

    if (nonceTime > now) // Future? That cannot be!
      return false;
    if (nonceTime + NONCE_TIMEOUT_SEC * 1000 < now) // timed out
      return false;

    if (nonce == null)
      return false;

    if (isNonceUsed(nonce))
      return false;
    markNonceUsed(nonce);

    String myNonce = generateNonce(action, timestamp);

    if (!myNonce.equals(nonce))
      return false;

    return true;
  }

  private void markNonceUsed(String nonce)
  {
    mRequest.setSessionAttribute(NONCE_STORE_CONTEXTATTRIBUTE_NAME + nonce, now);
  }

  private boolean isNonceUsed(String nonce)
  {
    Object result = mRequest.getSessionAttribute(NONCE_STORE_CONTEXTATTRIBUTE_NAME + nonce);
    return (result != null);
  }

  /**
   * Check if the nonce of this request is correct.
   *
   * @param request   Request - in order to get the nonce and the nonceStore
   * @param action    action-specific constant
   * @return FALSE if not valid.
   */
  public boolean checkNonce(PageRequest request, String action)
  {
    // TODO: Check if request was POST

    loadNonceStore(request);

    String nonce = null;
    String nonce_ts = null;
    try
    {
      nonce = request.getParameter("nonce");
      if (nonce == null)
         return false;
      nonce_ts = request.getParameter("nonce_ts");
      if (nonce_ts == null)
         return false;
    }
    catch (RegainException e)
    {
      return false;
    }
    return checkNonce(nonce, action, nonce_ts);
  }

  private void loadNonceStore(PageRequest request)
  {
    mRequest = request;
  }

  /**
   * Create a hidden input for usage in a form,
   * that contains a newly generated nonce.
   *
   * @param request
   * @param action
   * @return
   */
  public String generateHTML(PageRequest request, String action)
  {
    long now = System.currentTimeMillis();
    String nonce = generateNonce(action, Long.toString(now));
    String html = "\n<input type=\"hidden\" name=\"nonce\" value=\"" + nonce + "\" />";
    html += "\n<input type=\"hidden\" name=\"nonce_ts\" value=\"" + now + "\" />\n";
    return html;
  }

  private static class NonceHelper
  {

/* Currently not used

    private static Random rand = new Random();
    private static String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Create a random ASCII-String consisting of a certain number of chars.
     *
     * @param numChars  How many chars should the string have
     * @return  Random String
     *
    private static String randomString(int numChars)
    {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < numChars; i++) {
        sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
      }
      return sb.toString();
    }
*/
    public static String hash(String input, String algorithmName)
    {
      try
      {
        return _hash(input, HASH_ALGORITHM);
      }
      catch (NoSuchAlgorithmException e)
      {
        try
        {
          return _hash(input, null);
        }
        catch (NoSuchAlgorithmException e1)
        {
          return input;
        }
      }

    }

    /**
     * Create an MD5 Hash of an input String.
     * Uses the MD5 Algorithm of MessageDigest.
     * @param input String to Hash
     * @return  Hash (Hex-Encoded)
     * @throws NoSuchAlgorithmException Invalid algorithmName.
     */
    public static String _hash(String input, String algorithmName) throws NoSuchAlgorithmException{
       if (algorithmName == null)
         algorithmName = "MD5";
          StringBuilder res = new StringBuilder();
            MessageDigest algorithm = MessageDigest.getInstance(algorithmName);
            algorithm.reset();
            algorithm.update(input.getBytes());
            byte[] md5 = algorithm.digest();
            String tmp = "";
            for (int i = 0; i < md5.length; i++) {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1) {
                    res.append("0").append(tmp);
                } else {
                    res.append(tmp);
                }
            }
          return res.toString();
      }

    private static String HASH_ALGORITHM = "SHA-256";

    /**
     * This function generates a hash that should be
     * a) different for each machine/environment it runs int
     * b) same for each time the function is called, and if possible also between program invocations
     *
     * @return Hash-Value
     */
    public static String getSystemSpecificHashValue()
    {
      StringBuilder information = new StringBuilder(400);

      // System properties shouldn't change during runtime, right?
      // But not very random
      for(Object value : System.getProperties().values())
      {
        information.append(value);
      }

      // All environment variables are too many?
      // Well, can't take specific ones as I don't know the platform (Win/Linux)
      for(String value : System.getenv().values())
      {
        information.append(value);
      }

      return hash(information.toString(), HASH_ALGORITHM);

      // return "4"; // chosen by a fair dice roll. :-)
    }
  } // End inner class

}

