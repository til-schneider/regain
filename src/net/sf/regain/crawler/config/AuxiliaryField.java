package net.sf.regain.crawler.config;

import net.sf.regain.RegainException;

import org.apache.regexp.RE;

/**
 * An auxiliary field is a additional field put into the index.
 * <p>
 * Example: If you have a directory with a sub directory for every project,
 * then you may create a field with the project's name.
 * <p>
 * The following rule will create a field "project" with the value "otto23"
 * from the URL "file://c:/projects/otto23/docs/Spez.doc":
 * <code>new AuxiliaryField("project", "^file://c:/projects/([^/]*)", 1)</code>
 * <p>
 * URLs that doesn't match will get no "project" field.
 * <p>
 * Having done this you may search for "Offer project:otto23" and you will get
 * only hits from this project directory.
 *
 * @author Tilman Schneider, www.murfman.de
 */
public class AuxiliaryField {

  /** The source field types */
  public static enum SourceField {
    /** The document's URL */
    URL,
    /** The document's path. For a file this is the path from the file system. For other documents this equals the URL */
    PATH
  }

  /** The source field on which to apply the regex. */
  private SourceField mSourceField;

  /** The name of the auxiliary field to create. */
  private String mTargetFieldName;

  /**
   * The value of the auxiliary field. If null, the value will be extracted from
   * the regex using the urlRegexGroup.
   */
  private String mValue;

  /** Specifies whether the (extracted) value should be converted to lower case. */
  private boolean mToLowerCase;

  /** The regex that extracts the value of the field. */
  private RE mRegex;

  /** The group of the regex that contains the value. */
  private int mRegexGroup;

  /** Specifies whether the field value should be stored in the index. */
  private boolean mStore;

  /** Specifies whether the field value should be indexed. */
  private boolean mIndex;

  /** Specifies whether the field value should be tokenized. */
  private boolean mTokenize;


  /**
   * Creates a new instance of AuxiliaryField.
   *
   * @param sourceField The source field on which to apply the regex.
   * @param targetFieldName The name of the auxiliary field.
   * @param value The value of the auxiliary field. If null, the value will be
   *        extracted from the regex using the urlRegexGroup.
   * @param toLowerCase Whether the (extracted) value should be converted to
   *        lower case.
   * @param regex The regex that extracts the value of the field.
   * @param regexGroup The group of the regex that contains the value.
   * @param store Specifies whether the field value should be stored in the
   *        index.
   * @param index Specifies whether the field value should be indexed.
   * @param tokenize Specifies whether the field value should be tokenized.
   *
   * @throws RegainException If the regex has a syntax error.
   */
  public AuxiliaryField(SourceField sourceField, String targetFieldName, String value, boolean toLowerCase,
    RE regex, int regexGroup, boolean store, boolean index, boolean tokenize)
    throws RegainException
  {
    mSourceField = sourceField;
    mTargetFieldName = targetFieldName;
    mValue = value;
    mToLowerCase = toLowerCase;
    mRegex = regex;
    mRegexGroup = regexGroup;
    mStore = store;
    mIndex = index;
    mTokenize = tokenize;
  }


  /**
   * Returns the source field on which to apply the regex.
   *
   * @return The source field on which to apply the regex.
   */
  public SourceField getSourceField() {
    return mSourceField;
  }


  /**
   * Gets the name of the auxiliary field to create.
   *
   * @return The name of the auxiliary field to create.
   */
  public String getTargetFieldName() {
    return mTargetFieldName;
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
  public RE getRegex() {
    return mRegex;
  }


  /**
   * Gets the group of the regex that contains the value.
   *
   * @return The group of the regex that contains the value.
   */
  public int getRegexGroup() {
    return mRegexGroup;
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
