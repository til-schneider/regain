package net.sf.regain.crawler.preparator;

import java.io.IOException;
import java.io.InputStream;

import net.sf.regain.RegainException;
import net.sf.regain.crawler.document.AbstractPreparator;
import net.sf.regain.crawler.document.RawDocument;

import org.apache.poi.hdgf.extractor.VisioTextExtractor;

public class PoiMsVisioPreparator extends AbstractPreparator {

	/**
	 * Creates a new instance of PoiMsVisioPreparator.
	 * 
	 * @throws RegainException
	 *             If creating the preparator failed.
	 */
	public PoiMsVisioPreparator() throws RegainException {
		super(new String[] { "application/msvisio", "application/vnd.visio" });
	}

	/**
	 * Prepares a document for indexing.
	 * 
	 * @param rawDocument
	 *            The document to prepare.
	 * 
	 * @throws RegainException
	 *             When the document cannot be prepared.
	 */
	public void prepare(RawDocument rawDocument) throws RegainException {
		InputStream stream = null;
		try {
			stream = rawDocument.getContentAsStream();
			VisioTextExtractor extractor = new VisioTextExtractor(stream);
			setCleanedContent(extractor.getText());
		} catch (IOException exc) {
			throw new RegainException("Reading MS Visio document failed: " + rawDocument.getUrl(), exc);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception exc) {
				}
			}
		}
	}

}
