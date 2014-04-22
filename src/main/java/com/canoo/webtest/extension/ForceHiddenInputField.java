// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.form.AbstractSetNamedFieldStep;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import org.apache.log4j.Logger;

/**
 * Sets the value of a hidden input field.
 *
 * @author Paul King
 * @author groovesoftware
 * @webtest.step
 *    category="Extension"
 *    name="forceHiddenInputField"
 *    alias="setHiddenInputField"
 *    alias="emulateSetHiddenInputField"
 *    description="Sets the value of a hidden input field. An example usage would be for a hidden input field that would normally be set by some unsupported <key>javascript</key>."
 */
public class ForceHiddenInputField extends AbstractSetNamedFieldStep {
	private static final Logger LOG = Logger.getLogger(ForceHiddenInputField.class);

	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(),
			HtmlConstants.INPUT, HtmlConstants.HIDDEN, getName(), this);
	}

	protected boolean keepField(HtmlElement elt) {
		return elt instanceof HtmlHiddenInput;
	}

	protected void setField(final HtmlElement field) {
		if (!(field instanceof HtmlInput)) {
			throw new StepFailedException("Found " + field.getTagName() + " when looking for input", this);
		}
		((HtmlInput) field).setValueAttribute(getValue());
		LOG.debug("Set hidden input field " + field + " to value " + getValue());
	}

	protected void verifyParameters() {
		super.verifyParameters();
		paramCheck(isValueNull(), DEFAULT_VALUE_NULL_MESSAGE);
	}
}
