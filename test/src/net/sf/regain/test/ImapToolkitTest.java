/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.regain.test;

import net.sf.regain.*;
import junit.framework.TestCase;

/**
 *
 * @author thtesche
 */
public class ImapToolkitTest extends TestCase {

    public ImapToolkitTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

  /**
   * Test of isMessageURL method, of class ImapToolkit.
   */
  public void testIsMessageURL() throws Exception {
    System.out.println("isMessageURL");
    String url = "imaps://regaintest%40googlemail.com:meinpasswort99@imap.googlemail.com:993/googlemail/message_1";
    assertTrue( ImapToolkit.isMessageURL(url) );
    url = "imaps://regaintest%40googlemail.com:meinpasswort99@imap.googlemail.com:993/googlemail/message_x1";
    assertFalse(ImapToolkit.isMessageURL(url) );
  }

  /**
   * Test of cutMessageIdentifier method, of class ImapToolkit.
   */
  public void testCutMessageIdentifier() throws Exception {
    System.out.println("cutMessageIdentifier");
    String url = "imaps://regaintest%40googlemail.com:meinpasswort99@imap.googlemail.com:993/googlemail/message_1";
    String expResult = "imaps://regaintest%40googlemail.com:meinpasswort99@imap.googlemail.com:993/googlemail";
    String result = ImapToolkit.cutMessageIdentifier(url);
    assertEquals(expResult, result);

  }


}
