// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfVerifyEncryptionStrengthStepTest extends AbstractBaseVerifyPdfTestCase
{

    protected ContextStub createContext() {
        return new PdfContextStub(PdfTestResources.ENCRYPTED_FILE);
    }

    protected Step createStep() {
        return new PdfVerifyEncryptionStrengthStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyEncryptionStrengthStep step = (PdfVerifyEncryptionStrengthStep) getStep();
        step.setStrength(123);
        return step;
	}

	public void testAttributes() throws Exception {
        assertErrorOnExecute(getStep());
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyEncryptionStrengthStep correctStep = (PdfVerifyEncryptionStrengthStep) getStep();
        correctStep.setStrength(40);
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyEncryptionStrengthStep incorrectStep = (PdfVerifyEncryptionStrengthStep) getStep();
        incorrectStep.setStrength(128);
        assertFailOnExecute(incorrectStep);
    }
}
