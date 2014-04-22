// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension;

import com.canoo.webtest.self.WebResponseStub;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.verify.AbstractVerifyTextTest;
import com.gargoylesoftware.htmlunit.UnexpectedPage;

/**
 * Unit tests for {@link VerifyDocumentURL}.
 * @author unknown
 * @author Marc Guillemot
 */
public class VerifyDocumentURLTest extends AbstractVerifyTextTest {
	private VerifyDocumentURL fStep;

	protected Step createStep() {
		return new VerifyDocumentURL();
	}

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (VerifyDocumentURL) getStep();
	}

	public void testDoExecute() throws Exception {
		getContext().saveResponseAsCurrent(new UnexpectedPage(WebResponseStub.getDefault(), null));

		fStep.setText("http:");
		fStep.setRegex("true");
		executeStep(fStep);

		fStep.setRegex("false");
		assertFailOnExecute(fStep);
	}

    public void testNestedText() throws Exception {
    	testNestedTextEquivalent(getStep(), "text");
    }
}