// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step category="PDF" 
 * name="pdfVerifyEncryptionProperty"
 * alias="verifyPdfEncryptProperty"
 * description="This step verifies a property of the <em>encrypt</em> dictionary from an encrypted <key>pdf</key> document. 
 * The step must either verify the existence of the <em>encrypt</em> dictionary property key or it must verify its value."
 */
public class PdfVerifyEncryptionPropertyStep extends AbstractVerifyValuePdfStep {
	private String fKey;

	public String getKey() {
		return fKey;
	}

	/**
	 * @param key
	 * @webtest.parameter required="yes" 
	 * 	description="The text of the <em>encrypt</em> dictionary property key."
	 */
	public void setKey(final String key) {
		fKey = key;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getKey(), "key");
	}

	protected void verifyPdf(final PDFPage pdfPage) 
	{
		final String actualValue = pdfPage.getEncryptProperty(getKey());
		if (getExists() != null) 
		{
			final boolean exists = ConversionUtil.convertToBoolean(getExists(), true);
			if ((exists && actualValue == null) 
					|| (!exists && actualValue != null))
			{
			    final StringBuffer sb = new StringBuffer("Encrypt property with key \"");
			    sb.append(getKey());
			    sb.append("\" should ");
			    if (!exists)
			    	sb.append("not ");
			    sb.append("exist");
				throw new StepFailedException(sb.toString(), this);
			}
		}
		else
		{
			final boolean regex = ConversionUtil.convertToBoolean(getRegex(), false);
			if (!getVerifier(regex).verifyStrings(getValue(), actualValue)) {
				throw new StepFailedException("Wrong encrypt property value for key \"" + getKey() + "\"", getValue(), actualValue);
			}
		}
	}
}
