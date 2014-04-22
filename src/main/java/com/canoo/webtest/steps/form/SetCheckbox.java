// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Provides the ability to check / uncheck HTML checkboxes.
 *
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="setCheckbox"
 * alias="new_setcheckbox,setcheckbox"
 * description="Provides the ability to check and uncheck checkboxes in <key>HTML</key> forms."
 */
public class SetCheckbox extends AbstractSetNamedFieldStep {
	private static final Logger LOG = Logger.getLogger(SetCheckbox.class);
	private String fChecked;

	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(), HtmlConstants.INPUT, HtmlConstants.CHECKBOX, getName(), this);
	}

	protected boolean keepField(final HtmlElement elt) {
		if (!(elt instanceof HtmlCheckBoxInput)) {
			return false;
		}
		return isValueNull() || getValue().equals(((HtmlCheckBoxInput) elt).getValueAttribute());
	}

	/**
	 * Set the value.
	 *
	 * @param value
	 * @webtest.parameter required="no"
	 * description="The value to use to select the desired checkbox. If not present, the first possible checkbox is selected."
	 */
	public void setValue(final String value) {
		super.setValue(value);
	}

	/**
	 * Updates a field.
	 *
	 * @param field The field to update
	 */
	protected void setField(final HtmlElement field) throws IOException {
		if (!(field instanceof HtmlCheckBoxInput)) {
			throw new StepFailedException("Found " + field.getTagName() + " when looking for checkbox", this);
		}
		final boolean checked = isChecked();
		final HtmlCheckBoxInput checkBox = (HtmlCheckBoxInput) field;
		if (checkBox.isChecked() != checked) {
			checkBox.click();
			LOG.debug((checked ? "" : "un") + "checked checkbox " + checkBox);
		}
	}

	/**
	 * Gets the boolean value of the argument {@link #setChecked(String)}.
	 * @return The boolean value.
	 */
	private boolean isChecked() {
		return ConversionUtil.convertToBoolean(getChecked(), true);
	}

	/**
	 * Gets the value of the argument {@link #setChecked(String)}.
	 * @return The value as set by the user.
	 */
	public String getChecked() {
		return fChecked;
	}

	/**
	 * @webtest.parameter required="false"
	 * default="true"
	 * description="Specifies if the checkbox shall be checked (true) or unchecked (false)."
	 */
	public void setChecked(final String checked) {
		fChecked = checked;
	}

}