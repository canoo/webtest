package com.canoo.webtest.extension;

import junit.framework.TestCase;

/**
 * Test for {@link VerifyContentRegexPerLineDiff}.
 *
 * @author Marc Guillemot
 */
public class VerifyContentRegexPerLineDiffTest extends TestCase
{
    public void testCompareMessage() 
    {
    	final VerifyContentRegexPerLineDiff diff = new VerifyContentRegexPerLineDiff();
    	final String text1 = "bla bli\nfoo fii\nbali bla";
    	assertEquals(null, diff.produceTextDiffMessage(text1, text1, "reference", "new"));

    	assertEquals(null, diff.produceTextDiffMessage(".*\n.*\n.*", text1, "reference", "new"));
    	assertEquals(null, diff.produceTextDiffMessage("bla.*\n.*\n.* bl.", text1, "reference", "new"));

    	final String expected1 = "--- reference\n"
    		+ "+++ new\n"
    		+ "@@ -1,5 +1,4 @@\n"
			+ " foo fii\n"
			+ " bali bla\n"
			+ "-bli\n"
			+ " bla\n";
    	assertEquals(expected1, diff.produceTextDiffMessage(text1 + "\nbli\nbla", text1 + "\nbla", "reference", "new"));

    	final String expected1b = "--- reference\n"
    		+ "+++ new\n"
    		+ "@@ -1,5 +1,4 @@\n"
			+ " foo fii\n"
			+ " bali bla\n"
			+ " bl.*\n"
			+ "-bla\n";
    	assertEquals(expected1b, diff.produceTextDiffMessage(text1 + "\nbl.*\nbla", text1 + "\nbla", "reference", "new"));
    	
    	final String expected2 = "--- reference\n"
    		+ "+++ new\n"
    		+ "@@ -1,3 +1,3 @@\n"
    		+ " foo fii\n"
    		+ "-bali bla\n"
    		+ "+bali blabla\n";
    	assertEquals(expected2, diff.produceTextDiffMessage(text1, text1 + "bla", "reference", "new"));
    }
}
