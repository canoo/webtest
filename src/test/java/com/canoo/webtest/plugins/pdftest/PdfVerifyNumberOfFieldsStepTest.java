// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 */
public class PdfVerifyNumberOfFieldsStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyNumberOfFieldsStep();
    }
	
    public void testAttributes() throws Exception {
        assertErrorOnExecute(getStep());
    }
    
    /**
     * Gets an instance of the step under test with minimal configuration
     * allowing verifyParameters() to be successfull.
     * @return default is same as getStep()
     */
    protected Step getMinimallyConfiguredStep() {
        final PdfVerifyNumberOfFieldsStep step = (PdfVerifyNumberOfFieldsStep) getStep();
        step.setCount(123);
        return step;
	}
    

    public void testCorrectStep() throws Exception {
        final PdfVerifyNumberOfFieldsStep correctStep = (PdfVerifyNumberOfFieldsStep) getStep();
        correctStep.setCount(20);
        executeStep(correctStep);

        correctStep.setIncludeDuplicates(true);
        correctStep.setCount(23);
        executeStep(correctStep);

        correctStep.setPage(2);
        correctStep.setIncludeDuplicates(false);
        correctStep.setCount(8);
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyNumberOfFieldsStep incorrectStep = (PdfVerifyNumberOfFieldsStep) getStep();
        incorrectStep.setCount(99);
        assertFailOnExecute(incorrectStep);
    }
}
