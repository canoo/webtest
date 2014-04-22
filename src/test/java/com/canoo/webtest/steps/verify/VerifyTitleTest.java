// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Tests for {@link VerifyTitle}.
 * @author Marc Guillemot
 */

public class VerifyTitleTest extends BaseStepTestCase {

    protected Step createStep() {
        return new VerifyTitle();
    }
    
    public void testNestedText() throws Exception {
    	testNestedTextEquivalent(getStep(), "text");
    }
}
