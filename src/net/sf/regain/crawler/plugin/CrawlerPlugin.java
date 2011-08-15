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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package net.sf.regain.crawler.plugin;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import net.sf.regain.crawler.Crawler;
import net.sf.regain.crawler.CrawlerJob;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.document.WriteablePreparator;
import net.sf.regain.crawler.document.Pluggable;
/**
 * All Crawler Plugins need to satisfy this interface.
 * 
 * If you want to implement only some of these method,
 * you can inherit empty stub methods from AbstractCrawlerPlugin.
 * 
 * It is guaranteed that none of the arguments is 'null'.
 * 
 * A typical call order may be:
 * 
 * onStartCrawling
 * 
 * onAcceptURL
 * onAcceptURL
 * onDeclineURL
 * 
 * onBeforePrepare
 * onAfterPrepare
 * onCreateIndexEntry
 * onBeforePrepare
 * onAfterPrepare
 * onCreateIndexEntry
 * ... 
 * onDeleteIndexEntry
 * onDeleteIndexEntry
 * onDeleteIndexEntry
 * ...
 * onFinishCrawling
 * 
 * 
 * WARNING: Don't consider this API stable yet, the method signatures may still change.
 * 
 * @author Benjamin
 */
public interface CrawlerPlugin extends Pluggable {
	
		/**
		 * Called before the crawling process starts (Crawler::run()).
		 * 
		 * This may be called multiple times during the lifetime of a plugin instance,
		 * but CrawlerPlugin::onFinishCrawling() is always called in between.
		 * 
		 * @param crawler 		The crawler instance that is about to begin crawling
		 */
		void onStartCrawling(Crawler crawler);
	
		/**
		 * Called after the crawling process has finished or aborted (because of an exception).
		 * 
		 * This may be called multiple times during the lifetime of a plugin instance.
		 * 
		 * @param crawler 		The crawler instance that is about to finish crawling
		 */
		void onFinishCrawling(Crawler crawler);
	
		/**
		 * Called during the crawling process when a new URL is added to the processing Queue.
		 * 
		 * As the queue is filled recursively, these calls can come between prepare Calls.
		 * 
		 * @param url			URL that just was accepted
		 * @param job			CrawlerJob that was created as a consequence
		 */
		void onAcceptURL(String url, CrawlerJob job);
		
		/**
		 * Called during the crawling process when a new URL is declined to be added to the processing Queue.
		 * 
		 * Note that ignored URLs (that is, URL that were already accepted or declined before), do not appear here.
		 * 
		 * @param url			URL that just was declined
		 */
		void onDeclineURL(String url);
	
		/**
		 * Called when a document as added to the index.
		 * This may be a newly indexed document, or a document that has changed since
		 * and, thus, is reindexed.
		 *  
		 * @param doc			Document to write
		 * @param index			Lucene Index Writer
		 */
		void onCreateIndexEntry(Document doc, IndexWriter index);
		
		/**
		 * Called when a document is deleted from the index.
		 * Note that when being replaced by another document ("update index"),
		 * the old document is added to index first, deleting is part of the cleaning-up-at-the-end-Phase.
		 * 
		 * @param doc			Document to read
		 * @param index			Luce Index Reader
		 */
		void onDeleteIndexEntry(Document doc, IndexReader index);
	
		/**
		 * Called before a document is being prepared to be added to the index.
		 * (Good point to fill in default values.)
		 * 
		 * @param document		Regain document that will be analysed
		 * @param preparator	Preperator that was chosen to analyse this document
		 */
		void onBeforePrepare(RawDocument document, WriteablePreparator preparator);
	
		/**
		 * Called after a document is being prepared to be added to the index.
		 * Here you can override the results of the preperator, if necessary.
		 * 
		 * @param document		Regain document that was analysed
		 * @param preparator	Preperator that has analysed this document
		 */
		void onAfterPrepare(RawDocument document, WriteablePreparator preparator);
}
