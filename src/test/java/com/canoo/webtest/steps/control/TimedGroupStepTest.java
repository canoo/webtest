// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.control;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.StepStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;

/**
 * @author Paul King
 * @since <pre>Jun 2005</pre>
 */
public class TimedGroupStepTest extends BaseWrappedStepTestCase {

	protected Step createStep() {
		return new TimedGroupStep();
	}

	public void testInvalidParams() {
		Step containedStep = new StepStub();
		final TimedGroupStep timedGroupStep = (TimedGroupStep) getStep();
		timedGroupStep.addStep(containedStep);
		TestBlock b = new TestBlock() {
			public void call() throws Throwable {
				executeStep(timedGroupStep);
			}
		};
		// no params
		ThrowAssert.assertThrows(StepExecutionException.class, b);
		// both params
		timedGroupStep.setMaxMillis("500");
		timedGroupStep.setMaxSeconds("1");
		ThrowAssert.assertThrows(StepExecutionException.class, b);
		// non integer value for time
		timedGroupStep.setMaxSeconds("non-integer");
		timedGroupStep.setMaxMillis(null);
		ThrowAssert.assertThrows(StepExecutionException.class, b);
		// negative time
		timedGroupStep.setMaxSeconds("-1");
		ThrowAssert.assertThrows(StepExecutionException.class, b);
	}

}
