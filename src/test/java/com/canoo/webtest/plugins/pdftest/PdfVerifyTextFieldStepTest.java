// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 */
public class PdfVerifyTextFieldStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyTextFieldStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyTextFieldStep step = (PdfVerifyTextFieldStep) getStep();
        step.setName("foo");
        step.setExists("true");
        return step;
	}

	public void testAttributes() throws IOException {
        final Step stepWithoutAttributes = getStep();
        assertErrorOnExecute(stepWithoutAttributes);

        final PdfVerifyTextFieldStep stepWithTooManyArguments = (PdfVerifyTextFieldStep) createAndConfigureStep();
        stepWithTooManyArguments.setName("VERTRNR");
        stepWithTooManyArguments.setValue("DUMMY");
        stepWithTooManyArguments.setExists("true");
        assertErrorOnExecute(stepWithTooManyArguments);

        final PdfVerifyTextFieldStep stepWithTooFewArguments = (PdfVerifyTextFieldStep) createAndConfigureStep();
        stepWithTooFewArguments.setName("VERTRNR");
        assertErrorOnExecute(stepWithTooFewArguments);
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyTextFieldStep correctStep = (PdfVerifyTextFieldStep) getStep();
        correctStep.setName("VERTRNR");
        correctStep.setValue("Doku.");
        correctStep.setRegex("true");
        executeStep(correctStep);

        correctStep = (PdfVerifyTextFieldStep) createAndConfigureStep();
        correctStep.setName("NATION1");
        correctStep.setPage(1);
        correctStep.setValue("CH");
        executeStep(correctStep);

        correctStep = (PdfVerifyTextFieldStep) createAndConfigureStep();
        correctStep.setName("NAME1");
        correctStep.setExists("yes");
        executeStep(correctStep);

        correctStep = (PdfVerifyTextFieldStep) createAndConfigureStep();
        correctStep.setName("STRNR1");
        correctStep.setPage(2);
        correctStep.setExists("no");
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyTextFieldStep incorrectStep = (PdfVerifyTextFieldStep) getStep();
        incorrectStep.setName("NATION1");
        incorrectStep.setPage(2);
        incorrectStep.setValue("CH");
        assertFailOnExecute(incorrectStep);
    }
}
