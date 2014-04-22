// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;

/**
 * @author Paul King
 */
public class PdfVerifyLinkStepTest extends AbstractBaseVerifyPdfTestCase
{
    protected Step createStep() {
        return new PdfVerifyLinkStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyLinkStep step = (PdfVerifyLinkStep) getStep();
        step.setText("foo");
        return step;
	}

	public void testAttributes() throws IOException {
    	final PdfVerifyLinkStep step = (PdfVerifyLinkStep) getStep();
    	getContext().setDefaultResponse("", "application/pdf");

        String message = ThrowAssert.assertThrows("parameters should not be null", StepExecutionException.class, getExecuteStepTestBlock()).getMessage();
        assertEquals("One of 'text' or 'href' is required!", message);
        step.setText("dummy");
        step.setHref("dummy");
        message = ThrowAssert.assertThrows("parameters should not be null", StepExecutionException.class, getExecuteStepTestBlock()).getMessage();
        assertEquals("Only one of 'text' and 'href' can be set!", message);
    }

}
