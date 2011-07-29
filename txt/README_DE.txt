Version 1.7.8 am XX.XX.2011 PREVIEW
-----------------------------------
NEU 
 * Crawler-Plugin-Infrastruktur
 * commons-beanutils 1.8.3: Für die Reflection-API von CrawlerPluginManager

UPDATE
 * PDFBox auf 1.6.0: Performanceverbesserung, Bugfixes

BUGFIX
 * OpenOfficePreparator: Hatte eine Nullpointer-Exceptions geworfen bei Office 2007-Dateien

Version 1.7.7 am 03.06.2011 STABLE
-----------------------------------
Enthält alle Änderungen von 1.7.4-1.7.6

Version 1.7.6 am 28.04.2011 PREVIEW
-----------------------------------
UPDATE:
 * Icons für docx, pptx, xlsx hinzugefügt
 * Fehlermeldung bei falschem Config-Dir verbessert (Contrib. benjamin » 26.04.2011, 08:45)
 * Bibliotheks-Updates: PDFBox, Lucene, JaudioTager
 * Definition von Analysern für französisch und italiänisch hinzugefügt.

Version 1.7.5 am 18.04.2011 PREVIEW
-----------------------------------
FIX:
 * Beispiel für englische Stopwords
 * englische Ausgabe der Meldungen im FormTag (Einstellungen)


Version 1.7.4 am 11.04.2011 PREVIEW
-----------------------------------
Migration
 * Java 6 wird zwingend benötigt.
NEU
 * Annotations werden aus PDF Dokumenten ausgelesen.
 * TrayIcon wird auch auf 64-bit Systemen dargestellt.

Version 1.7.3 am 21.12.2010
---------------------------
Weihnachtsversion 2010. Beinhaltet alle Änderungen seit 1.7.0.

Frohes Suchen zu Weihnachten

Version 1.7.2 am 14.11.2010 PREVIEW
-----------------------------------
UPDATE
  * TrayIcon für Linux: Regain mit "java -Djava.library.path=/usr/lib/jni -jar regain.jar" aufrufen.
      Dann wird das TrayIcon korrekt angezeigt, wenn man libjdic-bin für sein System installiert hat.
      Unter Ubuntu/Debian z.B.: sudo apt-get install libjdic-bin 
  * Aperture 1.5.0: Benutzt für Mimetype-Detection
  * jcifs 1.3.14: Samba Bibliothek

NEU
  * Beispielscript für Linux (Desktopversion) zum korrekten Aufruf in Zusammenhang mit der
    Darstellung des TrayIcons (nur 32bit).

Version 1.7.1 am 07.11.2001 PREVIEW
-----------------------------------
* Lucene wurde auf Version 3.0.2 geupdated. Damit in Zusammenhang stehende deprecated
  Klasse wurden entfernt.
* UPDATE: PDFBox wurde von 0.8.0-incubating auf 1.2.1 geupdated.
* BUGFIX: Fehlendes lucene-memory-3.0.2.jar hinzugefügt.
* UPDATE: POI von Version 3.5 auf Version 3.7
* NEU:    Extraktion von Metadaten aus allen mit POI geparsten und PDF Dokumenten. Wenn ein
          Titel in den Metadaten gefunden wird, wird dieser zum Dokumenttitel.
          Extrahierte Metadaten werden im Index im Feld 'metadata' indiziert und
          gespeichert. Man kann also in der Suche mit metadata:ein_suchbegriff gezielt
          suchen. Desweiteren sollte man in der SearchConfiguration.xml metadata hinzufügen:
          <searchFieldList>content title metadata headlines location filename</searchFieldList>

Version 1.7.0 am 06.03.2010 STABLE
-----------------------------------
* Authentifizierung für http nun möglich
* Liste aller gecrawlten URLs wird am Ende des Crawlvorganges geschrieben.
* BUGFIX: Fehlende Apache Commons Bibliotheken hinzugefügt.
* Anwenderhandbuch in aktuelles OpenOfficeformat konvertiert. Zusätzlich liegt im
  Ordner txt ein pdf-Version des Handbuchs.
* BUGFIX: Fehlende *.o Libs für launch4j hinzugefügt.

Version 1.6.8 am 20.12.2009 PREVIEW
-----------------------------------
* BUGFIX: Pfad und Dateiname sind wieder korrekt indiziert.
* Ausgabe der Anzahl der indizierten Dokumente auf der Suchergebnisseite.

Version 1.6.7 am 17.12.2009 PREVIEW
-----------------------------------
* BUGFIX: PDFBox-Logeinträge aus dem regain Log entfernt.

Version 1.6.6 am 10.12.2009 PREVIEW
-----------------------------------
* BUGFIX: Fehlende commons-collections-xxx Klasses im Buildprozess berücksichtigt.

Version 1.6.5 am 09.12.2009 PREVIEW
-----------------------------------
* BUGFIX: Hoher Memorybedarf bei grossen Ergebnissets.

Version 1.6.4 am 29.11.2009 PREVIEW
-----------------------------------
* PDFBox wurde auf Version 0.8.0 geupdated.
* Lucene wurde auf Version 2.9.1 geupdated.
* Obsolete PoiMsWord, -Excel, -Powerpoint und -VisioPreparator wurde entfernt. Diese
  Klassen können über das Tag 'release-1.6.3' im Repo gefunden werden.
* Neue Sortiermöglichkeiten: Relevanz, Datum (last-modified), Größe, Titel, Mimetype, Pfad, Dateiname
  Die Sortierung wird in der SearchConfiguration.xml konfiguriert.
  In der mitgelieferten SearchConfiguration.xml ist nur die Relevanz
  (also bisheriges Verhalten) eingeschaltet.
* Last-Modified wird am Suchtreffer ausgegeben
* DEPRECATED: SingleSearchResults, MultipleSearchResults, MergedHits.
  Diese Klassen werden mit der Umstellung auf Lucene 3.0 entfernt.

Version 1.6.3 am 28.09.09 PREVIEW
----------------------------------
* POI-Extraktion für Officedokumente auf neueste Beta umgestellt. Damit lassen
  sich jetzt auch die neuen XML-Dokumente (xlsx, docx, pptx) indizieren.
* UPDATE: NSIS (installer tool)
* BUGFIX: Austausch des JAR-EXE wrapper (alt jsmooth, neu launch4j)

Version 1.6.2 am 05.09.2009 STABLE
----------------------------------
* UrlCleaner um störende Teile aus einer Url zu entfernen (z.B. sessionID)
* wieder mit Java 1.6 gebaut, da einige Bibliotheken Java 1.6 voraussetzen.
* Bugfix: RTF wird wieder indexiert. Dazu wurde der SimpleRTFPreparator in der Konfig entfernt.
* CDATA funktioniert in <starturls/>, <whitelist/> und <blacklist/>. Notwendig um eine URL mit
  einem & in der Query eintragen zu können.
* BUGFIX: Mehrfache Protokollangaben bei Benutzung der Einstellungseite Desktopsuche.
* BUGFIX: Samba-Bibliothek ausgetauscht. smb-URL Handling nach Ersetzung von Domain, User und PW
  geändert.
* BUGFIX: Link-Text für 'Cached Version' ist internationalisiert
* Französische Stoppwörter für die CrawlerConfiguration.xml

Version 1.6.1 am 21.04.2009 STABLE
----------------------------------
* regain wurde mit Java 1.5 übersetzt.

Version 1.6.0 am 08.03.2009 STABLE
----------------------------------
* BUGFIX: Alle Dokumente werden geparsed aber nicht indexiert,
  wenn man am selben Tag den Index updatet.
* Linkstrunkierung für Suchanfragen: z.B. *er hier, Wert, integriert, aber usw.
  Achtung: es wird immer auf die gestemmte Form gematched.

Version 1.5.7 am 24.01.09 (PREVIEW)
-----------------------------------
* Highlighting auch für Wildcard- und Fuzzysuchen (Contribution A.Larsson)
* BUGFIX: Aufrufen von lokalen Dateien mittels Browser nach Anwendung von <rewriteRules/>
* authentication.properties werden auch im Installationsroot gesucht (wichtig für Server-version)

Version 1.5.6 am 22.12.08 PREVIEW
---------------------------------
* EXE-Version nun wieder enthalten. Es gab ein merkwürdiges Verhalten bzgl. des Mailparsens. Fehler
  ließ sich nicht mehr reproduzieren. Dies trat auch nur bei der EXE-Version auf.
* E-Mails werden nur einmal vom Server gelesen und verarbeitet. Wenn man sein Postfach neu indexieren
  will, muss man die Dokumente aus dem Index löschen (am einfachsten den ganzen Index löschen)
* Auf neueste Versionen des Windows-Installers und JSmooth geupdated.

Version 1.5.5 am 30.11.08 PREVIEW
---------------------------------
* URL-Authentication für imap(s), http, smb in der Form protok://username:password@host:port/a_path/.
  Die Einträge von Name/Password für das URL-Pattern erfolgen mittels einer properties-Datei. Haupt-
  einsatzzweck wird das Crawlen von IMAP-Postfächern sein.
* Neues Eingabefeld für imap(s) Url auf der Konfigurationsseite der Desktopsuche.
* BUGFIX: Whitelist-Eintrag für Verzeichnisse/Datein von Festplatte nicht mehr nur file:/// sondern
  der ganze Pfad
* ACHTUNG: Keine Installerversion für Windows. Hier funktioniert das Parsen von MimeMessages nicht,
  Grund derzeit noch unbekannt. 
* ACHTUNG: Für das Parsen von Mailfächern (imap(s)) muss unbedingt Nutzername und Passwort unter 
  ...mein_Installationspfad/conf/authentication.properties nach Anleitung eingetragen werden. Sonst gibt
  es eine AuthenticationException.

Version 1.5.4 am 16.11.08 PREVIEW
---------------------------------
* Per Konfigurationseintrag in der CrawlerConfig.xml <storeContentForPreview/> {true,false}
  kann man das Ablegen des gesamten Contents im Index ein-/ausschalten.

Version 1.5.3 am 05.10.08 PREVIEW
---------------------------------
* POI von Version 3.0.1 auf 3.1, Contribution J.Stiepel
* MS-Visio Preparator (experimentell), Contribution J.Stiepel
* einige Bugfixe betreffs Schreibrechten und Fehlerbehandlung, Contribution J.Stiepel
* Verschachtelte Indexe für die Suche, Contribution filiadata
* Achtung Fehlerbehaftet: verschachtelte Indices !!!
* Bugfix: Umlaute werden nicht durch Suchmaske 'zerstört'
* neue Servletversion

Version 1.5.2 am 15.08.08 PREVIEW
---------------------------------
* Per Default wird der gesamte Content im Index gespeichert. Mittels <search:hit_content/> aus der
  Taglib kann man eine ausklappbare Contentansicht in die Ergebnisliste einbauen. 
  ACHTUNG: Experimentelles Feature. Die Indexe können sehr groß werden.

Version 1.5.1 am 07.08.08 
-------------------------
* Dateiname wird korrekt indiziert
* Datumsformat 'last-modified' auf "YYYYMMDD" geändert. Damit kann man die RangeSearch auf
  das Feld anwenden (Code-Contribution)
* locale-Handling im SharedTag verbessert (Code-Contribution)

Version 1.5.0 am 10.07.08 Preview
---------------------------------
* Linkextraktion von Regexp auf HTMLParser umgestellt
* HTMLParser jetzt komplett integriert
* -notrayicon - Kommandozeilenparameter für Desktopsuche (TrayIcon wird nicht angezeigt)
* Lucene auf 2.3.2 geupdated
* Anchors werden von URLs entfernt (http://forum.murfman.de/index.html#bottom --> http://forum.murfman.de/index.html)
* Definition eines default Indexupdate-Intervalles
* Änderung Löschvorgang temporäre Dateien
* Verbesserte Mimetype-Detection (Update Aperture, URL-Bestimmung)

Version 1.4.2 am 04.06.08 Preview
---------------------------------
* http://-Links, welche mit einem / enden, können extrahiert und indiziert werden

Version 1.4.1 am 27.04.08 Preview
---------------------------------
* JavaPreparator fuer *.java-Dateien. Der JavaPreparator ist aufgrund seiner 
  Groesse und dem begrenzten Nutzerkreis nicht Teil der Standardistribution.


Version 1.4.0 am 05.04.08 Preview
---------------------------------
* Bugfix: StackOverflowError in Linkextraction abgefangen
* mp3-Preparator extrahiert von ID3v2 oder ID3v1 Tags
* Generischer Audio Preparator, welcher von mp4 (iTunes), ogg-Vorbis oder 
  flac die Metadaten extrahiert


Version 1.3.0/1 am 16.03.08
---------------------------

* Zugriff auf Windows-Shares (ohne Authentifizierung) (Samba/CIFS)
* HTML-Parser zum Extrahieren des Contents
* Bugfix Mimetypdetection, wenn das File keine Dateinendung besitzt
* Priorität für Preparatoren. Default Prio ist 0. Damit lässt sich mittels
  URLRegEx .* in einem beliebigen Preparator (vorzugsweise EmptyPreparator)
  und priority=-1 ein CatchAll-Preparator konfigurieren
* Highlighting für Content und Titel
* Preparatorselektion anhand des Mimetypes (früher Dateiendung)
* Mimetype-detection (Dateiendung und MagicMime) 
* Umstellung Erweiterte Suche auf Auswahl Mimetype anstelle Dateiendung


Version 1.2.3 am 01.12.07
-------------------------

* Bugfix: In manchen F�llen wurden keine Dateiinhalte indexiert.


Version 1.2.2 am 01.11.07
-------------------------

* Es kann nun jeder beliebige Lucene-Analyzer verwendet werden.
* Bugfix: In der TLD-Definition hat das Attribut beautified im hit_url-Tag gefehlt.
* Bugfix: Im Zusammenhang mit der File-to-Http-Bridge gab es URL-encoding-Probleme


Version 1.2.1 am 30.10.07
-------------------------

* Bugfix: In regain 1.2 haben ein paar Bibliotheken gefehlt. Dies wurde mit
  Version 1.2.1 behoben.


Version 1.2 am 20.10.07
-----------------------

* In den Suchergebnissen werden nun Icons gezeigt, die den Typ einer Datei
  kennzeichnen. 
* Die Index-Felder "size" und "last-modified" sind nun suchbar.
* Neuer Präparator: EmptyPreparator (Beigesteuert von Gerhard Olsson). Dieser
  Präparator extrahiert keinen Inhalt aus den ihm zugeordneten Dateien. Dadurch
  landet im Index nur der Pfad und Dateiname (hilfreich für alle Dateitypen, für
  die es keinen Präparator gibt).
* Die maximale Anzahl von Termen pro Dokument ist nun einstellbar und zwar über
  das <maxFieldLength>-Tag in der CrawlerConfiguration.xml. Default ist 10000.
* Der IfilterPreparator funktioniert jetzt auch unter Windows Server 2003.
* Die Werte für das <search:input_fieldlist>-Tag können nun beim Indexieren
  ermittelt werden. Dadurch muss diese bei gro�en Indexen langsame Operation
  nicht mehr bei der ersten Suche gemacht werden. Dies kann über das
  <valuePrefetchFields>-Tag in der CrawlerConfiguration.xml eingestellt werden.
* Mehrere Bugfixes


Version 1.1.1 am 27.03.06
-------------------------

* Bugfixes in der Server-Variante


Version 1.1 am 26.02.06
-----------------------

* regain sucht nun auch in den URLs.
* Die Desktop-Suche zeigt nun die letzten Log-Meldungen.
* Bessere Behandlung von HTTP-Redirects. (Danke an Gerhard Olsson)
* Zusatzfelder haben nun die Optionen "tokenize", "store" und "index".
* Die Tag Library ist nun dokumentiert.
* Die Suchmaske akzeptiert nun mehrere "query" Parameter
  (diese werden einfach aneinandergeh�ngt)
* Die Jacob-Präparatoren wurden verbessert. (Danke an Reinhard Balling)
* Neuer Präparator ExternalPrepartor: Dieser Präparator ruft externe Programme
  oder Skripte auf, um den Text aus Dokumenten zu extrahieren.
  (Danke an Paul Ortyl)
* Italienische Lokalisierung fertiggestellt. (Danke an Franco Lombardo)
* Ein paar Bugfixes


Version 1.1 Beta 6 am 05.12.05
------------------------------

* Neuer Präparator: Mit dem PoiMsPowerPointPreparator steht nun auch ein
  plattformunabhängiger Präparator für Powerpoint zur Verfügung.
  (Vielen Dank an Gerhard Olsson)
* Neuer Präparator: Der IfilterPreparator nutzt die I-Filter-Schnittstelle von
  Microsoft um etliche Dateiformate zu lesen. Leider läuft er nur unter Windows.
* Multiindexsuche: In der SearchConfiguration.xml können nun auch mehrere
  Indizes als Default angegeben werden.
* Die Zusatzfelder (Auxiliary Fields) können nun besser mit Groß-/Kleinschreibung
  umgehen.
* Der vom Crawler an die Webserver gesendete HTTP-Agent ist nun in der
  CrawlerConfiguration.xml konfigurierbar. So kann sich der Crawler
  beispielsweise als Internet Explorer ausgeben.
* Mehrere Bugfixes


Version 1.1 Beta 5 am 13.08.05
------------------------------

* Multiindex-Suche: Es können nun mehrere Suchindizes über eine Suchmaske
  durchsucht werden. Die Suchanfragen werden dabei auf jeden Index losgelassen
  und anschlie�end vereint.
* In der Weißen und der Schwarzen Liste können jetzt auch regul�re Ausdr�cke
  angegeben werden.
* Suchmaske: Der Ort der Ressourcen und der Konfiguration wird jetzt besser
  erkannt, so dass regain auch dann korrekt funktioniert, wenn Tomcat als
  Service läuft.
* Suchmaske: Die File-zu-Http-Br�cke ist nun abschaltbar.
* Crawler: Der Crawler braucht nun beim Durchsuchen von Verzeichnissen weniger
  Arbeitsspeicher
* Crawler: Der Crawler nimmt nun auch fehlgeschlagene Dokumente in den Index
  auf, so dass diese bei einem erneuten Durchlauf nicht noch einmal probiert
  werden. Wird der Crawler jedoch mit der Option "-retryFailedDocs" aufgerufen,
  werden alle fehlgeschlagenen erneut probiert.
* Der Html-Präparator übernimmt nun auch die Endungen .jsp, .php, .php3, .php4
  und .asp.
* In der CrawlerConfiguration.xml kann nun bei einem Präparator angegeben
  werden, welche Dokumente er pr�parieren soll.
* Mehrere Bugfixes


Version 1.1 Beta 4 am 13.04.05
------------------------------

* Zugriffsrechte-Management: Es kann nun ein Rechte-Management eingebunden
  werden, das dafuer sorgt, dass ein Benutzer nur Treffer für Dokumente erhaelt,
  für die er Leserechte hat.
* Suche: Die search-Taglib hat nun ein Tag "hit_field", das ein beliebiges
  Indexfeld ausgibt. Das Tag "hit_summary" wurde in diesem Zuge entfernt.
* Suche: Wenn Sie die Konfiguration der Suche nicht von einer XML-Datei laden
  wollen oder wenn Sie die Lage der XML-Datei nicht in der web.xml angeben
  moechten, koennen Sie die Konfiguration nun ueber eine eigene Factory-Klasse
  erzeugen. Die SearchConfigFactory-Klasse wird in der web.xml festgelegt.
* Serversuche: Die beigelegten JSP-Seiten haben nicht funktioniert.


Version 1.1 Beta 3 am 17.03.05
------------------------------

* Crawler: Bugfix: Der PoiMsExcelPraeparator kam nicht mit allen Zahlen- und
  Datumsformaten klar.
* Crawler: Das Fehler-Log im Index ist nun ausfuehrlicher (Mit Stacktrace).
* Crawler: Die Praeparatoren sind nun in eigene Jars gekapselt. Dadurch ist im
  regain.jar nur das, was regain selbst braucht und die Praeparatoren lassen
  sich leichter austauschen. Ausserdem koennen nun andere Entwickler auch
  Praeparatoren anbieten, die sehr einfach eingebunden werden koennen.
  Die Konfiguration der Praeparatoren steckt weiterhin in der
  CrawlerConfiguration.xml, allerdings muessen dort nicht mehr alle
  Praeparatoren angegeben werden. Die Praeparatoren werden in der Reihenfolge
  abgearbeitet, in der sie konfiguriert sind, die nicht konfigurierten
  Praeparatoren in unbestimmter Reihenfolge danach.
* Desktopsuche: Die Desktopsuche lauft nun auch unter Linux.
* Suche: Bugfix: Dateien, deren URL ein doppelter Slash enthielt
  (Z.B. bei Netzlaufwerken: //fileserver/bla/blubb) konnten nicht geladen werden.
* Desktopsuche: Bugfix: Bei den Suchergebnissen wurden Umlaute falsch
  dargestellt.
* Desktopsuche: In der Statusseite kann nun eine laufende Indexierung angehalten
  und eine Indexierung manuell gestartet werden.
* Crawler: Bugfix: Der HtmlPraeparator kam nicht mit allen Dateien klar.


Version 1.1 Beta 2 am 12.03.05
------------------------------

* Crawler: Der Crawler erstellt nun regelm��ig sog. Breakpoints. Dabei wird
  der aktuellen Stand der Suchindex in ein gesondertes Verzeichnis kopiert.
  Falls die Indexierung abgebrochen wurde (Z.B. weil der Rechner
  heruntergefahren wurde), wird beim naechsten mal auf dem letzten Breakpoint
  aufgesetzt.
* Desktopsuche: Die Statusseite zeigt nun auch die Zeitmessungsergebnisse.


Version 1.1 Beta 1 am 10.03.05 
------------------------------

* Desktopsuche: regain bietet nun neben der Server-Suche auch eine Desktop-Suche.
  Die Desktop-Suche bietet viele Eigenschaften, die die Bedienung kinderleicht
  machen:
    - Ein Installer fuer Windows.
    - Integration in die Taskleiste unter Linux und Windows.
    - Konfiguration ueber den Browser.
    - Status-Anzeige ueber den Browser.
* Crawler: Es gibt nun einen Praeparator fuer OpenOffice- und StarOffice-Dokumente.
* Gesamt: Aktualisierung auf die neusten Versionen der genutzten Projekte.
* Crawler: Praeparatoren sind nun ueber die CrawlerConfiguration.xml
  konfigurierbar.
* Suche: Die Suche wird nun ueber die SearchConfiguration.xml
  konfiguriert, nicht mehr ueber die web.xml. Dort steht nun nur noch der Pfad
  zur SearchConfiguration.xml.
* Suche: In der Suche kann nun URL-Rewriting eingesetzt werden. Dadurch
  koennen Dokumente von file://c:/www-data/intranet/docs indiziert und im
  Browser als http://intranet.murfman.de/docs angezeigt werden.
* Crawler: Zusatzfelder: Der Index kann durch Zusatzfelder erweitert werden, die
  aus der URL eines Dokuments generiert werden.
  Beispiel: Angenommen Sie haben ein Verzeichnis mit einem Unterverzeichnis fuer
  jedes Projekt. Dann koennten Sie daraus ein Feld mit dem Projektnamen
  generieren. Dadurch bekommen Sie bei der Suche nach "Angebotsproject:otto23"
  nur Treffer aus diesem Verzeichnis.
* Suche: Expertensuche: Die Werte, die fuer ein Feld im Index stehen,
  koennen nun als ComboBox auf einer Suchseite angeboten werden. Vor allem in
  Verbindung mit Zusatzfeldern ist das sehr praktisch.
* Suche: Weil manche Browser aus Sicherheitsgruenden keine file-Links
  verfolgen, die auf http-Seiten stehen, sind nun alle Dokumente, die sich im
  Index befinden auch ueber HTTP erreichbar. Selbstverstaendlich sind sie bei der
  Desktopsuche nur vom lokalen Rechner aus abrufbar.
* Crawler: Der JacobMsWordPraeparator beruecksichtigt nun Formatvorlagen.
  Dadurch koennen Ueberschriften extrahiert werden, die dann bei der Suche
  staerker gewichtet werden.
* Crawler: Die JacobMsOfficePraeparatoren koennen nun die Beschreibungsfelder
  von MS Office-Dokumenten extrahieren (Titel, Autor, usw.)


Version 1.0 am 10.06.04
-----------------------

* Erste Version

