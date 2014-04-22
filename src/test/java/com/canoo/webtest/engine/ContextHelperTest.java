// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import com.canoo.webtest.self.LogCatchingTestCase;

import java.io.File;

/**
 * Tests for {@link com.canoo.webtest.engine.ContextHelper}.
 * @author Marc Guillemot
 * @author Paul King
 */
public class ContextHelperTest extends LogCatchingTestCase
{
    /**
     * Tests that no exception is thrown when an IOException occurs but that it gets logged.
     */
    public void testWriteResponseFileIOExceptionHandling() {
        ContextHelper.writeResponseFile(null, new File(""));
        assertTrue(getSpoofAppender().allMessagesToString().indexOf("Failed writing current response to") > -1);
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpCatchLoggerMessages();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        tearDownCatchLoggerMessages();
    }
}
