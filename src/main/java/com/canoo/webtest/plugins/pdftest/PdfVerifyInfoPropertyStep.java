// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyInfoProperty"
 *   alias="verifyPdfInfoProperty"
 *   description="This step verifies a property from the <em>info</em> dictionary of the current <key>pdf</key> document. The step must either verify the existence of the <em>info</em> dictionary property key or it must verify its value."
 */
public class PdfVerifyInfoPropertyStep extends AbstractVerifyValuePdfStep {
	private String fKey;

	public PdfVerifyInfoPropertyStep() {
		fKey = null;
	}

	/**
	 * @param key
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The key of the property of interest from the <em>info</em> dictionary. Typically one of <em>Title</em>, <em>Author</em>, <em>Subject</em>, <em>Keywords</em>, <em>Creator</em>, <em>Producer</em>, <em>CreationDate</em> or <em>ModDate</em>"
	 */
	public void setKey(String key) {
		fKey = key;
	}

	public String getKey() {
		return fKey;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getKey(), "key");
	}

	protected void verifyPdf(final PDFPage pdfPage) {
		final String actualValue = pdfPage.getInfoProperty(getKey());
		if (getExists() != null) {
			final boolean exists = ConversionUtil.convertToBoolean(getExists(), true);
			if ((exists && actualValue == null) 
					|| (!exists && actualValue != null))
			{
		        final StringBuffer sb = new StringBuffer("Info property with key \"");
			    sb.append(getKey());
			    sb.append("\" should ");
			    if (!exists)
			    	sb.append("not ");
			    sb.append("exist");
			    if (actualValue != null)
			    	sb.append(" (actual value: >" + actualValue + "<");
				throw new StepFailedException(sb.toString(), this);
			}
		}
		else
		{
			final boolean regex = ConversionUtil.convertToBoolean(getRegex(), false);
			if (!getVerifier(regex).verifyStrings(getValue(), actualValue)) 
			{
				throw new StepFailedException("Wrong info property value for key \"" + getKey() + "\"", getValue(), actualValue);
			}
		}
	}
}