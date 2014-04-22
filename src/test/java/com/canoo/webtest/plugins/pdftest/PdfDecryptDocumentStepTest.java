// Copyright © 2004-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 */
public class PdfDecryptDocumentStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfDecryptDocumentStep();
    }
	
	protected ContextStub createContext()
	{
		return new PdfContextStub(PdfTestResources.ENCRYPTED_FILE);
	}

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfDecryptDocumentStep step = (PdfDecryptDocumentStep) getStep();
        step.setPassword("foo");
        return step;
	}

    public void testAttributes() throws IOException {
        assertStepRejectsNullParam("password", getExecuteStepTestBlock()); 
    }

    public void testCorrectStep() throws Exception {
        PdfDecryptDocumentStep correctStep = (PdfDecryptDocumentStep) getStep();
        correctStep.setPassword("mymaster");
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws Exception {
        final PdfDecryptDocumentStep incorrectStep = (PdfDecryptDocumentStep) getStep();
        incorrectStep.setPassword("DUMMY");
        assertFailOnExecute(incorrectStep);
    }
}
