// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

public class VerifyElementTextTest extends BaseStepTestCase
{
    private static final String HTML_DOC_HEADER = "<html><head><title>foo</title></head><body>";
    private static final String HTML_DOC_FOOTER = "</body></html>";
    private static final String HTML_DOCUMENT = HTML_DOC_HEADER + " page text <b>1</b>"
            + "<form name='radioform' action='/dummy'>"
            + "  <textarea name='Hugo' cols='80' rows='20'>The very large text area named hugo.</textarea>"
            + "  <textarea name='notext' cols='5' rows='10'></textarea>"
            + "  <textarea id='someId' cols='5' rows='10'>The text area with id someId.</textarea>"
            + "</form>" + "Page text 2"
            + HTML_DOC_FOOTER;

    private VerifyElementText fStep;

    protected static final String ERR_FINGERPRINT = "Wrong contents found in HTML element";

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (VerifyElementText) getStep();
        getContext().setDefaultResponse(HTML_DOCUMENT);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

	protected Step createStep() {
        return new VerifyElementText();
    }

    public void testInsufficientParameters() throws Exception {
        String expectedErrorMessage = "One of 'htmlId' or 'type' must be set.";
        String message = ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Exception {
                fStep.setText("some text");
                executeStep(fStep);
            }
        });
        assertTrue("expected start <" + expectedErrorMessage + "> but was <" + message + ">",
                message.startsWith(expectedErrorMessage));
    }

    public void testTextAreaWithText() throws Exception {
        // <verifyElementText type="textarea" name="Hugo" text="The very large text area named hugo."/>
        fStep.setType("textarea");
        fStep.setName("Hugo");
        fStep.setText("The very large text area named hugo.");
        executeStep(fStep);
    }

    public void testTextAreaWithId() throws Exception {
        // <verifyElementText htmlId="someId" text="The text area with id someId."/>
        fStep.setHtmlId("someId");
        fStep.setText("The text area with id someId.");
        executeStep(fStep);
    }

    public void testNegativeTextAreaWithText() throws Exception {
        // <verifyElementText type="textarea" name="Hugo" text="Deep Blue"/>
        fStep.setType("textarea");
        fStep.setName("Hugo");
        fStep.setText("Deep Blue");
        final String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
        assertTrue(msg.indexOf(ERR_FINGERPRINT) != -1);
    }

    public void testTextAreaWithoutText() throws Exception {
        // <verifyElementText type="textarea" name="notext" text=""/>
        fStep.setType("textarea");
        fStep.setName("notext");
        fStep.setText("");
        executeStep(fStep);
    }

    public void testElementWithSubelements() throws Exception {
        fStep.setType("body");
        fStep.setText("The very large text area named hugo");
        fStep.setRegex("true");
        executeStep(fStep);
    }
}
