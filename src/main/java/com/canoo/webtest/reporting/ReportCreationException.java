// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;

public final class ReportCreationException extends Exception
{
	private final Throwable fInitialThrowable;

	public ReportCreationException(String s) {
		this(s, null);
    }

	public ReportCreationException(Throwable initialThrowable) {
		this(initialThrowable.getMessage(), initialThrowable);
	}

	private ReportCreationException(String s, Throwable initialThrowable) {
		super(s);
        fInitialThrowable = initialThrowable;
    }

	public Throwable getInitialThrowable()
	{
		return fInitialThrowable;
	}
}
