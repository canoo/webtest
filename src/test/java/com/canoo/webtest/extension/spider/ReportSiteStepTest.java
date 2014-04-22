// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension.spider;

import com.canoo.webtest.steps.AbstractStepContainerTest;

public class ReportSiteStepTest extends AbstractStepContainerTest {
	private ReportSiteStep fReportSiteStep;


	protected void setUp() throws Exception {
		super.setUp();
		fReportSiteStep = new ReportSiteStep();
	}

	public void testFile() {
		assertNull(fReportSiteStep.getFile());
		fReportSiteStep.setFile("foo");
		assertEquals("foo", fReportSiteStep.getFile());
	}

	public void testDepth() {
		assertNull(fReportSiteStep.getDepth());
		fReportSiteStep.setDepth("25");
		assertEquals("25", fReportSiteStep.getDepth());
	}
}