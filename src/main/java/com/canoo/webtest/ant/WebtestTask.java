// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnsupportedElementException;

import com.canoo.webtest.boundary.PackageBoundary;
import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.DefaultWebtestCustomizer;
import com.canoo.webtest.engine.WebClientContext;
import com.canoo.webtest.interfaces.IWebtestCustomizer;
import com.canoo.webtest.reporting.HTMLReportGenerator;
import com.canoo.webtest.reporting.IResultReporter;
import com.canoo.webtest.reporting.PlainTextReporter;
import com.canoo.webtest.reporting.RootStepResult;
import com.canoo.webtest.reporting.StepExecutionListener;

/**
 * Ant task that specifies a Web Test Sequence.
 *
 * @author Unknown
 * @author Marc Guillemot
 * @webtest.step
 *   category="General"
 *   name="webtest"
 *   alias="testSpec"
 *   description="This <key>ANT</key> task provides the ability to specify
 *   and execute functional tests for web-based applications.
 *   The steps of the test specification to execute are defined as a sequence
 *   of nested test steps.
 *   Each <em><webtest></em> task is executed in its own web session,
 *   i.e. two subsequent <em><webtest></em> tasks are executed in different sessions.
 *   This task was previously named \"testSpec\". For compatibility reasons, both names will work."
 */
public class WebtestTask extends Task implements TaskContainer {
	private static final Logger LOG = Logger.getLogger(WebtestTask.class);
	private String fName;
	private Configuration fConfig;
	private TestStepSequence fSteps;
	private boolean fImplicitSteps = true; // indicates that <steps> has been ommitted
	public static final String REPORTER_CLASSNAME_PROPERTY = "webtest.resultreporterclass";
	public static final String DEFAULT_REPORTER_CLASSNAME = "com.canoo.webtest.reporting.XmlReporter";
    private final Map fDynamicProperties = new HashMap();
    private static final ThreadLocal CONTEXT_HOLDER = new ThreadLocal();
	private File runningLog;


    private IWebtestCustomizer webtestCustomizer;
  
    
    /**
     * Gets the context to use for this thread.
     * In a normal execution, this is the one that is created at the beginning of &lt;webtest&gt;
     * and which should be used by all the steps from this webtest. These tests may
     * be nested within macros, external targets, ... and may not "see" the &lt;webtest&gt; directly.<br/>
     * A step doesn't need to call this method as it can access the context through {@link com.canoo.webtest.steps.Step#getContext()}.
     * @return the currently used context, <code>null</code> if not inside the execution of a webtest.
     */
    public static Context getThreadContext() {
        return (Context) CONTEXT_HOLDER.get();
    }

    /**
     * Sets the context for this thread. 
     * Normally this method should not be public and only the webtest should use it.
     *  Its visibility will be restricted once the unit tests have been adapted.
     * @param context the context to set
     */
    public static void setThreadContext(final Context context) {
        CONTEXT_HOLDER.set(context);
    }
    
    public void setDynamicProperty(final String name, final String value) {
        fDynamicProperties.put(name, value);
    }

    public String getDynamicProperty(final String name) {
        return (String) fDynamicProperties.get(name);
    }

    public Map getDynamicProperties() {
        return fDynamicProperties;
    }

    /**
	 * Called by ant to add the nested "configuration ..." task.
	 *
	 * @param config the configuration task
     * @webtest.nested.parameter
     * required="no"
     * description="The webtest configuration."
     */
	public void addConfig(final Configuration config) {
		if (fSteps != null) {
	        final String msg = config.getTaskName() + " invalid at this position! "
	        + "It has to be the first node of \"" + getTaskName() + "\"!";
	        throw new UnsupportedElementException(msg, config.getTaskName());
		}
		fConfig = config;
	}

	/**
	 * In a first time doesn't support other tasks as config and testSpec, but
	 * this class has to implement {@link TaskContainer} to work with Groovy's AntBuilder
	 * (a bug in this AntBuilder?)
	 * @see org.apache.tools.ant.TaskContainer#addTask(org.apache.tools.ant.Task)
	 */
	public void addTask(final Task task) {
		LOG.debug("addTask: " + task.getTaskName() + " " + task);
		if (task instanceof Configuration) {
			addConfig((Configuration) task);
        }
		else {
			if (task instanceof TestStepSequence) {
				addSteps((TestStepSequence) task);
	        }
			else if (!fImplicitSteps)
			{
		        throw new UnsupportedElementException("No step allowed after </steps>!", task.getTaskName());
			}
			else 
			{
				if (fSteps == null) {
					final TestStepSequence implicitSteps = new TestStepSequence();
					implicitSteps.setDescription("Implicit <steps> task");
					implicitSteps.setTaskName("steps");
					implicitSteps.setProject(getProject());
					implicitSteps.setOwningTarget(task.getOwningTarget());
					implicitSteps.setLocation(task.getLocation());
					addSteps(implicitSteps);
					
					fImplicitSteps = true;
				}
				if (fImplicitSteps)
				{
					// we have created <steps> by ourself, we need to populate its wrapper too 
					// as Ant would have done
					getStepSequence().getRuntimeConfigurableWrapper().addChild(task.getRuntimeConfigurableWrapper());
				}
				getStepSequence().addTask(task);
			}
		}
	}

	/**
	 * Called by ant to add the nested "webtest ..." task.
	 *
	 * @param steps the steps
     * @webtest.nested.parameter
     * required="yes"
     * description="All the webtest steps."
     */
	public void addSteps(final TestStepSequence steps) {
		// first create config if needed
		if (getConfig() == null)
		{
			addConfig(createDefaultConfiguration());
			LOG.info("No configuration defined, using default configuration.");
		}

		if (fSteps != null) {
	        final String msg = getTaskName()
	        + " doesn't support multiple nested \"" + steps.getTaskName() + "\" elements.";
	        throw new UnsupportedElementException(msg, steps.getTaskName());
		}
		fSteps = steps;
		fImplicitSteps = false;
	}

	/**
	 * Executes the task.
	 * If it doesn't contain a nested &lt;config&gt; a default one is created
	 * using {@link #createDefaultConfiguration()}.
	 */
	public void execute() throws BuildException {
		final String message = "webtest \"" + getName() + "\" (" + getLocation() + ")";
		LOG.info("Starting " + message);
		final String webtestVersion = PackageBoundary.versionMessage();
		LOG.info(webtestVersion);
		getProject().setProperty("webtest.version", webtestVersion);

		assertParametersNotNull();
    	final Context context = new Context(this);
    	CONTEXT_HOLDER.set(context);
    	
    	LOG.debug("Executing configuration task");
    	getConfig().setContext(context);
    	getConfig().perform();

		initRunningLog();

		// register custom property helper in place of the original one
    	final PropertyHelper originalPropertyHelper = PropertyHelper.getPropertyHelper(getProject());
    	
    	WebtestPropertyHelper.configureWebtestPropertyHelper(getProject());
    	
    	// register the listener that will capture the results
    	fResultBuilderListener = getWebtestCustomizer().createExecutionListener(this);
    	getProject().addBuildListener(fResultBuilderListener);

    	logRunningTestInfo("Start");
    	try {
            executeSteps();
    	}
    	catch (final BuildException e) {
            // nothing, exception is available in result build listener too
    	} 
    	finally {
    		fResultBuilderListener.webtestFinished();
            getProject().removeBuildListener(fResultBuilderListener);
            WebtestPropertyHelper.cleanWebtestPropertyHelper(getProject());
            
            // clean the WebClient(s) to stop running js scripts (like setTimeout)
            for (final Iterator iter=context.getWebClientContexts().values().iterator(); iter.hasNext();)
            {
            	final WebClientContext webClientContext = (WebClientContext) iter.next();
            	webClientContext.destroy();
            }
    	}

		LOG.info("Finished executing " + message);

		writeTestReportIfNeeded(fResultBuilderListener.getRootResult());
		
		if (getProject().getProperty(REPORTER_CLASSNAME_PROPERTY) == null) {
			HTMLReportGenerator.registerReportToFormat(getProject(), getConfig().getSummaryFile());
		}

		final RootStepResult result = fResultBuilderListener.getRootResult();
		
		String endMessage = result.isSuccessful() ? "Success" : "Failed";
    	logRunningTestInfo(endMessage);
		
		stopBuildIfNeeded(result, fConfig);
	}

	private synchronized void initRunningLog() {
		runningLog = new File(getConfig().getResultpath(), "running.txt");
		if (!runningLog.exists()) {
			try {
				runningLog.createNewFile();
			}
			catch (final IOException e) {
				throw new RuntimeException("Failed to create running.txt file", e);
			}
			runningLog.deleteOnExit();
		}
	}

	/**
	 * Log Start/Success/Failed to running.txt in results folder.
	 * @param msg Start/Success/Failed
	 */
	protected synchronized void logRunningTestInfo(final String msg) {
		Calendar cal = Calendar.getInstance();

		
		final String line = String.format("%1$tH:%1$tM:%1$tS	%2$s	%3$s	%4$s\n", 
				cal, 
				msg, 
				getConfig().getWebTestResultDir().getName(),
				getName());
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(runningLog, true);
			IOUtils.write(line, fos, "UTF-8");
			fos.close();
		}
		catch (final IOException e) {
			throw new RuntimeException("Failed to write to running.txt", e);
		}
	}

	/**
	 * Execute the nested steps (except the configuration)
	 */
	protected void executeSteps() {
		fSteps.perform();
	}

	private StepExecutionListener fResultBuilderListener;
	/**
	 * TODO: check if it should really be accessible
	 * @return the listener that will build the results
	 */
	protected StepExecutionListener getResultBuilderListener() {
		return fResultBuilderListener;
	}
	
	/**
	 * Creates the default configuration to use if no <configuration> was
	 * present in the ant file.
	 * @return the configuration
	 */
	protected Configuration createDefaultConfiguration() {
        final Configuration configuration = new Configuration(this);
        LOG.debug("Default configuration created: host=" + configuration.getHost() +
            ", port=" + configuration.getPort() +
            ", protocol=" + configuration.getProtocol());
        return configuration;
	}

	protected void stopBuildIfNeeded(final RootStepResult webTestResult, final Configuration config) {
		LOG.debug("Looking if it is needed to stop the build");
		if (webTestResult.isError() && config.isHaltOnError()
			|| webTestResult.isFailure() && config.isHaltOnFailure()) {
			LOG.debug("Exception: " + webTestResult.getException().getClass().getName());
			LOG.debug("Throwing BuildException");
			if (webTestResult.getException() instanceof BuildException)
			{
				throw (BuildException) webTestResult.getException();
			}
			else
			{
				final String str = PlainTextReporter.getBuildFailMessage(webTestResult);
				LOG.debug("str: " + str);
				throw new BuildException(webTestResult.getException());
			}
		}
		if (webTestResult.isError() && !StringUtils.isEmpty(config.getErrorProperty())) {
			LOG.debug("Set error property \"" + config.getErrorProperty() + "\" to true");
			getProject().setProperty(config.getErrorProperty(), "true");
		}
		if (webTestResult.isFailure() && !StringUtils.isEmpty(config.getFailureProperty())) {
			LOG.debug("Set failure property \"" + config.getFailureProperty() + "\" to true");
			getProject().setProperty(config.getFailureProperty(), "true");
		}
	}

	// *********************************************************************
	// Implementation of the IPropertyHandler interface
	// *********************************************************************

	private void assertParametersNotNull() throws BuildException {
		assertAttributeNotNull(fName, "name");
		assertNestedElementNotNull(fSteps, "steps");
	}

	private void assertAttributeNotNull(final Object parameter, final String parameterName) {
		final String[] msg = {"attribute ", "\n", parameterName, "\n"};
		assertNotNull(parameter, msg);
	}

	private void assertNestedElementNotNull(final Object parameter, final String parameterName) {
		final String[] msg = {"nested element ", "<", parameterName, ">"};
		assertNotNull(parameter, msg);
	}

	protected void assertNotNull(final Object parameter, final String[] msg) {
		if (parameter == null) {
			throw new BuildException("Required " + msg[0] + msg[1] + msg[2] + msg[3] + " is not set!");
		}
	}

	/**
	 * @param name
	 * @webtest.parameter
	 *   required="yes"
	 *   description="Defines a name for this test specification."
	 */
	public void setName(final String name) {
		fName = name;
	}

	/**
	 * gets the name of this webtest
	 * @return the name (as specified in &lt;webtest name="..."&gt;)
	 */
	public String getName() {
		return fName;
	}

	protected void writeTestReportIfNeeded(final RootStepResult result) {
		String reporterClass = getProject().getProperty(REPORTER_CLASSNAME_PROPERTY);
		if (reporterClass == null) {
			reporterClass = DEFAULT_REPORTER_CLASSNAME;
		}
		LOG.debug("Writing test report using Report class: " + reporterClass);
		callSelectedReporter(reporterClass, result);
		LOG.debug("Report written");
	}

	protected void callSelectedReporter(final String reporterClass, final RootStepResult result) {
		try {
			final IResultReporter reporter = (IResultReporter) Class.forName(reporterClass).newInstance();
			report(reporter, result);
		} catch (final Exception e) {
			LOG.error("Exception caught while writing test report", e);
		}
	}

	protected void report(final IResultReporter reporter, final RootStepResult result) {
		try {
			reporter.generateReport(result);
			LOG.info("Test report successfully created.");
		} catch (final Exception e) {
			LOG.error("Exception caught while writing test report", e);
		}
	}

	public Configuration getConfig() {
		return fConfig;
	}

	protected void setConfig(final Configuration config) {
		fConfig = config;
	}

	public void setProject(final Project project) {
		super.setProject(project);
		
    	//configure 
    	try {
    		webtestCustomizer = (IWebtestCustomizer) project.getReference(IWebtestCustomizer.KEY);
    	}
    	catch (final ClassCastException e) {
    		throw new BuildException("Provided customizer is not a IWebtestCustomizer: " + project.getReference(IWebtestCustomizer.KEY), e);
    	}
    	
    	if (webtestCustomizer == null) {
    		webtestCustomizer = new DefaultWebtestCustomizer();
    	}
    	else {
    		LOG.info("Using IWebtestCustomizer: " + webtestCustomizer);
    	}
	}
	
	public IWebtestCustomizer getWebtestCustomizer(){
		return webtestCustomizer;
	}
	
	public TestStepSequence getStepSequence() {
		return fSteps;
	}
}
