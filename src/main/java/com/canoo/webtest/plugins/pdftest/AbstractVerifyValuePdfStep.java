// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

/**
 * @author Paul King
 */
public abstract class AbstractVerifyValuePdfStep extends AbstractVerifyPdfStep
{
    private String fExists;
    private String fRegex;
    private String fValue;

    protected AbstractVerifyValuePdfStep() {
        fRegex = null;
        fValue = null;
        fExists = null;
    }

    /**
     * @param exists
     * @webtest.parameter required="yes/no"
     * description="Specifies whether the property/field of interest is expected to exist or not. 
     * Must not be set if 'value' attribute is set."
     */
    public void setExists(String exists) {
        fExists = exists;
    }

    public String getExists() {
        return fExists;
    }

    /**
     * @param regex
     * @webtest.parameter required="no"
     * default="'false'"
     * description="Specifies whether the value represents a <key>regex</key>."
     */
    public void setRegex(String regex) {
        fRegex = regex;
    }

    public String getRegex() {
        return fRegex;
    }

    /**
     * @param value
     * @webtest.parameter required="yes/no"
     * description="The text/<key>regex</key> required to match the property/field of interest. 
     * Must not be set if 'exists' attribute is set."
     */
    public void setValue(String value) {
        fValue = value;
    }

    public String getValue() {
        return fValue;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        paramCheck(fExists == null && fValue == null, "Parameter 'exists' or 'value' is required.");
        paramCheck(fExists != null && fValue != null, "Parameter 'exists' and 'value' are not both allowed.");
    }
}
