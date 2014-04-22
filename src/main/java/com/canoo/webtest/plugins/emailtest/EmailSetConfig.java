// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.util.Map;

import com.canoo.webtest.steps.Step;

/**
 * Sets Email Configuration information.
 *
 * @author Paul King
 * @webtest.step
 *   category="Email"
 *   name="emailSetConfig"
 *   description="Sets Email Configuration information."
 */
public class EmailSetConfig extends Step
{
    private String fServer;
    private String fUsername;
    private String fPassword;
    private String fDelay;
    private String fType = "pop3";

    public String getServer() {
        return fServer;
    }

    /**
     * Sets the email server.
     *
     * @param server The Server domain name (and optional port)
     * @webtest.parameter
     *   required="yes"
     *   description="The email server (including port if required)."
     */
    public void setServer(final String server) {
        fServer = server;
    }

    public String getType() {
        return fType;
    }

    /**
     * Sets the email server type.
     *
     * @param type The server type
     * @webtest.parameter
     *   required="no"
     *   default="pop3"
     *   description="The email server type (only 'pop3' has been tested)."
     */
    public void setType(final String type) {
        fType = type;
    }

    public String getUsername() {
        return fUsername;
    }

    /**
     * Sets the delay to use between email steps.
     *
     * @param delay The delay between operaitons in seconds
     * @webtest.parameter
     *   required="no"
     *   default="0"
     *   description="The delay between email steps in seconds - sometimes helps with email servers which are fussy about multiple logins."
     */
    public void setDelay(final String delay) {
        fDelay = delay;
    }

    public String getDelay() {
        return fDelay;
    }

    /**
     * Sets the email account username.
     *
     * @param username
     * @webtest.parameter
     *   required="yes/no"
     *   description="The email account username. Probably required for your steps to work. Might be set using a system property."
     */
    public void setUsername(final String username) {
        fUsername = username;
    }

    public String getPassword() {
        return fPassword;
    }

    /**
     * Sets the email account password.
     *
     * @param password
     * @webtest.parameter
     *   required="yes/no"
     *   description="The email account password. Probably required for your steps to work."
     */
    public void setPassword(final String password) {
        fPassword = password;
    }

    public void doExecute() throws Exception {
        getContext().put("EmailConfigInfo", new EmailConfigInfo(getServer(), getType(), getUsername(), getPassword(), getDelay()));
    }

    protected void verifyParameters() {
        super.verifyParameters();
        optionalIntegerParamCheck(getDelay(), "delay", true);
    }

    public Map getParameterDictionary() {
        final Map map = super.getParameterDictionary();
        map.put("password", "******"); // don't report password
        return map;
    }
}
