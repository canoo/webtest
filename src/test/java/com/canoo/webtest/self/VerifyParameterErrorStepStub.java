package com.canoo.webtest.self;

/**
 * @author Denis N. Antonioli
 */
public class VerifyParameterErrorStepStub extends StepStub {
    protected void verifyParameters() {
        paramCheck(true, "Expected failure of parameter verification.");
    }
}
