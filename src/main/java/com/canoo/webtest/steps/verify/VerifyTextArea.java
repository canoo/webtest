// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.util.Map;

import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import org.apache.log4j.Logger;

/**
 * Verify a textarea in the current response.
 * 
 * @author Marc Guillemot
 * @webtest.step
 *   category="Core"
 *   name="verifyTextarea"
 *   alias="verifytextarea"
 *   description="This step is a \"shortcut\" for <em><verifyElementText type=\"TEXTAREA\" ...></em>."
 */
public class VerifyTextArea extends VerifyElementText {
	private static final Logger LOG = Logger.getLogger(VerifyTextArea.class);

	public VerifyTextArea() {
		setType(HtmlConstants.TEXTAREA);
	}

	/* (non-Javadoc)
	 * @see com.canoo.webtest.steps.verify.VerifyElementText#readText(com.gargoylesoftware.htmlunit.html.HtmlElement)
	 */
	protected String readText(final HtmlElement elt) {
		LOG.debug("Reading text for " + elt);
		return ((HtmlTextArea) elt).getText();
	}

	public Map getParameterDictionary()
	{
		final Map map = super.getParameterDictionary();
		map.remove("type"); // as it is fixed type here
		return map;
	}
}
