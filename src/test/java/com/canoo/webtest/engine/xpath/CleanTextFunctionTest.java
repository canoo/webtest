// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine.xpath;

import junit.framework.TestCase;

/**
 * Tests for {@link CleanTextFunction}.
 * @author Marc Guillemot
 */
public class CleanTextFunctionTest extends TestCase
{
	private final CleanTextFunction fCleanTextFunction = new CleanTextFunction();

	public void testCleans() throws Exception
	{
		testCleans("foo foo");
		testCleans("  foo\r\nfoo");
		testCleans("\tfoo\ffoo");
		testCleans("\tfoo\ffoo");
		testCleans("  foo\u00A0\ffoo");
	}

	protected void testCleans(final String arg) throws Exception
	{
		assertEquals("foo foo", fCleanTextFunction.doExecute(arg));
	}
}
