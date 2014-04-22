// Copyright © 2005-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.extension.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;

/**
 * @author Unknown
 * @author Marc Guillemot
 */
class GroovyInvoker
{
    private static final Logger LOG = Logger.getLogger(GroovyInvoker.class);
    /**
     * Key used to save the binding as webtest properties and allow reuse across different Groovy steps of a webtest
     */
    private static final String KEY_GROOVY_BINDING = GroovyInvoker.class.getName() + "#binding";

	public void doExecute(final Step step, final String script)
	{
		final Binding variables = getBinding(step);
		
		// set standard variable (need to be set even if binding is reused as these 2 variables are bound to current step)
		variables.setVariable("step", step);
		final DummyPrinter out = new DummyPrinter(step, Level.INFO);
		variables.setVariable("out", out);
		final GroovyShell shell = new GroovyShell(getClass().getClassLoader(), variables);
		try
		{
			LOG.debug("Evaluating script: " + StringUtils.abbreviate(script, 20));
            shell.evaluate(script);
		}
		catch (final CompilationFailedException e)
		{
			LOG.error("CompilationFailedException", e);
			throw new StepExecutionException("Cannot compile groovy code: " + script, step, e);
		}
        catch (final AssertionError e) {
            LOG.info("AssertionError", e);
            throw new StepFailedException("Assertion failed within groovy code: " + script, step);
        }
        catch (final RuntimeException e)
		{
			LOG.error("RuntimeException", e);
            throw new StepExecutionException("Error invoking groovy: " + e.getMessage(), step, e);
		}
		finally
		{
			out.flush();
		}
	}

	/**
	 * Gets the binding to use for step execution. Retrieve the binding for this webtest if one exists
	 * or create a new one if this is the first groovy step of this webtest
	 * @param step the current step
	 * @return the binding to use for script execution
	 */
	Binding getBinding(final Step step)
	{
		Binding binding = (Binding) step.getWebtestProperties().get(KEY_GROOVY_BINDING);
		if (binding == null)
		{
			LOG.info("No existing binding for this webtest, creating a new one");
			binding = new Binding();
			step.getWebtestProperties().put(KEY_GROOVY_BINDING, binding);
		}
		else
		{
			LOG.info("Reusing existing binding of this webtest.");
		}

		return binding;
	}

}

/**
 * A small bridge between groovy output and Step's logger. This doesn't need to
 * be a {@link java.io.PrintWriter}: an object with methods <code>print</code>
 * and <code>println</code> is enough.
 */
class DummyPrinter
{
	private final Logger fLogger;
	private final Level fLevel;
	private final StringBuffer fBuffer = new StringBuffer();

	DummyPrinter(final Step step, final Level level)
	{
		fLogger = Logger.getLogger(step.getClass());
		fLevel = level;
	}

	void println(final Object object)
	{
        print(object);
        final String message = fBuffer.toString();
		fBuffer.setLength(0);
		fLogger.log(fLevel, message);
	}

	void print(final Object object)
	{
		fBuffer.append(String.valueOf(object));
	}

	/**
	 * prints the remaining message (if any)
	 */
	void flush()
	{
		if (fBuffer.length() > 0)
			println(""); // forces print of the current buffer
	}
}
