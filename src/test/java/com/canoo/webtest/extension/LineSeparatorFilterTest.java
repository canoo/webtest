// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import junit.framework.TestCase;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

/**
 * LineSeparatorFilter Tester.
 *
 * @author Paul King
 */
public class LineSeparatorFilterTest extends TestCase
{
    private static final String OLD_MAC = "Test\r";
    private static final String UNIX = "Test\n";
    private static final String WINDOWS = "Test\r\n";
    private static final String WINDOWS_0 = "";
    private static final String WINDOWS_1 = "\r\n";
    private static final String WINDOWS_2 = "Test";
    private static final String WINDOWS_3 = "Test\r\nTest";
    private static final String WINDOWS_4 = "Test\r\nTest\r\n";
    private static final String EXPECTED = "Test\n";
    private static final String EXPECTED_0 = "";
    private static final String EXPECTED_1 = "\n";
    private static final String EXPECTED_2 = "Test";
    private static final String EXPECTED_3 = "Test\nTest";
    private static final String EXPECTED_4 = "Test\nTest\n";
    private static final String LINE_SEP_KEY = "line.separator";

    private final String fSavedLineSeparator = System.getProperty(LINE_SEP_KEY);
    private final Step fFilter = new LineSeparatorFilter();

    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty(LINE_SEP_KEY, fSavedLineSeparator);
    }

    public void testAllPlatforms() {
        System.setProperty("line.separator", "\r");
        checkFilter(EXPECTED, OLD_MAC);
        System.setProperty("line.separator", "\n");
        checkFilter(EXPECTED, UNIX);
       System.setProperty("line.separator", "\r\n");
        checkFilter(EXPECTED, WINDOWS);
    }

    private void checkFilter(final String expected, final String target) {
    	final Context context = new ContextStub(target, "text/plain");
    	WebtestTask.setThreadContext(context);
    	fFilter.setProject(context.getWebtest().getProject());
        fFilter.execute();
        assertEquals(expected, context.getCurrentResponse().getWebResponse().getContentAsString());
    }

    public void testBoundaries() {
        System.setProperty("line.separator", "\r\n");
        checkFilter(EXPECTED_0, WINDOWS_0);
        checkFilter(EXPECTED_1, WINDOWS_1);
        checkFilter(EXPECTED_2, WINDOWS_2);
        checkFilter(EXPECTED_3, WINDOWS_3);
        checkFilter(EXPECTED_4, WINDOWS_4);
    }
}
