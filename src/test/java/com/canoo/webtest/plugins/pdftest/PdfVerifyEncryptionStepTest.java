// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfVerifyEncryptionStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyEncryptionStep();
    }
	
	protected ContextStub createContext()
	{
		return new PdfContextStub(PdfTestResources.ENCRYPTED_FILE);
	}

    public void testCorrectStep() throws Exception {
        final PdfVerifyEncryptionStep step = (PdfVerifyEncryptionStep) getStep();
        step.setEncrypted(true);
        step.setUserPassword("");
        step.setOwnerPassword("mymaster");
        executeStep(step);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyEncryptionStep step = (PdfVerifyEncryptionStep) getStep();
        setFakedContext(new PdfContextStub(PdfTestResources.DEFAULT_FILE));
        step.setEncrypted(true);
        assertFailOnExecute(step);
    }
}
