/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.regain.crawler.preparator.java;

/**
 *
 * @author Thomas Tesche <thomas.tesche@clustersystems.de>
 */
public enum Type{ 

  CLASS("Class"), INTERFACE("Interface"), ENUM("Enum");
  private String _type;

  Type(String _type) {
    this._type = _type;
  }

  @Override
  public String toString() {
    return _type;
  }
}
