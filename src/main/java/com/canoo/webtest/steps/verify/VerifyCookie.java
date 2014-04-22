// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.store.StoreCookie;
import com.gargoylesoftware.htmlunit.util.Cookie;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies a cookie value.<p>
 *
 * @author Paul King, ASERT
 * @author Denis N. Antonioli
 * @webtest.step category="Core"
 * name="verifyCookie"
 * description="Provides the ability to check an <key>HTTP</key> Cookie value."
 */
public class VerifyCookie extends AbstractVerifyTextStep {
    private static final Logger LOG = Logger.getLogger(VerifyCookie.class);
    private String fName;

    {
        setOptionalText(true);
    }

    /**
     * Sets the Name of the cookie of interest
     *
     * @param name The new Name name
     * @webtest.parameter required="yes"
     * description="The name of the cookie of interest."
     */
    public void setName(String name) {
        fName = name;
    }

    public String getName() {
        return fName;
    }

    /**
     * @param text The new text
     * @webtest.parameter required="no"
     * description="The text of the cookie of interest. If omitted just checks for existence of the cookie."
     */
    public void setText(String text) {
        super.setText(text);
    }

    public void doExecute() {
        final Cookie[] cookies = StoreCookie.getCookies(getContext());
        LOG.debug("Found " + cookies.length + " cookie(s)");
        if (cookies.length == 0) {
            throw new StepFailedException("No cookie available!", this);
        }
        Cookie mostSpecificMatchingCookie = null;
        String expextedValue = getText();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals(getName())) {
                if (cookie.getPath() != null && getContext().getCurrentResponse().getUrl().toString().contains(cookie.getPath())) {
                    if (mostSpecificMatchingCookie == null || mostSpecificMatchingCookie.getPath().length() < cookie.getPath().length()) {
                        // just check for existence of cookie if no text given
                        if (expextedValue == null) {
                            return;
                        }
                        mostSpecificMatchingCookie = cookie;
                    }
                }
            }
        }

        if (mostSpecificMatchingCookie == null) {
            throw new StepFailedException("Cookie \"" + getName() + "\" not set for current URL!", this);
        }

        if (verifyText(mostSpecificMatchingCookie.getValue())) {
            return;
        }
        throw new StepFailedException("Wrong cookie \"" + getName() + "\" value found!",
                expextedValue, mostSpecificMatchingCookie.getValue(), this);
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getName(), "name");
        nullResponseCheck();
    }
}