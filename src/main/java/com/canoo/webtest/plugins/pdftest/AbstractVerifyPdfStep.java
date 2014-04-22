// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;


/**
 * @author Etienne Studer
 * @author Paul King
 * @author Marc Guillemot
 */
public abstract class AbstractVerifyPdfStep extends AbstractPdfStep
{
    /**
     * @throws com.canoo.webtest.engine.StepExecutionException
     *          if current response is not available or if parameter verification fails
     * @throws com.canoo.webtest.engine.StepFailedException
     *          if pdf verification fails
     */
    public void doExecute() throws Exception {
    	
   		verifyPdf(getPdfPage());
    }

    /**
     * @throws com.canoo.webtest.engine.StepFailedException
     *          if pdf verification fails
     */
    protected abstract void verifyPdf(PDFPage pdfPage);

}
