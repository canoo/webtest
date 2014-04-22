// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import com.canoo.webtest.steps.request.AbstractTargetAction;
import com.gargoylesoftware.htmlunit.Page;
import org.xml.sax.SAXException;

import java.io.IOException;

public class StepStub extends AbstractTargetAction {
	private int fCallCount;

	public void doExecute() {
		fCallCount++;
	}

	protected Page findTarget() throws IOException, SAXException {
		return null;
	}

	protected String getLogMessageForTarget() {
		return "by stepStub";
	}

	public int getCallCount() {
		return fCallCount;
	}
}
