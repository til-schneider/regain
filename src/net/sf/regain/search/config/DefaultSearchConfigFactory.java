/*
 * CVS information:
 *  $RCSfile: DefaultSearchConfigFactory.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/config/DefaultSearchConfigFactory.java,v $
 *     $Date: 2005/08/10 14:00:45 $
 *   $Author: til132 $
 * $Revision: 1.3 $
 */
package net.sf.regain.search.config;

import java.io.File;

import net.sf.regain.RegainException;
import net.sf.regain.util.sharedtag.PageRequest;

/**
 * Loads the search config from a XML file. The location of the XML file is
 * specified by the init parameter "searchConfigFile".
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class DefaultSearchConfigFactory implements SearchConfigFactory {

  /**
   * Creates the configuration of the search mask.
   * 
   * @param request The page request. Used to get the "configFile" init
   *        parameter, which holds the name of the configuration file.
   * @return The configuration of the search mask.
   * @throws RegainException If loading failed.
   */
  public SearchConfig createSearchConfig(PageRequest request)
    throws RegainException
  {
    String configFileName = request.getInitParameter("searchConfigFile");
    if (configFileName == null) {
      throw new RegainException("The init parameter 'searchConfigFile' was not specified.");
    }

    File configFile = new File(request.getWorkingDir(), configFileName);
    try {
      return new XmlSearchConfig(configFile);
    }
    catch (RegainException exc) {
      throw new RegainException("Loading configuration file failed: "
          + configFile.getAbsolutePath(), exc);
    }
  }

}
