// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfVerifyReadOnlyFieldStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyReadOnlyFieldStep();
    }

    public void testAttributes() throws Exception {
        assertErrorOnExecute(getStep());
    }
    
    protected Step getMinimallyConfiguredStep() {
        final PdfVerifyReadOnlyFieldStep step = (PdfVerifyReadOnlyFieldStep) getStep();
        step.setName("foo");
        return step;
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyReadOnlyFieldStep correctStep = (PdfVerifyReadOnlyFieldStep) getStep();
        correctStep.setName("UNTER2");
        correctStep.setReadOnly(true);
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyReadOnlyFieldStep incorrectStep = (PdfVerifyReadOnlyFieldStep) getStep();
        incorrectStep.setName("UNTER");
        incorrectStep.setReadOnly(true);
        assertFailOnExecute(incorrectStep);
    }

}
