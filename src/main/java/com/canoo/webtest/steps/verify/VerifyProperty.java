// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.verify;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;

/**
 * Step which verifies the value of a property.<p>
 *
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="verifyProperty"
 *   description="This step verifies whether the value of a webtest dynamic property or an ant property matches an expected text value. The text value can represent a <key>regex</key>. If the text value is omitted, the step simply checks for the presence of the property."
 */
public class VerifyProperty extends AbstractVerifyTextStep {
	private static final Logger LOG = Logger.getLogger(VerifyProperty.class);
	private String fPropName;
	private String fPropertyType;

    {
        setOptionalText(true);
        setOptionalPreviousPage(true);
    }

	public String getName() {
		return fPropName;
	}

	/**
	 * @param text
	 * @webtest.parameter
	 * 	 required="no"
	 *   description="The expected value of the property. 
	 *   If omitted just checks for existence of the property."
	 */
	public void setText(final String text) {
		super.setText(text);
	}

	/**
	 * @param value
	 * @webtest.parameter
	 * 	 required="no"
	 *   description="Alias for <em>text</em>."
	 */
	public void setValue(final String value) {
		super.setText(value);
	}

    public String getValue() {
        return getText();
    }

    /**
	 * Sets the property name to verify
	 *
	 * @param name The Property name
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The name of a property to test. Must be set if <em>property</em> is not set."
	 */
	public void setName(final String name) {
		fPropName = name;
	}

	/**
	 * Sets the property name to verify
	 *
	 * @param name The Property name
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="Alias for <em>name</em>. Must be set if <em>name</em> is not set."
	 */
	public void setProperty(final String name) {
		setName(name);
	}

    public String getProperty() {
        return getName();
    }

    public String getPropertyType() {
		return fPropertyType;
	}

	/**
	 * Sets the property type to verify
	 *
	 * @param type The Property type
	 * @webtest.parameter
	 *   required="no"
	 *   description="The type of the property in which to store the value. Either \"ant\" or \"dynamic\"."
	 *   default="the \"defaultPropertyType\" as specified in the \"config\" element is used."
	 */
	public void setPropertyType(final String type) {
		fPropertyType = type;
	}

	/**
	 * Does the verification work
	 *
	 * @see com.canoo.webtest.steps.Step#doExecute()
	 */
	public void doExecute() {
		if (!getWebtestProperties(getPropertyType()).containsKey(getName())) {
			throw new StepFailedException("Expected property \"" + getName() + "\" to be defined!",
			   this);
		}
		final String propValue = getWebtestProperty(getName(), getPropertyType());
		LOG.debug("propName=" + getName() + ", propertyType=" + getPropertyType() + ", value=" + propValue
		   + ", text=" + getText());

		// null text indicates we are just checking if property is defined
		if (getText() != null && !verifyText(propValue)) {
			throw new StepFailedException("Incorrect property value found!", getText(), propValue,
			   this);
		}
	}

	/** Verifies the parameters */
	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(getName(), "name");
	}
}