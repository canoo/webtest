// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.util.Collections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.canoo.webtest.boundary.UrlBoundary;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;

/**
 * @author Paul King
 * @author Marc Guillemot
 */
public class AbstractTargetActionTest extends BaseStepTestCase
{
	private TargetActionStub fTargetStep;

	protected Step createStep() {
		return new TargetActionStub();
	}

	protected void setUp() throws Exception {
		super.setUp();
		fTargetStep = (TargetActionStub) getStep();
	}

	public void testUnprotectedCall() throws Exception {
		executeStep(fTargetStep);
	}

	public void testProtectedCalls() throws Exception {
        final WebResponseData responseData = new WebResponseData(new byte[]{},
                404, "Not Found", Collections.EMPTY_LIST);
		final WebResponse webResponse = new WebResponse(responseData, UrlBoundary.tryCreateUrl("http://foo"), HttpMethod.GET, 1);
		assertProtectionToStepFailed(new FailingHttpStatusCodeException(webResponse), StepFailedException.class);
		final Logger stepLogger = Logger.getLogger(Step.class);
		final Level oldLevel = stepLogger.getLevel();
		//stepLogger.setLevel(Level.OFF); // dk: we produce an error log on purpose. don't show it here.
		assertProtectionToStepFailed(new RuntimeException("any other"), StepExecutionException.class);
		stepLogger.setLevel(oldLevel);
	}

	private void assertProtectionToStepFailed(final Exception toThrow, final Class expectedException) {
		ThrowAssert.assertThrows(expectedException, new TestBlock()
        {
			public void call() throws Exception {
				fTargetStep.setException(toThrow);
				fTargetStep.execute();
			}
		});
	}

	private static class TargetActionStub extends AbstractTargetAction {
		private Exception fException;
		public void setException(final Exception exception) {
			fException = exception;
		}

		protected Page findTarget() throws Exception {
			if (fException != null) {
				throw fException;
			}
			return null;
		}

		protected String getLogMessageForTarget() {
			return "by stub";
		}
	}
}
