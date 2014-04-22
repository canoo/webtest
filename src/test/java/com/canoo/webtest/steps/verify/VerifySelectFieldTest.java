// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * User: marcel
 * Date: 16.07.2003
 */
public class VerifySelectFieldTest extends BaseStepTestCase
{

	protected Step createStep() {
        return new VerifySelectField();
    }

    public void testParameters() throws Exception {
        final VerifySelectField step = (VerifySelectField) getStep();
        step.setName("test");
        assertErrorOnExecute(step, "", "");
    }
}
