// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.Iterator;
import java.util.List;

import com.canoo.webtest.engine.IStringVerifier;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFBookmark;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyBookmark"
 *   alias="verifyPdfBookmark"
 *   description="This step verifies the existence of a bookmark within a <key>pdf</key> document."
 */
public class PdfVerifyBookmarkStep extends AbstractVerifyPdfStep {
    private boolean fRegex;
    private String fName;

	/**
	 * @param name
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The name of the bookmark to find."
	 */
	public void setName(final String name) {
        fName = name;
	}

	public String getName() {
		return fName;
	}

    /**
     * @param regex
     * @webtest.parameter required="no"
     * default="'false'"
     * description="Specifies whether the name represents a <key>regex</key>."
     */
    public void setRegex(final boolean regex) {
        fRegex = regex;
    }

    public boolean getRegex() {
        return fRegex;
    }

    protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getName(), "name");
	}

    protected void verifyPdf(final PDFPage pdfPage)
    {
    	final List bookmarks = pdfPage.getBookmarks();
    	final IStringVerifier verifier = getVerifier(getRegex());
    	for (Iterator iter = bookmarks.iterator(); iter.hasNext();) {
			final PDFBookmark element = (PDFBookmark) iter.next();
			if (verifier.verifyStrings(getName(), element.getTitle()))
				return; // bookmark found
		}
    	
    	throw new StepFailedException("No bookmark found with name >" + getName() + "< (regex: " + getRegex() + ")", this);
    }
}