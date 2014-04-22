// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
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
 *   name="pdfVerifyReadOnlyField"
 *   alias="verifyPdfReadOnlyField"
 *   description="This step verifies the read-only status of a field in the <key>pdf</key> document."
 */
public class PdfVerifyReadOnlyFieldStep extends AbstractVerifyPdfStep {
	private String fName;
	private int fPage = ANY_PAGE;
	private boolean fReadOnly = true;

    public String getName() {
        return fName;
    }

    /**
	 * @param name
	 * @webtest.parameter required="yes"
	 * description="The name of the field"
	 */
	public void setName(final String name) {
		fName = name;
	}

    public boolean getReadOnly() {
        return fReadOnly;
    }

    /**
     * @param readOnly
     * @webtest.parameter required="no"
     * default="'yes'"
     * description="Specifies whether the field is expected to be read-only or not."
     */
    public void setReadOnly(final boolean readOnly) {
        fReadOnly = readOnly;
    }

    public int getPage() {
        return fPage;
    }

    /**
	 * @param page
	 * @webtest.parameter required="no"
	 * default="all the pages"
	 * description="The numeric value of the page to restrict the verification to."
	 */
	public void setPage(final int page) {
		fPage = page;
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
		
		if (fields.isEmpty())
			throw new StepFailedException("No field found", this);
		
		for (final Iterator iter = fields.iterator(); iter.hasNext();) {
			final PDFField field = (PDFField) iter.next();
			if (field.isReadOnly() != getReadOnly())
			{
				final String msg = "Field with name >" + getName() + "< is " + (field.isReadOnly() ? "" : "not ")
					+ "read-only!";
				throw new StepFailedException(msg, this);
			}
		}
	}
}
