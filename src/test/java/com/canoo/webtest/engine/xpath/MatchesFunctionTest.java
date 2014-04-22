// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine.xpath;

import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;

/**
 * Tests for {@link MatchesFunction}.
 * @author Marc Guillemot
 */
public class MatchesFunctionTest extends TestCase
{
	private final MatchesFunction fMatchesFunction = new MatchesFunction();

	public void testMatches() throws Exception
	{
		assertTrue(fMatchesFunction.doExecute("", "", null));
		assertTrue(fMatchesFunction.doExecute("", "", null));

		assertTrue(fMatchesFunction.doExecute("abracadabra", "bra", null));
		assertTrue(fMatchesFunction.doExecute("abracadabra", "^a.*a$", null));
		assertFalse(fMatchesFunction.doExecute("abracadabra", "^bra", null));

		final String xpathFunctionExample = "Kaum hat dies der Hahn gesehen,\n"
			+ "Fängt er auch schon an zu krähen:\n" 
			+ "«Kikeriki! Kikikerikih!!»\n"
			+ "Tak, tak, tak! - da kommen sie.\n" + "\n";
		assertFalse(fMatchesFunction.doExecute(xpathFunctionExample, "Kaum.*krähen", null));
		assertTrue(fMatchesFunction.doExecute(xpathFunctionExample, "Kaum.*krähen", "s"));
		assertTrue(fMatchesFunction.doExecute(xpathFunctionExample, "^Kaum.*gesehen,$", "m"));

		assertFalse(fMatchesFunction.doExecute(xpathFunctionExample, "^Kaum.*gesehen,$", null));

		assertTrue(fMatchesFunction.doExecute("kiki", "i", null));
		
		testThrows("kiki", "i", "some non existing flags");
	}

	protected void testThrows(final String input, final String regex, final String flagsString)
	{
		ThrowAssert.assertThrows(TransformerException.class, new TestBlock()
			{
				public void call() throws Throwable
				{
					fMatchesFunction.doExecute(input, regex, flagsString);
				}
			});
	}
}
