// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * Test helper that captures the strategy of asserting proper Exception throwing in production code.
 */
public class ThrowAssert {

	public static void assertThrows(Class throwable, String exceptionMessage, TestBlock block) {
		assertThrows("", throwable, exceptionMessage, block);
	}

	public static void assertThrows(String message, Class throwable, String exceptionMessage, TestBlock block) {
		final String result = assertThrows(message, throwable, block).getMessage();
		if (!result.startsWith(exceptionMessage)) {
			Assert.fail("The exception message is <" + result + ">, it does not start with <" + exceptionMessage + ">.");
		}
	}

	public static String assertThrows(Class throwable, TestBlock block) {
		return assertThrows("", throwable, block).getMessage();
	}

	public static Throwable assertThrows(String message, Class throwable, TestBlock block) {
		boolean expectedThrowableThrown = false;
        String prefixedMessage = appendPrefix(message);
        Throwable thrown = null;
        try {
			block.call();
		} catch (final Throwable t) {
			thrown = t;
            expectedThrowableThrown = checkException(throwable, t, prefixedMessage);
        }
		Assert.assertTrue(prefixedMessage + "failed to throw " + throwable.getName(), expectedThrowableThrown);
		return thrown;
	}

    private static boolean checkException(Class throwable, Throwable t, String prefixedMessage) {
        boolean expectedThrowableThrown = throwable.isAssignableFrom(t.getClass());
        if (!expectedThrowableThrown) {
            t.printStackTrace(System.out);
            Assert.fail(prefixedMessage + "expected exception " + throwable.getName() + " but was " + t.getClass().getName());
        }
        return expectedThrowableThrown;
    }

    private static String appendPrefix(String message) {
        if (message.length() > 0) return message + " :";
        return message;
    }

    public static void assertPasses(String message, TestBlock block) {
        try {
			block.call();
		} catch (Throwable t) {
            String fullMessage = appendPrefix(message) + "should not have raised exception " + t.getClass().getName();
            AssertionFailedError junitError = new AssertionFailedError(fullMessage);
            junitError.setStackTrace(t.getStackTrace());
            throw junitError;
		}
	}

}
