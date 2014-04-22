package com.canoo.webtest.plugins.emailtest;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AbstractSelectStep}.
 * @author Marc Guillemot
 */
public class AbstractSelectStepTest extends TestCase {

    public void testDoMatch() {
    	assertTrue(AbstractSelectStep.doMatch("bla", "bla"));
    	assertTrue(AbstractSelectStep.doMatch("", "bla"));
    	assertTrue(AbstractSelectStep.doMatch(null, "bla"));
    	assertTrue(AbstractSelectStep.doMatch("", null));
    	assertTrue(AbstractSelectStep.doMatch(null, null));
    	assertFalse(AbstractSelectStep.doMatch("bla", null));
    	assertFalse(AbstractSelectStep.doMatch("/bla/", null));
    	assertFalse(AbstractSelectStep.doMatch("/bla/", "bli"));
    	assertTrue(AbstractSelectStep.doMatch("/bla/", "bla"));
    	assertTrue(AbstractSelectStep.doMatch("/.*/", "bla"));
    	assertTrue(AbstractSelectStep.doMatch("/.*/", ""));
    	assertTrue(AbstractSelectStep.doMatch("/.*/", null));
    }

    public void testDoMatchMultiple() {
    	assertFalse(AbstractSelectStep.doMatchMultiple("bla", null));
    }
}
