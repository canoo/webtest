package com.canoo.webtest.engine;

/**
 * Abstraction for Http Headers with name and value.
 *
 * @author Dierk K&ouml;nig
 * @webtest.nested
 * category="General"
 * name="header"
 * description="Specify http headers by name and value"
 */
public class Header extends NameValuePair {
    /**
     * @webtest.parameter required="yes"
     * description="The name of the HTTP header as defined by the HTTP spec., e.g. <example>User-Agent</example>, <example>Accept-Language</example> or <example>Cookie</example>."
     */
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * @webtest.parameter required="yes"
     * description="The header value. Range and format are restricted by the HTTP spec., e.g. <example>Mozilla/4.0</example>, <example>de-ch</example> or <example>cookieName=cookieValue</example>"
     */
    public void setValue(String value) {
        super.setValue(value);
    }
}