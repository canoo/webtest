// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights
// Reserved.
package com.canoo.webtest.extension.groovy;

import groovy.lang.Closure;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.apache.tools.ant.RuntimeConfigurable;

import com.canoo.webtest.groovy.WebTestBuilder;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.FileUtil;

/**
 * Wrapper class for groovy scripting.
 * <p>
 * @author Dierk Koenig
 * @author Marc Guillemot
 * @webtest.step category="Extension" name="groovy" 
 * 	description="Executes the provided Groovy script (the binding is the same for all
 * groovy steps within a webtest)."
 */
public class GroovyStep extends Step
{
    private static final Logger LOG = Logger.getLogger(GroovyStep.class);

    private File fFile;
    private Integer closureKey;
	private String fText;
	private Closure bodyClosure;
	private boolean fReplaceProperties = false;

	class DelegateForClosureBody
	{
		Step getStep()
		{
			return GroovyStep.this;
		}
	}


	/**
	 * Registers the closure that will be the body of the created <groovy> step with the given wrapper.
	 * This is used by {@link WebTestBuilder} that makes writing WebTest tests in Groovy more... groovy.
	 */
	public static Integer registerBodyClosure(final Closure closure) {
		final Integer key = closure.hashCode();
		closuresMap_.put(key, closure);
		return key;
	}
	private static final Map closuresMap_ = new HashMap(); // TODO: check when to remove them???
	
	public void execute() {
		// if test written in Groovy, perhaps the body has been specified as closure
		// then it has to be retrieved before param verification occurs
		bodyClosure = (Closure) closuresMap_.get(closureKey);

		super.execute();
	}

	public void doExecute()
	{
		if (bodyClosure != null)
		{
			bodyClosure.setDelegate(new DelegateForClosureBody());
			bodyClosure.call();
		}
		else
		{
			String script = getScript();
			if (isReplaceProperties())
			{
				script = getProject().replaceProperties(script);
			}
	
			final GroovyInvoker invoker = new GroovyInvoker();
			invoker.doExecute(this, script);
		}
	}

	public boolean isReplaceProperties() {
		return fReplaceProperties;
	}

	/**
	 * Indicates if properties should be replaced in the script (default false)
	 * @webtest.parameter required="no" 
	 * 	default="false"
	 * description="Indicates if properties (${...} and #{...}) present in the script
	 * should be replaced by their value. Use carefully as ${...} is although the syntax for GString."
	 */
	public void setReplaceProperties(final boolean replaceProperties) {
		fReplaceProperties = replaceProperties;
	}

	/**
	 * Gets the script code from the file or the nested content
	 * @return the script code
	 */
	private String getScript()
	{
		final String script;
		if (fFile != null)
		{
			LOG.debug("Reading script from file: " + fFile);
			script = FileUtil.readFileToString(fFile, this);
		}
		else
		{
			LOG.debug("Reading script from nested text");
			script = fText;
		}
		return script;
	}

	protected void verifyParameters()
	{
		super.verifyParameters();
		if (bodyClosure != null)
		{
			final String end = " attribute not allowed when step body is provided as closure";
			paramCheck(fReplaceProperties, "\"replaceProperties\"" + end);
			paramCheck(fFile != null, "\"file\"" + end);
		}
		else
		{
			paramCheck(fFile == null && fText == null,
					"Either \"file\" attribute or nested groovy text must be given.");
			paramCheck(fFile != null && fText != null,
					"Only one of \"file\" attribute or nested groovy text may be given.");
		}
	}

	/**
	 * Defines the file containing scripting code (optional).
	 * @param fileName Sets the name of the file containing script code.
	 * @webtest.parameter required="yes/no" description="The name of the file
	 *                    containing the script code. You may omit this
	 *                    parameter if you have embedded script code."
	 */
	public void setFile(final File fileName)
	{
		fFile = fileName;
	}

	public File getFile()
	{
		return fFile;
	}

	/**
	 * The script text.
	 * @param text Sets the value for the script variable.
	 * @webtest.nested.parameter required="yes/no" description="The script to
	 *                           execute. You may omit this if you use the
	 *                           attribute file."
	 */
	public void addText(final String text)
	{
		fText = text;
	}
	
	/**
	 * Set by {@link WebTestBuilder} to allow this task to retrieve the associated closure
	 * when the tests are written in Groovy.
	 */
	public void setClosureKey(final Integer key) {
		closureKey = key;
	}
}
