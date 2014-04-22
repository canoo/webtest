// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension.spider;

import junit.framework.TestCase;

public class PatternVisitorStrategyTest extends TestCase {
	private PatternVisitorStrategy fPatternVisitorStrategy;

	protected void setUp() throws Exception {
		super.setUp();
		fPatternVisitorStrategy = new PatternVisitorStrategy("m/followMe/");
	}

	public void testPatternVisitorStrategy() {
		assertTrue(fPatternVisitorStrategy.accept(SpiderTest.newLink("foo?p1=followMe&p2=watchYourStep")));
		assertFalse(fPatternVisitorStrategy.accept(SpiderTest.newLink("foo?")));
		assertFalse(fPatternVisitorStrategy.accept(SpiderTest.newLink("")));
	}
}