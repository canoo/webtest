// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.io.IOException;
import java.util.List;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

/**
 * @author Unknown
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step category="Core"
 * name="verifyInputField"
 * alias="verifyinputfield"
 * description="This step verifies the value of a particular input field. 
 * It is typically used to check that input field defaults are correctly set."
 */
public class VerifyInputField extends AbstractVerifyFormStep {
	private String fRegex;

	public String getRegex() {
		return fRegex;
	}

	/**
	 * @param regex
	 * @webtest.parameter
	 * required="no"
	 * default="'false'"
	 * description="Specifies whether the value represents a <key>regex</key>."
	 */
	public void setRegex(final String regex) {
		fRegex = regex;
	}

	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(), HtmlConstants.INPUT, null, getName(), this);
	}

	protected List findFields(HtmlForm form) {
		return form.getInputsByName(getName());
	}

	protected void verifyField(final HtmlElement field) throws IOException {
		final HtmlInput input = (HtmlInput) field;
		final String fieldContents = input.getValueAttribute();
		if (!verifyStrings(getValue(), fieldContents, getRegex())) {
			throw new StepFailedException("Wrong contents found in input field: " + input, getValue(), fieldContents);
		}

	}

	private static boolean verifyStrings(String expectedValue, String actualValue, String regex) {
		return getVerifier(ConversionUtil.convertToBoolean(regex, false)).verifyStrings(expectedValue, actualValue);
	}

	protected void verifyParameters() {
		super.verifyParameters();
		paramCheck(isValueNull(), "Required parameter \"value\" not set!");
	}

}
