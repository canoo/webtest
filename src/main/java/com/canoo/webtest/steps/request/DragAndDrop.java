package com.canoo.webtest.steps.request;

import javax.xml.xpath.XPathException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Drags an element to an other one
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="dragAndDrop"
 * description="Drags an element to an other one"
 */
public class DragAndDrop extends Step {
	private static final Logger LOG = Logger.getLogger(DragAndDrop.class);
	private String fFromXPath_;
	private String fToXPath_;
	private String fFromHtmlId_;
	private String fToHtmlId_;

	/**
	 * @return the fFromXPath.
	 */
	public String getFromXPath()
	{
		return fFromXPath_;
	}

	/**
	 * @param _fromXPath the fFromXPath to set
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The xpath allowing to select the element to drag.
	 *   One of fromXPath and fromHtmlId is required." 
	 */
	public void setFromXPath(final String _fromXPath)
	{
		fFromXPath_ = _fromXPath;
	}

	/**
	 * @return the fToXPath.
	 */
	public String getToXPath()
	{
		return fToXPath_;
	}

	/**
	 * @param _toXPath the fToXPath to set
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The xpath allowing to select the element to drop on.
	 *   One of toXPath and toHtmlId is required."
	 */
	public void setToXPath(final String _toXPath)
	{
		fToXPath_ = _toXPath;
	}

	/**
	 * @return the fFromHtmlId.
	 */
	public String getFromHtmlId()
	{
		return fFromHtmlId_;
	}

	/**
	 * @param _fromHtmlId the fFromHtmlId to set
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The id of the html element to drag.
	 *   One of fromXPath and fromHtmlId is required." 
	 */
	public void setFromHtmlId(final String _fromHtmlId)
	{
		fFromHtmlId_ = _fromHtmlId;
	}

	/**
	 * @return the fToHtmlId.
	 */
	public String getToHtmlId()
	{
		return fToHtmlId_;
	}

	/**
	 * @param _toHtmlId the fToHtmlId to set
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The id of the html element to drop on.
	 *   One of toXPath and toHtmlId is required."
	 */
	public void setToHtmlId(final String _toHtmlId)
	{
		fToHtmlId_ = _toHtmlId;
	}

	public void doExecute() throws Exception
	{
		final HtmlPage page = getContext().getCurrentHtmlResponse(this);
		final HtmlElement from = getByXPathOrId("From element", page, getFromXPath(), getFromHtmlId());
		final HtmlElement to = getByXPathOrId("To element", page, getToXPath(), getToHtmlId());

		from.mouseDown();
		to.mouseMove();
		to.mouseUp();
	}

	private HtmlElement getByXPathOrId(final String description, final HtmlPage page, final String xpath, String htmlId) throws XPathException
	{
		final HtmlElement elt;
		if (!StringUtils.isEmpty(htmlId)) {
			try {
				elt = page.getHtmlElementById(htmlId);
			} 
			catch (final ElementNotFoundException e) {
				throw new StepFailedException(description + ": no element found with id >" + htmlId + "<");
			}
		}
		else {
			final Object node = HtmlUnitBoundary.trySelectSingleNodeByXPath(xpath, page, this);
			LOG.debug("XPath >" + xpath + "< evaluates to: " + node);
			if (node == null) {
				throw new StepFailedException(description + ": no element found with xpath >" + xpath + "<");
			}
			if (node instanceof HtmlElement) {
				elt = (HtmlElement) node;
			}
			else {
				throw new StepFailedException("The xpath doesn't select an HTML element but a '" + node.getClass() + "'");
			}
		}
		
		return elt;
	}
	
	protected void verifyParameters()
	{
		nullResponseCheck();
		paramCheck(getFromHtmlId() == null && getFromXPath() == null, "\"fromHtmlId\" or \"fromXPath\" must be set!");
		paramCheck(getFromHtmlId() != null && getFromXPath() != null, "Only one from \"fromHtmlId\" and \"fromXPath\" can be set!");
		
		paramCheck(getToHtmlId() == null && getToXPath() == null, "\"toHtmlId\" or \"toXPath\" must be set!");
		paramCheck(getToHtmlId() != null && getToXPath() != null, "Only one from \"toHtmlId\" and \"toXPath\" can be set!");
	}
}
