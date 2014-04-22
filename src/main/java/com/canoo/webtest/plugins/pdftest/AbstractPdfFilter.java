// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.interfaces.IContentFilter;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * Helper class for filtering PDF content.
 * @author Paul King
 * @author Marc Guillemot
 */
public abstract class AbstractPdfFilter extends AbstractPdfStep implements IContentFilter
{
    public void doExecute() throws Exception 
    {
    	try {
    		doFilter(getPdfPage());
    	}
    	catch (final Exception e)
    	{
    		if (e instanceof StepFailedException || e instanceof StepExecutionException)
    			throw e;
    		else
    			throw new StepExecutionException("Error during PDF access", this, e);
    	}
    }

    protected abstract void doFilter(final PDFPage pdfPage);
}
