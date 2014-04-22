// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 */
public class PdfVerifyEncryptionPropertyStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyEncryptionPropertyStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyEncryptionPropertyStep step = (PdfVerifyEncryptionPropertyStep) getStep();
        step.setKey("foo");
        step.setExists("true");
        return step;
	}

	public void testAttributes() throws IOException {
        assertErrorOnExecute(getStep());

        final PdfVerifyEncryptionPropertyStep stepWithTooManyAttributes = (PdfVerifyEncryptionPropertyStep) createStep();
        stepWithTooManyAttributes.setKey("DUMMY1");
        stepWithTooManyAttributes.setExists("DUMMY2");
        stepWithTooManyAttributes.setValue("DUMMY3");
        assertErrorOnExecute(stepWithTooManyAttributes);

        final PdfVerifyEncryptionPropertyStep stepWithTooFewArguments = (PdfVerifyEncryptionPropertyStep) createStep();
        stepWithTooFewArguments.setKey("DUMMY");
        assertErrorOnExecute(stepWithTooFewArguments);
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyEncryptionPropertyStep correctStep = (PdfVerifyEncryptionPropertyStep) createStep();
        setFakedContext(new PdfContextStub(PdfTestResources.ENCRYPTED_FILE));
        correctStep.setKey("Filter");
        correctStep.setExists("true");
        executeStep(correctStep);

        correctStep = (PdfVerifyEncryptionPropertyStep) createStep();
        correctStep.setKey("Filter");
        correctStep.setValue("Standard");
        executeStep(correctStep);

        correctStep = (PdfVerifyEncryptionPropertyStep) createStep();
        correctStep.setKey("Filter");
        correctStep.setValue(".tanda.");
        correctStep.setRegex("true");
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyEncryptionPropertyStep incorrectStep = (PdfVerifyEncryptionPropertyStep) getStep();
        setFakedContext(new PdfContextStub(PdfTestResources.ENCRYPTED_FILE));
        incorrectStep.setKey("Filter");
        incorrectStep.setValue("DUMMY");
        assertFailOnExecute(incorrectStep);
    }
}
