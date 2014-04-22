// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;


import javax.xml.xpath.XPathException;

import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;


/**
 * Tests for {@link StoreXPath}.
 * @author	Walter Rumsby (wrumsby@netscape.net)
 * @version	1.0, 29/10/2002
 * @author Marc Guillemot
 * @version 3.0, 11/08/2003
 * @author Denis N. Antonioli
 */
public class StoreXPathTest extends BaseStepTestCase {
	private StoreXPath fStep;
	public static final String PROPERTY_NAME = "result";

	protected void setUp() throws Exception {
		super.setUp();

		fStep = (StoreXPath) getStep();
		fStep.setProperty(PROPERTY_NAME);
	}

	private static final String DOCUMENT = "<html><body><form><input type=\"hidden\" id=\"id\" value=\"1\"/><input/></form></body></html>";

	protected Step createStep() {
		return new StoreXPath();
	}

	public void testHandleHtmlPage() throws Exception {
		String document = "<html><head></head><body><h1>hello</h1></body></html>";
		getContext().setDefaultResponse(document);

		fStep.setXpath("/html/body/h1");
		executeStep(fStep);
		assertEquals("hello", fStep.getComputedValue());
	}

	public void testHandleXmlPage() throws Exception {
		getContext().setDefaultResponse("<xml><body><h1>hello</h1></body></xml>", "text/xml");

		fStep.setXpath("/xml/body/h1");
		executeStep(fStep);
		assertEquals("hello", fStep.getComputedValue());
	}

	/**
	 * Tests on an XmlPage containing an badly formed xml document
	 * @throws Exception if the test fails
	 */
	public void testHandleInvalidXmlPageIllegalChar() throws Exception {
		getContext().setDefaultResponse("<xml type='foo & fii'></xml>", "text/xml");
		fStep.setXpath("/xml");
		assertFailOnExecute(fStep, "", "");
	}

	public void testHandleInvalidXmlPageNotWellFormed() throws Exception {
		getContext().setDefaultResponse("<xml></html>", "text/xml");
		fStep.setXpath("/xml");
		assertFailOnExecute(fStep, "", "");
	}

	public void testHandleMissingPage() throws Exception {
		getContext().fakeLastResponse(null);
		fStep.setXpath("'valeur'='value'");
		executeStep(fStep);
		assertEquals("false", fStep.getComputedValue());

		fStep.setXpath("/not/here");
		assertThrowOnExecute(fStep, "", "", XPathException.class);

		fStep.setXpath("/");
		assertThrowOnExecute(fStep, "", "", XPathException.class);
	}

	public void testHandleUnknownPage() {
		getContext().setDefaultResponse("hello", "text/plain");

		fStep.setXpath("/html/head/title");
		assertFailOnExecute(fStep, "", "");
	}


	public void testHandleMalformedXmlPage() {
		getContext().setDefaultResponse("hello", "text/xml");

		fStep.setXpath("/html/head/title");
		assertFailOnExecute(fStep, "", "");
	}

	public void testVerifyParameters() throws Exception {
		fStep.setXpath("some xpath");
		fStep.setProperty(null);
		assertStepRejectsEmptyParam("property", getExecuteStepTestBlock());

		fStep.setXpath(null);
		fStep.setProperty(PROPERTY_NAME);
		assertStepRejectsNullParam("xpath", getExecuteStepTestBlock());
	}

	public void testExceptionIfNoMatch() throws Exception {
		getContext().setDefaultResponse(DOCUMENT);

		fStep.setXpath("/html/foot");
		assertFailOnExecute(fStep, "No match for xpath expression", "");

		// no exception if default value is provided
		fStep.setDefault("bla");
		fStep.execute();
	}

	public void testStringExpressionInMatch() throws Exception {
		getContext().setDefaultResponse(DOCUMENT);

		fStep.setXpath("count(/html)");

		ThrowAssert.assertPasses("xpath evaluates to string - not node", getExecuteStepTestBlock());
		assertEquals("1", fStep.getWebtestProperty(PROPERTY_NAME));
	}

	/**
	 * This test proves that there is a bug now.
	 * It will fail after the installation of a newer, correct version of jaxen.
	 * @throws Exception if the test fails
	 */
	public void testWT52() throws Exception {
		String document
		   = "<html><body>"
		   + "<p><a href='no'><img src='no.gif'></a></p>"
		   + "<p><a href='yes'><img src='yes.gif'></a></p>"
		   + "</body></html> ";
		getContext().setDefaultResponse(document);

		fStep.setXpath("(//a/img[contains(@src,'gif')])[2]/../@href");
		executeStep(fStep);
		assertEquals("yes", fStep.getWebtestProperty(PROPERTY_NAME));
	}

	/**
	 * This test proves that there is a bug now.
	 * It will fail after the installation of a newer, correct version of jaxen.
	 * @throws Exception if the test fails
	 */
	public void testStoreEmtpyString() throws Exception {
		String document
		   = "<html><body>"
		   + "<form><select><option value=''></option>"
		   + "<option value='1'>first</option>"
		   + "<option value='2'>second</option>"
		   + "</form>"
		   + "</body></html> ";
		getContext().setDefaultResponse(document);

		fStep.setXpath("//select/option[1]");
		executeStep(fStep);
		assertEquals("", fStep.getWebtestProperty(PROPERTY_NAME));

		fStep.setXpath("//div[@id='notExisting']");
		fStep.setDefault("bla bla");
		executeStep(fStep);
		assertEquals("bla bla", fStep.getWebtestProperty(PROPERTY_NAME));
	}
}
