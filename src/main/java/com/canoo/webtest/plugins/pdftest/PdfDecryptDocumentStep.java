// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFInvalidPasswordException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Etienne Studer
 * @webtest.step
 *   category="PDF"
 *   name="pdfDecryptDocument"
 *   alias="decryptPdfDocument"
 *   description="This step decrypts the current <key>pdf</key> document. 
 *   It fails if the <key>pdf</key> document is not encrypted or if the password is wrong. 
 *   After decrypting the <key>pdf</key> document, it will be available in its decrypted state for later <key>pdf</key> test steps."
 */
public class PdfDecryptDocumentStep extends AbstractVerifyPdfStep {
	private String fPassword;

	/**
	 * @param password
	 * @webtest.parameter required="yes"
	 * description="The text required to decrypt the PDF document."
	 */
	public void setPassword(final String password) {
		fPassword = password;
	}

	public String getPassword() {
		return fPassword;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getPassword(), "password");
	}

	protected void verifyPdf(final PDFPage pdfPage) {
		if (pdfPage.isEncrypted())
		{
			try
			{
				pdfPage.decrypt(getPassword());
			}
			catch (final PDFInvalidPasswordException e)
			{
				throw new StepFailedException("Invalid password", this);
			}
			catch (final Throwable e)
			{
				throw new StepFailedException("Invalid password", this);
			}
		}
		else
			throw new StepFailedException("Document is not encrypted!");
	}
}
