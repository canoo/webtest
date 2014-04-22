// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 */
public class PdfVerifyTitleStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyTitleStep();
    }

    public void testAttributes() throws IOException {
    	assertErrorOnExecute(getStep());
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyTitleStep step = (PdfVerifyTitleStep) getStep();
        step.setTitle("foo");
        return step;
	}

	public void testCorrectStep() throws Exception {
        PdfVerifyTitleStep correctStep = (PdfVerifyTitleStep) getStep();
        correctStep.setTitle("TestDokuA");
        executeStep(correctStep);
        correctStep = (PdfVerifyTitleStep) createAndConfigureStep();
        correctStep.setTitle("Test.*");
        correctStep.setRegex(true);
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyTitleStep incorrectStep = (PdfVerifyTitleStep) getStep();
        incorrectStep.setTitle("DUMMY");
        assertFailOnExecute(incorrectStep);
    }
}
