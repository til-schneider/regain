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
package net.sf.regain.util.sharedtag.taglib;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.jsp.JspException;

/**
 * Eine JspException, die eingebettete Exceptions unterst�tzt.
 * <p>
 * This class has the same code as {@link net.sf.regain.RegainException}.
 * The only difference is, that is inherits from <code>JspException</code>.
 *
 * @author Til Schneider, www.murfman.de
 */
public class ExtendedJspException extends JspException {

  /** The nested exception. May be null. */
  private Throwable mCause;


  /**
   * Creates a new instance of SearchException.
   *
   * @param message The error message
   */
  public ExtendedJspException(String message) {
    this(message, null);
  }



  /**
   * Creates a new instance of SearchException.
   *
   * @param message The error message
   * @param cause The nested exception.
   */
  public ExtendedJspException(String message, Throwable cause) {
    super(message);

    mCause = cause;
  }



  /**
   * Gets the cause of this exception. (May be null)
   *
   * @return The cause of this exception or <code>null</code> if there no other
   *         Throwable has caused this exception.
   */
  public Throwable getCause() {
    return mCause;
  }


  /**
   * Show cause as part of the Message
   *
   * @return Message of this exception, with message of cause (if available)
   */
  @Override
  public String getMessage() {
    String msg = super.getMessage();
    if (mCause != null)
      msg += " (" + mCause.getMessage() + ")";
    return msg;
  }

  /**
   * Prints the stack trace of this exception an of the nested exception, if
   * present.
   *
   * @param stream The stream to print to.
   */
  public void printStackTrace(PrintStream stream) {
    super.printStackTrace(stream);

    if ((mCause != null) && (! superClassPrintsCause())) {
      stream.println("Caused by: " + mCause.getMessage() + ":");
      mCause.printStackTrace(stream);
    }
  }



  /**
   * Prints the stack trace of this exception an of the nested exception, if
   * present.
   *
   * @param writer The writer to print to.
   */
  public void printStackTrace(PrintWriter writer) {
    super.printStackTrace(writer);

    if ((mCause != null) && (! superClassPrintsCause())) {
      writer.println("Caused by: " + mCause.getMessage() + ":");
      mCause.printStackTrace(writer);
    }
  }


  /**
   * Gets whether the superclass is able to print the cause of the exception.
   * This is true for Java 1.4 and above.
   *
   * @return Whether the superclass is able to print the cause of the exception.
   */
  private boolean superClassPrintsCause() {
    // Check whether there is a getCause method in the super class
    try {
      getClass().getSuperclass().getMethod("getCause");

      // The superclass has a getCause method -> It must be Java 1.4 or more
      return true;
    }
    catch (Exception exc) {
      // The superclass has no getCause method -> It must be Java 1.3 or less
      return false;
    }
  }

}
