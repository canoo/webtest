// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;
import org.apache.xerces.xni.XNIException;
import org.xml.sax.SAXException;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.WebTestException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Helper class for {@link Step}.
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @author Paul King
 */
public class StepUtil
{
	private static final Logger LOG = Logger.getLogger(StepUtil.class);

	static Throwable extractNestedException(final Throwable e)
	{
		Throwable originalException = e;
		Throwable cause = ((XNIException) e).getException();
		while (cause != null)
		{
			originalException = cause;
			if (cause instanceof XNIException)
			{
				cause = ((XNIException) cause).getException();
			}
			else if (cause instanceof InvocationTargetException)
			{
				cause = cause.getCause();
			}
			else
			{
				cause = null;
			}
		}
		return originalException;
	}

	/**
	 * Called if {@link Step#doExecute()} throws an exception
	 * @param e the thrown exception
	 */
	public static void handleException(Throwable e)
	{
		if (e instanceof WebTestException)
		{
			throw (WebTestException) e;
		}
		else if (e instanceof FailingHttpStatusCodeException)
		{
			LOG.debug("Wrapping FailingHttpStatusCodeException in StepFailedException: " + e.getMessage());
			final FailingHttpStatusCodeException he = (FailingHttpStatusCodeException) e;
			throw new StepFailedException("HTTP error " + he.getStatusCode(), he);
		}
		else if (e instanceof XPathException)
		{
			LOG.debug("Wrapping XPathException in StepFailedException: " + e.getMessage());
			throw new StepFailedException(e.getMessage(), (Exception) e);
		}
		else if (e instanceof ScriptException)
		{
			final ScriptException se = (ScriptException) e;
			final HtmlPage page = se.getPage(); // should normally not be null but it happens in HtmlUnit 1.14 ;-(
			final StepFailedException sfe = new StepFailedException(
					"JavaScript error loading page "
							+ (page != null ? page.getUrl().toString() : "") 
							+ ": " + se.getMessage(), se);
			sfe.addDetail("javascript error", se.getMessage());
			sfe.addDetail("line", String.valueOf(se.getFailingLineNumber()));
			sfe.addDetail("javascript source", se.getScriptSourceCode());
			final String failingLine = se.getFailingLine();
			if (failingLine != null)
				sfe.addDetail("failing line", failingLine);

			// the javascript call stack
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			se.printScriptStackTrace(printWriter);
			sfe.addDetail("javascript call stack", stringWriter.toString());

			throw sfe;
		}
		else if (e instanceof SocketTimeoutException)
		{
			final SocketTimeoutException ste = (SocketTimeoutException) e;
			throw new StepFailedException("Server took to long to answer: "
					+ ste.getMessage(), ste);
		}
		else if (e instanceof SAXException)
		{
			throw new StepExecutionException("Response is not well-formed: "
					+ e.getMessage(), e);
		}
		else if (e instanceof XNIException)
		{ // XNIException are not helpful, we need to extract the deeply
			// nested original exception
			final Throwable originalException = StepUtil.extractNestedException(e);
			throw new StepExecutionException("XNIException caused by "
					+ originalException.getMessage(), originalException);
		}
		// TODO: See WT-194 should we catch java.io.IOException here and rethrow
		// as
		// StepFailed with message 'Server closed connection'? This would allow
		// retry around this situation

		throw new StepExecutionException("Unexpected exception caught: "
				+ e.getClass().getName(), e);
	}
}
