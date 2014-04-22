package com.canoo.webtest.engine;

import junit.framework.TestCase;

/**
 * Tests for {@link RegExStringVerifier}.
 * @author Unknown
 * @author Marc Guillemot
 */
public class RegExStringVerifierTest extends TestCase
{
	public void testVerifyStrings() {
		final RegExStringVerifier verifier = new RegExStringVerifier();

		assertFalse(verifier.verifyStrings(null, null));
		assertTrue(verifier.verifyStrings("foo", "foo"));
		assertTrue(verifier.verifyStrings("fo+", "foo"));
		assertTrue(verifier.verifyStrings("fo+", "foo2"));
		assertTrue(verifier.verifyStrings("foo.*foo", "foo1\nfoo2"));
		assertTrue(verifier.verifyStrings("(?i:foo)", "Foo"));
	}
}
