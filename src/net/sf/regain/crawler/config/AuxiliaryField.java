/*
 * CVS information:
 *  $RCSfile: AuxiliaryField.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/AuxiliaryField.java,v $
 *     $Date: 2005/02/15 09:30:32 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.config;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import net.sf.regain.RegainException;

/**
 * An auxiliary field is a additional field put into the index.
 * <p>
 * Example: If you have a directory with a sub directory for every project,
 * then you may create a field with the project's name.
 * <p>
 * The folling rule will create a field "project" with the value "otto23"
 * from the URL "file://c:/projects/otto23/docs/Spez.doc":
 * <code>new AuxiliaryField("project", "^file://c:/projects/([^/]*)", 1)</code>
 * <p>
 * URLs that doen't match will get no "project" field.
 * <p>
 * Having done this you may search for "Offer project:otto23" and you will get
 * only hits from this project directory.
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class AuxiliaryField {
  
  /** The name of the auxiliary field. */
  private String mFieldName;
  
  /** The regex that extracts the value of the field. */
  private RE mUrlRegex;
  
  /** The group of the regex that contains the value. */
  private int mUrlRegexGroup;
  

  /**
   * Creates a new instance of AuxiliaryField.
   * 
   * @param fieldName The name of the auxiliary field.
   * @param urlRegex The regex that extracts the value of the field.
   * @param urlRegexGroup The group of the regex that contains the value.
   * 
   * @throws RegainException If the regex has a syntax error.
   */
  public AuxiliaryField(String fieldName, String urlRegex, int urlRegexGroup)
    throws RegainException
  {
    mFieldName = fieldName;
    
    try {
      mUrlRegex = new RE(urlRegex);
    }
    catch (RESyntaxException exc) {
      throw new RegainException("Regex of auxiliary field '" + fieldName
          + "' has a wrong syntax: '" + urlRegex + "'", exc);
    }
    
    mUrlRegexGroup = urlRegexGroup;
  }
  
  
  /**
   * Gets the name of the auxiliary field.
   * 
   * @return The name of the auxiliary field.
   */
  public String getFieldName() {
    return mFieldName;
  }
  
  
  /**
   * Gets the regex that extracts the value of the field.
   * 
   * @return The regex that extracts the value of the field.
   */
  public RE getUrlRegex() {
    return mUrlRegex;
  }
  
  
  /**
   * Gets the group of the regex that contains the value.
   * 
   * @return The group of the regex that contains the value.
   */
  public int getUrlRegexGroup() {
    return mUrlRegexGroup;
  }
  
}
