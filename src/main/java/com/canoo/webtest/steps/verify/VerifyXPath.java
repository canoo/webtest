// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import javax.xml.xpath.XPathException;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.xpath.XPathHelper;
import com.gargoylesoftware.htmlunit.Page;

/**
 * <p>A webtest verify step that evaluates XPath expressions.</p>
 *
 * <p>This step can either simply verify that an xpath is true, or that it matches something in particular. In keeping
 * with XSLT parlance, the <i>test</i> attribute chooses the first mode and the <i>select</i> attribute chooses the
 * second. If the <i>select</i> attribute exists, a <i>value</i> attribute must also exist which contains the expected
 * value.</p>
 *
 * @author <a href="balld@webslingerz.com">Donald Ball</a>, Dierk König, Carsten Seibert
 * @author Marc Guillemot
 * @since Oct 2002
 * @webtest.step
 *   category="Core"
 *   name="verifyXPath"
 *   alias="verifyxpath"
 *   description="This step verifies that an <key>XPATH</key> expression is true or has a certain value. Useful for <key>XML</key> and <key>HTML</key> pages."
 */

public class VerifyXPath extends AbstractVerifyTextStep {
	private String fXpath;

	{
		setOptionalText(true);
		setOptionalPreviousPage(true);
	}

	/**
	 * @param text
	 * @webtest.parameter
	 * 	 required="no"
	 *   description="The expected text value of the xpath evaluation.
	 *   If omitted the step checks that the result is not the boolean false and not empty."
	 */
	public void setText(String text) {
		super.setText(text);
	}

	public String getXpath() {
		return fXpath;
	}

	/**
	 * @webtest.parameter
	 * 	required="true"
	 *  description="Specifies the <key>XPATH</key> expression to evaluate."
	 */
	public void setXpath(final String xpath) {
		fXpath = xpath;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(fXpath, "xpath");
	}

	public void doExecute() throws XPathException
	{
		verifyXPath();
	}

	protected boolean isComparingPathAndValue() {
		return getText() != null;
	}

	protected void verifyXPath() throws XPathException, StepFailedException {
		final Page currentResponse = getContext().getCurrentResponse();
		final XPathHelper xpathHelper = getContext().getXPathHelper();

		if (isComparingPathAndValue()) {
			final String actualValue = xpathHelper.stringValueOf(currentResponse, getXpath());
			if (!verifyText(actualValue)) {
				throw new StepFailedException("Wrong result for xpath >" + fXpath + "<", getText(), actualValue, this);
			}
		}
		else
		{
			final Object singleNode = xpathHelper.selectFirst(currentResponse, getXpath());
			if (singleNode == null) {
				throw new StepFailedException("xpath test: " + fXpath + " matched no nodes", this);
			}
			else if (Boolean.FALSE.equals(singleNode))
			{
				throw new StepFailedException("xpath test: " + fXpath + " evaluates to false", this);
			}
		}
	}
}
