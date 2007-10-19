/*
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-01-19 22:03:53 +0100 (Do, 19 Jan 2006) $
 *   $Author: til132 $
 * $Revision: 191 $
 */
package net.sf.regain.search.sharedlib.input;

import java.util.Arrays;
import java.util.HashSet;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
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
 * <li><code>field</code>: The name of the index field to created the list for.</li>
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
      // Undo the encoding of spaces done by Crawler.addJob
      String value = fieldValues[i];
      String unescapedValue = RegainToolkit.replace(value, "%20", " ");

      if (unescapedValue.length() == value.length()) {
        // Nothing was replaced -> We don't have to set an extra value
        response.print("<option>" + value + "</option>");
      } else {
        // There was something replaced -> Set an extra value
        response.print("<option value=\"" + value + "\">" + unescapedValue + "</option>");
      }
    }
    response.print("</select>");
  }

}
