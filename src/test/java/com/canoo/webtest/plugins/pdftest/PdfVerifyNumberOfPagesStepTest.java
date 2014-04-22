// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 */
public class PdfVerifyNumberOfPagesStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyNumberOfPagesStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyNumberOfPagesStep step = (PdfVerifyNumberOfPagesStep) getStep();
        step.setCount(12);
        return step;
	}
	
    public void testAttributes() throws IOException {
        final Step stepWithoutAttributes = getStep();
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(stepWithoutAttributes);
            }
        });
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyNumberOfPagesStep correctStep = (PdfVerifyNumberOfPagesStep) getStep();
        correctStep.setCount(2);
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyNumberOfPagesStep incorrectStep = (PdfVerifyNumberOfPagesStep) getStep();
        incorrectStep.setCount(99);
        assertFailOnExecute(incorrectStep);
    }
}
