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

import net.sf.regain.RegainException;
import net.sf.regain.crawler.Crawler;
import net.sf.regain.crawler.CrawlerJob;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.RawDocument;
import net.sf.regain.crawler.document.WriteablePreparator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

/**
 * Abstract Crawler Plugin.
 * Contains empty stub method for each event.
 * 
 * @see CrawlerPlugin
 * @author Benjamin
 */
public abstract class AbstractCrawlerPlugin implements CrawlerPlugin {

	@Override
	public void onStartCrawling(Crawler crawler) { }

	@Override
	public void onFinishCrawling(Crawler crawler) { }

	@Override
	public boolean checkDynamicBlacklist(String url, String sourceUrl, String sourceLinkText) { return false; };
	
	@Override
	public void onAcceptURL(String url, CrawlerJob job) { }

	@Override
	public void onDeclineURL(String url) { }

	@Override
	public void onCreateIndexEntry(Document doc, IndexWriter index) { }

	@Override
	public void onDeleteIndexEntry(Document doc, IndexReader index) { }

	@Override
	public void onBeforePrepare(RawDocument document, WriteablePreparator preparator) { }

	@Override
	public void onAfterPrepare(RawDocument document, WriteablePreparator preparator) { }

	@Override
	public void init(PreparatorConfig config) throws RegainException { }
}
