// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.form.AbstractSetFieldStep;

/**
 * Test class for {@link ForceInputFieldAttribute}.<p>
 *
 * @author Paul King
 */
public class ForceInputFieldAttributeTest extends BaseStepTestCase
{
    private ForceInputFieldAttribute fStep;

	protected Step createStep() {
        return new ForceInputFieldAttribute();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (ForceInputFieldAttribute) getStep();
    }

    public void testVerifyParameterUsage() {
        TestBlock block = new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        };
        // <forceInputFieldAttribute name="someName" attributeName="someName"/>
        fStep.setName("someName");
        fStep.setAttributeName("someName");
        fStep.setAttributeValue(null);
        assertStepRejectsNullParam("attributeValue", block);
        // <forceInputFieldAttribute attributeName="someName" attributeValue="someValue"/>
        fStep.setName(null);
        fStep.setAttributeValue("someValue");
        String msg = ThrowAssert.assertThrows(StepExecutionException.class, block);
        assertTrue(msg.indexOf(AbstractSetFieldStep.MESSAGE_ARGUMENT_MISSING) != -1);
        // <forceInputFieldAttribute name="someName" attributeValue="someValue"/>
        fStep.setAttributeName(null);
        fStep.setName("someName");
        assertStepRejectsNullParam("attributeName", block);
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setAttributeValue("someValue");
        fStep.setAttributeName("someName");
        fStep.setName("someName");
        assertStepRejectsNullResponse(fStep);
    }

}
