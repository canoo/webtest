// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import org.apache.tools.ant.BuildException;

import com.canoo.webtest.steps.Step;

public final class StepFailedException extends WebTestException {

    public StepFailedException(String message) {
		super(message, (Exception) null);
	}

	public StepFailedException(final String message, final String expectedValue, final String actualValue, final Step failedStep) {
		super(message, ". Expected value \"" + expectedValue + "\" but got \"" + actualValue + "\"", failedStep);
		addDetail("expected value", expectedValue);
		addDetail("actual value", actualValue);
	}

	public StepFailedException(final String message, final int expectedValue, final int actualValue) {
		this(message, String.valueOf(expectedValue), String.valueOf(actualValue));
	}
	public StepFailedException(final String message, final String expectedValue, final String actualValue) {
		this(message, expectedValue, actualValue, null);
	}


	public StepFailedException(final String message, final Step failedStep) {
		super(message, failedStep);
	}

	public StepFailedException(final String message, final Exception cause) {
		super(message, cause);
	}

	/**
	 * Indicates if the provided exception is caused by a {@link StepFailedException}.
	 * Even if {@link StepFailedException} extends {@link BuildException} in some cases 
	 * (for instance failed macro) the original {@link StepFailedException} is wrapped
	 * within a {@link BuildException}.
	 * @param e the exception to analyze
	 * @return <code>true</code> if the cause is a {@link StepFailedException}
	 */
	public static boolean isCausedByStepFailedException(final Throwable e) {
        Throwable cause = e;
        while (cause != null)
        {
        	if (cause instanceof StepFailedException)
        		return true;
        	cause = cause.getCause(); 
        }

        return false; 
	}
}
