// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link PdfToLinksFilter}.
 *
 * @author Paul King
 * @author Marc Guillemot
 */
public class PdfToLinksFilterTest extends AbstractBaseVerifyPdfTestCase
{
    protected Step createStep() {
        return new PdfToLinksFilter();
    }

    public void testAnalyzerError() throws Exception {
        final Step step = new PdfToLinksFilter()
        {
        	protected PDFPage getPdfPage() throws Exception {
        		return getPDFPageStub();
        	}
        };
        assertErrorOnExecute(step);
    }
}
