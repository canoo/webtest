// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.extension.StoreElementAttribute;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Clicks an html element determined by its id or xpath.<p>
 *
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step category="Extension"
 * name="clickElement"
 * alias="click"
 * description="Clicks an <key>HTML</key> element identified by its <em>id</em> or <em>xpath</em>. If the click triggers the load of a new page, then this page becomes the current one."
 */
public class ClickElement extends AbstractTargetAction {
	private static final Logger LOG = Logger.getLogger(ClickElement.class);
	private String fHtmlId;
	private String fXPath;

	public String getHtmlId() {
		return fHtmlId;
	}

	/**
	 * Sets the id attribute of the element to click.<p>
	 *
	 * @param str the new value
	 * @webtest.parameter required="yes/no"
	 * description="The id of the html element to click on. One of <em>htmlId</em> or <em>xPath</em> must be set."
	 */
	public void setHtmlId(final String str) {
		fHtmlId = str;
	}

	public String getXpath() {
		return fXPath;
	}

	/**
	 * Sets the XPath used to identify the element to click.<p>
	 *
	 * @param path the new value
	 * @webtest.parameter required="yes/no"
	 * description="The XPath identifying the html element to click on. One of <em>htmlId</em> or <em>xPath</em> must be set."
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

	protected Page findTarget() throws IOException {
        return findElement().click();
	}

	protected HtmlElement findElement() {
		final HtmlPage currentResp = getContext().getCurrentHtmlResponse(this);
		final HtmlElement element = StoreElementAttribute.findElement(currentResp, getHtmlId(), getXpath(), LOG, this);

		// does this check make sense? This is a replacement of a check for deprecated HtmlUnit class ClickableElement
		if ("head".equals(element.getParentNode().getNodeName())) {
			throw new StepFailedException("Element is not clickable (" + element + ")", this);
		}
		
		// check that element is displayed if needed
		if (getContext().getConfig().isRespectVisibility() && !element.isDisplayed()) {
			throw new StepFailedException("Element is not displayed and can't be clicked (" + element + ")");
		}
		
		return element;
	}

	protected String getLogMessageForTarget() {
		return "by clickElement with " + ((getXpath() == null) ? "id: " + getHtmlId() : "xpath: " + getXpath());
	}

}
