// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import com.canoo.webtest.boundary.FileBoundary;
import com.canoo.webtest.boundary.UrlBoundary;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFEncryptionPermission;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox.PdfBoxPDFPage;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public abstract class AbstractBaseVerifyPdfTestCase extends BaseStepTestCase
{
	private PDFPage pdfPageStub;

	public void testNoCurrentResponse() throws IOException {
        assertStepRejectsNullResponse(getMinimallyConfiguredStep());
    }
    
    protected void tearDown() throws Exception {
    	final Page page = getContext().getCurrentResponse();
    	if (page != null)
    		page.cleanUp();
    	if (pdfPageStub != null)
    		pdfPageStub.cleanUp();
    	super.tearDown();
    }
    
    /**
     * Gets an instance of the step under test with minimal configuration
     * allowing verifyParameters() to be successfull.
     * @return default is same as getStep()
     */
    protected Step getMinimallyConfiguredStep() {
		return getStep();
	}

	/**
     * Creates a context with the default file for pdf tests
     * @see com.canoo.webtest.steps.BaseStepTestCase#createContext()
     */
    protected ContextStub createContext()
    {
    	return new PdfContextStub(PdfTestResources.DEFAULT_FILE);
    }

    public void testNonPdf() throws IOException {
        final Step step = getMinimallyConfiguredStep();
        getContext().setDefaultResponse("", "text/plain");
        assertErrorOnExecute(step, "Current response is not a PDF page but has following mime type text/plain", "");
    }

    protected PDFPage getPDFPageStub() throws Exception {
    	if (pdfPageStub == null)
    		pdfPageStub = createPDFPageStub();
    	
    	return pdfPageStub;
    }
    	
    private PDFPage createPDFPageStub() throws Exception {
    	final byte[] pdfBytes = FileBoundary.getBytes(PdfTestResources.DEFAULT_FILE);
    	final WebResponseData data = new WebResponseData(pdfBytes, 200, "OK", Collections.EMPTY_LIST);
    	final URL url = UrlBoundary.tryCreateUrl(ContextStub.SOME_BASE_URL);
    	final WebResponse webResp = new WebResponse(data, url, HttpMethod.GET, 1);
        return new PdfBoxPDFPage(webResp, null)
        {
        	public boolean isEncrypted() {
                return true;
            }

            public String getText(String s1, String s2, String s3, String s4) {
                throw new RuntimeException("Dummy Error");
            }

            public List getBookmarks() {
                throw new RuntimeException("Dummy Error");
            }

            public List getLinks() {
                throw new RuntimeException("Dummy Error");
            }

        	public boolean hasPermission(final PDFEncryptionPermission permission) {
                throw new RuntimeException("Dummy Error");
            }
        };
    }
}
