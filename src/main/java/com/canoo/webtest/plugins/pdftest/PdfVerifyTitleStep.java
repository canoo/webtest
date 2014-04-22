// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyTitle"
 *   alias="verifyPdfTitle"
 *   description="This step verifies the title of the <key>pdf</key> document."
 */
public class PdfVerifyTitleStep extends AbstractVerifyPdfStep {
	private boolean fRegex;
	private String fTitle;

	public PdfVerifyTitleStep() {
		fTitle = null;
	}

	/**
	 * @param regex
	 * @webtest.parameter
	 *   required="no"
	 *   default="'false'"
	 *   description="Specifies whether the text represents a <key>regex</key>."
	 */
	public void setRegex(final boolean regex) {
		fRegex = regex;
	}

	public boolean getRegex() {
		return fRegex;
	}

	/**
	 * @param title
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The text/<key>regex</key> required to match the title."
	 */
	public void setTitle(final String title) {
		fTitle = title;
	}

	public String getTitle() {
		return fTitle;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getTitle(), "title");
	}

	protected void verifyPdf(final PDFPage pdfPage) {
		final String actualTitle = pdfPage.getDocumentTitle();
		if (!getVerifier(getRegex()).verifyStrings(getTitle(), actualTitle)) {
			throw new StepFailedException("Wrong document title", getTitle(), actualTitle);
		}
	}
}