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
package net.sf.regain.ui.desktop.config.sharedlib;

import net.sf.regain.RegainException;
import net.sf.regain.util.io.Localizer;
import net.sf.regain.util.io.MultiLocalizer;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates a combo box with the index update interval.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IntervalTag extends SharedTag {

  /** The MultiLocalizer for this class. */
  private static MultiLocalizer mMultiLocalizer = new MultiLocalizer(IntervalTag.class);

  /** The possible choices. */
  private final String[] CHOICES = { "60", "1440", "10080" };


  /**
   * Called when the parser reaches the end tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @throws RegainException If there was an exception.
   */
  public void printEndTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    Localizer localizer = mMultiLocalizer.getLocalizer(request.getLocale());
    String currValue = (String) request.getContextAttribute("settings.interval");
    
    response.print("<select name=\"interval\">");
    for (int i = 0; i < CHOICES.length; i++) {
      String value = CHOICES[i];
      String name  = localizer.msg("choice." + value, value);
      
      response.print("<option value=\"" + value + "\"");
      if (value.equals(currValue)) {
        response.print(" selected=\"selected\"");
      }
      response.print(">" + name + "</option>");
    }
    response.print("</select>");
  }
  
}
