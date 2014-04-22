// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

/**
 * Base class for steps which update a named field within a form.
 *
 * @author Marc Guillemot
 * @author Paul King
 */
public abstract class AbstractSetNamedFieldStep extends AbstractSetFieldStep {
	protected static final String DEFAULT_VALUE_NULL_MESSAGE = "Attribute \"value\" must be set or inner text must be supplied!";
	private String fValue;

	/**
	 * Set the value
	 *
	 * @param value
	 * @webtest.parameter
	 * 	 required="yes"
	 *   description="The value to use when setting the field of interest."
	 */
	public void setValue(final String value) {
		fValue = value;
	}

	public String getValue() {
		return fValue;
	}

	protected boolean isValueNull() {
		return fValue == null;
	}

	/**
	 * Called by ant to set the text contained in the tag.
	 * An alternative to value="blabla" for e.g. Large TextAreas. Usage:<br/>
	 * &lt;setInputField>blabla<br/>
	 * blibli<br/>
	 * &lt;/setInputField>
	 *
	 * @param str the text value to add
     * @webtest.nested.parameter
     *    required="no"
     *    description="An alternative to the attribute value for e.g. large TextAreas."
     */
	public void addText(final String str) {
		final String strExpanded = getProject().replaceProperties(str);
		if (fValue == null) {
			setValue(strExpanded);
		} else {
			setValue(fValue + strExpanded);
		}
	}
}
