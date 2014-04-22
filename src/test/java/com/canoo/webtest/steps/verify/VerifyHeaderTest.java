// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * Tests for {@link VerifyTitle}.
 * @author Marc Guillemot
 */

public class VerifyHeaderTest extends BaseStepTestCase {

	private VerifyHeader fStep;

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (VerifyHeader) getStep();
	}

	protected Step createStep() {
		return new VerifyHeader();
	}

	public void testBasic() throws Exception {
		final List<NameValuePair> responseHeaders = new ArrayList<NameValuePair>();
		responseHeaders.add(new NameValuePair("X-foo", "hello"));
		
		prepareCurrentResponseWithHeaders(responseHeaders);

		fStep.setName("X-foo");
		fStep.setText("hello");
		executeStep(fStep);
		
		fStep.setText("bye bye");
		assertFailOnExecute(fStep, "", "");
	}

	public void testAddText() throws Exception {
		final List<NameValuePair> responseHeaders = new ArrayList<NameValuePair>();
		responseHeaders.add(new NameValuePair("X-foo", "hello"));
		
		prepareCurrentResponseWithHeaders(responseHeaders);

		fStep.addText("X-foo: hello");
		executeStep(fStep);
		
		// ensure values are trimmed
		fStep.addText(" X-foo : hello ");
		executeStep(fStep);

		fStep.addText("X-foo: bye bye");
		final StepFailedException e = (StepFailedException) assertFailOnExecute(fStep, "", "");
		assertEquals("Wrong header value found for header \"X-foo\"!", e.getShortMessage());
	}

	public void testDuplicateHeader() throws Exception {
		final List<NameValuePair> responseHeaders = new ArrayList<NameValuePair>();
		responseHeaders.add(new NameValuePair("X-foo", "hello"));
		responseHeaders.add(new NameValuePair("X-foo", "world"));
		
		prepareCurrentResponseWithHeaders(responseHeaders);

		fStep.addText("X-foo: hello");
		executeStep(fStep);
		
		fStep.addText("X-foo: world");
		executeStep(fStep);
	}

	private void prepareCurrentResponseWithHeaders(final List<NameValuePair> responseHeaders)
			throws MalformedURLException {
		final WebClient webClient = getContext().getWebClient();
		final MockWebConnection conn = ((MockWebConnection) webClient.getWebConnection());
		
		conn.setDefaultResponse("", 200, "OK", "text/plain", responseHeaders);
        HtmlUnitBoundary.tryGetPage(new URL("http://webtest.canoo.com"), webClient); // just to make it the current response
	}

}
