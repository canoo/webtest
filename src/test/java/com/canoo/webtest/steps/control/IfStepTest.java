// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.control;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.StepStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;

/**
 * @author Paul King
 * @author Gerald Klopp
 * @since <pre>Jun 2005</pre>
 */
public class IfStepTest extends BaseWrappedStepTestCase {
    private IfStep fStep;
    private GroupStep fTestStepOk;
    private GroupStep fTestStepFails;

    protected Step createStep() {
        return new IfStep();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (IfStep) getStep();

        Step aStep = new StepStub();
        configureStep(aStep);

        NotStep notStep = new NotStep();
        configureStep(notStep);
        notStep.addStep(aStep);

        fTestStepOk = new GroupStep();
        configureStep(fTestStepOk);

        fTestStepFails = new GroupStep();
        configureStep(fTestStepFails);
        fTestStepFails.addStep(notStep);
    }

    public void testParameters() {
        TestBlock b = new TestBlock() {
            public void call() throws Throwable {
                executeStep(getStep());
            }
        };
        String message = ThrowAssert.assertThrows(StepExecutionException.class, b);
        assertEquals("One of the 'test' or the 'unless' attributes or nested tags is required.", message);

        fStep.setTest("true");
        fStep.setUnless("true");
        message = ThrowAssert.assertThrows(StepExecutionException.class, b);
        assertEquals("Only one of the 'test' or the 'unless' attributes or nested tags allowed.", message);

        fStep.setTest("true");
        fStep.setUnless(null);
        fStep.addCondition(fTestStepOk);
        message = ThrowAssert.assertThrows(StepExecutionException.class, b);
        assertEquals("Only one of the 'test' or the 'unless' attributes or nested tags allowed.", message);
    }

    public void testRunNestedTests() {
        assertRunNested("true", null, null, true);
        assertRunNested("false", null, null, false);
        assertRunNested(null, "true", null, false);
        assertRunNested(null, "false", null, true);
        assertRunNested(null, null, fTestStepOk, true);
        assertRunNested(null, null, fTestStepFails, false);
    }

    private void assertRunNested(final String test, final String unless, final GroupStep testStep, final boolean expected) {
        fStep.setTest(test);
        fStep.setUnless(unless);
        fStep.addCondition(testStep);
        assertEquals(expected, fStep.runNestedTests());
    }

    public void testAccessorCondition() {
        assertNull(fStep.getCondition());
        fStep.addCondition(fTestStepOk);
        assertSame(fTestStepOk, fStep.getCondition());
    }
}
