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
 *   name="pdfVerifyCheckboxField"
 *   alias="verifyPdfCheckboxField"
 *   description="This step verifies a checkbox field in a <key>pdf</key> document. According to the <key>pdf</key> specification, fields with the same name must have the same value."
 */
public class PdfVerifyCheckboxFieldStep extends AbstractVerifyValuePdfStep {
	private String fName;
	private int fPage = ANY_PAGE;

	/**
	 * @param name
	 * @webtest.parameter required="yes"
	 * description="The checkbox field name."
	 */
	public void setName(final String name) {
		fName = name;
	}

	public String getName() {
		return fName;
	}

	/**
	 * @param page
	 * @webtest.parameter required="no"
	 * description="The page to restrict the verification to."
	 */
	public void setPage(final int page) {
		fPage = page;
	}

	public int getPage() {
		return fPage;
	}

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getName(), "name");
    }

	protected void verifyPdf(final PDFPage pdfPage) 
	{
		final List fields;
		if (getPage() == ANY_PAGE)
			fields = pdfPage.getFields(getName(), PDFField.CHECKBOX);
		else
			fields = pdfPage.getFields(getName(), getPage(), PDFField.CHECKBOX);

		if (getExists() != null)
		{
			boolean exists = ConversionUtil.convertToBoolean(getExists(), true);
			if (exists == fields.isEmpty())
			{
				String msg = "";
				if (fields.isEmpty())
					msg = "No";
				else
					msg += fields.size();
				msg += " checkbox field(s) found with name >" + getName() + "<";
				if (getPage() != ANY_PAGE)
					msg += " on page " + getPage();
				throw new StepFailedException(msg);
			}
		}
		else // test value
		{
			if (fields.isEmpty())
			{
	            throw new StepFailedException("No checkbox field named '" + getName() + "' found.");
			}
			boolean regex = ConversionUtil.convertToBoolean(getRegex(), false);
			final IStringVerifier verifier = getVerifier(regex);
			for (final Iterator iter = fields.iterator(); iter.hasNext();) {
				final PDFField field = (PDFField) iter.next();
				if (!verifier.verifyStrings(getValue(), field.getValue()))
					throw new StepFailedException("Wrong checkbox field value", getValue(), field.getValue());
			}
		}
	}
}
