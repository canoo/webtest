// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Etienne Studer
 */
public class PdfVerifyTextStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyTextStep();
    }

	protected Step getMinimallyConfiguredStep() 
	{
        final PdfVerifyTextStep step = (PdfVerifyTextStep) getStep();
        step.setText("foo");
        return step;
	}

	public void testAttributes() throws IOException {
        assertStepRejectsNullParam("text", getExecuteStepTestBlock());
    }

    public void testCorrectStep() throws Exception {
        PdfVerifyTextStep correctStep = (PdfVerifyTextStep) getStep();
        correctStep.setText("Das Gleiche gilt sinngem\u00e4ss auch f\u00fcr dessen Bevollm\u00e4chtigte.");
        executeStep(correctStep);

        correctStep.setStartPage(2);
        correctStep.setText("Der Kunde best\u00e4tigt hiermit, von der Bank folgende Unterlagen erhalten zu haben:");
        executeStep(correctStep);

        correctStep.setStartPage(2);
        correctStep.setEndPage(2);
        correctStep.setText("Deren Inhalt sowie die von der Bank bekanntgegebenen Konditionen werden ausdr\u00fccklich anerkannt.");
        executeStep(correctStep);

        correctStep.setStartPage(2);
        correctStep.setEndPage(2);
        correctStep.setRegex(true);
        correctStep.setText("Deren Inhalt sowie .+ werden .+ anerkannt.");
        executeStep(correctStep);
    }

    public void testIncorrectStep() throws IOException {
        final PdfVerifyTextStep incorrectStep = (PdfVerifyTextStep) getStep();
        incorrectStep.setStartPage(2);
        incorrectStep.setEndPage(2);
        incorrectStep.setText("Erklärung/Unterschriftsmuster");
        assertFailOnExecute(incorrectStep);
    }

}
