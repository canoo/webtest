// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

/**
 * Email Configuration information.
 *
 * @author Paul King
 */
public class EmailConfigInfo
{
    private final String fServer;
    private final String fType;
    private final String fUsername;
    private final String fPassword;
    private final String fDelay;

    public EmailConfigInfo(final String server, final String type, final String username, final String password, final String delay) {
        fServer = server;
        fType = type;
        fUsername = username;
        fPassword = password;
        fDelay = delay;
    }

    public String getServer() {
        return fServer;
    }

    public String getType() {
        return fType;
    }

    public String getUsername() {
        return fUsername;
    }

    public String getPassword() {
        return fPassword;
    }

    public String getDelay() {
        return fDelay;
    }

}