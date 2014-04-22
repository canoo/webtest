// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.List;

import com.canoo.webtest.steps.form.AbstractSetNamedFieldStep;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

/**
 * ForceInputFieldAttribute
 *
 * @author Paul King
 * @webtest.step
 *    category="Extension"
 *    name="forceInputFieldAttribute"
 *    alias="emulateSetInputFieldAttribute,setInputFieldAttribute,emulateSetElementAttribute"
 *    description="Sets (or removes) the attribute value of a selected input field. An example usage would be to enable a disabled input field (perhaps to mimic some unsupported <key>javascript</key>)."
 */
public final class ForceInputFieldAttribute extends AbstractSetNamedFieldStep {
	private String fAttributeName;
	private String fAttributeValue;
	private String fTagName = HtmlConstants.INPUT;

	public String getTagName() {
		return fTagName;
	}

	/**
	 * Sets the name of the tag of interest.
	 *
	 * @param name Sets the name of the tag.
	 * @webtest.parameter
	 *   required="no"
	 *   default="input"
	 *   description="The name of the tag of interest, e.g. \"input\" or \"select\"."
	 */
	public void setTagName(final String name) {
		fTagName = name;
	}

	public String getAttributeName() {
		return fAttributeName;
	}

	/**
	 * Sets the name of the attribute of interest.<p>
	 *
	 * @param name Sets the name of the attribute.
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The name of the attribute of interest, e.g. \"disabled\" or \"checked\"."
	 */
	public void setAttributeName(final String name) {
		fAttributeName = name;
	}

	public String getAttributeValue() {
		return fAttributeValue;
	}

	/**
	 * Sets the new value of the attribute of interest.<p>
	 *
	 * @param value Sets the value of the attribute.
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The value to set for the attribute of interest; causes the attribute to be removed if set to the empty string (i.e. \"\")."
	 */
	public void setAttributeValue(final String value) {
		fAttributeValue = value;
	}

	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(), getTagName(), null, getName(), this);
	}

	protected List findFields(final HtmlForm form) {
		return form.getElementsByAttribute(getTagName(), HtmlConstants.NAME, getName());
	}

	protected boolean keepField(HtmlElement elt) {
		// TODO: HtmlInput are not the only element with a value: button, option
		if (!(elt instanceof HtmlInput)) {
			return true;
		}
		return isValueNull() || getValue().equals(((HtmlInput) elt).getValueAttribute());
	}

	/**
	 * Updates a field.
	 *
	 * @param field The field to update
	 */
	protected void setField(HtmlElement field) {
		if ("".equals(getAttributeValue())) {
			field.removeAttribute(getAttributeName());
		} else {
			field.setAttribute(getAttributeName(), getAttributeValue());
		}
	}

	/**
	 * Verifies the parameters.<p>
	 */
	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getAttributeName(), "attributeName");
		nullParamCheck(getAttributeValue(), "attributeValue");
		// todo: check that tag names a valid argument. How??
	}
}
