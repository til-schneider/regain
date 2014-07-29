package net.sf.regain.util.io;

import net.sf.regain.RegainException;

/**
 * An interface for printing Strings. This is needed for separating
 * {@link net.sf.regain.util.io.MemoryAppender} from
 * {@link net.sf.regain.util.sharedtag.PageResponse}. Otherwise the
 * MemoryAppender couldn't be used without having PageResponse in the classpath.
 *
 * @author Tilman Schneider, STZ-IDA an der HS Karlsruhe
 */
public interface Printer {

  /**
   * Prints a text.
   *
   * @param text The text to print.
   * @throws RegainException If printing failed.
   */
  public void print(String text) throws RegainException;

}
