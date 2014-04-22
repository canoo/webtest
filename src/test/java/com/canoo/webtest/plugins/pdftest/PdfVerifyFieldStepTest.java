// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfVerifyFieldStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyFieldStep();
    }
	
	protected Step getMinimallyConfiguredStep() {
        final PdfVerifyFieldStep step = (PdfVerifyFieldStep) getStep();
        step.setName("foo");
        step.setExists(true);
		return step;
	}
	
    public void testAttributes() throws IOException {
        assertErrorOnExecute(getStep());
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyFieldStep correctStep = (PdfVerifyFieldStep) getStep();
        correctStep.setName("UNTER2");
        correctStep.setExists(true);
        executeStep(correctStep);

        correctStep.setPage(1);
        correctStep.setName("NATION1");
        correctStep.setExists(true);
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyFieldStep incorrectStep = (PdfVerifyFieldStep) getStep();
        incorrectStep.setPage(2);
        incorrectStep.setName("NATION1");
        incorrectStep.setExists(true);
        assertFailOnExecute(incorrectStep);
    }
}
