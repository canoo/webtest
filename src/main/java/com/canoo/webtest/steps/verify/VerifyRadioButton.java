// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.canoo.webtest.engine.StepFailedException;

import java.util.List;
import java.util.Iterator;

/**
 * @author Denis N. Antonioli
 * @webtest.step
 *   category="Core"
 *   name="verifyRadioButton"
 *   description="This step is used to verify the state of a radio button (checked/unchecked). A particular radio button can be specified via <em>name</em> and optionally <em>value</em> (or alternatively by <em>fieldIndex</em>)."
 */
public class VerifyRadioButton extends AbstractVerifyFormStep {
	private String fChecked;

	public String getChecked() {
		return fChecked;
	}

	/**
	 * @webtest.parameter
	 * 	required="yes"
	 *  description="Specifies if the radio button shall be checked (true) or unchecked (false)."
	 */
	public void setChecked(final String checked) {
		fChecked = checked;
	}

	/**
	 * Finds the relevant form.
	 *
	 */
	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(), HtmlConstants.INPUT, HtmlConstants.RADIO, getName(), this);
	}

	/**
	 * Finds then verifies the field of interest.
	 *  @param form
	 */
	protected List findFields(final HtmlForm form) {
		List li = form.getInputsByName(getName());
		// remove those not having the right name, and optionally value
		for (Iterator iter = li.iterator(); iter.hasNext();) {
			final HtmlElement elt = (HtmlElement) iter.next();
			if (elt instanceof HtmlRadioButtonInput) {
				if (getValue() == null || getValue().equals(((HtmlRadioButtonInput) elt).getValueAttribute())) {
					continue;
				}
			}
			iter.remove();
		}
		return li;
	}

	protected void verifyField(final HtmlElement element) {
		HtmlRadioButtonInput radioButton = (HtmlRadioButtonInput) element;
		if (radioButton.isChecked() != ConversionUtil.convertToBoolean(getChecked(), false)) {
			throw new StepFailedException("RadioButton is " + (radioButton.isChecked() ? "" : "not") + "checked!", this);
		}
	}

}
