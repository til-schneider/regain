package net.sf.regain.search.config;

import java.io.File;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
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
  @Override
  public SearchConfig createSearchConfig(PageRequest request)
    throws RegainException
  {
    String configFileName = request.getInitParameter("searchConfigFile");
    if (configFileName == null) {
      throw new RegainException("The init parameter 'searchConfigFile' was not specified.");
    }

    File configFile = new File(request.getWorkingDir(), configFileName);
    RegainToolkit.addLibraryJarPath(configFile.getParentFile());
    try {
      return new XmlSearchConfig(configFile);
    }
    catch (RegainException exc) {
      throw new RegainException("Loading configuration file failed: "
          + configFile.getAbsolutePath(), exc);
    }
  }

}
