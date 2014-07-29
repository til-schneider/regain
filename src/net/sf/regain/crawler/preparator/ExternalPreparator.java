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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

/**
 * Prepares a document by calling an external program that writes the plain text
 * to Standard.out.
 *
 * @author Til Schneider, www.murfman.de
 * @author Paul Ortyl
 */
public class ExternalPreparator extends AbstractPreparator {

  /** The command pattern. */
  private String[] mCommandLineArr;

  private RE[] mUrlRegexArr;

  private boolean[] mCheckExitCodeArr;


  /**
   * Creates a new instance of ExternalPreparator.
   *
   * @throws RegainException If creating the preparator failed.
   */
  public ExternalPreparator() throws RegainException {
    super("");
  }

  @Override
  public void init(PreparatorConfig config) throws RegainException {
    Map[] commandArr = config.getSectionsWithName("command");
    mCommandLineArr = new String[commandArr.length];
    mUrlRegexArr = new RE[commandArr.length];
    mCheckExitCodeArr = new boolean[commandArr.length];

    for (int i = 0; i < commandArr.length; i++) {
      String urlPattern = (String) commandArr[i].get("urlPattern");
      String commandLine = (String) commandArr[i].get("commandLine");
      String checkExitCode = (String) commandArr[i].get("checkExitCode");

      if (urlPattern == null) {
        throw new RegainException("Error in ExternalPreparator config: No " +
                "urlPattern defined in command section #" + (i + 1));
      }
      if (commandLine == null) {
        throw new RegainException("Error in ExternalPreparator config: No " +
                "commandLine defined in command section #" + (i + 1));
      }

      try {
        mUrlRegexArr[i] = new RE(urlPattern);
      }
      catch (RESyntaxException exc) {
        throw new RegainException("Error in ExternalPreparator config: " +
                "urlPattern has wrong syntax: " + urlPattern, exc);
      }
      mCommandLineArr[i] = commandLine;
      mCheckExitCodeArr[i] = (checkExitCode == null) ? true : checkExitCode.equals("true");
    }
  }


  @Override
  public boolean accepts(RawDocument rawDocument) {
    for (int i = 0; i < mUrlRegexArr.length; i++) {
      if (mUrlRegexArr[i].match(rawDocument.getUrl())) {
        return true;
      }
    }

    return false;
  }


  // overridden super().super().prepare
  // @Override
  public void prepare(RawDocument rawDocument) throws RegainException {
    // Get the right command line
    String commandLine = null;
    boolean checkExitCode = true;
    for (int i = 0; i < mUrlRegexArr.length; i++) {
      if (mUrlRegexArr[i].match(rawDocument.getUrl())) {
        commandLine = mCommandLineArr[i];
        checkExitCode = mCheckExitCodeArr[i];
        break;
      }
    }
    if (commandLine == null)
      throw new RegainException("Running external command failed: no Regex matched, so I don't know which command to use.");

    // Execute the command
    String filename = rawDocument.getContentAsFile().getAbsolutePath();
    String cmd = RegainToolkit.replace(commandLine, "${filename}", filename);
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      InputStream stream = process.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      StringWriter writer = new StringWriter();

      RegainToolkit.pipe(reader, writer);

      stream.close();
      reader.close();
      writer.close();

      // Check the exit code
      if (checkExitCode) {
        int exitCode;
        try {
          exitCode = process.waitFor();
        } catch (InterruptedException exc2) {
          throw new RegainException("Waiting for termination of external command" +
                  " failed: " + cmd, exc2);
        }
        if (exitCode != 0) {
          throw new RegainException("External command returned exit code "
              + exitCode + ": '" + cmd + "'");
        }
      }

      String cleanedContent = writer.toString();
      setCleanedContent(cleanedContent);
    } catch (IOException exc) {
      throw new RegainException("Running external command failed: '" + cmd + "'", exc);
    }
  }

}
