// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link StoreResponseCode}.<p>
 *
 * @author Paul King
 */
public class StoreResponseCodeTest extends BaseStepTestCase
{
    private StoreResponseCode fStep;

	protected Step createStep() {
        return new StoreResponseCode();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (StoreResponseCode) getStep();
    }

    public void testVerifyParameterUsage() {
        assertStepRejectsNullParam("property", new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setProperty("someProp");
        assertStepRejectsNullResponse(fStep);
    }

}
