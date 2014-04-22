// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import org.apache.commons.lang.StringUtils;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyText"
 *   alias="verifyPdfText"
 *   description="This step verifies the existence of the specified text in the <key>pdf</key> document."
 */
public class PdfVerifyTextStep extends AbstractVerifyPdfStep {
    public static final int FIRST_PAGE = 1;
    public static final int LAST_PAGE = -1;

    private int fStartPage = FIRST_PAGE;
	private int fEndPage = LAST_PAGE;
	private boolean fRegex;
	private String fText;

	/**
	 * @param startPage
	 * @webtest.parameter
	 *   required="no"
	 *   description="The numeric value of the start page to restrict the verification to."
	 */
	public void setStartPage(int startPage) {
		fStartPage = startPage;
	}

	public int getStartPage() {
		return fStartPage;
	}

	/**
	 * @param endPage
	 * @webtest.parameter
	 *   required="no"
	 *   description="The numeric value of the end page to restrict the verification to."
	 */
	public void setEndPage(final int endPage) {
		fEndPage = endPage;
	}

	public int getEndPage() {
		return fEndPage;
	}

	/**
	 * @param regex
	 * @webtest.parameter
	 *   required="no"
	 *   description="Specifies whether the text represents a <key>regex</key>."
	 */
	public void setRegex(final boolean regex) {
		fRegex = regex;
	}

	public boolean getRegex() {
		return fRegex;
	}

	/**
	 * @param text
	 * @webtest.parameter required="yes"
	 * description="The text expected in the PDF document."
	 */
	public void setText(final String text) {
		fText = text;
	}

	public String getText() {
		return fText;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getText(), "text");
	}

	protected void verifyPdf(final PDFPage pdfPage) {
        final int lastPage = (getEndPage() == LAST_PAGE) ? pdfPage.getNumberOfPages() : getEndPage();
        final String text = pdfPage.getText(getStartPage(), lastPage);
        final boolean ok;
        if (getRegex())
        	ok = getVerifier(true).verifyStrings(getText(), text);
        else
        	ok = (text.indexOf(getText()) > -1);
        
        if (!ok) {
        	final String shortenedText = StringUtils.abbreviate(text.trim(), 200);
        	throw new StepFailedException("Text not found in pdf: \"" + shortenedText + "\"");
        }
	}
}