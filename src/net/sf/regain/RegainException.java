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
 *  $RCSfile: RegainException.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/RegainException.java,v $
 *     $Date: 2004/07/28 20:26:05 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An exception for the lucene search.
 * <p>
 * It can handle nested exceptions. Nested exceptions will be printed with the
 * stacktrace.
 * <p>
 * This class has the same code as
 * {@link net.sf.regain.search.ExtendedJspException}.
 * The only difference is, that is inherits from <code>Exception</code>.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class RegainException extends Exception {

  /** The nested exception. May be null. */
  private Throwable mCause;



  /**
   * Creates a new instance of SearchException.
   *
   * @param message The error message
   */
  public RegainException(String message) {
    this(message, null);
  }



  /**
   * Creates a new instance of SearchException.
   *
   * @param message The error message
   * @param cause The nested exception.
   */
  public RegainException(String message, Throwable cause) {
    super(message);

    mCause = cause;
  }



  /**
   * Gets the cause of this exception. (May be null)
   * 
   * @return The cause of this exception.
   */
  public Throwable getCause() {
    return mCause;
  }



  /**
   * Prints the stack trace of this exception an of the nested exception, if
   * present.
   *
   * @param stream The stream to print to.
   */
  public void printStackTrace(PrintStream stream) {
    super.printStackTrace(stream);

    if (mCause != null) {
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

    if (mCause != null) {
      writer.println("Caused by: " + mCause.getMessage() + ":");
      mCause.printStackTrace(writer);
    }
  }

}
