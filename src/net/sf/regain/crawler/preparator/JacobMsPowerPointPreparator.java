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
 *  $RCSfile: JacobMsPowerPointPreparator.java,v $
 *   $Source: /cvsroot/regain/regain/src/net/sf/regain/crawler/preparator/JacobMsPowerPointPreparator.java,v $
 *     $Date: 2004/07/28 20:26:04 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */
package net.sf.regain.crawler.preparator;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.RawDocument;

import com.jacob.com.*;

import de.filiadata.lucene.spider.generated.msoffice2000.powerpoint.*;

/**
 * Präpariert ein Microsoft-Powerpoint-Dokument für die Indizierung mit Hilfe der
 * <a href="http://danadler.com/jacob/">Jacob-API</a>, wobei
 * <a href="http://www.bigatti.it/projects/jacobgen/">Jacobgen</a>
 * genutzt wurde, um den Zugriff zu erleichtern.
 * <p>
 * Dabei werden die Rohdaten des Dokuments von Formatierungsinformation befreit,
 * es wird der Titel extrahiert.
 *
 * @author Tilman Schneider, STZ-IDA an der FH Karlsruhe
 */
public class JacobMsPowerPointPreparator extends AbstractPreparator {

  /**
   * Die PowerPoint-Applikation. Ist <code>null</code>, solange noch kein Dokument
   * bearbeitet wurde.
   */
  private Application mPowerPointApplication;


  /**
   * Präpariert ein Dokument für die Indizierung.
   *
   * @param rawDocument Das zu präpariernde Dokument.
   *
   * @throws RegainException Wenn die Präparation fehl schlug.
   */
  public void prepare(RawDocument rawDocument) throws RegainException {
    if (mPowerPointApplication == null) {
      // COM-Thread initialisieren
      ComThread.InitSTA();

      // Neue PowerPoint-Applikation erstellen
      mPowerPointApplication = new Application();

      // Applikation öffnen
      // mPowerPointApplication.activate();
      // -> Bei der Nutzung von .activate() lässt sich Powerpoint auf dem
      //    Win2000-Server nicht durch .quit() beenden.

      // Workaround: Man muss das Powerpoint-Fenster erst aktivieren, sonst
      //             schlägt das Öffnen eines Powerpoint-Dokuments fehl.
      //             Wenn man .activate() nutzt, so wird die Oberfläche
      //             sichtbar und Powerpoint beendet sich am Ende trotz
      //             eines .quit() Aufrufs nicht.
      //             Einzig der Aufruf von .setVisible(true) zeigt bei einem
      //             abschließenden .quit() Aufruf Wirkung.
      Dispatch.put(mPowerPointApplication, "Visible", new Variant(true));

      // PowerPoint unsichtbar machen
      // Dispatch.put(mPowerPointApplication, "Visible", new Variant(false));
      // -> Fehlermeldung: "Application.Visible : Invalid request.
      //                    Hiding the application window is not allowed."
      // -> PowerPoint verbietet das Unsichtbarmachen des Fensters!!

      // Workaround: Da unsichtbar machen nicht funktioniert (zumindest bei
      //             PowerPoint 2000), wenigstens das Fenster minimieren, damit
      //             möglichst wenig Zeit mit malen verbracht wird.
      // mPowerPointApplication.setWindowState(PpWindowState.ppWindowMinimized);
      // -> Funktioniert leider auch nicht
    }

    try {
      // Präsentation öffnen
      String fileName = rawDocument.getContentAsFile().getAbsolutePath();
      Presentation pres = mPowerPointApplication.getPresentations().open(fileName);

      // Durch alle Folien gehen und Text extrahieren
      StringBuffer contentBuf = new StringBuffer();
      Slides slides = pres.getSlides();
      int slideCount = slides.getCount();
      for (int slideIdx = 1; slideIdx <= slideCount; slideIdx++) {
        Slide slide = slides.item(new Variant(slideIdx));

        // Durch alle Objekte gehen und Text extrahieren
        Shapes shapes = slide.getShapes();
        int shapeCount = shapes.getCount();
        for (int shapeIdx = 1; shapeIdx <= shapeCount; shapeIdx++) {
          Shape shape = shapes.item(new Variant(shapeIdx));

          extractTextFrom(shape, contentBuf);
        }

        contentBuf.append('\n');
      }

      // Set the content
      setCleanedContent(contentBuf.toString());

      // Präsentation ohne zu speichern schließen
      pres.close();
    }
    catch (ComFailException exc) {
      throw new RegainException("Using COM failed. "
        + "Be sure to use Java 1.3 or older!", exc);
    }
  }


  /**
   * Extrahiert den Text aus einem Powerpoint-Form-Objekt und trägt ihn in den
   * StringBuffer ein.
   *
   * @param shape Das zu durchsuchende Powerpoint-Form-Objekt.
   * @param contentBuf Der Puffer in den der evtl. gefundene Text einzutragen
   *        ist.
   */
  private void extractTextFrom(Shape shape, StringBuffer contentBuf) {
    // Text extrahieren
    int hasTextFrame = shape.getHasTextFrame();
    if (hasTextFrame == -1) {
      String text = shape.getTextFrame().getTextRange().getText();
      if (text != null) {
        text = text.trim();
        if (text.length() != 0) {
          contentBuf.append(text);
          contentBuf.append('\n');
        }
      }
    }

    // Kind-Shapes bearbeiten (bei Gruppierung)
    // TODO: Besseren Weg finden, eine Gruppe zu identifizieren
    String name = shape.getName();
    if (name.startsWith("Group")) {
      GroupShapes group = shape.getGroupItems();
      int childCount = group.getCount();
      for (int childIdx = 1; childIdx <= childCount; childIdx++) {
        Shape child = group.item(new Variant(childIdx));

        extractTextFrom(child, contentBuf);
      }
    }
  }


  /**
   * Gibt alle Ressourcen frei, die von diesem Präparator genutzt wurden.
   * <p>
   * Wird ganz am Ende des Crawler-Prozesses aufgerufen, nachdem alle Dokumente
   * bearbeitet wurden.
   */
  public void close() throws RegainException {
    if (mPowerPointApplication != null) {
      // PowerPoint schließen
      try {
        System.out.println("quitting Powerpoint");
        mPowerPointApplication.quit();
        System.out.println("Powerpoint quitted");
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

}
