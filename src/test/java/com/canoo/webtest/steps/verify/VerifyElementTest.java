// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;


import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;


public class VerifyElementTest extends BaseStepTestCase
{

	protected Step createStep() {
        return new VerifyElement();
    }

    public void testExcecutionFailureScenarios() {
        final VerifyElement plainStep = (VerifyElement) getStep();
        assertStepRejectsNullParam("text", new TestBlock()
        {
            public void call() throws Exception {
                executeStep(plainStep);
            }
        });
        plainStep.setText("text");
        assertStepRejectsNullParam("type", new TestBlock()
        {
            public void call() throws Exception {
                executeStep(plainStep);
            }
        });
        plainStep.setType("type");
        String message = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(plainStep);
            }
        });
        assertTrue("no element yet", message.startsWith("No element of type"));

        final VerifyElement tooManyStep = new VerifyElement()
        {
            protected int getNumberOfHits() {
                return 2;
            }
        };

        tooManyStep.setText("text");
        tooManyStep.setType("type");
        message = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(tooManyStep);
            }
        });
        assertTrue("too many hits", message.startsWith("More than 1 element"));
    }
}

