package com.canoo.webtest.steps.mouse;

import org.apache.log4j.Logger;

import com.canoo.webtest.extension.StoreElementAttribute;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Base class for mouse events steps.
 *
 * @author Marc Guillemot
 */
public abstract class MouseBaseStep extends Step
{
	private static final Logger LOG = Logger.getLogger(MouseBaseStep.class);
	private String fHtmlId;
	private String fXPath;

	public String getHtmlId() {
		return fHtmlId;
	}

	/**
	 * Sets the id attribute of the element to identify.<p>
	 *
	 * @param str the new value
	 * @webtest.parameter required="yes/no"
	 * description="The id of the HTML element to trigger the event on. One of <em>htmlId</em> or <em>xPath</em> must be set."
	 */
	public void setHtmlId(final String str) {
		fHtmlId = str;
	}

	public String getXpath() {
		return fXPath;
	}

	/**
	 * Sets the XPath used to identify the element to identify.<p>
	 *
	 * @param path the new value
	 * @webtest.parameter required="yes/no"
	 * description="The XPath identifying the HTL element to trigger the event on. One of <em>htmlId</em> or <em>xPath</em> must be set."
	 */
	public void setXpath(final String path) {
		fXPath = path;
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullResponseCheck();
		paramCheck(getHtmlId() == null && getXpath() == null, "\"htmlId\" or \"xPath\" must be set!");
		paramCheck(getHtmlId() != null && getXpath() != null, "Only one from \"htmlId\" and \"xPath\" can be set!");
	}

	
	protected HtmlElement findElement() {
		final HtmlPage currentResp = getContext().getCurrentHtmlResponse(this);
		return StoreElementAttribute.findElement(currentResp, getHtmlId(), getXpath(), LOG, this);
	}

}
