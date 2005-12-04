/*
 * CVS information:
 *  $RCSfile: AuxiliaryField.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/config/AuxiliaryField.java,v $
 *     $Date: 2005/11/14 08:12:56 $
 *   $Author: til132 $
 * $Revision: 1.2 $
 */
package net.sf.regain.crawler.config;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import net.sf.regain.RegainException;
import net.sf.regain.XmlToolkit;

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

  /**
   * The value of the auxiliary field. If null, the value will be extracted from
   * the regex using the urlRegexGroup.
   */
  private String mValue;

  /** Specifies whether the (extracted) value should be converted to lower case. */
  private boolean mToLowerCase;
  
  /** The regex that extracts the value of the field. */
  private RE mUrlRegex;
  
  /** The group of the regex that contains the value. */
  private int mUrlRegexGroup;


  /**
   * Creates a new instance of AuxiliaryField.
   * 
   * @param fieldName The name of the auxiliary field.
   * @param value The value of the auxiliary field. If null, the value will be
   *        extracted from the regex using the urlRegexGroup.
   * @param toLowerCase Whether the (extracted) value should be converted to
   *        lower case.
   * @param urlRegex The regex that extracts the value of the field.
   * @param urlRegexGroup The group of the regex that contains the value.
   * 
   * @throws RegainException If the regex has a syntax error.
   */
  public AuxiliaryField(String fieldName, String value, boolean toLowerCase,
    RE urlRegex, int urlRegexGroup)
    throws RegainException
  {
    mFieldName = fieldName;
    mValue = value;
    mToLowerCase = toLowerCase;
    mUrlRegex = urlRegex;
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
   * Returns the value of the auxiliary field.
   * <p>
   * If null, the value will be extracted from the regex using the urlRegexGroup.
   *
   * @return The value of the auxiliary field.
   */
  public String getValue() {
    return mValue;
  }


  /**
   * Returns whether the (extracted) value should be converted to lower case.
   *
   * @return Whether the (extracted) value should be converted to lower case. 
   */
  public boolean getToLowerCase() {
    return mToLowerCase;
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
