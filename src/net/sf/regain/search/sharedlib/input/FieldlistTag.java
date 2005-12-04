/*
 * CVS information:
 *  $RCSfile: FieldlistTag.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/search/sharedlib/input/FieldlistTag.java,v $
 *     $Date: 2005/08/19 11:48:57 $
 *   $Author: til132 $
 * $Revision: 1.5 $
 */
package net.sf.regain.search.sharedlib.input;

import java.util.Arrays;
import java.util.HashSet;

import net.sf.regain.RegainException;
import net.sf.regain.search.IndexSearcherManager;
import net.sf.regain.search.SearchToolkit;
import net.sf.regain.search.config.IndexConfig;
import net.sf.regain.util.sharedtag.PageRequest;
import net.sf.regain.util.sharedtag.PageResponse;
import net.sf.regain.util.sharedtag.SharedTag;

/**
 * Generates a combobox list that shows all distinct values of a field in the
 * index.
 * <p>
 * Tag Parameters:
 * <ul>
 * <li><code>field</code>: The name of the field to created the list for.</li>
 * <li><code>allMsg</code>: The message to show for the item that ignores this
 *     field.</li>
 * </ul>
 * 
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class FieldlistTag extends SharedTag {

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
    String fieldName = getParameter("field", true);
    String allMsg = getParameter("allMsg", true);

    // Get the IndexConfig
    IndexConfig[] configArr = SearchToolkit.getIndexConfigArr(request);
    String[] fieldValues;
    if (configArr.length == 1) {
      // We have only one index -> Get the field values
      IndexConfig config = configArr[0];
      IndexSearcherManager manager = IndexSearcherManager.getInstance(config.getDirectory());
      fieldValues = manager.getFieldValues(fieldName);
    } else {
      // We have multiple indexes -> Get the values of each index and merge them
      HashSet valueSet = new HashSet();
      for (int i = 0; i < configArr.length; i++) {
        IndexSearcherManager manager = IndexSearcherManager.getInstance(configArr[i].getDirectory());
        String[] currFieldValues = manager.getFieldValues(fieldName);
        for (int j = 0; j < currFieldValues.length; j++) {
          valueSet.add(currFieldValues[j]);
        }
      }

      // Put the merged values into an array
      fieldValues = new String[valueSet.size()];
      valueSet.toArray(fieldValues);

      // Sort the array
      Arrays.sort(fieldValues);
    }
    
    // Generate a combo box containing the field values
    response.print("<select name=\"field." + fieldName + "\" size=\"1\">");
    response.print("<option value=\"\">" + allMsg + "</option>");
    for (int i = 0; i < fieldValues.length; i++) {
      response.print("<option>" + fieldValues[i] + "</option>");
    }
    response.print("</select>");
  }

}
