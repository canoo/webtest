// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

import junit.framework.TestCase;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.self.TestBlock;

/**
 * Test for {@link com.canoo.webtest.util.Checker}.
 *
 * @author Paul King
 */
public class CheckerTest extends TestCase
{
    public void testTrue() {
        Checker.assertFalse(false, "dummy");
        Checker.assertTrue(true, "dummy");
    }

    public void testFalse() {
        final String errorMessage = "error message";
        String msg = ThrowAssert.assertThrows(RuntimeException.class, new TestBlock() {
            public void call() throws Throwable {
                Checker.assertFalse(true, errorMessage);
            }
        });
        assertEquals(msg, errorMessage);
        msg = ThrowAssert.assertThrows(RuntimeException.class, new TestBlock() {
            public void call() throws Throwable {
                Checker.assertTrue(false, errorMessage);
            }
        });
        assertEquals(msg, errorMessage);
    }

    public void testNonNull() {
        Checker.assertNonNull(this, "I am not a null");
        final String errorMessage = "parameter should not be null";
        String msg = ThrowAssert.assertThrows(RuntimeException.class, new TestBlock()
        {
            public void call() throws Throwable {
                Checker.assertNonNull(null, errorMessage);
            }
        });
        assertEquals(msg, errorMessage);
    }
}
