// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.form.AbstractSetFieldStep;

public abstract class AbstractVerifyFormTest extends BaseStepTestCase {
	private AbstractVerifyFormStep fStep;

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (AbstractVerifyFormStep) getStep();
	}

	public void testNoCurrentResponse() {
		fStep.setName("some name");
		assertStepRejectsNullResponse(fStep);
	}

	public void testMandatoryParams() {
		final Throwable t = ThrowAssert.assertThrows("param == null", StepExecutionException.class, new TestBlock() {
			public void call() throws Exception {
				executeStep(fStep);
			}
		});
		assertEquals(AbstractSetFieldStep.MESSAGE_ARGUMENT_MISSING, t.getMessage());
	}

	public void testRejectsInvalidFieldIndex() {
		fStep.setName("some name");
		fStep.setFieldIndex("non-numeric value");
		String message = ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
			public void call() throws Exception {
				executeStep(fStep);
			}
		});
		assertEquals("Can't parse fieldIndex parameter with value 'non-numeric value' as an integer.", message);
	}
}
