// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.FormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Sets the value of a text field (inputs of type 'text' and 'password' as well as textareas).
 *
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="setInputField"
 *   alias="new_setinputfield,setinputfield"
 *   description="Provides the ability to update text-oriented input fields in <key>HTML</key> forms (supports input fields of type <em>text</em> and <em>password</em> as well as <em>textareas</em>)."
 */
public class SetInputField extends AbstractSetNamedFieldStep {
	private static final Logger LOG = Logger.getLogger(SetInputField.class);

	protected HtmlForm findForm() {
		return FormUtil.findFormForTextField(getContext(), getFormName(), getName(), this);
	}

	/**
	 * Finds all text and password inputs or textareas with the given name in the form.
	 *
	 * @param form The form to search
	 * @return The list of fields with the given name
	 */
	protected List findFields(final HtmlForm form) {
		final List fields = super.findFields(form);
		List textAreaFields = form.getTextAreasByName(getName());
		LOG.debug("Found " + textAreaFields.size() + " textarea field(s)");
		fields.addAll(textAreaFields);
		return fields;
	}

	protected boolean keepField(HtmlElement elt) {
		return (elt instanceof HtmlTextInput) || (elt instanceof HtmlPasswordInput) || (elt instanceof HtmlTextArea);
	}

	protected void setField(final HtmlElement elt) throws IOException {
		elt.focus(); // "stores" value at focus to know if onchange should be triggered when focus lost
		
		if (elt instanceof HtmlInput) {
			// HU doesn't write "on" previous text
			// first remove existing text
			((HtmlInput) elt).setAttribute("value", ""); // and not setValueAttribute() to avoid triggering onchange
			elt.type(getValue());
		}
		else if (elt instanceof HtmlTextArea) {
			// TODO: use type(...) once HU stops firing onchange after each typed letter
			((HtmlTextArea) elt).setText(getValue());
		}
		else {
			throw new StepFailedException("Found " + elt.getTagName() + " when looking for input", this);
		}

		((HtmlPage) elt.getPage()).setFocusedElement(null);
		LOG.debug("Set text for " + elt + " to value " + getValue());
	}

	protected void verifyParameters() {
		super.verifyParameters();
		paramCheck(isValueNull(), DEFAULT_VALUE_NULL_MESSAGE);
	}
}
