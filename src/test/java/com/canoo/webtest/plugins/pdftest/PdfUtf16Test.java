package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

public class PdfUtf16Test extends AbstractBaseVerifyPdfTestCase
{
    protected ContextStub createContext()
    {
    	return new PdfContextStub(PdfTestResources.UTF16_FILE);
    }

	protected Step createStep() {
        return new PdfVerifyTextStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyTextStep step = (PdfVerifyTextStep) getStep();
        step.setText("");
        return step;
	}

    // Test for FontBox BUG 1362857
    // Ensures FontBox PATCH 1490186 is applied
    public void testUtf16File() throws Exception {
        executeStep(getMinimallyConfiguredStep());
    }
}
