// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.canoo.webtest.engine.IStringVerifier;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFLink;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyLink"
 *   alias="verifyPdfLink"
 *   description="This step verifies the existence of a link within a <key>pdf</key> document."
 */
public class PdfVerifyLinkStep extends AbstractVerifyPdfStep {
    private boolean fRegex;
    private String fLinkText;
    private String fLinkHref;
    private int fPage = ANY_PAGE;

	/**
	 * @param value
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The text of the link to find. One of <em>text</em> or <em>href</em> must be set."
	 */
	public void setText(final String value) {
        fLinkText = value;
	}

	public String getText() {
		return fLinkText;
	}

	/**
	 * @param value
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The href of the link to find. One of <em>text</em> or <em>href</em> must be set."
	 */
	public void setHref(final String value) {
        fLinkHref = value;
	}

	public String getHref() {
		return fLinkHref;
	}

    public int getPage() {
        return fPage;
    }

    /**
	 * @param value
	 * @webtest.parameter
	 *   required="no"
     *   default="all pages"
	 *   description="The page on which to search."
	 */
	public void setPage(final int value) {
        fPage = value;
	}

    /**
     * @param regex
     * @webtest.parameter required="no"
     * default="'false'"
     * description="Specifies whether the search value represents a <key>regex</key>."
     */
    public void setRegex(final boolean regex) {
        fRegex = regex;
    }

    public boolean getRegex() {
        return fRegex;
    }

    protected void verifyParameters() {
		super.verifyParameters();
        paramCheck(getText() == null && getHref() == null, "One of 'text' or 'href' is required!");
        paramCheck(getText() != null && getHref() != null, "Only one of 'text' and 'href' can be set!");
	}

    protected void verifyPdf(final PDFPage pdfPage)
    {
    	final IStringVerifier verifier = getVerifier(getRegex());
    	final String expectedValue = StringUtils.defaultString(getText(), getHref());

    	final List links = pdfPage.getLinks();
    	for (final Iterator iter = links.iterator(); iter.hasNext();)
    	{
			final PDFLink element = (PDFLink) iter.next();
			if (verifyLink(element, verifier, expectedValue))
				return;
		}
    	
    	throw new StepFailedException("No link found matching criteria.");
    }

    boolean verifyLink(final PDFLink link, final IStringVerifier verifier, final String expectedValue)
    {
    	if (getPage() != ANY_PAGE && getPage() != link.getPage())
    	{
    		return false;
    	}
    	
    	final String actualValue = (getHref() != null) ? link.getHref() : link.getText();
		return verifier.verifyStrings(expectedValue, actualValue);
	}
}