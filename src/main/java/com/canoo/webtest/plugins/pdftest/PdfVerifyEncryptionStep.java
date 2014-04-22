// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyEncryption"
 *   alias="verifyPdfEncryption"
 *   description="This step verifies whether a <key>pdf</key> document is encrypted 
 *   and whether the user password and owner password are correct. 
 *   This step should only be executed before (and not after) decrypting an encrypted <key>pdf</key> document."
 */
public class PdfVerifyEncryptionStep extends AbstractVerifyPdfStep {
	private boolean fEncrypted = true;
	private String fUserPassword;
	private String fOwnerPassword;

	/**
	 * @param encrypted
	 * @webtest.parameter
	 *	required="no"
	 *	description="Specifies whether the PDF document is expected to be encrypted or not.
     * 	default="'true'"
	 */
	public void setEncrypted(final boolean encrypted) {
		fEncrypted = encrypted;
	}

	public boolean getEncrypted() {
		return fEncrypted;
	}

	/**
	 * @param userPassword
	 * @webtest.parameter
	 *   required="no"
	 *   description="The text required to decrypt the PDF document using the user password."
	 */
	public void setUserPassword(final String userPassword) {
		fUserPassword = userPassword;
	}

	public String getUserPassword() {
		return fUserPassword;
	}

	/**
	 * @param ownerPassword
	 * @webtest.parameter
	 *   required="no"
	 *   description="The text required to decrypt the PDF document using the owner password."
	 */
	public void setOwnerPassword(final String ownerPassword) {
		fOwnerPassword = ownerPassword;
	}

	public String getOwnerPassword() {
		return fOwnerPassword;
	}

	protected void verifyPdf(final PDFPage pdfPage) 
	{
		if (pdfPage.isEncrypted() != getEncrypted())
		{
		    final String desc = "Document is " + (getEncrypted() ? " not" : "") + "encrypted";
			throw new StepFailedException(desc, this);
		}

		if (getUserPassword() != null)
	    {
	    	if (!pdfPage.isUserPassword(getUserPassword()))
	    		throw new StepFailedException("Wrong user password >" + getUserPassword() + "<", this);
	    }

	    if (getOwnerPassword() != null)
	    {
	    	if (!pdfPage.isOwnerPassword(getOwnerPassword()))
	    		throw new StepFailedException("Wrong owner password >" + getOwnerPassword() + "<", this);
	    }
	}
}
