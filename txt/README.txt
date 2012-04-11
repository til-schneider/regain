Version 1.7.12 on 2012/Apr/12 PREVIEW
-------------------------------------

NEW
 * File2Http-Bridge: /file/$/$...file.txt?askPermission=1 returns "true"/"false" to indicate whether the file could be downloaded
   (exists in index and is permitted by SearchAccessController) 
 * CrawlerAccessController: Is closed after crawling, if interface "Closeable" is implemented

UPDATE
 * Crawler Thumbnailer in version 0.5 Stable
 * POI Library 3.7 -> 3.8

BUGFIX
 * Regression: search_xml.jsp didn't work as Server Version
 * Xml escaping doesn't give invalid entities like &copy; anymore
 * allowFileAccess: Don't block file access when SearchAccessController is used, but respect its groups.
 * FilesizeFilterPlugin: Do not filter directories.

Version 1.7.11 on 2012/Feb/03 PREVIEW
-------------------------------------

NEW
 * CrawlerPlugin allows dynamic blacklisting (new method checkDynamicBlacklist()
 * Add FilesizeFilterPlugin

UPDATE
 * Crawler Thumbnailer in version 0.3 Stable

CLEANUP
 * Removal of no longer used libraries (bcmail und bcprov)


Version 1.7.10 on 2011/Dec/10 PREVIEW
-------------------------------------

NEW
 * Add XML interface to search results (search_xml.jsp). The URL parameters are identical to search.jsp.
 * All tags now have an optional attribute 'escape' to escape its output (html, xml or none)
 * JarPreparator which indexes the filenames in *.jar, *.war, *.ear archives.
 * ZipPreparator which indexes the filenames in *.zip archives.

BUGFIX
 * DocumentFactory.createDocument(): IO-Stream was never closed
 * IndexUpdateManager.checkUpdate(): IO-Stream was never closed
 * No NullpointerException if AnalyzerType unknown; instead, re-index
 * build.xml: Allow compiling when only JAVA_HOME is set
 * build.xml: Throw message when build.properties not found
 * Regression from 1.7.9: File2Http-Bridge failed to work

Version 1.7.9 on 2011/Aug/16 PREVIEW
-------------------------------------
NEW
 * CONFIGURATION: Namespaces of Desktop Taglib are now in DesktopConfiguration.xml
 * Taglib Classes can be added in web/taglib as namespace.jar (Desktop)

BUGFIX
 * Crawler didn't work (could not access to search.IndexConfig.getLuceneVersion())
 * LastModifiedTag could throw an NullpointerException (if lucene field last-modified is not set)
 * Redirect to noindex.jsp instead of NullpointerException when no index given
 * Fix Links: CrawlerConfiguration.xml Help and Query Syntax Help (errorpage.jsp)
 
Version 1.7.8 on 2011/Jul/30 PREVIEW
-------------------------------------
NEW
 * Capability for Crawler Plugins
 * commons-beanutils 1.8.3: Used for Reflection in CrawlerPluginManager

UPDATE
 * PDFBox to 1.6.0: performance improvements, bugfixes

BUGFIX
 * Catch Null-Pointerexception when OpenOfficePreparator treats a Office 2007-File

Version 1.7.7 on 2011/Jun/03 STABLE
-----------------------------------
Release includes all changes from 1.7.4
to 1.7.6.

Version 1.7.6 on 2011/Apr/28 PREVIEW
-------------------------------------
UPDATE
 * Add icons for docx, xlsx, pptx on the search result.
 * Improve error handling and message for a non-existent conf-dir
 * Lib-Updates: PDFBox, Lucene, JaudioTager
 * Add predefined analyzer definitions for french and italian analyzers.

Version 1.7.5 on 2011/Apr/18 PREVIEW
-------------------------------------
FIX
 * english stopwords example in CrawlerConfiguration.xml
 * english response in FormTag (settings)

Version 1.7.4 on 2011/Apr/11 PREVIEW
-------------------------------------
MIGRATE
 * Use Java 6 only to start regain.
NEW
 * Annotations extraction from PDF documents
 * System Tray will be correctly displayed on 64-bit Systems.

Version 1.7.3 on 2010/Dec/21
----------------------------
Christmas_2010 release. Covers all changes from 1.7.1 and 1.7.2.

Happy searching on XMAS 

Version 1.7.2 on 2010/Nov/14 PREVIEW
-----------------------------------
UPDATE
  * TrayIcon for Linux: call Regain with "java -Djava.library.path=/usr/lib/jni -jar regain.jar"
      The trayicon will be shown correct if you installed libjdic.
      For Ubuntu/Debian: sudo apt-get install libjdic-bin
  * Aperture 1.5.0: Used for mimetype detection
  * jcifs 1.3.14: Samba lib

NEW
  * Example start script for Linux (trayicon).

Version 1.7.1 on 2010/11/07 PREVIEW
-----------------------------------
* UPDATE: Lucene from 2.9 to 3.0.2. Some deprecated regain classes where removed.
* UPDATE: PDFBox from 0.8.0-incubating to 1.2.1
* BUGFIX: Add missing lucene-memory-3.0.2.jar.
* UPDATE: Poi v3.5 to v3.7.
* NEW:    Extraction of meta data for all POI parsed and PDF documents. If there exists a title
          the document title will be set from this value. The metadata will be indexed and stored
          in the new index field 'metadata'. Please add metadata in the SearchConfiguration.xml
          for older installations:
          <searchFieldList>content title metadata headlines location filename</searchFieldList>

Version 1.7.0 on 2010/08/26 STABLE
----------------------------------
* Authentication for http now possible.
* List of all crawled URL will be written at the end of crawling.
* BUGFIX: Added missing Apache Commons libs.
* convert 'Anwenderhandbuch_regain' into current OpenOffice format. Add PDF-export
  of document to the txt section.
* BUGFIX: Add missing libs for launch4j.

Version 1.6.8 on 2009/12/20 PREVIEW
-----------------------------------
* BUGFIX: Path and file name are indexed correct.
* Enhancement: Total count of indexed documents is shown on search result page.

Version 1.6.7 on 2009/12/17 PREVIEW
-----------------------------------
* BUGFIX: Remove unwanted PDFBox-log entries from regain log.

Version 1.6.6 on 2009/12/10 PREVIEW
-----------------------------------
* BUGFIX: Insert missing classes from commons-collections into desktop and server version.

Version 1.6.5 on 2009/12/09 PREVIEW
-----------------------------------
* BUGFIX: Huge memory consumption on large result sets.

Version 1.6.4 on 2009/11/29 PREVIEW
-----------------------------------
* UPDATED: PDFBox to version 0.8.0
* UPDATED: Lucene to version 2.9.1
* Obsolete PoiMsWord, -Excel, -Powerpoint and -VisioPreparator classes are removed.
* New Sorting: relevance, last-modified, size, title, mimetype, path, filename
  The sorting feature has to be configured in the SearchConfiguration.xml.
  Only sorting by relevance is enabled in the delivered default config (same behaviour
  as before on the search results).
* The last-modified date will be displayed on every search hit.
* DEPRECATED: SingleSearchResults, MultipleSearchResults, MergedHits.
  These classes will be removed in one of the next distribution (dependíng on
  the Lucene 3.0 update)
  
Version 1.6.3 on 2009/09/28 PREVIEW
-----------------------------------
* New POI-lib for extraction of Office-XML documents (xlsx, docx,pptx). All POI-
  Extractors are merged to one POITextExtractor (PoiMsOfficePreparator).
* UPDATE: NSIS (installer tool)
* BUGFIX: Replacement of JAR-EXE wrapper (old jsmooth, new launch4j)

Version 1.6.2 on 2009/09/05 STABLE
-----------------------------------
* URLCleaner for removing parts from the URL (e.g.session id)
* regain is build again with Java 1.6 because some of the libs requires 1.6
* BUGFIX: Rtf documents will be indexed correctly (replace wrong mimetype, disable SimpleRTFPreparator)
* <starturls/>, <whitelist/> and <blacklist/> accepts <![CDATA[]]> sections. Usefull for
  URLs which contains an &.
* BUGFIX: Multiple protocols occurs some times when using the setup page of the desktop version.
* BUGFIX: Update of samba lib (jcifs). Handling of smb-URL improved (username, password no longer
  stored in index).
* BUGFIX: 'Cached' text is now internationalized
* Example of French stoppwords in CrawlerConfiguration.xml

Version 1.6.1 on 04/21/2009 STABLE
----------------------------------
* regain compiled with java 1.5

Version 1.6.0 on 03/08/2009 STABLE
----------------------------------
* BUGFIX: Index update at the same day leads to parsing of the document but the documents
  will not be indexed.
* left truncation of query terms *ack finds back, stack and so on

Version 1.5.7 on 24.01.09 PREVIEW
---------------------------------
* Highlighting for wildcard- and fuzzysearches (contribution: A.Larsson)
* BUGFIX: local files couldn't executed after execution of rewriteRules
* authentication.properties will be located in installation root too (usefull for server version 
  which doesn't have a conf directory)

Version 1.5.6 on 22.12.08 PREVIEW
---------------------------------
* EXE-Version once again part of the distribution. There exists an unreproducible bug concerning
  the parsing of a mime message. 
* Mime messages will be fetched only once from the imap server. In the case of wanted reindexing 
  the documents or the complete index have to be dropped.
* Update of windows installer and jsmooth.

Version 1.5.5 on 30.11.08 PREVIEW
---------------------------------
* URL-Authentication for imap(s), http, smb according to the following schema  
  protoc://username:password@host:port/a_path/. The account/password pair are stored together 
  with the url-pattern in a properties-files. Main usage will be the crawling of imap mail stores.
* New input field for imap(s) urls on the config page (desktop search).
* BUGFIX: Whitelist entry for directories/files from file system not more any longer file:///
* MISSING: Windows installer version. The Mailparsing doesn't work inside a wrapped Java program.
  This bug will hopefully be fixed in a later release.
* CONFIGURATION: There exists a new configuration .../my_installation_path/conf/authentication.properties.
  The matching entries of account name/password for a specific host has to be provided before the start
  of the crawler.
  
Version 1.5.4 on 16.11.08 PREVIEW
---------------------------------
* Content storage (for the new preview function from 1.5.2) could be disabled with <storeContentForPreview/> {true,false} in the 
  Crawler configuration. See example config.

Version 1.5.3 on 05.10.08 PREVIEW
---------------------------------
* POI update to version 3.1, changed word- and excel text extraktion (contribution J.Stiepel)
* new Visio preparator (experimental) (contribution J.Stiepel)
* Bugfix: fixed exception handling for file permission problems (contribution J.Stiepel)
* Feature: sub indices. This feature allows the aggregation of single indices into one. (experimental) (contribution filiadata)
* Bugfix: 'encoding preservation' of search term (regain server frontend in application container)
* updatet servlet api

Version 1.5.2 on 15.08.08 PREVIEW
---------------------------------
* The whole extracted content will be stored in the index. This feature allows the creation 
  of a content-view in the result jsp with <search:hit_content/> from the updated Tag-lib. 
  The output is not localised (by now it's only in German).
  ATTENTION: This is a highly experimental feature. The index could grow very fast.

Version 1.5.1 on 07.08.08 
-------------------------
* Filename will be indexed correctly
* Date format of field 'last-modified' changed to "YYYYMMDD". By now range search could 
  be applied to the field. (code contribution by filiadat)
* Improved locale-Handling in SharedTag (code contribution by filiadata)

Version 1.5.0 on 09.07.08 Preview
---------------------------------
* Link extraction with REgexp switched to HTMLParser link extraction
* HTMLParser now complete integrated
* -notrayicon - command line parameter for  desktop-search (TrayIcon will never be shown, code contribution by Stefan Gottlieb)
* Lucene updated to version 2.3.2
* Removing anchors from URLs
* Definition of a default index update interval
* Deletion of temporary files is handled safer
* Improved mime-type detection

Version 1.4.2 on 04.06.08 Preview
---------------------------------
* http://-links which ends with a / will be extracted and indexed

Version 1.4.1 on 2008-04-27 Preview
-----------------------------------
* JavaPreparator for *.java-files. The JavaPreparator is not part
  of the 'standard'-distribution because of his size and the limited 
  user group.


Version 1.4.0 on 05.04.08 Preview
---------------------------------
* Bugfix: StackOverflowError in link extraction catched
* mp3-preparator extracts ID3v2 or ID3v1 tags
* Generic audio preparator, which extract metadata from mp4 (iTunes), 
  ogg-Vorbis and flac


Version 1.3.0/1 on 16.03.08 Preview
-----------------------------------

* smb/CIFS driver 
* new HTML-Parser for better content extraction
* Bugfix mime typ detection
* Priority for preparators
* Highlighting for content and title
* Preparator selektion on basis of mime-types 
* Mimetype-detection (based on file extension and MagicMime)
* Replacement "Extended Search" from extension selection to mimetype selection


Version 1.2.3 on 2007-12-01
---------------------------

* Bugfix: In some cases no file contents were indexed.


Version 1.2.2 on 2007-11-01
---------------------------

* It is now possible to use any Lucene analyzer.
* Bugfix: The attibute beautified of hit_url tag was missing in the TLD
  definition.
* Bugfix: Fixed URL-encoding problems when using the file-to-http-bridge.


Version 1.2.1 on 2007-10-30
---------------------------

* Bugfix: In regain 1.2 some libs were missing. This was fixed with
  version 1.2.1.


Version 1.2 on 2007-07-20
-------------------------

* The search result show now icons indicating the file's type.
* The index fields "size" and "last-modified" are now searchable.
* New preparator: EmptyPreparator (Contributed by Gerhard Olsson). This
  preparator extracts no content files assigned to it. Therefore only the path
  and the filename is written to the index (helpful for all file types having no
  matching preparator).
* The maximum number of terms per document is now configurable using the
  <maxFieldLength> tag in the CrawlerConfiguration.xml. Default is 10000.
* The IfilterPreparator works now under Windows Server 2003, too.
* The values for the <search:input_fieldlist> tag may now be determined when
  indexing. Therefore this operation beeing slow for large indexes must not be
  executed any more when searching the first time. This may be configured
  using the <valuePrefetchFields> tag in the CrawlerConfiguration.xml.
* Several bugfixes


Version 1.1.1 on 2006-03-27
---------------------------

* Bugfixes in the server variant


Version 1.1 on 2006-02-26
-------------------------

* regain now searches the URLs, too.
* The desktop search now shows the last log messages.
* Better handling of HTTP-Redirects. (Thanks to Gerhard Olsson)
* Auxiliary fields have new options: "tokenize", "store" and "index".
* Added documentation of the Tag Library.
* The search mask now accepts multiple "query" parameters
  (they are just concatinated)
* The Jacob preparators have been improved. (Thanks to Reinhard Balling)
* New preparator ExternalPrepartor: This preparator calls an external program
  or script in order to get the text of documents. (Thanks to Paul Ortyl)
* Completed italian localization. (Thanks to Franco Lombardo)
* Some Bugfixes


Version 1.1 Beta 6 on 2005-12-05
--------------------------------

* New preparator: With the PoiMsPowerPointPreparator there is now a platform
  independant preparator for Powerpoint. (Thanks to Gerhard Olsson)
* New preparator: The IfilterPreparator uses the I-Filter interface of
  Microsoft to read various file formats. Unfortunately it only works on Windows.
* Multi index search: In the SearchConfiguration.xml now several indexes may be
  specified as default.
* The auxiliary fields have now a better handling for case sensitivity.
* The HTTP agent sent by the crawler to the web servers may now be configured in
  the CrawlerConfiguration.xml. This way the crawler can identify itself as
  Internet Explorer, for example.
* Several bugfixes


Version 1.1 Beta 5 on 2005-08-13
--------------------------------

* Multi index search: It is now possible to search several search indexes over
  one search mask. The search query is executed on every index and is merged
  afterwards.
* The white and the black list now allow regular expressions, too.
* Search mask: The location of ressources and the configuration is better
  detected now. Therefor regain works properly now, even if Tomcat is running
  as service.
* Search mask: The file-to-http-bridge is may be switched off now.
* Crawler: The crawler needs less memory now when crawling directories.
* Crawler: The crawler now adds failed documents as well to the index. Therefore
  they are not retried the next time the crawler is running. But if the crawler
  is executed with the option "-retryFailedDocs", all failed documents are
  retried.
* The HTML preparator now preparates the extensions .jsp, .php, .php3, .php4
  and .asp as well.
* It's now possible to specify in the CrawlerConfiguration.xml which documents
  should be preparated with a certain preparator.
* Several bugfixes


Version 1.1 Beta 4 on 2005-04-13
--------------------------------

* Access rights management: It is now possible to integrate an access rights
  management, that ensures that a user only sees results for documents he has
  reading rights for.
* Search: The search taglib has now a tag "hit_field", that writes an index
  field value. The tag "hit_summary" was thereby removed.
* Search: If you don't want to read the search config from an XML file or if
  you don't want to write the location of the XML file in the web.xml, you
  may write your own SearchConfigFactory class and create the config on your
  own. The SearchConfigFactory class is specified in the web.xml.
* Server search: The enclosed JSP pages did not work.


Version 1.1 Beta 3 on 2005-03-17
--------------------------------

* Crawler: Bugfix: The PoiMsExcelPreparator did not get on with all number and
  date formats.
* Crawler: The error log is now more detailed (with stacktraces).
* Crawler: Preparators are now encapsulated in own jars. Thus the regain.jar
  only contains what regain itself needs and preparators may be replaced more
  easily. Also other developers may provide preparators that can be mounted
  very easily.
  The configuration of the preparators is still in the CrawlerConfiguration.xml.
  But now not all preparators must be declared. The preparators are executed in
  the same order as they are configured, the unconfigured preparators afterwards.
* Desktop search: The desktop search now runs under Linux, too.
* Search: Bugfix: Files whichs URL contains a double slash (e.g. of network
  drives: //fileserver/bla/blubb) couldn't be loaded.
* Desktop search: Bugfix: At the search results umlauts were presentet wrong.
* Desktop search: On the status page a currently running index update can now be
  paused and an index update can be started.
* Crawler: Bugfix: The HtmlPraeparator did not get on with all files.


Version 1.1 Beta 2 on 2005-03-12
--------------------------------

* Crawler: The crawler now creates periodically so called breakpoint. When doing
  so the current state of the search index is copied into a separate directory.
  If the index update should be cancelled (e.g. if the computer is shut down),
  the crawler will go on from the last breakpoint.
* Desktop search: The status page now shows the timing results.


Version 1.1 Beta 1 on 2005-03-10
--------------------------------

* Desktop search: reagain now provides a desktop search besides the server
  search. The desktop searchs provides many features that makes the use as easy
  as winking:
    - An installer for Windows.
    - Integration in the task bar under Linux and Windows.
    - Configuration over the browser.
    - Status monitoring over the browser.
* Crawler: There is now a preparator for OpenOffice and StarOffice documents.
* All: Updated to the newest versions of the used projects.
* Crawler: Preparators are now configurable by the CrawlerConfiguration.xml.
* Search: The Search is now configured by the SearchConfiguration.xml, not the
  web.xml any more. There is only the path to the SearchConfiguration.xml
  any more.
* Search: The Search now provides URL rewriting. This way you can index
  documents in file://c:/www‑data/intranet/docs and show the documents in the
  browser as http://intranet.murfman.de/docs.
* Crawler: Auxiliary fields: The index may now be extended by auxiliary fields
  that are extracted from a document's URL.
  Example: Assumed you have a directory with a sub directory for every project.
  Then you can generate an auxiliary field with the project name. Doing so you
  get only documents from that directory when searching for
  "Offer project:otto23".
* Search: Advanced search: All values that are in the index for one field may
  now be provided as a combo box on the search page. Particularly together with
  auxiliary fields this is very useful.
* Search: Some browsers load for security reasons no file links from http pages.
  Thus all documents that are in the index are now provided over HTTP. Of corse
  at the desktop search these documents are only accessible from the local host.
* Crawler: The JacobMsWordPreparator now regards styles. Thus it is possible
  to extract headlines that will be weight more when searching.
* Crawler: The JacobMsOfficePreparators are now able to extract the description
  fields (Title, author, etc.)


Version 1.0 on 2004-06-10
-------------------------

* First version
