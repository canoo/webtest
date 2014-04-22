package com.canoo.webtest.steps.control;

import com.canoo.webtest.steps.AbstractStepContainer;

/**
 * @webtest.step
 *   category="Core"
 *   name="group"
 *   description="This step allows grouping and giving a description to a sequence of nested steps. Any kind of step can be nested."
 */
public class GroupStep extends AbstractStepContainer {

	public void doExecute() throws CloneNotSupportedException {
		executeContainedSteps();
	}
}
