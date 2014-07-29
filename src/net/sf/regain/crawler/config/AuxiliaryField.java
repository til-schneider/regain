package net.sf.regain.crawler.config;

import net.sf.regain.RegainException;

import org.apache.regexp.RE;

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

  /** Specifies whether the field value should be stored in the index. */
  private boolean mStore;

  /** Specifies whether the field value should be indexed. */
  private boolean mIndex;

  /** Specifies whether the field value should be tokenized. */
  private boolean mTokenize;


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
   * @param store Specifies whether the field value should be stored in the
   *        index.
   * @param index Specifies whether the field value should be indexed.
   * @param tokenize Specifies whether the field value should be tokenized.
   * 
   * @throws RegainException If the regex has a syntax error.
   */
  public AuxiliaryField(String fieldName, String value, boolean toLowerCase,
    RE urlRegex, int urlRegexGroup, boolean store, boolean index, boolean tokenize)
    throws RegainException
  {
    mFieldName = fieldName;
    mValue = value;
    mToLowerCase = toLowerCase;
    mUrlRegex = urlRegex;
    mUrlRegexGroup = urlRegexGroup;
    mStore = store;
    mIndex = index;
    mTokenize = tokenize;
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

  
  /**
   * Returns whether the field value should be stored in the index.
   *
   * @return whether the field value should be stored in the index.
   */
  public boolean isStored() {
    return mStore;
  }

  
  /**
   * Returns whether the field value should be indexed.
   *
   * @return whether the field value should be indexed.
   */
  public boolean isIndexed() {
    return mIndex;
  }

  
  /**
   * Returns whether the field value should be tokenized.
   *
   * @return whether the field value should be tokenized.
   */
  public boolean isTokenized() {
    return mTokenize;
  }
  
}
