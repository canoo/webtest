// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.canoo.webtest.boundary.ResetScriptRunner;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.FileUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Wrapper class for the ant script command.<p>
 *
 * @author Paul King
 * @webtest.step
 *    category="Extension"
 *    name="scriptStep"
 *    description="Provides the ability to use scripting code in your tests."
 */
public class ScriptStep extends Step {
	private static final Logger LOG = Logger.getLogger(ScriptStep.class);
	private String fLanguage;
    private String fKeep;
	private File fSrc;
	private String fScriptText = "";

	/**
	 * Perform the step's actual work.
	 *
	 * @throws Exception if a problem occurred
	 */
	public void doExecute() throws Exception {
        final ResetScriptRunner runner; // delegate pattern
        final Context context = getContext();
        if (context.getRunner() == null) {
            runner = new ResetScriptRunner();
            runner.setLanguage(getLanguage());
            context.setRunner(runner);
            LOG.debug("Creating new Script Runner with language: " + fLanguage);
        } 
        else {
            runner = context.getRunner();
            runner.reset();
        }
		buildScript();
		getProject().addReference("step", this);
		if (context.getCurrentResponse() == null) {
			LOG.warn("No response found. Previous invoke missing? Related scripting variables not created");
		} 
		else {
			setupResponseScriptingVariables(context);
		}

		try {
			executeByRunner(runner, getProject().replaceProperties(fScriptText), this, getProject());
            // languages like groovy can throw assert errors which are useful to fail test
            // but what about an assert in groovy's implementation that has failed - this should
            // probably be configurable to potentially throw StepExecutionError
        } catch (AssertionError ae) {
            final String msg = "Assertion error during scriptStep: " + ae.getMessage();
            LOG.debug(msg, ae);
            throw new StepFailedException(msg, this);
        } catch (BuildException be) {
            LOG.debug(be.getMessage(), be);
            throw new StepExecutionException("Error invoking script: " + be.getMessage(), this);
		} finally {
            if (!isKeep()) {
                context.setRunner(null);
            }
        }
	}

    public static String evalScriptExpression(final Context context, final String expression, final Step step) {
        final ResetScriptRunner runner = context.getRunner();
        if (runner == null) {
            throw new StepExecutionException("Can't evaluate script property because no previous <scriptStep> with keep=true.", step);
        }
        runner.reset();
        try {
            return evalByRunner(runner, expression, step);
        } catch (BuildException be) {
            throw new StepExecutionException("Error invoking script: " + be.getMessage(), step);
        }
    }

    public static void executeByRunner(final ResetScriptRunner runner, final String script, final Step step, final Project project) throws BuildException {
        runner.addText(script);
        runner.addBeans(project.getProperties());
        runner.addBeans(project.getUserProperties());
        runner.addBeans(project.getTargets());
        runner.addBeans(project.getReferences());
        runner.addBean("project", project);
        runner.addBean("self", step);
        runner.executeScript("WebTest");
    }

    public static String evalByRunner(final ResetScriptRunner runner, final String script, final Step step) throws BuildException {
        runner.addText(script);
        runner.addBean("self", step);
        return runner.evalScript("WebTest");
    }

    private void buildScript() {
		if (fSrc != null) {
			fScriptText += FileUtil.readFileToString(fSrc, this);
		}
	}

	private void setupResponseScriptingVariables(final Context context) {
		getProject().addReference("response", context.getCurrentResponse().getWebResponse());
		if (context.getCurrentResponse() instanceof HtmlPage) {
			getProject().addReference("document", ((HtmlPage) context.getCurrentResponse()).getDocumentElement());
		} else if (context.getCurrentResponse() instanceof XmlPage) {
			getProject().addReference("document", ((XmlPage) context.getCurrentResponse()).getXmlDocument());
		}
	}

	/**
	 * Verify that language is set
	 *
	 * @throws StepExecutionException if a mandatory attribute is not set
	 */
	protected void verifyParameters() {
		super.verifyParameters();
        final ResetScriptRunner runner = getContext().getRunner();
        if (runner == null) {
            emptyParamCheck(fLanguage, "language");
        } else {
            paramCheck(fLanguage != null && !fLanguage.equals(runner.getLanguage()),
                    "You may not change 'language' to '" + fLanguage +
                    "' after previously using the 'keep' attribute (was: " + runner.getLanguage() + ")");
        }
		paramCheck(fSrc == null && StringUtils.isEmpty(fScriptText),
		   "Either \"src\" attribute or nested script text must be given.");
	}

	/**
	 * override to add actual attribute values to the reporting
	 */
	protected void addComputedParameters(final Map map) {
		map.put("language", fLanguage); // cannot be null (mandatory)
		if (fSrc == null) {
			map.put("script", fScriptText);
		} else {
			map.put("src", fSrc); // (optional)
		}
	}

	/**
	 * Defines the language (required).
	 *
	 * @param language Sets the scripting language.
	 * @webtest.parameter
	 * 	 required="yes/no"
	 *   description="The scripting language to use. Required unless using the <em>keep</em> attribute in which case the value is optional but must agree with the original language if used. The value can be any language supported by the <key>BSF</key>, e.g. javascript, jacl, netrexx, java, javaclass, bml, vbscript, jscript, perlscript, perl, jpython, jython, lotusscript, xslt, pnuts, beanbasic, beanshell, ruby, judoscript, groovy."
	 */
	public void setLanguage(final String language) {
		fLanguage = language;
	}

	public String getLanguage() {
		return fLanguage;
	}

	/**
	 * Defines the src file containing scripting code (optional).
	 *
	 * @param fileName Sets the name of the file containing script code.
	 * @webtest.parameter
	 * 	required="yes/no"
	 *  description="The name of the file containing the scripting code. You may omit this parameter if you have embedded script code."
	 */
	public void setSrc(final File fileName) {
		fSrc = fileName;
	}

	public File getSrc() {
		return fSrc;
	}

    /**
     * Flag to indicate that the scripting engine should be kept after the step completes and re-used for future script steps.
     *
     * @param keep Indicates that the script engine should be kept for future steps.
     * @webtest.parameter
     * 	 required="no"
     *   default="false"
     *   description="Indicates that the script engine should be kept for future steps. Variables created during one script step will remain available."
     */
    public void setKeep(final String keep) {
        fKeep = keep;
    }

    public String getKeep() {
        return fKeep;
    }

    public boolean isKeep() {
        return ConversionUtil.convertToBoolean(fKeep, false);
    }

	/**
	 * The script text if inlined.
	 *
	 * @param text The nested scripting code.
     * @webtest.nested.parameter
     *    required="yes/no"
     *    description="The nested script code. You may omit this if you use the parameter src."
     */
	public void addText(final String text) {
		fScriptText += text;
	}

}
