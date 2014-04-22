// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyEncryptionStrength"
 *   alias="verifyPdfEncryptionStrength"
 *   description="This step verifies the encryption strength of an encrypted <key>pdf</key> document."
 */
public class PdfVerifyEncryptionStrengthStep extends AbstractVerifyPdfStep {
	private int fStrength = -1;

	/**
	 * @param strength
	 * @webtest.parameter required="yes"
	 * description="The numeric value required to match the encryption strength."
	 */
	public void setStrength(final int strength) {
		fStrength = strength;
	}

	public int getStrength() {
		return fStrength;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		paramCheck(fStrength == -1, "Required parameter \"strength\" not set!");
	}

	protected void verifyPdf(final PDFPage pdfPage) 
	{
        final int actualStrength = pdfPage.getEncryptionStrength();
        if (actualStrength != getStrength())
        	throw new StepFailedException("Incorrect document encryption strength found", 
        			getStrength(), actualStrength);
	}
}
