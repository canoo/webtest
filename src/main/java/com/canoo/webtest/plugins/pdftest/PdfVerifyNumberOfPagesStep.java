// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyPageCount"
 *   alias="verifyPdfPageCount"
 *   description="This step verifies the number of fields in the <key>pdf</key> document."
 */
public class PdfVerifyNumberOfPagesStep extends AbstractVerifyPdfStep {
	private int fCount = -1;

	/**
	 * @param count
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The expected number of pages."
	 */
	public void setCount(final int count) {
		fCount = count;
	}

	public int getCount() {
		return fCount;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		paramCheck(getCount() == -1, "Required parameter \"count\" not set!");
	}
	
	protected void verifyPdf(final PDFPage pdfPage) {
		final int actualCount = pdfPage.getNumberOfPages();
		if (getCount() != actualCount)
			throw new StepFailedException("Wrong number of pages", getCount(), actualCount);
	}
}
