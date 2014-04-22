// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.engine.EqualsStringVerfier;
import com.canoo.webtest.engine.IStringVerifier;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFFont;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyFont"
 *   alias="verifyPdfFont"
 *   description="This step verifies the existence of a font within the current <key>pdf</key> document."
 */
public class PdfVerifyFontStep extends AbstractVerifyPdfStep {
	
    private static final Logger LOG = Logger.getLogger(PdfVerifyFontStep.class);
    private boolean fMatchCase;
    private String fName;
    private String fType;
    private int fPage = ANY_PAGE;

	/**
	 * @param value
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The name of the font to find, e.g. <em>Times</em>, <em>Courier-Bold</em> 
	 *   or <em>Helvetica-Oblique</em>. 
	 *   One of <em>name</em> or <em>type</em> must be set."
	 */
	public void setName(final String value) {
        fName = value;
	}

	public String getName() {
		return fName;
	}

	/**
	 * @param value
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The type of the font to find, e.g. <em>Type 1</em> or <em>TrueType</em>. 
	 *   One of <em>name</em> or <em>type</em> must be set."
	 */
	public void setType(final String value) {
        fType = value;
	}

	public String getType() {
		return fType;
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

	public int getPage() {
		return fPage;
	}

    /**
     * @param matchCase
     * @webtest.parameter required="no"
     * default="'false'"
     * description="Specifies whether to match case when comparing the expected and actual name 
     * and type values of a font."
     */
    public void setMatchCase(final boolean matchCase) {
        fMatchCase = matchCase;
    }

    public boolean getMatchCase() {
        return fMatchCase;
    }

    protected void verifyParameters() {
		super.verifyParameters();
        paramCheck(getName() == null && getType() == null, "One of 'name' or 'type' is required!");
	}

    protected void verifyPdf(final PDFPage pdfPage) 
    {
    	LOG.debug("Retrieving fonts");
    	final List fonts = pdfPage.getFonts();
    	LOG.debug(fonts.size() + " fonts to examine");

    	final IStringVerifier verifier = getVerifier(); 
    	for (final Iterator iter = fonts.iterator(); iter.hasNext();) {
			final PDFFont font = (PDFFont) iter.next();
			if (verifyFont(font, verifier))
			{
		    	LOG.info("Found font " + font);
				return; // right font has been found
			}
		}
    	
    	LOG.info("No matching font found");
    	final StringBuffer sb = new StringBuffer("No font found with ");
    	if (getName() != null)
    		sb.append("name >" + getName() + "< ");
    	if (getType() != null)
    		sb.append("type >" + getType() + "< ");
    	sb.append("(matchCase: " + getMatchCase() + ")");
    	throw new StepFailedException(sb.toString(), this);
    }

    private boolean verifyFont(final PDFFont font, final IStringVerifier verifier) {
    	LOG.debug("Testing font: " + font);
        
    	try
    	{
            if (getPage() != ANY_PAGE && getPage() != font.getPage())
            {
            	LOG.debug("Page doesn't match: " + font.getPage());
                return false;
            }
            
            boolean typeMatches = (getType() == null || verifier.verifyStrings(getType(), font.getType()));
            boolean nameMatches = (getName() == null || verifier.verifyStrings(getName(), font.getName()));
        	LOG.debug("type match: " + typeMatches + ", name match: " + nameMatches);
            return typeMatches && nameMatches;
    	}
    	catch (RuntimeException e)
    	{
    		LOG.debug("Exception", e);
    		throw e;
    	}
	}

	private IStringVerifier getVerifier() {
		if (getMatchCase())
			return EqualsStringVerfier.INSTANCE;
		else
		{
			return new IStringVerifier()
			{
				public boolean verifyStrings(final String expectedValue, final String actualValue) {
					return StringUtils.equalsIgnoreCase(expectedValue, actualValue);
				}
			};
		}
	}
}