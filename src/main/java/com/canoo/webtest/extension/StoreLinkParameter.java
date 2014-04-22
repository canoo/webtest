package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.store.BaseStoreStep;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Extracts the value of the specified parameter from the URL for a link and stores it as a property.<p>
 *
 * Ex: permits to retrieve "123" or "plouf" in a link
 * with href="toto.jsp?titi=123&tutu=plouf".
 *
 * @author Marc Guillemot
 * @webtest.step
 *   category="Extension"
 *   name="storeLinkParameter"
 *   alias="extractlinkparameter"
 *   description="Extracts the value of the specified parameter in the link and stores it as a property."
 */
public class StoreLinkParameter extends BaseStoreStep {
	private String fHtmlId;
	private String fParameter;

	/* (non-Javadoc)
	 * @see com.canoo.webtest.steps.Step#doExecute(com.canoo.webtest.engine.Context)
	 */
	public void doExecute() throws Exception {
		HtmlElement elt = null;
		try {
			elt = ((HtmlPage) getContext().getCurrentResponse()).getHtmlElementById(fHtmlId);
		} catch (final ElementNotFoundException e) {
			//ignore
		}
		if (elt == null || !(elt instanceof HtmlAnchor)) {
			throw new StepFailedException("No link found with id >" + getHtmlId() + "<", this);
		}
		final HtmlAnchor webLink = (HtmlAnchor) elt;
		final String value = extractParameterValue(webLink.getHrefAttribute(), getParameter());
		if (value == null) {
			throw new StepFailedException("No parameter >" + getParameter() + "< found in link >"
			   + webLink.getHrefAttribute() + "<", this);
		}
		storeProperty(value, getParameter());
	}

	/**
	 * Extracts the first value of the query parameter in the url
	 * @param url the url string
	 * @param parameterName 
	 * @return <code>null</code> if no parameter found with the given name
	 */
	public static String extractParameterValue(final String url, final String parameterName) {
		int iQueryStart = url.indexOf('?');
		if (iQueryStart == -1) {
			return null;
		}
		final String strQuery = url.substring(iQueryStart + 1);
		final String[] tabParamValues = strQuery.split("&");
		for (int i = 0; i < tabParamValues.length; ++i) {
			final String strTmp = tabParamValues[i];
			if (strTmp.startsWith(parameterName + "=")) {
				return strTmp.substring(parameterName.length() + 1);
			}
		}
		return null;
	}

	/**
	 * @param string
	 * @webtest.parameter
	 * 	required="yes"
	 *  description="The id of the html link to extract from."
	 */
	public void setHtmlId(final String string) {
		fHtmlId = string;
	}

	public String getHtmlId() {
		return fHtmlId;
	}

	/**
	 * @param string
	 * @webtest.parameter
	 * 	required="yes"
	 *  description="The name of the parameter whose value should be extracted.
     * If the property name is not specified, the parameter name is used as key to store the value found."
	 */
	public void setParameter(final String string) {
		fParameter = string;
	}

	public String getParameter() {
		return fParameter;
	}

	/**
	 * Checks that required parameters are set
	 */
	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(fHtmlId, "htmlid");
		nullParamCheck(fParameter, "parameter");
		nullResponseCheck();
	}
}
