// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link StoreHeader}.<p>
 *
 * @author <a href="mailto:paulk at asert dot com dot au">Paul King</a>
 * @author Marc Guillemot
 */
public class StoreHeaderTest extends BaseStepTestCase
{
    private StoreHeader fStep;
    private TestBlock fTestBlock;

    protected Step createStep() {
        return new StoreHeader();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (StoreHeader) getStep();
        fTestBlock = new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        };
    }

    public void testVerifyParameterUsage() {
        fStep.setProperty("someHeaderProp");
        assertStepRejectsNullParam("name", fTestBlock);
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setProperty("someProp");
        fStep.setName("someHeader");
        assertStepRejectsNullResponse(fStep);
    }

    public void testHeaderNotSet() {
        final String name = "nonExistingHeaderName";
        fStep.setName(name);
        fStep.setProperty("myProp");
        assertFailOnExecute(fStep, "Header not defined", "Header \"" + name + "\" not set!");
    }

}
