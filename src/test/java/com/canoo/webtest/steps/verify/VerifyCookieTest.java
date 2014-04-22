// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.self.WebResponseStub;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.store.StoreCookie;
import com.canoo.webtest.steps.store.StoreCookieTest;
import com.gargoylesoftware.htmlunit.TextPage;
import junit.framework.AssertionFailedError;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test class for {@link VerifyCookie}.<p>
 *
 * @author Denis N. Antonioli
 * @author Paul King
 * @author Marc Guillemot
 */
public class VerifyCookieTest extends BaseStepTestCase {

    private VerifyCookie fVerifyStep;

    protected Step createStep() {
        return new VerifyCookie();
    }

    private void addCookie(final String name, final String value) {
        addCookie(name, value, "/");
    }

    private void addCookie(String name, String value, String path) {
        StoreCookieTest.addCookie(getContext(), name, value, path);
    }

    protected void setUp() throws Exception {
        super.setUp();
        fVerifyStep = (VerifyCookie) getStep();
        assertEquals(0, StoreCookie.getCookies(getContext()).length);
    }

    public void testVerifyParametersWithoutName() {
        assertStepRejectsNullParam("name", new TestBlock() {
            public void call() throws Exception {
                fVerifyStep.setText("some text");
                executeStep(fVerifyStep);
            }
        });
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fVerifyStep.setText("some text");
        fVerifyStep.setName("2046");
        assertStepRejectsNullResponse(fVerifyStep);
    }

    public void testNoCookieSet() {
        final String name = "SessionID";
        fVerifyStep.setName(name);
        fVerifyStep.setText("42");
        assertFailOnExecute(fVerifyStep, "Cookie not defined", "No cookie available!");
    }

    public void testCookieNotSet() {
        final String name = "SessionID";
        addCookie("Not" + name, "2046");
        fVerifyStep.setName(name);
        fVerifyStep.setText("42");
        assertFailOnExecute(fVerifyStep, "Cookie not defined", "Cookie \"" + name + "\" not set for current URL!");
    }

    public void testCookieSetWithWrongValue() {
        final String name = "SessionID";
        final String value = "42";
        addCookie(name, value);
        fVerifyStep.setName(name);
        fVerifyStep.setText("Not " + value);
        assertFailOnExecute(fVerifyStep, "Cookie has other value",
                "Wrong cookie \"" + name + "\" value found!");
    }

    public void testCookieExistence() throws Exception {
        final String name = "SessionID";
        final String value = "42";
        addCookie(name, value);
        fVerifyStep.setName(name);
        executeStep(fVerifyStep);
    }

    public void testCookieSetWithCorrectValue() throws Exception {
        final String name = "SessionID";
        final String value = "42";
        addCookie(name, value);
        fVerifyStep.setName(name);
        fVerifyStep.setText(value);
        executeStep(fVerifyStep);
    }

    public void testCookieWithMoreSpecificPath() throws Exception {
        getContext().fakeLastResponse(new TextPage(new WebResponseStub("some content", new URL("http://localhost/some/path"), 200), null));
        String cookieName = "testcookie";

        addCookie(cookieName, "unspecificValue", "/some/not/related/path");
        fVerifyStep.setName(cookieName);
        try {
            executeStep(fVerifyStep);
            fail("should fail because cookie is not for actual path");
        } catch (Exception e) {

        }

        addCookie(cookieName, "unspecificValue", "/some/path/longer/than/actual");
        fVerifyStep.setName(cookieName);
        try {
            executeStep(fVerifyStep);
            fail("should fail because cookie path is longer than actual path");
        } catch (Exception e) {

        }

        addCookie(cookieName, "unspecificValue", "/");
        fVerifyStep.setName(cookieName);
        fVerifyStep.setText("unspecificValue");
        executeStep(fVerifyStep);

        addCookie(cookieName, "moreSpecificValue", "/some");
        fVerifyStep.setName(cookieName);
        fVerifyStep.setText("moreSpecificValue");
        executeStep(fVerifyStep);

        addCookie(cookieName, "specificValue", "/some/path");
        fVerifyStep.setName(cookieName);
        fVerifyStep.setText("specificValue");
        executeStep(fVerifyStep);
    }

    public void testSelf() {
        // coverage hack
        fVerifyStep.setName("SessionID");
        fVerifyStep.setText("42");
        ThrowAssert.assertThrows("hack", AssertionFailedError.class, new TestBlock() {
            public void call() throws Exception {
                assertFailOnExecute(fVerifyStep, "coverage hack", "wrong message prefix");
            }
        });
    }

}
