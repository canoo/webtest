// Copyright © 2004-2005 ASERT.
// Parts Copyright © 2005 Canoo Engineering AG, Switzerland.
// Released under the Canoo Webtest license.
package com.canoo.webtest.steps.store;

import org.apache.commons.lang.StringUtils;

import com.canoo.webtest.interfaces.IComputeValue;
import com.canoo.webtest.steps.Step;

/**
 * Base store step offering common functionalities for concrete implemetations
 * Either ant or dynamic properties are supported. The property can
 * be checked subsequently with \"verifyProperty\".
 *
 * @author Marc Guillemot
 */
public abstract class BaseStoreStep extends Step implements IComputeValue {
    private String fPropertyName;
    private String fPropertyType;
    private String fComputedValue;

    /**
     * Sets the Name of the Property.<p>
     *
     * @param name The Property Name
     * @webtest.parameter required="no"
     * description="The name of the property in which to store the retrieved value."
     */
    public void setProperty(final String name) {
        fPropertyName = name;
    }

    public String getProperty() {
        return fPropertyName;
    }

    /**
     * Sets the Type of the Property.<p>
     *
     * @param type The Property type
     * @webtest.parameter required="no"
     * description="The type of the property in which to store the retrieve value. 
     * Either \"ant\" or \"dynamic\"."
     * default="the \"defaultPropertyType\" as specified in the \"config\" element is used."
     */
    public void setPropertyType(final String type) {
        fPropertyType = type;
    }

    public String getPropertyType() {
        return fPropertyType;
    }

    /**
     * Stores the property value
     * @param value the value to store
     * @param defaultName the name to use to store the property if no
     * property name is configured
     */
    protected void storeProperty(final String value, final String defaultName)
    {
    	fComputedValue = value;
    	final String propertyName = StringUtils.defaultIfEmpty(getProperty(), defaultName);
    	setWebtestProperty(propertyName, value, getPropertyType());
    }

    /**
     * Stores the property value
     * @param value the value to store
     */
    protected void storeProperty(final String value)
    {
    	fComputedValue = value;
    	setWebtestProperty(getProperty(), value, getPropertyType());
    }

	public String getComputedValue() {
		return fComputedValue;
	}

    public boolean isPerformingAction() {
    	return false;
    }
}