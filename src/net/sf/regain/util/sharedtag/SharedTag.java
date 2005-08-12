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
 *  $RCSfile: SharedTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/util/sharedtag/SharedTag.java,v $
 *     $Date: 2005/08/10 14:00:44 $
 *   $Author: til132 $
 * $Revision: 1.6 $
 */
package net.sf.regain.util.sharedtag;

import java.util.HashMap;
import java.util.Locale;

import net.sf.regain.RegainException;
import net.sf.regain.util.io.Localizer;
import net.sf.regain.util.io.MultiLocalizer;

/**
 * A tag that may be used within the taglib technology or the simpleweb
 * technology.
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class SharedTag {
  
  /** Specifies that the tag body should be evaluated. */
  public static final int EVAL_TAG_BODY = 1;

  /** Specifies that the tag body should be skipped. */
  public static final int SKIP_TAG_BODY = 2;
  
  /** The MultiLocalizer that holds the Localizers for the tags. */
  private static MultiLocalizer mMultiLocalizer;

  /** The parameters for this tag. May be null. */
  private HashMap mParamMap;
  
  /**
   * The current Localizer. Is <code>null</code> when the is currently not
   * executed.
   */
  private Localizer mLocalizer;


  /**
   * Creates a new instance of SharedTag.
   */
  public SharedTag() {
  }
  
  
  /**
   * Gets the name of this Tag.
   * <p>
   * The name of the tag is extracted from the class name. In order to work,
   * subclasses must be named using the pattern [TagName]Tag
   * (e.g. <code>NavigationTag</code> for the tag <code>navigation</code>).
   * 
   * @return The name of this tag.
   */
  public String getTagName() {
    String className = getClass().getName();
    
    // Remove the package name
    int packageEnd = className.lastIndexOf('.');
    
    // Remove the Tag postfix
    int nameEnd = className.length();
    if (className.endsWith("Tag")) {
      nameEnd -= 3;
    }
    // NOTE: This even works, if packageEnd is -1
    String tagName = className.substring(packageEnd + 1, nameEnd).toLowerCase();
    
    int packageStart;
    while ((packageStart = className.lastIndexOf('.', packageEnd - 1)) != -1) {
      String packageName = className.substring(packageStart + 1, packageEnd);
      if (packageName.equals("sharedlib")) {
        break;
      } else {
        tagName = packageName + "_" + tagName;
        packageEnd = packageStart;
      }
    }
    
    return tagName;
  }


  /**
   * Sets a parameter.
   * 
   * @param name The parameter's name.
   * @param value The parameter's value.
   */
  public void setParameter(String name, String value) {
    if (mParamMap == null) {
      mParamMap = new HashMap();
    }
    
    mParamMap.put(name, value);
  }


  /**
   * Gets a parameter.
   * 
   * @param name The parameter's name.
   * @return The value of the parameter or <code>null</code> if the parameter
   *         was not set.
   */
  public String getParameter(String name) {
    if (mParamMap == null) {
      return null;
    } else {
      String value = (String) mParamMap.get(name);
      if (value != null) {
        value = localize(value);
      }
      return value;
    }
  }


  /**
   * Gets a parameter.
   * 
   * @param name The parameter's name.
   * @param defaultValue The value to return if the parameter was not set.
   * @return The value of the parameter or the default value if the parameter
   *         was not set.
   */
  public String getParameter(String name, String defaultValue) {
    String asString = getParameter(name);
    if (asString == null) {
      return defaultValue;
    } else {
      return asString;
    }
  }
  
  
  /**
   * Gets a parameter.
   * 
   * @param name The parameter's name.
   * @param mandatory Specifies whether the parameter is mandatory.
   * @return The parameter value or <code>null</code> if no such parameter was
   *         given and mandatory is <code>false</code>.
   * @throws RegainException If mandatory is <code>true</code> and the parameter
   *         was not specified.
   */
  public String getParameter(String name, boolean mandatory)
    throws RegainException
  {
    String asString = getParameter(name);
    if (mandatory && (asString == null)) {
      throw new RegainException("Parameter " + name + " of tag "
          + getTagName() + " was not specified");
    } else {
      return asString;
    }
  }
  
  
  /**
   * Gets a parameter and converts it to an int.
   *
   * @param name The name of the parameter.
   * @param defaultValue The value to return if the parameter is not set.
   * @throws RegainException When the parameter value is not a number.
   * @return The int value of the parameter.
   */
  public int getParameterAsInt(String name, int defaultValue)
    throws RegainException
  {
    String asString = getParameter(name);
    if (asString == null) {
      return defaultValue;
    } else {
      try {
        return Integer.parseInt(asString);
      }
      catch (NumberFormatException exc) {
        throw new RegainException("Parameter " + name + " of tag "
            + getTagName() + " must be a number: '" + asString + "'");
      }
    }
  }
  
  
  /**
   * Gets a parameter and converts it to a boolean.
   *
   * @param name The name of the parameter.
   * @param defaultValue The value to return if the parameter is not set.
   * @throws RegainException When the parameter value is not a number.
   * @return The int value of the parameter.
   */
  public boolean getParameterAsBoolean(String name, boolean defaultValue)
    throws RegainException
  {
    String asString = getParameter(name);
    if (asString == null) {
      return defaultValue;
    } else {
      if (asString.equalsIgnoreCase("true")) {
        return true;
      } else if (asString.equalsIgnoreCase("false")) {
        return false;
      } else {
        throw new RegainException("Parameter " + name + " of tag "
            + getTagName() + " must be a boolean: '" + asString + "'");
      }
    }
  }
  
  
  /**
   * Sets the tag execution context.
   * <p>
   * Is called by the shared tag engine before the start tag is processed.
   * 
   * @param request The request to get the context from
   * @throws RegainException If setting the context failed.
   */
  public final void setContext(PageRequest request)
    throws RegainException
  {
    // Get the Localizer
    mLocalizer = (Localizer) request.getContextAttribute("Localizer");
    if (mLocalizer == null) {
      // The default resource bundles are in english
      Locale.setDefault(Locale.ENGLISH);

      // Get the locale
      Locale locale = request.getLocale();
      if (locale == null) {
        locale = Locale.getDefault();
      }
      
      // Init the MultiLocalizer if nessesary
      if (mMultiLocalizer == null) {
        mMultiLocalizer = new MultiLocalizer(request.getResourceBaseUrl(), "msg");
      }
      
      // Get the localizer
      mLocalizer = mMultiLocalizer.getLocalizer(locale);
      request.setContextAttribute("Localizer", mLocalizer);
    }
  }
  
  
  /**
   * Unsets the tag execution context.
   * <p>
   * Is called by the shared tag engine after the end tag was processed.
   */
  public final void unsetContext() {
    mLocalizer = null;
  }
  
  
  /**
   * Gets the localizer.
   * <p>
   * Note: The localizer is only not null, when the tag is currently executed.
   * Which is in the {@link #printStartTag(PageRequest, PageResponse)}, the
   * {@link #printAfterBody(PageRequest, PageResponse)} ant the
   * {@link #printEndTag(PageRequest, PageResponse)}.
   * 
   * @return The localizer.
   */
  protected Localizer getLocalizer() {
    return mLocalizer;
  }


  /**
   * Localizes a text. Replaces all "{msg:...}" fields with the matching
   * localized messages.
   * 
   * @param text The text to localize.
   * @return The localized text.
   */
  private String localize(String text) {
    StringBuffer buffer = null;
    int startPos = 0;
    int endPos = 0;
    while ((startPos = text.indexOf("{msg:", endPos)) != -1) {
      if (buffer == null) {
        buffer = new StringBuffer(text.length());
      }
      
      // Add the text before
      buffer.append(text.substring(endPos, startPos));
      
      // Get the new endPos
      endPos = text.indexOf('}', startPos + 1);
      if (endPos == -1) {
        endPos = text.length();
      }
      
      // Append the localized message
      String key = text.substring(startPos + 5, endPos);
      buffer.append(mLocalizer.msg(key, "?"));
    }
    
    if (buffer != null) {
      // Append the last text
      buffer.append(text.substring(endPos + 1));
      
      text = buffer.toString();
    }
    
    return text;
  }


  /**
   * Called when the parser reaches the start tag.
   *  
   * @param request The page request.
   * @param response The page response.
   * @return {@link #EVAL_TAG_BODY} if you want the tag body to be evaluated or
   *         {@link #SKIP_TAG_BODY} if you want the tag body to be skipped.
   * @throws RegainException If there was an exception.
   */
  public int printStartTag(PageRequest request, PageResponse response)
    throws RegainException
  {
    return EVAL_TAG_BODY;
  }
  

  /**
   * Called after the body content was evaluated.
   *  
   * @param request The page request.
   * @param response The page response.
   * @return {@link #EVAL_TAG_BODY} if you want the tag body to be evaluated
   *         once again or {@link #SKIP_TAG_BODY} if you want to print the
   *         end tag.
   * @throws RegainException If there was an exception.
   */
  public int printAfterBody(PageRequest request, PageResponse response)
    throws RegainException
  {
    return SKIP_TAG_BODY;
  }


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
  }


  /**
   * Gets the String representation of this tag.
   * 
   * @return The String representation.
   */
  public String toString() {
    return getTagName();
  }

}
