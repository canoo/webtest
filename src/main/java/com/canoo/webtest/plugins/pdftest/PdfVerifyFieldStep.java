// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

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
 *   name="pdfVerifyField"
 *   alias="verifyPdfField"
 *   description="This step verifies the existence of a field in a <key>pdf</key> document."
 */
public class PdfVerifyFieldStep extends AbstractVerifyPdfStep {
	private String fName;
	private int fPage = ANY_PAGE;
	private boolean fExists = true;

	/**
	 * @param page
	 * @webtest.parameter required="no"
	 * default="all the pages"
	 * description="The page to restrict the verification to."
	 */
	public void setPage(final int page) {
		fPage = page;
	}

	public int getPage() {
		return fPage;
	}

    /**
     * @param name
     * @webtest.parameter required="yes"
     * description="The field name."
     */
    public void setName(final String name) {
        fName = name;
    }

    public String getName() {
        return fName;
    }

    /**
	 * @param exists
	 * @webtest.parameter 
	 * 	required="no"
	 *	default="true"
	 * 	description="Specifies whether the field is expected to exist or not."
	 */
	public void setExists(final boolean exists) {
		fExists = exists;
	}

	public boolean getExists() {
		return fExists;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getName(), "name");
	}

	protected void verifyPdf(final PDFPage pdfPage) 
	{
		final List fields;
		if (getPage() == ANY_PAGE)
			fields = pdfPage.getFields(getName());
		else
			fields = pdfPage.getFields(getName(), getPage());

		if (getExists() == fields.isEmpty())
		{
			String msg = "";
			if (fields.isEmpty())
				msg = "No";
			else
				msg += fields.size();
			msg += " field(s) found with name >" + getName() + "<";
			if (getPage() != ANY_PAGE)
				msg += " on page " + getPage();
			
			final StepFailedException sfe = new StepFailedException(msg);
			final List availableFiels;
			if (getPage() == ANY_PAGE)
				availableFiels = pdfPage.getFields();
			else
				availableFiels = pdfPage.getFields(getPage());

			if (availableFiels.isEmpty())
				sfe.addDetail("available fields", "- none -");
			else
			{
				final StringBuffer sb = new StringBuffer();
				for (final Iterator iter=availableFiels.iterator(); iter.hasNext();)
				{
					final PDFField field = (PDFField) iter.next();
					sb.append(field.getName());
					sb.append("\n");
				}
				sfe.addDetail("available fields", sb.toString());
			}
			throw sfe; 
		}
	}
}
