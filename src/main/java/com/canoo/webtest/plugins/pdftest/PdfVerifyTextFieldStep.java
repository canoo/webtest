// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.util.Iterator;
import java.util.List;

import com.canoo.webtest.engine.IStringVerifier;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFField;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyTextField"
 *   alias="verifyPdfTextField"
 *   description="Verifies a text field in the <key>pdf</key> document. 
 *   According to the <key>pdf</key> specification, fields with the same name must have the same value."
 */
public class PdfVerifyTextFieldStep extends AbstractVerifyValuePdfStep {
	private String fName;
	private int fPage = ANY_PAGE;

	public String getName() {
		return fName;
	}

	/**
	 * @param name
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The text of the <em>info</em> dictionary property key."
	 */
	public void setName(final String name) {
		fName = name;
	}

	public int getPage() {
		return fPage;
	}

	/**
	 * @param page
	 * @webtest.parameter
	 *   required="no"
	 *   description="The numeric value of the page to restrict the verification to."
	 */
	public void setPage(final int page) {
		fPage = page;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getName(), "name");
	}

	protected void verifyPdf(final PDFPage pdfPage) {
		final List fields;
		if (getPage() == ANY_PAGE)
			fields = pdfPage.getFields(getName(), PDFField.TEXTBOX);
		else
			fields = pdfPage.getFields(getName(), getPage(), PDFField.TEXTBOX);

		if (getExists() != null) {
			boolean exists = ConversionUtil.convertToBoolean(getExists(), true);
			if (exists && fields.isEmpty())
				throw new StepFailedException("No field found with name \"" + getName() + "\"");
			else if (!exists && !fields.isEmpty())
				throw new StepFailedException(fields.size() + " fields found with name \"" + getName() + "\"");
		}
		else
		{
			if (fields.isEmpty())
			{
	            throw new StepFailedException("No text field named '" + getName() + "' found.");
			}
			boolean regex = ConversionUtil.convertToBoolean(getRegex(), false);
			final IStringVerifier verifier = getVerifier(regex);
			for (final Iterator iter = fields.iterator(); iter.hasNext();) {
				final PDFField field = (PDFField) iter.next();
				if (!verifier.verifyStrings(getValue(), field.getValue()))
					throw new StepFailedException("Wrong field value", getValue(), field.getValue());
			}
		}
	}
}
