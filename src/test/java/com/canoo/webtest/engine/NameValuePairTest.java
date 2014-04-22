package com.canoo.webtest.engine;

import junit.framework.TestCase;

/**
 * @author Denis N. Antonioli
 */
public abstract class NameValuePairTest extends TestCase {
    private static final String FOO = "webtest";
    private NameValuePair fNameValuePair;

    abstract NameValuePair getNameValuePair();

    protected void setUp() throws Exception {
        super.setUp();
        fNameValuePair = getNameValuePair();
    }

    public void testConstructor() {
        assertNull(fNameValuePair.getName());
        assertNull(fNameValuePair.getValue());
    }

    public void testName() {
        fNameValuePair.setName(FOO);
        assertEquals(FOO, fNameValuePair.getName());
        assertNull(fNameValuePair.getValue());
    }

    public void testValue() {
        fNameValuePair.setValue(FOO);
        assertEquals(FOO, fNameValuePair.getValue());
        assertNull(fNameValuePair.getName());
    }
}