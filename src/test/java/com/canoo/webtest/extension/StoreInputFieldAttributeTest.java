// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link StoreInputFieldAttribute}.<p>
 *
 * @author Paul King
 * @author Marc Guillemot
 */
public class StoreInputFieldAttributeTest extends BaseStepTestCase
{
    private StoreInputFieldAttribute fStep;

	protected Step createStep() {
        return new StoreInputFieldAttribute();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (StoreInputFieldAttribute) getStep();
    }

	/**
	 * Test deprecated methods
	 * @deprecated
	 */
	public void testDeprecatedAttributes()
	{
		assertNull(fStep.getProperty());
		fStep.setPropertyName("foo");
		assertEquals("foo", fStep.getProperty());
	}

    public void testVerifyParameterUsage() {
        // <storeInputFieldAttributeTest attributeName="someName" name="someName" />
        fStep.setAttributeName("someName");
        fStep.setName("someName");
        fStep.setPropertyName(null);
        assertStepRejectsNullParam("property", getExecuteStepTestBlock());

        // <storeInputFieldAttributeTest attributeName="someName" propertyName="someName" />
        fStep.setName(null);
        fStep.setPropertyName("someName");
        assertStepRejectsNullParam("name", getExecuteStepTestBlock());

        // <storeInputFieldAttributeTest name="someName" propertyName="someName" />
        fStep.setAttributeName(null);
        fStep.setName("someName");
        assertStepRejectsNullParam("attributeName", getExecuteStepTestBlock());

        // bad index
        fStep.setFieldIndex("blah");
        ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock());
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setPropertyName("someName");
        fStep.setAttributeName("someName");
        fStep.setName("someName");
        assertStepRejectsNullResponse(fStep);
    }

}
