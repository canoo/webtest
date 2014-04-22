// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.control;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.verify.VerifyXPath;

public class NotStepTest extends BaseWrappedStepTestCase {

	protected Step createStep() {
		return new NotStep();
	}

	public void testSuccessScenario()
	{
		final NotStep step = (NotStep) getStep();
		
		final VerifyXPath nestedStep = new VerifyXPath();
		nestedStep.setProject(step.getProject());
		nestedStep.setXpath("1 < 0");
		
		step.addStep(nestedStep);
		
		step.execute();
	}
}
