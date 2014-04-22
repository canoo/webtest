// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

/**
 * Utility methods for checking assertions.
 *
 * @author Paul King
 */
public final class Checker
{
    public static void assertFalse(boolean test, String errorMessage) {
        assertTrue(!test, errorMessage);
    }

    public static void assertTrue(boolean test, String errorMessage) {
        if (!test) {
            throw new RuntimeException(errorMessage);
        }
    }

    public static void assertNonNull(Object testObject, String errorMessage) {
        assertTrue(testObject != null, errorMessage);
    }

}
