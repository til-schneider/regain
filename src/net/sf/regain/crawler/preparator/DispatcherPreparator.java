/*
 * Created on 23.08.2006
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.sf.regain.crawler.preparator;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.config.PreparatorConfig;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.log4j.Logger;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LongField;

/**
 * @author Florian Schluefter
 * 
 * This Class is a Preparaor for files with wrong content. For instance a *.doc
 * file witch contains rtf data, or a *.xls file that contains xml data.
 * 
 * This Preparator should identify the real content and pass the file to the
 * right preparator.
 * 
 * 
 */
public class DispatcherPreparator extends AbstractPreparator {

	// MS office signature just to know which one it is for documentary reasons:
	// public static final long _signature = 0xE11AB1A1E011CFD0L; //MS Office
	// signature
	public static final int _signature_offset = 0;

	HashMap signatures = new HashMap(); // table with signatures associated with

	// Preparators

	/** The logger for this class */
	private static Logger mLog = Logger.getLogger(DispatcherPreparator.class);

	/**
	 * Creates a new instance of DispatcherPreparator.
	 * 
	 * @throws RegainException
	 *             If creating the preparator failed.
	 */
	public DispatcherPreparator() throws RegainException {
		super(new String[] { "xls", "doc" });
	}

	/**
	 * Liest die Konfiguration des Pr�partors aus.
	 * 
	 * @param config
	 *            Präparator-Konfiguration, d.h. der Abschnitt zum
	 *            DispatcherPreprarator
	 * 
	 * @throws RegainException
	 *             Wenn die Pr�paration fehl schlug.
	 */
  @Override
	public void init(PreparatorConfig config) throws RegainException {
		// Read the different Preparator sections of config
		int secnum = config.getSectionCount();

		String signature; // signature of the file
		String name; // Name of the coresponding Preparator

		for (int i = 0; i < secnum; i++) {
			Map sec_content = config.getSectionContent(i);
			name = config.getSectionName(i);
			int j = 0;
			boolean isThere = sec_content.containsKey("signature" + j);
			while (isThere == true) {
				signature = (String) sec_content.get("signature" + j);
				signatures.put(signature, name);
				j++;
				isThere = sec_content.containsKey("signature" + j);
			}
		}
	}

	/**
	 * Präpariert ein Dokument für die Indizierung.
	 * 
	 * @param rawDocument
	 *            Das zu pr�pariernde Dokument.
	 * 
	 * @throws RegainException
	 *             Wenn die Pr�paration fehl schlug.
	 */
	public void prepare(RawDocument rawDocument) throws RegainException {
		InputStream stream = null;
		String sig;

		// reads the first 512 byte for verifiying the headerblock
		try {
			stream = rawDocument.getContentAsStream();
			byte[] _data = new byte[POIFSConstants.SMALLER_BIG_BLOCK_SIZE];
			int byte_count = IOUtils.readFully(stream, _data);

			// verify if there is enough data
			if (byte_count != POIFSConstants.SMALLER_BIG_BLOCK_SIZE) {
				String type = " byte" + ((byte_count == 1) ? ("") : ("s"));

				throw new IOException("Unable to read entire header; "
						+ byte_count + type + " read; expected "
						+ POIFSConstants.SMALLER_BIG_BLOCK_SIZE + " bytes");
			}

			LongField signature = new LongField(_signature_offset, _data);
			sig = signature.toString();

			String Prep = signatures.get(sig).toString();

			if (Prep != null) {
				// rtf file?
				if (Prep.equals("SwingRtfPreparator")) {
					SwingRtfPreparator rtfprep = new SwingRtfPreparator();	// todo: avoid new instantiation of preparators by singleton 

					rtfprep.prepare(rawDocument);
					this.setCleanedContent(rtfprep.getCleanedContent());
					this.setTitle(rtfprep.getTitle());
					this.setHeadlines(rtfprep.getHeadlines());
					this.setSummary(rtfprep.getSummary());
					this.setPath(rtfprep.getPath());
				}

				// xml file?
				if (Prep.equals("XmlPreparator")) {
					XmlPreparator xmlprep = new XmlPreparator();	// todo: avoid new instantiation of preparators by singleton

					xmlprep.prepare(rawDocument);
					this.setCleanedContent(xmlprep.getCleanedContent());
					this.setTitle(xmlprep.getTitle());
					this.setHeadlines(xmlprep.getHeadlines());
					this.setSummary(xmlprep.getSummary());
					this.setPath(xmlprep.getPath());
				}

				// HTML File?
				if (Prep.equals("HtmlPreparator")) {
					HtmlPreparator htmlprep = new HtmlPreparator();	// todo: avoid new instantiation of preparators by singleton

					htmlprep.prepare(rawDocument);
					this.setCleanedContent(htmlprep.getCleanedContent());
					this.setTitle(htmlprep.getTitle());
					this.setHeadlines(htmlprep.getHeadlines());
					this.setSummary(htmlprep.getSummary());
					this.setPath(htmlprep.getPath());
				}
			} else // other kind of files, e.g. unknown signature 
			{
				PlainTextPreparator plainprep = new PlainTextPreparator();	// todo: avoid new instantiation of preparators by singleton

				plainprep.prepare(rawDocument);
				this.setCleanedContent(plainprep.getCleanedContent());
				this.setTitle(plainprep.getTitle());
				this.setHeadlines(plainprep.getHeadlines());
				this.setSummary(plainprep.getSummary());
				this.setPath(plainprep.getPath());
			}

		}

		catch (IOException exc) {
			throw new RegainException(
					"Reading File failed, unkown File Header: "
							+ rawDocument.getUrl(), exc);
		}

		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception exc) {
				}
			}
		}
	}
}