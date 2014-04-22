// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.form.AbstractSetFieldStep;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Test class for {@link ForceHiddenInputField}.<p>
 *
 * @author Paul King
 */
public class ForceHiddenInputFieldTest extends BaseStepTestCase
{
    private ForceHiddenInputField fStep;

	protected Step createStep() {
        return new ForceHiddenInputField();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (ForceHiddenInputField) getStep();
    }

	public void testVerifyParameterUsage() {
		TestBlock block = new TestBlock() {
			public void call() throws Exception {
				executeStep(fStep);
			}
		};
		// <ForceHiddenInputField value="someValue"/>
		fStep.setName(null);
		fStep.setValue("someValue");
		String message = ThrowAssert.assertThrows(StepExecutionException.class, block);
		assertTrue(message.indexOf(AbstractSetFieldStep.MESSAGE_ARGUMENT_MISSING) != -1);
		// <ForceHiddenInputField name="someName" />
		fStep.setName("someName");
		fStep.setValue(null);
		// can't use assertStepRejects because of custom message due to inner text possibility
		message = ThrowAssert.assertThrows(StepExecutionException.class, block);
		assertTrue(message.indexOf("\"" + "value" + "\" must be set") != -1);
	}

	public void testVerifyParametersWithoutPreviousPage() {
        fStep.setValue("someValue");
        fStep.setName("someName");
	    assertStepRejectsNullResponse(fStep);
	}

	public void testSetField() {
		final HtmlPage page = getDummyPage("<html></html>");
		ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
			public void call() throws Throwable {
				fStep.setValue("foo");
				fStep.setField(page.createElement("img"));
			}
		});

		final HtmlInput input = (HtmlInput) page.createElement("input");
		fStep.setValue("foo");
		fStep.setField(input);
		assertEquals("foo", input.getValueAttribute());
	}
}
