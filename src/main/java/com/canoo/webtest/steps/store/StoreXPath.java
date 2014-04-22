// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.xpath.XPathHelper;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Attributes:
 * description   The description of this test step         optional
 * select        The XPath expression to evaluate          mandatory
 * property      The name of the property                  mandatory
 * propertyType  The type of the property                  optional
 *
 * @author Walter Rumsby
 * @author Marc Guillemot
 * @author Paul King
 * @since 1.5
 * @webtest.step
 *   category="Core"
 *   name="storeXPath"
 *   alias="storexpath"
 *   description="This step stores the result of an <key>XPATH</key> expression into a property."
 */
public class StoreXPath extends BaseStoreStep {
	private static final Logger LOG = Logger.getLogger(StoreXPath.class);
	private String fXpath;
	private String fDefault;

	public String getXpath() {
		return fXpath;
	}

	/**
	 * @webtest.parameter
	 * 	required="yes"
	 *  description="The <key>XPATH</key> that shall be evaluated."
	 */
	public void setXpath(final String xpath) {
		fXpath = xpath;
	}

	/**
	 * @webtest.parameter
	 * 	required="no"
	 *  description="The value to store in the property when the xpath evaluation returns no result 
	 *  (if not set, the step will fail if the xpath evaluation returns no result)."
	 */
	public void setDefault(final String defaultValue) {
		fDefault = defaultValue;
	}

	public String getDefault() {
		return fDefault;
	}

	public void doExecute() throws XPathException {
		storeProperty(evaluateXPath());
	}

	protected String evaluateXPath() throws XPathException {
		final Page currentResponse = getContext().getCurrentResponse();
		final XPathHelper xpathHelper = getContext().getXPathHelper();
		final String result = xpathHelper.stringValueOf(currentResponse, getXpath());

		// seems that result is "" and not null when nothing is found
		if (result == null
			|| (result.length() == 0 && xpathHelper.selectFirst(currentResponse, getXpath()) == null)) {

			if (getDefault() == null)
			{
				throw new StepFailedException("No match for xpath expression <" + fXpath + ">", this);
			}
			else
			{
				LOG.debug("No result, using default value");
				return getDefault();
			}
		}

		LOG.debug("Xpath result: " + result);
		return result;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		emptyParamCheck(getProperty(), "property");
		nullParamCheck(getXpath(), "xpath");
	}

}
