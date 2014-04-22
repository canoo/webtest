// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Test class for {@link StoreElementAttribute}.
 * @author Paul King
 */
public class StoreElementAttributeTest extends BaseStepTestCase {
	private static final Logger LOG = Logger.getLogger(StoreElementAttributeTest.class);

	private StoreElementAttribute fStep;

	protected Step createStep() {
		return new StoreElementAttribute();
	}

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (StoreElementAttribute) getStep();
	}

	public void testVerifyParameterUsage() {
		// <storeElementAttribute />
		String message = ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock());
		assertEquals("\"htmlId\" or \"xPath\" must be set!", message);

		// <storeElementAttribute htmlId="X" xPath="Y" />
		fStep.setHtmlId("X");
		fStep.setXpath("Y");
		message = ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock());
		assertEquals("Only one from \"htmlId\" and \"xPath\" can be set!", message);

		fStep.setXpath(null);
		assertStepRejectsNullParam("property", getExecuteStepTestBlock());
		fStep.setProperty("someName");
		assertStepRejectsNullParam("attributeName", getExecuteStepTestBlock());
	}

	public void testVerifyParametersWithoutPreviousPage() {
		fStep.setHtmlId("someId");
		fStep.setProperty("someName");
		fStep.setAttributeName("someName");
		assertStepRejectsNullResponse(fStep);
	}

	/**
	 * Test deprecated methods
	 * @deprecated
	 */
	public void testDeprecatedAttributes()
	{
		assertNull(fStep.getProperty());
		fStep.setPropertyName("foo");
		assertEquals("foo", fStep.getProperty());
	}

	public void testNonHtmlUsingHtmlId() {
		getContext().setDefaultResponse("", "text/plain");
		fStep.setHtmlId("someId");
		fStep.setProperty("someName");
		fStep.setAttributeName("someName");
		ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock());
	}

	public void testFindElement() {
		Page page = getDummyPage("<html><body><img id='image'><p></body></html>");
		assertEquals("img", StoreElementAttribute.findElement(page, "image", null, LOG, fStep).getNodeName());
		assertEquals("img", StoreElementAttribute.findElement(page, "image", "//p", LOG, fStep).getNodeName());
		assertEquals("p", StoreElementAttribute.findElement(page, null, "//p", LOG, fStep).getNodeName());
	}

	public void testFindElementByXpath() throws XPathException {
		final Page page = getDummyPage("<html><body><img id='image'></body></html>");
		assertEquals("img", StoreElementAttribute.findElementByXpath(page, "//img", LOG, fStep).getNodeName());
		ThrowAssert.assertThrows(StepFailedException.class,
			new TestBlock() {
				public void call() throws Exception {
					StoreElementAttribute.findElementByXpath(page, "//p", LOG, fStep);
				}
			});
		ThrowAssert.assertThrows(StepFailedException.class,
			new TestBlock() {
				public void call() throws Exception {
					StoreElementAttribute.findElementByXpath(page, "//img/@id", LOG, fStep);
				}
			});
	}
	public void testFindElementById()  {
		final Page page = getDummyPage("<html><body><img id='image'></body></html>");
		assertEquals("img", StoreElementAttribute.findElementById(page, "image", LOG, fStep).getNodeName());
		ThrowAssert.assertThrows(StepFailedException.class,
			new TestBlock() {
				public void call() throws Exception {
					StoreElementAttribute.findElementById(page, "pasImage", LOG, fStep);
				}
			});
		ThrowAssert.assertThrows(StepExecutionException.class,
			new TestBlock() {
				public void call() throws Exception {
					StoreElementAttribute.findElementById(getDummyPage("hello", "text/plain"), "pasImportant", LOG, fStep);
				}
			});
	}
}
