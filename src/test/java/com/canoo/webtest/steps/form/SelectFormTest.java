// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.steps.form;

import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Unit tests for {@link SelectForm}.
 * @author unknown
 * @author Marc Guillemot
 */
public class SelectFormTest extends BaseStepTestCase {
	private SelectForm fStep;

	protected Step createStep() {
		return new SelectForm();
	}

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (SelectForm) getStep();
	}

	public void testVerifyParameters() throws Exception {
		assertNull(fStep.getIndex());
		assertNull(fStep.getName());
	}

    public void testNestedText() throws Exception {
    	testNestedTextEquivalent(getStep(), "name");
    }
}