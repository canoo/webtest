// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.request;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link ClickElementTest}.<p>
 *
 * @author Paul King
 */
public class ClickElementTest extends BaseStepTestCase
{
    private ClickElement fStep;

    private final TestBlock fBlock = new TestBlock()
    {
        public void call() throws Exception {
            executeStep(fStep);
        }
    };

	protected Step createStep() {
        return new ClickElement();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (ClickElement) getStep();
    }

    public void testVerifyParameterUsage() {
        // <clickElement />
        String message = ThrowAssert.assertThrows(StepExecutionException.class, fBlock);
        assertEquals("\"htmlId\" or \"xPath\" must be set!", message);
        // <clickElement htmlId="X" xPath="Y" />
        fStep.setHtmlId("X");
        fStep.setXpath("Y");
        message = ThrowAssert.assertThrows(StepExecutionException.class, fBlock);
        assertEquals("Only one from \"htmlId\" and \"xPath\" can be set!", message);
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setHtmlId("someId");
        assertStepRejectsNullResponse(fStep);
    }
}
