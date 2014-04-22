// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

/**
 * Tests for {@link PdfToBookmarksFilter}.
 *
 * @author Paul King
 * @author Marc Guillemot
 */
public class PdfToBookmarksFilterTest extends AbstractBaseVerifyPdfTestCase
{
    protected Step createStep() {
        return new PdfToBookmarksFilter();
    }

    protected ContextStub createContext() {
    	return new ContextStub(); // not a pd
    }

    public void testAnalyzerError() throws Exception {
        final Step step = new PdfToBookmarksFilter()
        {
            protected PDFPage getPdfPage() throws Exception {
                return getPDFPageStub();
            }
        };
        
        assertErrorOnExecute(step);
    }
}
