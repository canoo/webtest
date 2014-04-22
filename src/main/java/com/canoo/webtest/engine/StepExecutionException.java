// Copyright © 2002 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import com.canoo.webtest.steps.Step;

public class StepExecutionException extends WebTestException {

    /**
     * @param failedStep may only be null for testing purposes *
     */
    public StepExecutionException(final String message, final Step failedStep, final Throwable cause) {
        super(message, failedStep, cause);
    }

    /**
     */
    public StepExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Should be removed
     */
    public StepExecutionException(final String message, final Step failedStep) {
        super(message, failedStep);
    }
}
