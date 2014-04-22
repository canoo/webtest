package com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.canoo.webtest.engine.PdfAwarePageCreator;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Unit tests for {@link PdfBoxPDFPage}.
 * @author Marc Guillemot
 * @version $Revision: 102192 $
 */
public class PdfBoxPDFPageTest {

	/**
	 *
	 */
	@Test
	public void getLinks() throws Exception {
		final PdfBoxPDFPage pdfPage = getPdfPage("testDocLinks.pdf");
		final List<PDFBoxPDFLink> links = pdfPage.getLinks();

		final List<PDFBoxPDFLink> expected = new ArrayList<PDFBoxPDFLink>();
		expected.add(new PDFBoxPDFLink("website (external reference)", "http://www.asert.com.au/", 1));
		expected.add(new PDFBoxPDFLink("please jump to a local destination", "#top", 2));
		assertEquals(expected, links);
	}
	
	/**
	 *
	 */
	@Test
	public void getText() throws Exception {
		final PdfBoxPDFPage pdfPage = getPdfPage("testDocBookmarks.pdf");
		final String text = pdfPage.getText("--\n", "\n", "\n--end page--\n", PDFPage.MODE_LINES);

		final String expected = "Heading One \n"
			+ "Subheading \n"
			+ "--end page--\n"
			+ "Heading Two \n"
			+ "--end page--\n";
		assertEquals(expected, text);
	}

	private PdfBoxPDFPage getPdfPage(final String resource) throws IOException {
		final WebClient wc = new WebClient();
		wc.setPageCreator(new PdfAwarePageCreator());
		
		final URL url = getClass().getClassLoader().getResource(resource);
		return wc.getPage(url);
	}

}
