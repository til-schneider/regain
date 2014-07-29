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
 */
package net.sf.regain.crawler.document;

/**
 * Prepares a document for indexing.
 * Via this interface, the values of the preparator can be changed from the outside.
 * <p>
 * This is done by extracting the raw text from a document. In other words the
 * document is stripped from formating information. Specific text parts like a
 * title or a summary may be extracted as well.
 * <p>
 * The procedure of preparation is the following:
 * <ul>
 *   <li>First {@link #init(PreparatorConfig)} is called.</li>
 *   <li>For each document {@link #accepts(RawDocument)} is called.<br>
 *     If <code>true</code> was returned the actual preparation of the document
 *     is made:
 *     <ul>
 *       <li>{@link #prepare(RawDocument)} is called. The preparator extracts
 *         now all nessesary information.</li>
 *       <li>This information is retrieved by arbitrary calls of
 *         {@link #getCleanedContent()}, {@link #getHeadlines()},
 *         {@link #getPath()}, {@link #getSummary()} and {@link #getTitle()}.</li>
 *       <li>After all information for this document was retrieved,
 *         {@link #cleanUp()} is called. The preparator should release all
 *         information about the current document in order to prepare the
 *         next one.</li>
 *     </ul>
 *   </li>
 *   <li>After all documents have been prepared, {@link #close()} is called.
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public interface WriteablePreparator extends Preparator {

	/**
	 * Adds an additional field to the current document.
	 * <p>
	 * This field will be indexed and stored.
	 *
	 * @param fieldName The name of the field.
	 * @param fieldValue The value of the field.
	 */
	public void addAdditionalField(String fieldName, String fieldValue);

	/**
	 * @param mCleanedMetaData the mCleanedMetaData to set
	 */
	public void setCleanedMetaData(String mCleanedMetaData);

	/**
	 * Setzt von Formatierungsinformation befreiten Inhalt des Dokuments, das
	 * gerade Präpariert wird.
	 *
	 * @param The cleanedContent
	 */
	public void setCleanedContent(String cleanedContent);

	/**
	 * Setzt die Zusammenfassung des Dokuments, das gerade Präpariert wird.
	 *
	 * @param summary Die Zusammenfassung
	 */
	public void setSummary(String summary);

	/**
	 * Setzt die überschriften, in im Dokument, das gerade Präpariert wird,
	 * gefunden wurden.
	 *
	 * @param headlines Die Zusammenfassung
	 */
	public void setHeadlines(String headlines);

	/**
	 * Setzt den Titel des Dokuments, das gerade Präpariert wird.
	 *
	 * @param title Der Titel.
	 */
	public void setTitle(String title);
}
