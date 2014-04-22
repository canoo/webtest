// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Base class for PDF steps.
 * @author Paul King
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public abstract class AbstractPdfStep extends Step
{
    /**
     * Value indicating that a no page restriction occurs for the step
     */
    protected static final int ANY_PAGE = -1;

    protected PDFPage getPdfPage() throws Exception
    {
        nullResponseCheck();
    	final Page page = getContext().getCurrentResponse();
    	if (page instanceof PDFPage)
    		return (PDFPage) page;
    	
    	throw new StepExecutionException("Current response is not a PDF page but has following mime type "
    			+ page.getWebResponse().getContentType() + " (" + page + ")", this);
    }
    
    
}
