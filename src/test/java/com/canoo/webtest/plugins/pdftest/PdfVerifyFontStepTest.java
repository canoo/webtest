// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;

/**
 * @author Paul King
 * @author Marc Guillemot
 */
public class PdfVerifyFontStepTest extends AbstractBaseVerifyPdfTestCase
{
    protected Step createStep() {
        return new PdfVerifyFontStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyFontStep step = (PdfVerifyFontStep) getStep();
        step.setName("foo");
        return step;
	}

    public void testAttributes() throws IOException {
        String message = ThrowAssert.assertThrows("parameters should not be null", StepExecutionException.class, getExecuteStepTestBlock()).getMessage();
        assertEquals("One of 'name' or 'type' is required!", message);
    }

}
