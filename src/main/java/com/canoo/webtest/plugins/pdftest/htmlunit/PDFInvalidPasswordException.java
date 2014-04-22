package com.canoo.webtest.plugins.pdftest.htmlunit;

/**
 * Indicates that the provided user/name password is not valid 
 * @author Marc Guillemot
 */
public class PDFInvalidPasswordException extends RuntimeException {
	public PDFInvalidPasswordException(final Throwable e)
	{
		super(e);
	}

	public PDFInvalidPasswordException(final String message, final Throwable e)
	{
		super(message, e);
	}
}
