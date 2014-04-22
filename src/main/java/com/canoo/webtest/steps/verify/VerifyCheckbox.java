// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import java.util.Iterator;
import java.util.List;

/**
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="verifyCheckbox"
 *   alias="verifycheckbox"
 *   description="This step is used to verify the state of a checkbox (checked/unchecked). A particular checkbox can be specified via <em>name</em> and optionally <em>value</em> (or alternatively by <em>fieldIndex</em>)."
 */
public class VerifyCheckbox extends AbstractVerifyFormStep {
	private String fChecked;

	public String getChecked() {
		return fChecked;
	}

	/**
	 * @webtest.parameter
	 * 	required="yes"
	 *  description="Specifies if the checkbox shall be checked (true) or unchecked (false)."
	 */
	public void setChecked(final String checked) {
		fChecked = checked;
	}

	/**
	 * Finds the relevant form.
	 *
	 */
	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(), HtmlConstants.INPUT, HtmlConstants.CHECKBOX, getName(), this);
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
			if (elt instanceof HtmlCheckBoxInput) {
				if (getValue() == null || getValue().equals(((HtmlCheckBoxInput) elt).getValueAttribute())) {
					continue;
				}
			}
			iter.remove();
		}
		return li;
	}

	protected void verifyField(final HtmlElement element) {
		HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) element;
		if (checkbox.isChecked() != ConversionUtil.convertToBoolean(getChecked(), false)) {
			throw new StepFailedException("Checkbox is " + (checkbox.isChecked() ? "" : "not") + "checked!", this);
		}
	}

}
