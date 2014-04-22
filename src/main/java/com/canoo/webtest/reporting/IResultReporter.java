// Copyright © 2002 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;


public interface IResultReporter
{
	void generateReport(final RootStepResult result) throws Exception;
}
