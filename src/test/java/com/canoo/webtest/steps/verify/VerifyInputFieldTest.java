// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.form.AbstractSetFieldStep;

public class VerifyInputFieldTest extends BaseStepTestCase
{
	/**
     * Factory method
     *
     * @return your class under test
     */
    protected Step createStep() {
        return new VerifyInputField();
    }

    /**
     * Test step-specific logic as needed. <p>
     * Here only the parameter verification is under test.
     * Pls also check the "self" package for further test helpers.
     */
    public void testAttributes() {
        final VerifyInputField step = (VerifyInputField) getStep();
        TestBlock block = new TestBlock()
        {
            public void call() throws Exception {
                executeStep(step);
            }
        };

        step.setValue("some value");
	    String message = ThrowAssert.assertThrows("param == null", StepExecutionException.class, block).getMessage();
	    assertEquals(AbstractSetFieldStep.MESSAGE_ARGUMENT_MISSING, message);
	    
	    step.setName("some name");
        step.setValue(null);
        assertStepRejectsNullParam("value", block);
        step.setValue("some value");
        step.setValue("some value");
        ThrowAssert.assertPasses("name and value set", new TestBlock()
        {
            public void call() throws Exception {
                step.verifyParameters();
            }
        });
    }

}
