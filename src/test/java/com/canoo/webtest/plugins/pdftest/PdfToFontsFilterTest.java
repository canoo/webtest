// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.ArrayList;
import java.util.List;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFFont;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link PdfToFontsFilter}.
 *
 * @author Paul King
 */
public class PdfToFontsFilterTest extends AbstractBaseVerifyPdfTestCase
{
    protected Step createStep() {
        return new PdfToFontsFilter();
    }
    
    public void testFontsToString() {
        final String lineSep = System.getProperty("line.separator");
    	final String expected = "1|Type1|Symbol" + lineSep + "1|Type1|Times-Roman";

    	final List li = new ArrayList();
    	li.add(new DummyFont("Times-Roman", "Type1", 1));
    	li.add(new DummyFont("Symbol", "Type1", 1));
    	
    	assertEquals(expected, PdfToFontsFilter.fontsToString(li));

    	li.clear();
    	li.add(new DummyFont("Symbol", "Type1", 1));
    	li.add(new DummyFont("Times-Roman", "Type1", 1));
    	assertEquals(expected, PdfToFontsFilter.fontsToString(li));
    }
}


class DummyFont implements PDFFont
{
	private final String fName, fType;
	private final int fPage;
	
	DummyFont(final String name, final String type, final int page)
	{
		fName = name;
		fType = type;
		fPage = page;
	}
	public String getName()
	{
		return fName;
	}
	public int getPage()
	{
		return fPage;
	}
	public String getType()
	{
		return fType;
	}
}
