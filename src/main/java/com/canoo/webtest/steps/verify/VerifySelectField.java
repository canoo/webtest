// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Verifies that a select form field meets particular criteria
 *
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="verifySelectField"
 *   alias="verifyselectfield"
 *   description="This step verifies whether a particular SELECT - Option Selector exists and is marked as selected. The option selector may be chosen by either its <em>value</em> attribute or the <em>text</em> associated with the selector."
 */
public class VerifySelectField extends AbstractVerifyFormStep {
	private static final Logger LOG = Logger.getLogger(VerifySelectField.class);

	private String fText;
	private String fRegex;

	public String getText() {
		return fText;
	}

	/**
	 * @param text
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The option text. Specification of either <em>text</em> or <em>value</em> is mandatory."
	 */
	public void setText(final String text) {
		fText = text;
	}

	public String getRegex() {
		return fRegex;
	}

	/**
	 * @param regex
	 * @webtest.parameter
	 *   required="no"
	 *   default="'false'"
	 *   description="Specifies whether the text or value represents a <key>regex</key>."
	 */
	public void setRegex(final String regex) {
		fRegex = regex;
	}

	/**
	 * Finds the relevant form.
	 *
	 */
	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(), HtmlConstants.SELECT, null, getName(), this);
	}

	protected List findFields(final HtmlForm form) {
		return form.getSelectsByName(getName());
	}

	protected void verifyField(final HtmlElement field) throws IOException {
		final HtmlSelect curSelect = (HtmlSelect) field;
		for (final Iterator iter = curSelect.getOptions().iterator(); iter.hasNext();) {
			final HtmlOption curOption = (HtmlOption) iter.next();

			if ((getText() == null || verifyStrings(getText(), curOption.asText()))
				&& (getValue() == null || verifyStrings(getValue(), curOption.getValueAttribute()))) {
				LOG.debug("Found corresponding option " + curOption);
				if (curOption.isSelected()) {
					return;
				}
				throw new StepFailedException(getStepLabel() + ": " + buildFailMessage(getValue()), this);
			}
		}

		// if we reach here, we haven't found anything
		throw new StepFailedException("Select option \"" + fText + " : " + getValue()
			+ "\" not found for inputfield: <" + getName() + ">",
			this);
	}

	private boolean verifyStrings(final String expectedValue, final String actualValue) {
		return getVerifier(ConversionUtil.convertToBoolean(getRegex(), false)).verifyStrings(expectedValue, actualValue);
	}

	private String buildFailMessage(final String value) {
		final StringBuffer sb = new StringBuffer();
		sb.append("Select option <");
		sb.append("value:").append(value);
		sb.append(" / text:").append(getText());
		sb.append("> is not selected!");
		return sb.toString();
	}

	protected void verifyParameters() {
		super.verifyParameters();
		paramCheck(isValueNull() && getText() == null, "Required parameter \"text\" or \"value\" not set!");
	}

}
