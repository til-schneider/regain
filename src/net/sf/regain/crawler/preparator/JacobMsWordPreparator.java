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
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.RawDocument;

import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import de.filiadata.lucene.spider.generated.msoffice2000.word.*;

/**
 * Präpariert ein Microsoft-Word-Dokument für die Indizierung mit Hilfe der
 * <a href="http://danadler.com/jacob/">Jacob-API</a>, wobei
 * <a href="http://www.bigatti.it/projects/jacobgen/">Jacobgen</a>
 * genutzt wurde, um den Zugriff zu erleichtern.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class JacobMsWordPreparator extends AbstractPreparator {

  /**
   * Die Word-Applikation. Ist <code>null</code>, solange noch kein Dokument
   * bearbeitet wurde.
   */
  Application mWordApplication;


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

      // Überschrift holen
      Section firstSection = doc.getSections().item(1);
      int headerFirstPage = WdHeaderFooterIndex.wdHeaderFooterFirstPage;
      HeaderFooter firstHeader = firstSection.getHeaders().item(headerFirstPage);
      String title = firstHeader.getRange().getText();
      setTitle(title);
      
      // Text holen
      firstSection.getRange().select();
      Selection sel = mWordApplication.getSelection();
      // Alternative (VB): sel.moveEndWhile(?? cset:=vbCr ??, WdConstants.wdBackward);
      // Alternative (VB): Call app.ActiveDocument.Bookmarks.Item("\endofdoc").Select()
      sel.moveEnd();
      sel.copy();
      String content = sel.getText();
      setCleanedContent(content);
      
      // Dokument schließen (ohne Speichern)
      doc.close(new Variant(false));
    }
    catch (ComFailException exc) {
      throw new RegainException("Using COM failed. "
        + "Be sure to use Java 1.3 or older!", exc);
    }
  }


  /**
   * Gibt alle Ressourcen frei, die von diesem Präparator genutzt wurden.
   * <p>
   * Wird ganz am Ende des Crawler-Prozesses aufgerufen, nachdem alle Dokumente
   * bearbeitet wurden.
   */
  public void close() throws RegainException {
    if (mWordApplication != null) {
      try {
        // Word schließen
        mWordApplication.quit();
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
