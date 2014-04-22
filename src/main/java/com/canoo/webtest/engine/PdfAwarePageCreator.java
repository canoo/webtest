package com.canoo.webtest.engine;

import java.io.IOException;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox.PdfBoxPDFPage;
import com.gargoylesoftware.htmlunit.DefaultPageCreator;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.PageCreator;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;

/**
 * {@link PageCreator} able to create {@link PdfBoxPDFPage} for "application/pdf".
 * @author Marc Guillemot
 * @version $Revision: 102192 $
 */
public class PdfAwarePageCreator extends DefaultPageCreator
{
    public Page createPage(
            final WebResponse webResponse,
            final WebWindow webWindow )
        throws
            IOException {
        final String contentType = webResponse.getContentType().toLowerCase();
        
        if ("application/pdf".equals(contentType))
        {
        	final PDFPage newPage = new PdfBoxPDFPage(webResponse, webWindow);
            webWindow.setEnclosedPage(newPage);
        	return newPage;
        }
        else
        	return super.createPage(webResponse, webWindow);
	}
}
