package com.canoo.webtest.extension;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Inheriting your extension step test from BaseStepTestCase allows for some consistency checks
 * that are in jeopardy to be forgotten otherwise.
 */
public class MyCustomStepTest extends BaseStepTestCase
{

	/**
	 * Factory method
	 * @return your class under test
	 */
	protected Step createStep() {
		return new MyCustomStep();
	}

	/**
	 * Test step-specific logic as needed. <p>
	 * Here only the parameter verification is under test.
	 * Pls also check the "self" package for further test helpers.
	 */
	public void testExecute() throws Exception {
		final MyCustomStep myStep = (MyCustomStep) getStep();
		assertStepRejectsNullParam("myMandatoryAttribute", new TestBlock()
		{
			public void call() throws Exception {
				executeStep(myStep);
			}
		});
		myStep.setMyMandatoryAttribute("bla");
		executeStep(myStep);
		// now it works
	}

}