[English translation](README.md)

regain <span style="font-size: 50%">your hidden information</span>
==================================================================

regain ist eine Suchmaschine für Desktop- oder Server-Betrieb mit Unterstützung für viele Dateiformate.


Was ist regain?
---------------

<img src="doc/regain_screenshot.jpg" alt="regain screenshot" style="width: 400px; margin-left: 30px; float: right">

regain ist eine Suchmaschine, ähnlich zu Web-Suchmaschinen wie Google, nur mit dem Unterschied, dass Sie damit nicht im Internet, sondern in Ihren eigenen Dateien und Dokumenten suchen können. Mit regain können sie große Datenmengen (mehrere Gigabyte!) in Sekundenbruchteilen durchsuchen.

Dies wird durch einen Suchindex ermöglicht. regain durchsucht ihre Dateien und Webseiten, extrahiert allen Text und erstellt daraus einen intelligenten Suchindex. All das geschieht im Hintergrund, so dass Sie sofort ihre Suchergebnisse erhalten, wenn Sie etwas suchen wollen.

Es gibt zwei Versionen von regain: Die Desktop-Suche und die Server-Suche. Die Desktop-Suche ist für den Einsatz auf einem normalen Desktop-Computer konzipiert und bietet Ihnen eine schnelle Suche für Dokumente oder Intranet-Webseiten. Die Server-Suche können Sie auf einem Webserver installieren. Sie bietet eine Suchfunktion für eine Webseite oder für Intranet-Dateiserver.

regain ist in Java geschrieben und somit auf allen Java-fähigen Plattformen einsetzbar (u.a. Windows, Linux, Mac OS, Solaris). Die Server-Suche arbeitet mit Java Server Pages (JSPs) und einer Taglibrary, die Desktop-Suche bringt ihren eigenen kleinen Webserver mit.

regain ist unter der Open-Source-Lizenz [LGPL](LICENSE.md) (Lesser General Public License) freigegeben. Das heißt, regain darf zeitlich unbegrenzt und kostenlos genutzt werden.


Features
--------

regain bietet eine Reihe von vielfältigen, sehr nützlichen Funktionen, die für eine effektive Suchmaschine wichtig sind.

Detaillierte Information über die einzelnen Features finden Sie in der [Hilfe von regain](http://regain.murfman.de/de:features).


Download
--------

Es gibt zwei Varianten von regain: Die Server-Suche und die Desktop-Suche. Die Desktop-Suche ist für diejenigen gedacht, die regain auf ihrem Arbeitsplatzrechner nutzen wollen oder regain einfach mal ausprobieren möchten. Die Server-Suche ist für Administratoren eines Webservers gedacht, die eine Suche in ihre Webseite oder Intranetseite integrieren wollen.

Mehr Details zu den Unterschieden der beiden Varianten erfahren Sie im [Vergleich der regain-Varianten](http://regain.murfman.de/de:project_info:variant_comparison).

Wenn Sie nicht genau wissen, welcher Download für Sie der beste ist, dann nehmen Sie den obersten (Desktop-Suche für Windows).

Download [Regain Desktop-Suche 2.1.0 für Windows (Installer)](https://github.com/til-schneider/regain/releases/download/2.1.0/regain_v2.1.0_desktop_win.exe)

Download [Regain Desktop-Suche 2.1.0 für Windows (ZIP-Datei)](https://github.com/til-schneider/regain/releases/download/2.1.0/regain_v2.1.0_desktop_win.zip)

Download [Regain Desktop-Suche 2.1.0 für Linux (ZIP-Datei)](https://github.com/til-schneider/regain/releases/download/2.1.0/regain_v2.1.0_desktop_linux.zip)

Download [Regain Server-Suche 2.1.0 für alle Plattformen (ZIP-Datei)](https://github.com/til-schneider/regain/releases/download/2.1.0/regain_v2.1.0_server.zip)


Dokumentation
-------------

### regain-Hilfe

In der regain Hilfe finden Sie Informationen über die Installation, Konfiguration und Benutzung von regain.

[**Zur regain-Hilfe**](http://regain.murfman.de/de:start)


### Javadoc- und Tag-Library-Dokumentation

Die Dokumentation der Java-Klassen und der Tag-Library. Sie ist für alle interessant, die regain weiterentwickeln wollen.

Download [Javadoc- und Tag-Library-Dokumentation für regain 2.1.0](https://github.com/til-schneider/regain/releases/download/2.1.0/regain_v2.1.0_doc.zip)

Sie können die Dokumentation auch online lesen: [Javadoc-Dokumentation zu regain 2.1.0](http://regain.sourceforge.net/doc/v2.1.0-STABLE/javadoc/index.html) bzw. [Tag-Library-Dokumentation zu regain 2.1.0](http://regain.sourceforge.net/doc/v2.1.0-STABLE/tlddoc/index.html).


### Handbuch zu regain 1.1

Bis zur Version 1.1 hatte regain ein Anwenderhandbuch. Bis jetzt ist noch nicht alles vom alten Handbuch in die neue regain-Hilfe übertragen.

Das alte Handbuch können Sie hier herunterladen: [Download Anwenderhandbuch_Regain.pdf](http://regain.sourceforge.net/download/Anwenderhandbuch_Regain.pdf)


### Seminararbeit über Lucene

Im Rahmen meines Studiums an der FH Karlsruhe habe ich eine Seminararbeit über Lucene gehalten. Die Seminararbeit erwähnt regain nur am Rande, zeigt jedoch interne Details von [Jakarta Lucene](http://jakarta.apache.org/lucene) worauf regain aufbaut.

Die Arbeit ist für denjenigen interessant, der ein wenig mehr über die Hintergründe einer Suchmaschine erfahren möchte.

Download: [Seminararbeit_Lucene_Ausarbeitung.pdf](http://regain.sourceforge.net/download/Seminararbeit_Lucene_Ausarbeitung.pdf)

Download: [Seminararbeit_Lucene_Vortrag.pdf](http://regain.sourceforge.net/download/Seminararbeit_Lucene_Vortrag.pdf)
