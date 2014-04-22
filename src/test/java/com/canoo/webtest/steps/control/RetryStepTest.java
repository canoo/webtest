// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
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
public class RetryStepTest extends BaseWrappedStepTestCase {

	protected Step createStep() {
		return new RetryStep();
	}

	public void testInvalidParams() {
		final Step containedStep = new StepStub();
		final RetryStep retryStep = new RetryStep();
		retryStep.addStep(containedStep);
		final TestBlock b = new TestBlock() {
			public void call() throws Throwable {
				executeStep(retryStep);
			}
		};
		// no params
		ThrowAssert.assertThrows(StepExecutionException.class, b);
		// negative loop count
		retryStep.setMaxcount("-1");
		ThrowAssert.assertThrows(StepExecutionException.class, b);
		// non-integer loop count
		retryStep.setMaxcount("non-integer");
		ThrowAssert.assertThrows(StepExecutionException.class, b);
	}
}
