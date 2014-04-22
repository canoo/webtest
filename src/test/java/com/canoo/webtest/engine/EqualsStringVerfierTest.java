package com.canoo.webtest.engine;

import junit.framework.TestCase;

public class EqualsStringVerfierTest extends TestCase
{
	public void testVerifyStrings() {
		assertFalse(new EqualsStringVerfier().verifyStrings(null, null));
		assertTrue(new EqualsStringVerfier().verifyStrings("", ""));
	}
}
