package com.canoo.webtest.engine;

/**
 * Abstraction for name and value.
 *
 * @author Dierk König
 * @author Marc Guillemot
 */
public class NameValuePair {
    private String fName;
    private String fValue;

    public NameValuePair()
    {
    	
    }
    
    public NameValuePair(final String name, final String value)
    {
    	fName = name;
    	fValue = value;
    }

    public String getName() {
        return fName;
    }

    public void setName(final String name) {
        fName = name;
    }

    public String getValue() {
        return fValue;
    }

    public void setValue(final String value) {
        fValue = value;
    }
    
    public String toString()
    {
    	return getName() + ": " + getValue();
    }
}