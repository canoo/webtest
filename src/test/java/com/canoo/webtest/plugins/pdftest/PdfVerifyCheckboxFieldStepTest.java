// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfVerifyCheckboxFieldStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyCheckboxFieldStep();
    }

	protected Step getMinimallyConfiguredStep() {
        final PdfVerifyCheckboxFieldStep step = (PdfVerifyCheckboxFieldStep) getStep();
        step.setName("foo");
        step.setExists("true");
        return step;
	}

	public void testAttributes() throws IOException {
        final Step stepWithoutAttributes = getStep();
        assertErrorOnExecute(stepWithoutAttributes);

        final PdfVerifyCheckboxFieldStep stepWithTooFewArguments = (PdfVerifyCheckboxFieldStep) getStep();
        stepWithTooFewArguments.setName("VERTRNR");
        assertErrorOnExecute(stepWithTooFewArguments);

        final PdfVerifyCheckboxFieldStep stepWithTooManyArguments = (PdfVerifyCheckboxFieldStep) getStep();
        stepWithTooManyArguments.setName("OK");
        stepWithTooManyArguments.setValue("Yes");
        stepWithTooManyArguments.setExists("true");
        assertErrorOnExecute(stepWithTooManyArguments);
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyCheckboxFieldStep correctStep = (PdfVerifyCheckboxFieldStep) getStep();
        correctStep.setName("OK");
        correctStep.setValue("(Yes|Ja)");
        correctStep.setRegex("true");
        executeStep(correctStep);

        correctStep = (PdfVerifyCheckboxFieldStep) createAndConfigureStep();
        correctStep.setName("OK");
        correctStep.setPage(2);
        correctStep.setValue("Yes");
        executeStep(correctStep);

        correctStep = (PdfVerifyCheckboxFieldStep) createAndConfigureStep();
        correctStep.setName("NOK");
        correctStep.setExists("yes");
        executeStep(correctStep);

        correctStep = (PdfVerifyCheckboxFieldStep) createAndConfigureStep();
        correctStep.setName("NOK");
        correctStep.setPage(1);
        correctStep.setExists("no");
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws Exception {
        final PdfVerifyCheckboxFieldStep incorrectStep = (PdfVerifyCheckboxFieldStep) getStep();
        incorrectStep.setName("OK");
        incorrectStep.setPage(1);
        incorrectStep.setValue("Yes");
        assertFailOnExecute(incorrectStep);
    }

}
