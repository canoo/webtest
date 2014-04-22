// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;

import java.net.URL;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * Test class for {@link StoreCookie}.<p>
 *
 * @author Denis N. Antonioli
 * @author Paul King, ASERT
 * @author Marc Guillemot
 */
public class StoreCookieTest extends BaseStepTestCase
{
    private StoreCookie fStep;
    private TestBlock fTestBlock;

    protected Step createStep() {
        return new StoreCookie();
    }

    /**
     * Adds a cookie for the domain of the currently loaded page in the context
     * @param context the current context
     * @param name the cookie name
     * @param value the cookie value
     */
    public static void addCookie(final Context context, final String name, final String value) {
        addCookie(context, name, value, "/");
    }

    public static void addCookie(Context context, String name, String value, String path) {
        final URL url = context.getCurrentResponse().getUrl();
        final Cookie cookie = new Cookie(url.getHost(), name, value, path, null, false);
        context.getWebClient().getCookieManager().addCookie(cookie);
    }

    protected void setUp() throws Exception {
        super.setUp();
        assertEquals(0, StoreCookie.getCookies(getContext()).length);
        fStep = (StoreCookie) getStep();
        fTestBlock = new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        };
    }

    public void testVerifyInsufficientParameters() {
        fStep.setProperty("someCookieProp");
        assertStepRejectsNullParam("name", fTestBlock);
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setProperty("somePropertyName");
        fStep.setName("someCookieName");
        assertStepRejectsNullResponse(fStep);
    }

    public void testNoCookieSet() {
        final String name = "SessionID";
        fStep.setName(name);
        fStep.setProperty("myProp");
        fStep.setPropertyType(Step.PROPERTY_TYPE_DYNAMIC);
        assertFailOnExecute(fStep, "Cookie not defined", "No cookies set!");
    }

    public void testCookieNotSet() {
        final String name = "SessionID";
        addCookie(getContext(), "Not" + name, "2046");
        fStep.setName(name);
        fStep.setProperty("myProp");
        assertFailOnExecute(fStep, "Cookie not defined", "Cookie \"" + name + "\" not set!");
    }

}
