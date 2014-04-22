// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.helpers.AttributesImpl;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.InputElementFactory;

/**
 * @author Carsten Seibert, adapted by Dierk Koenig
 * @author Marc Guillemot
 */
public class VerifyCheckboxTest extends AbstractVerifyFormTest
{
    private class VerifyCheckboxTestStub extends VerifyCheckbox
    {
        private int fListSize;
        private boolean fShouldBeChecked;
		private final HtmlPage page = getDummyPage("<html></html>");

        VerifyCheckboxTestStub(int size) {
            this(size, true);
        }

        VerifyCheckboxTestStub(int size, boolean checked) {
            fListSize = size;
            fShouldBeChecked = checked;
        }

        protected HtmlForm findForm() {
            return (HtmlForm) page.createElement("form");
        }

        protected List findFields(HtmlForm form) {
            List<HtmlCheckBoxInput> result = new ArrayList<HtmlCheckBoxInput>(fListSize);
            for (int i = 0; i < fListSize; i++) {
                result.add(getDummyNode());
            }
            return result;
        }

        private HtmlCheckBoxInput getDummyNode() {
            final AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(null, "type", "type", null, "checkbox");
            if (fShouldBeChecked) {
                attributes.addAttribute(null, "checked", "checked", null, String.valueOf(fShouldBeChecked));
            }
            return (HtmlCheckBoxInput) InputElementFactory.instance.createElement(getDummyPage(), "input", attributes);
        }
    }

	protected Step createStep() {
        return new VerifyCheckbox();
    }

    public void testNoCheckboxFound() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(0);
        step.setName("dummy");
        assertThrowsExceptionOnExecute(step, StepFailedException.class);
    }

    public void testTooManyCheckboxesFound() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(2);
        step.setName("dummy");
        step.setChecked("true");
        executeStep(step);
    }

    public void testMultipleCheckboxesWithBadIndex() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(2);
        step.setName("dummy");
        step.setFieldIndex("blah");
        assertThrowsExceptionOnExecute(step, StepExecutionException.class);
    }

    public void testMultipleCheckboxesWithIndex() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(2);
        step.setName("dummy");
        step.setChecked("true");
        step.setFieldIndex("1");
        executeStep(step);
    }

    public void testNotChecked() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(1, false);
        step.setName("X");
        step.setChecked("false");
        executeStep(step);
        // runs without exception
    }

    public void testNotCheckedButExpected() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(1, false);
        step.setName("X");
        step.setChecked("true");
        assertThrowsExceptionOnExecute(step, StepFailedException.class);
    }

    public void testChecked() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(1, true);
        step.setName("X");
        step.setChecked("true");
        executeStep(step);
        // runs without exception
    }

    public void testCheckedButNotExpected() throws Exception {
        VerifyCheckbox step = new VerifyCheckboxTestStub(1, true);
        step.setName("X");
        step.setChecked("false");
        assertThrowsExceptionOnExecute(step, StepFailedException.class);
    }

    private void assertThrowsExceptionOnExecute(final VerifyCheckbox step, Class throwable) {
        ThrowAssert.assertThrows(throwable, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(step);
            }
        });
    }
}
