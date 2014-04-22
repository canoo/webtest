// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @author Marc Guillemot
 * @webtest.step
 */
public abstract class AbstractVerifyTextStep extends Step {
	/** True if the text attribute is optional. */
	private boolean fOptionalText;
	/** True if the step does not require a previous page available. */
	private boolean fOptionalPreviousPage;
	private String fText;
	private String fRegex;

	public String getText() {
		return fText;
	}

	public boolean isRegex() {
		return ConversionUtil.convertToBoolean(getRegex(), false);
	}

	public String getRegex() {
		return fRegex;
	}

	/**
	 * @param regex
	 * @webtest.parameter
	 * 	 required="no"
	 *   default="'false'"
	 *   description="Specifies whether the text value represents a <key>regex</key>."
	 */
	public void setRegex(final String regex) {
		fRegex = regex;
	}

	/**
	 * @param text
	 * @webtest.parameter
	 * 	 required="yes"
	 *   description="The text value to verify against."
	 */
	public void setText(final String text) {
		fText = text;
	}

	public void setOptionalPreviousPage(final boolean optionalPreviousPage) {
		fOptionalPreviousPage = optionalPreviousPage;
	}

	public void setOptionalText(final boolean optionalText) {
		fOptionalText = optionalText;
	}

    protected boolean verifyStrings(final String expectedValue, final String actualValue) {
        return getVerifier(isRegex()).verifyStrings(expectedValue, actualValue);
    }

    protected boolean verifyText(final String actualValue) {
        return verifyStrings(getText(), actualValue);
    }

	protected void verifyParameters() {
		super.verifyParameters();
        if (!fOptionalText) {
            nullParamCheck(getText(), "text");
        }
        if (!fOptionalPreviousPage) {
            nullResponseCheck();
        }
	}

    public boolean isPerformingAction() {
    	return false;
    }
}
