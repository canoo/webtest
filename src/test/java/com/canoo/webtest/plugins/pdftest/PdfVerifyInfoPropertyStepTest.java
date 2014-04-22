// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfVerifyInfoPropertyStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyInfoPropertyStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyInfoPropertyStep step = (PdfVerifyInfoPropertyStep) getStep();
        step.setKey("foo");
        step.setExists("true");
        return step;
	}

    public void testAttributes() throws IOException {
        final Step stepWithoutAttributes = getStep();
        assertErrorOnExecute(stepWithoutAttributes);

        final PdfVerifyInfoPropertyStep stepWithTooManyArguments = (PdfVerifyInfoPropertyStep) getStep();
        stepWithTooManyArguments.setKey("DUMMY1");
        stepWithTooManyArguments.setExists("DUMMY2");
        stepWithTooManyArguments.setValue("DUMMY3");
        assertErrorOnExecute(stepWithTooManyArguments);

        final PdfVerifyInfoPropertyStep stepWithTooFewArguments = (PdfVerifyInfoPropertyStep) getStep();
        stepWithTooFewArguments.setKey("DUMMY");
        assertErrorOnExecute(stepWithTooFewArguments);
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyInfoPropertyStep correctStep = (PdfVerifyInfoPropertyStep) getStep();
        correctStep.setKey("Author");
        correctStep.setExists("true");
        executeStep(correctStep);

        correctStep = (PdfVerifyInfoPropertyStep) createStep();
        correctStep.setKey("Author");
        correctStep.setValue("Mac4");
        executeStep(correctStep);

        correctStep = (PdfVerifyInfoPropertyStep) createStep();
        correctStep.setKey("Creator");
        correctStep.setValue("Quark.*");
        correctStep.setRegex("true");
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyInfoPropertyStep step = (PdfVerifyInfoPropertyStep) getStep();
        step.setKey("Author");
        step.setValue("DUMMY");
        assertFailOnExecute(step);
    }
}
