// Copyright © 2004-2005 ASERT.
// Parts Copyright © 2005 Canoo Engineering AG, Switzerland.
// Released under the Canoo Webtest license.
package com.canoo.webtest.steps.store;

import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.MapUtil;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * Stores a cookie value (from the Http Response) into a property.<p>
 * <p/>
 * Either ant or dynamic properties are supported. The property can
 * be checked subsequently with \"verifyProperty\".
 *
 * @author Paul King, ASERT
 * @author Denis N. Antonioli
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="storeCookie"
 * description="Provides the ability to store an <key>HTTP</key> Cookie value for later checking."
 */
public class StoreCookie extends BaseStoreStep {
    private static final Logger LOG = Logger.getLogger(StoreCookie.class);
    private String fCookieName;
    private String fCookieValue; // will hold the value of the found cookie to give it to the report

    /**
     * Sets the Name of the cookie of interest.<p>
     *
     * @param name The cookie Name
     * @webtest.parameter required="yes"
     * description="The name of the cookie of interest.
     * If the property name is not specified, the cookie name is used as key to store the value found."
     */
    public void setName(final String name) {
        fCookieName = name;
    }

    public String getName() {
        return fCookieName;
    }

    public void doExecute() {
        final Cookie[] cookies = getCookies(getContext());
        LOG.debug("Found " + cookies.length + " cookie(s)");
        if (cookies.length == 0) {
            throw new StepFailedException("No cookies set!", this);
        }
        final Cookie cookie = findCookie(cookies);
        if (cookie == null)
            throw new StepFailedException("Cookie \"" + fCookieName + "\" not set!", this);
        else
        {
        	storeProperty(cookie.getValue(), getName());
        	fCookieValue = cookie.getValue();
        }
    }

    /**
     * Adds the value of the found cookie
     */
    protected void addComputedParameters(final Map map) {
    	MapUtil.putIfNotNull(map, "-> cookie value", fCookieValue);
    }
    
    /**
     * Search the cookie with the requested property
     * @param cookies the cookies to search in
     * @return the first cookie found, <code>null</code> if none is found 
     */
    Cookie findCookie(final Cookie[] cookies)
    {
        for (int i = 0; i < cookies.length; i++) {
        	final Cookie cookie = cookies[i];
            if (cookie.getName().equals(fCookieName)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Gets the cookies for the current page of the context
     * @param context the context
     * @return the cookies
     */
    public static Cookie[] getCookies(final Context context) {
        final URL url = context.getCurrentResponse().getUrl();
        final WebClient webClient = context.getWebClient();
        final Set/*<Cookie>*/ cookies = webClient.getCookieManager().getCookies(url);
        return (Cookie[]) cookies.toArray(new Cookie[]{});
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(fCookieName, "name");
        nullResponseCheck();
	}
}