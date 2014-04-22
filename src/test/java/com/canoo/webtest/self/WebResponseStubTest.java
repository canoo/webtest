// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import junit.framework.TestCase;

/**
 * @author unknown
 * @author Marc Guillemot
 */
public class WebResponseStubTest extends TestCase
{

    public WebResponseStubTest(String name) {
        super(name);
    }

    public void testIntendedUsage() {
        WebResponseStub resp = WebResponseStub.getDefault();
        assertNotNull(resp.getHeaderField("Content-type"));
        assertNotNull(resp.getHeaderFields("Content-type"));
        assertEquals(200, resp.getResponseCode());
        assertNotNull(resp.getResponseMessage());
        assertNotNull(resp.getText());
        assertNotNull(resp.toString());
        resp.getHeaders().put("another header", "x");
        assertEquals(2, resp.getHeaderFieldNames().length);
    }
}