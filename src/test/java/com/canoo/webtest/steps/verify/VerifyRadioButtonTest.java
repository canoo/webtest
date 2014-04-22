// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.helpers.AttributesImpl;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.InputElementFactory;

/**
 * @author Denis N. Antonioli
 */
public class VerifyRadioButtonTest extends AbstractVerifyFormTest
{
    private class VerifyRadioButtonTestStub extends VerifyRadioButton
    {
        private int fListSize;
        private boolean fShouldBeChecked;

        VerifyRadioButtonTestStub(final int size) {
            this(size, true);
        }

        VerifyRadioButtonTestStub(final int size, final boolean checked) {
            fListSize = size;
            fShouldBeChecked = checked;
        }

        protected HtmlForm findForm() {
            return (HtmlForm) getDummyPage().createElement("form");
        }

        protected List findFields(final HtmlForm form) {
        	final List<HtmlRadioButtonInput> result = new ArrayList<HtmlRadioButtonInput>(fListSize);
            for (int i = 0; i < fListSize; i++) {
                result.add(getDummyNode());
            }
            return result;
        }

        private HtmlRadioButtonInput getDummyNode() {
            final AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(null, "type", "type", null, "radio");
            if (fShouldBeChecked) {
                attributes.addAttribute(null, "checked", "checked", null, String.valueOf(fShouldBeChecked));
            }
            return (HtmlRadioButtonInput) InputElementFactory.instance.createElement(getDummyPage(), "input", attributes);
        }
    }

	protected Step createStep() {
        return new VerifyRadioButton();
    }

    public void testNoRadioButtonFound() throws Exception {
        VerifyRadioButton step = new VerifyRadioButtonTest.VerifyRadioButtonTestStub(0);
        step.setName("dummy");
        assertThrowsExceptionOnExecute(step, StepFailedException.class);
    }

    public void testTooManyRadioButtonsFound() throws Exception {
        VerifyRadioButton step = new VerifyRadioButtonTest.VerifyRadioButtonTestStub(2);
        step.setName("dummy");
        step.setChecked("true");
        executeStep(step);
    }

    public void testMultipleRadioButtonsWithBadIndex() throws Exception {
        VerifyRadioButton step = new VerifyRadioButtonTest.VerifyRadioButtonTestStub(2);
        step.setName("dummy");
        step.setFieldIndex("blah");
        assertThrowsExceptionOnExecute(step, StepExecutionException.class);
    }

    public void testMultipleRadioButtonsWithIndex() throws Exception {
        VerifyRadioButton step = new VerifyRadioButtonTest.VerifyRadioButtonTestStub(2);
        step.setName("dummy");
        step.setChecked("true");
        step.setFieldIndex("1");
        executeStep(step);
    }

    public void testNotChecked() throws Exception {
        VerifyRadioButton step = new VerifyRadioButtonTest.VerifyRadioButtonTestStub(1, false);
        step.setName("X");
        step.setChecked("false");
        executeStep(step);
        // runs without exception
    }

    public void testNotCheckedButExpected() throws Exception {
        VerifyRadioButton step = new VerifyRadioButtonTest.VerifyRadioButtonTestStub(1, false);
        step.setName("X");
        step.setChecked("true");
        assertThrowsExceptionOnExecute(step, StepFailedException.class);
    }

    public void testChecked() throws Exception {
        VerifyRadioButton step = new VerifyRadioButtonTest.VerifyRadioButtonTestStub(1, true);
        step.setName("X");
        step.setChecked("true");
        executeStep(step);
        // runs without exception
    }

    public void testCheckedButNotExpected() throws Exception {
        VerifyRadioButtonTestStub step = new VerifyRadioButtonTestStub(1, true);
        step.setName("X");
        step.setChecked("false");
        assertThrowsExceptionOnExecute(step, StepFailedException.class);
    }

    private void assertThrowsExceptionOnExecute(final VerifyRadioButton step, Class throwable) {
        ThrowAssert.assertThrows(throwable, getExecuteStepTestBlock(step));
    }
}
