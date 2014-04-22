// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

public class AbstractVerifyTextTest extends BaseStepTestCase
{

    protected Step createStep() {
        return new AbstractVerifyTextStub();
    }

    public void testNoPreviousPage() throws Exception {
        final AbstractVerifyTextStep step = (AbstractVerifyTextStep) getStep();
        step.setText("dummy");
        assertStepRejectsNullResponse(step);
    }

    public void testDoExecuteForCoverage() throws Exception {
        new AbstractVerifyTextStub().doExecute();
    }

    public void testAllVerificationsShouldCallVerifyTextParameter() {
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(getStep());
            }
        });
    }

    protected static class AbstractVerifyTextStub extends AbstractVerifyTextStep
    {
        public void doExecute() {
        }

        // to get it into the list of declared methods
        public void setText(String newText) {
            super.setText(newText);
        }

    }
}
