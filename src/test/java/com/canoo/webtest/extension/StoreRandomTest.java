// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link StoreRandom}.<p>
 *
 * @author Paul King
 */
public class StoreRandomTest extends BaseStepTestCase
{
    private StoreRandom fStep;

	protected Step createStep() {
        return new StoreRandom();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (StoreRandom) getStep();
    }

    public void testVerifyParameterUsage() {
        TestBlock block = new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        };
        // <storeRandom />
        assertStepRejectsNullParam("property", block);
        // <storeRandom description="from without to" property="someProp" from="2" />
        fStep.setProperty("someProp");
        fStep.setFrom("2");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom description="to without from" property="someProp" to="3" />
        fStep.setFrom(null);
        fStep.setTo("3");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom description="to smaller than from" property="someProp" from="3" to="2" />
        fStep.setFrom("3");
        fStep.setTo("2");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom description="non-integer from" property="someProp" from="three" to="2" />
        fStep.setFrom("three");
        fStep.setTo("2");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom description="non-integer to" property="someProp" from="3" to="two" />
        fStep.setFrom("3");
        fStep.setTo("two");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom description="negative from" property="someProp" from="-3" to="2" />
        fStep.setFrom("-3");
        fStep.setTo("2");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom description="negative to" property="someProp" from="3" to="-2" />
        fStep.setFrom("3");
        fStep.setTo("-2");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom property="someProp" from="2" to="3" />
        fStep.setFrom("2");
        fStep.setTo("3");
        ThrowAssert.assertPasses("Valid params for random number", block);
        // <storeRandom property="someProp" from="2" length="5" />
        fStep.setFrom("2");
        fStep.setTo(null);
        fStep.setLength("5");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom description="negative length" property="someProp" length="-5" />
        fStep.setFrom(null);
        fStep.setLength("-5");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom property="someProp" length="5" />
        fStep.setLength("5");
        ThrowAssert.assertPasses("Valid params for random string", block);
        // <storeRandom property="someProp" length="5" chars="abc" />
        fStep.setChars("abc");
        ThrowAssert.assertPasses("Valid params for random string", block);
        // <storeRandom property="someProp" choice="a,b" length="5" />
        fStep.setChoice("a,b");
        fStep.setChars(null);
        ThrowAssert.assertThrows(StepExecutionException.class, block);
        // <storeRandom property="someProp" choice="a,b" />
        fStep.setLength(null);
        ThrowAssert.assertPasses("Valid params for random choice", block);
    }

}
