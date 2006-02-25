package net.sf.regain.crawler;

import net.sf.regain.RegainException;

/**
 * Used for handling HTTP redirects.
 *
 * @see net.sf.regain.crawler.CrawlerToolkit#getHttpStream(java.net.URL)
 * @author Gerhard Olsson
 */
public class RedirectException extends RegainException {

  /** Extra info, for instance redirectUrl */
  private String mRedirectUrl;


  /**
   * Creates a new instance of RedirectException.
   *
   * @param message The error message. 
   * @param url The URL the redirect points to.
   */
  public RedirectException(String message, String url) {
    super(message);

    mRedirectUrl = url;
  }

  /**
   * Gets the redirectUrl of this exception. (May be null)
   *
   * @return The extra info of this exception.
   */
  public String getRedirectUrl() {
    return mRedirectUrl;
  }

}
