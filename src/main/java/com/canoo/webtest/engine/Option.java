package com.canoo.webtest.engine;

/**
 * Value object used when setting up HtmlUnit options.
 *
 * @author Dierk K&ouml;nig
 * @webtest.nested
 * category="General"
 * name="option"
 * description="Backdoor to configure HtmlUnit directly."
 */
public class Option extends NameValuePair {
    /**
     * @webtest.parameter
     * required="yes"
     * description="The name of the option, e.g. <example>ThrowExceptionOnFailingStatusCode</example>,
    <example>ThrowExceptionOnScriptError</example> or <example>JavaScriptEnabled</example>."
     */
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * @webtest.parameter
     * required="yes"
     * description="The option value. e.g. <example>false</example>"
     */
    public void setValue(String value) {
        super.setValue(value);
    }
}