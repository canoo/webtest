// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;

import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.canoo.webtest.engine.StepFailedException;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * @author Unknown
 * @author Marc Guillemot
 * @author Denis N. Antonioli
 * @webtest.step
 */
public abstract class AbstractIdOrLabelTarget extends AbstractTargetAction {
	private static final Logger LOG = Logger.getLogger(AbstractIdOrLabelTarget.class);

	private String fLabel;
	private String fHtmlId;
	private String fXPath;

	/**
	 * @param newLabel The Label to set.
	 * @webtest.parameter required="yes/no"
	 * description="The label of the element to click.
	 * One of 'label', 'htmlid' or 'xpath' must be set."
	 */
	public void setLabel(String newLabel) {
		fLabel = newLabel;
	}

	public String getLabel() {
		return fLabel;
	}

	/**
	 * @param htmlId The HtmlId to set.
	 * @webtest.parameter required="yes/no"
	 * description="The htmlId of the element to click.
	 * One of 'label', 'htmlid' or 'xpath' must be set."
	 */
	public void setHtmlId(final String htmlId) {
		fHtmlId = htmlId;
	}

	public String getHtmlId() {
		return fHtmlId;
	}


	/**
	 * @param xpath The xpath to set.
	 * @webtest.parameter required="yes/no"
	 * description="The xpath of the element to click.
	 * One of 'label', 'htmlid' or 'xpath' must be set."
	 */
	public void setXpath(final String xpath) {
		fXPath = xpath;
	}

	public String getXpath() {
		return fXPath;
	}

	/**
	 * Finds the element in the page according to the properties set on this step
	 *
	 * @param page the page to search in
	 * @return the clickable element, <code>null</code> if not found
	 */
	protected HtmlElement findClickableElement(final HtmlPage page) throws XPathException {
		if (getHtmlId() != null) {
			return findClickableElementById(page);
		}
		if (getXpath() != null) {
			return findClickableElementByXPath(page);
		}
		return findClickableElementByAttribute(page);
	}

	protected abstract HtmlElement findClickableElementByAttribute(HtmlPage page);

	/**
	 * Finds the button with the configured id.
	 *
	 * @param page the page to search in
	 * @return the clickable element, <code>null</code> if no button with this id is found
	 *         or if label and name don't match
	 */
	HtmlElement findClickableElementById(final HtmlPage page) {
		LOG.debug("Looking for html element with id: " + getHtmlId());
		final HtmlElement elt;
		try {
			elt = page.getHtmlElementById(getHtmlId());
		} catch (final ElementNotFoundException e) {
			LOG.info("No html element found with id: " + getHtmlId());
			return null;
		}

		return checkFoundElement(elt);
	}

	/**
	 * Finds the button with the configured xpath.
	 *
	 * @param page the page to search in
	 * @return the button, <code>null</code> if no button with this xpath is found
	 *         or if label and name don't match
	 */
	HtmlElement findClickableElementByXPath(final HtmlPage page) throws XPathException {
		LOG.debug("Looking for html element with xpath: " + getXpath());
		final Object node = HtmlUnitBoundary.trySelectSingleNodeByXPath(getXpath(), page, this);
		LOG.debug("XPath evaluates to: " + node);
		if (node == null) {
			return null;
		}
		if (node instanceof HtmlElement) {
			return checkFoundElement((HtmlElement) node);
		}
		throw new StepFailedException("The xpath doesn't select an HTML element but a '" + node.getClass() + "'");
	}

	/**
	 * Checks that the element is of the desired html type and has the right name and label (if needed)
	 *
	 * @param elt the element to check
	 * @return the element if ok, <code>null</code> if the elment has the correct type has wrong attribute.
	 * @throws StepFailedException If the element has a wrong type.
	 */
	abstract HtmlElement checkFoundElement(HtmlElement elt) throws StepFailedException;
}
