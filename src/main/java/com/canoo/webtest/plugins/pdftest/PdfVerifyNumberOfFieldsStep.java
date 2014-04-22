// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFField;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyFieldCount"
 *   alias="verifyPdfFieldCount"
 *   description="This step verifies the number of fields in the <key>pdf</key> document."
 */
public class PdfVerifyNumberOfFieldsStep extends AbstractVerifyPdfStep {
	private int fCount = -1;
	private int fPage = ANY_PAGE;
	private boolean fIncludeDuplicates;

	/**
	 * @param count
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The expected number of fields."
	 */
	public void setCount(final int count) {
		fCount = count;
	}

	public int getCount() {
		return fCount;
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

	public int getPage() {
		return fPage;
	}

	/**
	 * @param includeDuplicates
	 * @webtest.parameter
	 *   required="no"
	 *   default="false"
	 *   description="Specifies whether fields with the same name should be counted only once or individually."
	 */
	public void setIncludeDuplicates(final boolean includeDuplicates) {
		fIncludeDuplicates = includeDuplicates;
	}

	public boolean getIncludeDuplicates() {
		return fIncludeDuplicates;
	}

	protected void verifyPdf(final PDFPage pdfPage) 
	{
		final List fields;
		if (ANY_PAGE == getPage())
			fields = pdfPage.getFields();
		else
			fields = pdfPage.getFields(getPage());

		final Collection fieldNames;
		if (getIncludeDuplicates())
			fieldNames = new ArrayList(); // preserves duplicates
		else
			fieldNames = new HashSet(); // removes duplicates
		
		for (final Iterator iter = fields.iterator(); iter.hasNext();) 
		{
			final PDFField field = (PDFField) iter.next();
			fieldNames.add(field.getName());
		}
		
		if (fieldNames.size() != getCount())
		{
			throw new StepFailedException(buildFailureMessage(), getCount(), fieldNames.size());
		}
	}

	protected String buildFailureMessage()
	{
		final StringBuffer sb = new StringBuffer("Wrong number of fields on ");
		if (ANY_PAGE == getPage())
			sb.append("all pages ");
		else
			sb.append("page " + getPage());

		sb.append("(");
		sb.append((getIncludeDuplicates() ? "including" : "without")); 
		sb.append(" duplicates)");
		return sb.toString();
	}

	protected void verifyParameters() {
		super.verifyParameters();
		paramCheck(getCount() == -1, "Required parameter \"count\" not set!");
	}
}
