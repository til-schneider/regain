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
 *  $RCSfile: JacobMsWordPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/JacobMsWordPreparator.java,v $
 *     $Date: 2005/03/14 15:03:34 $
 *   $Author: til132 $
 * $Revision: 1.9 $
 */
package net.sf.regain.crawler.preparator;

import java.util.HashSet;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.RegainToolkit;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.log4j.Logger;

import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import de.filiadata.lucene.spider.generated.msoffice2000.word.Application;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Document;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Documents;
import de.filiadata.lucene.spider.generated.msoffice2000.word.GroupShapes;
import de.filiadata.lucene.spider.generated.msoffice2000.word.HeaderFooter;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Paragraph;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Paragraphs;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Section;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Sections;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Selection;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Shape;
import de.filiadata.lucene.spider.generated.msoffice2000.word.Shapes;
import de.filiadata.lucene.spider.generated.msoffice2000.word.WdHeaderFooterIndex;

/**
 * Präpariert ein Microsoft-Word-Dokument für die Indizierung mit Hilfe der
 * <a href="http://danadler.com/jacob/">Jacob-API</a>, wobei
 * <a href="http://www.bigatti.it/projects/jacobgen/">Jacobgen</a>
 * genutzt wurde, um den Zugriff zu erleichtern.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Til Schneider, www.murfman.de
 */
public class JacobMsWordPreparator extends AbstractJacobMsOfficePreparator {

  /** The logger for this class */
  private static Logger mLog = Logger.getLogger(JacobMsWordPreparator.class);

  /**
   * The word application. Is <code>null</code> as long as no document was
   * processed.
   */
  private Application mWordApplication;
  
  /**
   * The word style names (style == format template) that are used by paragraphs
   * holding a headline. Is <code>null</code> if no headline styles were
   * configured. 
   */
  private HashSet mHeadlineStyleNameSet;
  

  /**
   * Creates a new instance of JacobMsPowerPointPreparator.
   */
  public JacobMsWordPreparator() {
    super(new String[] { "doc", "dot" });
  }


  /**
   * Initializes the preparator.
   * 
   * @param config The configuration
   * @throws RegainException If the configuration has an error.
   */
  public void init(PreparatorConfig config) throws RegainException {
    super.init(config);
    
    Map main = config.getSectionWithName("main");
    if (main != null) {
      String headlineStyles = (String) main.get("headlineStyles");
      if (headlineStyles != null) {
        String[] styleArr = RegainToolkit.splitString(headlineStyles, ";", true);
        mHeadlineStyleNameSet = new HashSet();
        for (int i = 0; i < styleArr.length; i++) {
          mHeadlineStyleNameSet.add(styleArr[i]);
        }
      }
    }
  }


  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    if (mWordApplication == null) {
      // COM-Thread initialisieren
      ComThread.InitSTA();

      // Neue Word-Applikation erstellen
      mLog.info("Starting MS Word");
      mWordApplication = new Application();

      // Word unsichtbar machen
      // mWordApplication.setVisible(false);
      Dispatch.put(mWordApplication, "Visible", new Variant(false));
    }

    try {
      // Dokument öffnen (Bei Konvertierung nicht fragen und Read only)
      // Workaround: Wenn das Dokument von einer anderen Person bearbeitet wird,
      //             dann erscheint ein Popup. Um das zu verhindern, wird in
      //             jedem Fall (auch bei file-Dokumenten) anstatt der
      //             Originaldatei eine temporäre Kopie genutzt, da diese
      //             unmöglich von jemandem bearbeitet werden kann.
      String fileName = rawDocument.getContentAsFile(true).getAbsolutePath();
      Documents docs = mWordApplication.getDocuments();
      Document doc = docs.open(new Variant(fileName),
                               new Variant(false),    // confirmConversions
                               new Variant(true));    // readOnly

      // iterate through the sections
      StringBuffer content = new StringBuffer();
      Sections sections = doc.getSections();
      for (int i = 1; i <= sections.getCount(); i++) {
        Section sec = sections.item(i);

        // Get the header of the first section as title
        if (i == 1) {
          int headerFirstPage = WdHeaderFooterIndex.wdHeaderFooterFirstPage;
          HeaderFooter firstHeader = sec.getHeaders().item(headerFirstPage);
          String title = firstHeader.getRange().getText();
          setTitle(title);
        }

        // Get the text
        sec.getRange().select();
        content.append(getSelection(mWordApplication) + "\n");
      }

      // iterate through the shapes
      Shapes shapes = doc.getShapes();
      for (int i = 1; i <= shapes.getCount(); i++) {
        Shape shape = shapes.item(new Variant(i));
        appendShape(shape, content);
      }
      
      // iterate through the paragraphs and extract the headlines
      StringBuffer headlines = null;
      if ((mHeadlineStyleNameSet != null) && (! mHeadlineStyleNameSet.isEmpty())) {
        Paragraphs paragraphs = doc.getParagraphs();
        for (int i = 1; i <= paragraphs.getCount(); i++) {
          Paragraph paragraph = paragraphs.item(i);
          
          // Get the name of the style for this paragraph
          // NOTE: See the Style class for getting other values from the style
          Object styleDispatch = paragraph.getFormat().getStyle().getDispatch();
          String formatName = Dispatch.get(styleDispatch, "NameLocal").toString();
          
          if (mHeadlineStyleNameSet.contains(formatName)) {
            // This paragraph is a headline -> add it to the headlines StringBuffer
            
            // Extract the text
            paragraph.getRange().select();
            String text = getSelection(mWordApplication);
            text = removeBinaryStuff(text);
            
            // Add it to the headlines
            if (headlines == null) {
              headlines = new StringBuffer();
            }
            headlines.append(text + "\n");
            
            if (mLog.isDebugEnabled()) {
              mLog.debug("Extracted headline: '" + text + "'");
            }
          }
        }
      }
      
      // Read the document properties
      readProperties(doc);
      
      // Set the extracted text and the headlines
      setCleanedContent(content.toString());
      if (headlines != null) {
        setHeadlines(headlines.toString());
      }

      // Dokument schließen (ohne Speichern)
      doc.close(new Variant(false));
    }
    catch (ComFailException exc) {
      throw new RegainException("Using COM failed.", exc);
    }
  }

  
  /**
   * Gets the currently selected text from a Word application.
   * 
   * @param wordAppl The Word application to get the selected text from.
   * @return The currently selected text.
   */
  private String getSelection(Application wordAppl) {
    Selection sel = wordAppl.getSelection();
    // Alternative (VB): sel.moveEndWhile(?? cset:=vbCr ??, WdConstants.wdBackward);
    // Alternative (VB): Call app.ActiveDocument.Bookmarks.Item("\endofdoc").Select()
    sel.moveEnd();
    sel.copy();
    return sel.getText();
  }

  
  /**
   * Appends the text content of a shape to a StringBuffer.
   * 
   * @param shape The shape to add.
   * @param buffer The buffer where to append the text
   */
  private void appendShape(Shape shape, StringBuffer buffer) {
    String shapeName = shape.getName();
    if (shapeName.startsWith("Text Box ")) {
      shape.getTextFrame().getTextRange().select();
      buffer.append(getSelection(mWordApplication) + "\n");
    }
    else if (shapeName.startsWith("Group ")) {
      GroupShapes group = shape.getGroupItems();
      for (int i = 1; i <= group.getCount(); i++) {
        Shape child = group.item(new Variant(i));
        appendShape(child, buffer);
      }
    }
  }
  
  
  /**
   * Removes all characters that are less that 32 from the given String 
   * 
   * @param text The String where to remove the binary stuff.
   * @return The cleaned String.
   */
  private String removeBinaryStuff(String text) {
    StringBuffer newText = new StringBuffer(text.length());
    
    for (int j = 0; j < text.length(); j++) {
      char c = text.charAt(j);
      if (c >= 32) {
        newText.append(c);
      }
    }
    
    return newText.toString();
  }
  

  /**
   * Frees all resources reserved by the preparator.
   * <p>
   * Is called at the end of the crawler process after all documents were
   * processed.
   * 
   * @throws RegainException If freeing the resources failed.
   */
  public void close() throws RegainException {
    if (mWordApplication != null) {
      try {
        // Word schließen
        mWordApplication.quit();
        mLog.info("Closed MS Word");
      }
      catch (Throwable thr) {
        throw new RegainException("Using COM failed.", thr);
      }
      finally {
        // Alle Ressourcen des COM-Threads freigeben
        ComThread.Release();
      }
    }
  }


  /* VB source code

  'ObjectWord ist das Word-Object
  If objWord Is Nothing Then
    objWord = New Word.Application
  End If

  'Wenn Du keinen With-Block machst mußt Du bei VB überall das objWord
  'vornedran schreiben! So reicht ein "."
  With objWord

    '### Word Sichtbar/unsichtbar ###
    .Visible = False

    '### Oeffnen des Dokuments ###
    .Documents.Open("Dokumentenname").Activate()

    '### Header+footer kopieren Anfang ###

    '### Ueberschrift kopieren header ###
    .Documents.Item(("Dokumentenname").Activate()
    strUeberschrift = .ActiveDocument.Sections.Item(1).Headers.Item(Word.WdHeaderFooterIndex.wdHeaderFooterFirstPage).Range.Text

    '### Hauptteil kopieren Anfang ###
    .Documents.Item(("Dokumentenname").Activate()
    .ActiveDocument.Sections.Item(1).Range.Select()
    Call .Selection.MoveEndWhile(cset:=vbCr, Count:=Word.WdConstants.wdBackward)
    .Selection.Copy()
    strVariable = .Selection.Text

    '### kopiere textfeld in fussnote ###
    .Documents.Item("Dokumentenname").Activate()
    .ActiveDocument.StoryRanges.Item(7).Select()
    strText = .Selection.Text

    '### um das ende zu markieren
    Call .ActiveDocument.Bookmarks.Item("\endofdoc").Select()

    '### zum schliessen de dokumentes - ohn zu speichern                              Call
    .Documents.Close(savechanges:=Word.WdSaveOptions.wdDoNotSaveChanges)
  */

}
