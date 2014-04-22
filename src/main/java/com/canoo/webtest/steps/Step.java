// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Task;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.EqualsStringVerfier;
import com.canoo.webtest.engine.IStringVerifier;
import com.canoo.webtest.engine.RegExStringVerifier;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.interfaces.IComputeValue;
import com.canoo.webtest.reporting.IStepResultListener;
import com.canoo.webtest.util.Checker;
import com.canoo.webtest.util.MapUtil;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;

/**
 * Abstract superclass for all test steps. Provides generic services for all subclasses.
 *
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @author Paul King, ASERT
 * @webtest.step
 */
public abstract class Step extends Task implements Serializable, Cloneable {
	private static final Logger LOG = Logger.getLogger(Step.class);
	public static final String ELEMENT_ATTRIBUTE_NAME = "name";
	public static final String ELEMENT_ATTRIBUTE_ID = "id";
	/**
	 * @deprecated The default is now not to set  the value at all.
	 */
	public static final String DEFAULT_DESCRIPTION = "<unknown>";

	private Date fStarted;
	private Date fCompleted;
	private boolean fSuccessful;

	/**
	 * The property type is set according to webtest's <em>defaultPropertyType</em>.
	 */
	public static final String PROPERTY_TYPE_DEFAULT = null;
	/**
	 * The property is a <em>dynamic</em> property.
	 */
	public static final String PROPERTY_TYPE_DYNAMIC = "dynamic";
	/**
	 * The property is an <em>ant</em> property.
	 */
	public static final String PROPERTY_TYPE_ANT = "ant";
	/**
	 * The property is an <em>ant</em> property that must not overwrite an existing property.
	 */
	public static final String PROPERTY_TYPE_ANT_STRICT = "antstrict";

	/**
	 * This is the abstract base class for all test step specifications.
	 */
	protected Step() {
	}

	private void checkSetup() {
		Checker.assertNonNull(getProject(), "Project cannot be null");
	}

	public Context getContext() {
		return WebtestTask.getThreadContext();
	}

	/**
	 * Called to perform the step's functionality.
	 * Before calling this method, the step has been completely initialized (including expanding and verifying the parameters)
	 * and the environment notified of the start.
	 *
	 * @throws Exception
	 */
	public abstract void doExecute() throws Exception;

	/**
	 * Ant calls this method to invoke this task's functionality.
	 * We expand and verify the steps parameters then call doExecute()
	 * as well as handle lifecycle notifications.
	 */
	public void execute() {
		//expandProperties(); FIXME: use PropertyHelper!
		checkContextDefined();
		notifyStarted();
		try {
			verifyParameters();
			doExecute();
			
			// give time for background js tasks to finish when needed
			final Context context = getContext();
			final Page currentPage = context.getCurrentResponse();
			if (isPerformingAction() && currentPage != null && context.getConfig().isEasyAjax()) {
				final int easyAjaxDelay = context.getConfig().getEasyAjaxDelay();
				LOG.debug("Easy AJAX waiting for task starting before " + easyAjaxDelay);
				final long before = System.currentTimeMillis();
				context.getWebClient().waitForBackgroundJavaScriptStartingBefore(easyAjaxDelay);
				LOG.debug("Easy AJAX waited: " + (System.currentTimeMillis() - before) + "ms");
			}
			
			final ScriptException bgException = context.getBackgroundJSError();
			if (bgException != null) {
				handleException(bgException);
			}
		} 
		catch (final Exception ex) {
			handleException(ex);
		}
		finally {
			notifyCompleted();
			notifyStepResultsListeners();
		}
		notifySuccess();
	}

	/**
	 * Indicates if this step performs an action on the browser
	 */
	public boolean isPerformingAction() {
		return true;
	}
	
	private void checkContextDefined() {
		if (WebtestTask.getThreadContext() == null) {
			throw new StepExecutionException("Step not inside a webtest", this);
		}
	}

	/**
	 * Notifies the interested project build listeners (typically only the {@link com.canoo.webtest.reporting.StepExecutionListener})
	 * that this step has produced results
	 */
	protected void notifyStepResultsListeners() {
		final Map results = getComputedParameters();
		if (results.isEmpty()) {
			LOG.debug("Step didn't produce results, no need to notifying listeners");
			return;
		}

		for (final Iterator iter = getProject().getBuildListeners().iterator(); iter.hasNext();) {
			final BuildListener listener = (BuildListener) iter.next();
			if (listener instanceof IStepResultListener) {
				LOG.debug("Notifying " + listener + " of " + results.size() + " results");
				((IStepResultListener) listener).stepResults(results);
			}
		}
	}

	/**
	 * Called to let the step's implementation validate its parameters.
	 * The method is called after parameter extensions but before {@link #doExecute()}.
	 * This implementation does nothing, overwrite as needed.
	 */
	protected void verifyParameters() {
		// default is do nothing
	}

	public boolean hasDescription() {
		return StringUtils.isNotEmpty(getDescription());
	}

	/**
	 * Gets the description with a prefix and suffix if the description is set.
	 *
	 * @param prefix A string to display before the description.
	 * @param suffix A string to display after the description.
	 * @return the concatenation of prefix, description and suffix.
	 */
	public String getDescription(final String prefix, final String suffix) {
		final String description = getDescription();
		if (!StringUtils.isNotEmpty(description)) {
			return "";
		}
		return prefix + description + suffix;
	}

	/**
	 * Gets the execution time for a completed step.
	 *
	 * @return the execution time in ms
	 */
	public long getDuration() {
		return fCompleted.getTime() - fStarted.getTime();
	}

	protected String getStepLabel() {
		return "Step[" + getStepLabelBrief() + "]";
	}

	private String getStepLabelBrief() {
		final StringBuffer message = new StringBuffer();
		message.append(getTaskName());
		message.append(getDescription(" \"", "\""));
		message.append(" (").append(getContext().getCurrentStepNumber()).append("/");
		message.append(getContext().getNumberOfSteps()).append(")");

		return message.toString();
	}

	public boolean isCompleted() {
		return fStarted != null && fCompleted != null;
	}

	public boolean isStarted() {
		return fStarted != null;
	}

	public boolean isSuccessful() {
		return fSuccessful;
	}

	/**
	 * Called after {@link #doExecute()} has completed (successfully or not)
	 */
	public void notifyCompleted() {
		fCompleted = new Date();
		LOG.debug("Completed Step: " + getStepLabelBrief());
	}

	/**
	 * Called before calling {@link #doExecute()}
	 */
	public void notifyStarted() {
		fStarted = new Date();
		LOG.info(">>>> Start Step: " + getStepLabelBrief());
	}

	public void notifySuccess() {
		fSuccessful = true;
		LOG.debug("<<<< Successful Step: " + getStepLabelBrief());
	}

	/**
	 * @param description
	 * @deprecated since June 10 2005. Use {@link Task#setDescription(String)}
	 *             (setter should not be removed for compatibility with existing test sequences)
	 */
	public void setStepid(final String description) {
		LOG.warn("'stepid' is deprecated - use 'description' instead");
		setDescription(description);
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer(64);

		sb.append(ClassUtils.getShortClassName(getClass()));
		sb.append(" at ");
		sb.append(getLocation().toString());
		sb.append(" with (");

		final Map parms = getParameterDictionary();

		for (final Iterator iter = parms.keySet().iterator(); iter.hasNext(); sb.append(", ")) {
			final Object param = iter.next();
			sb.append(param).append("=\"").append(parms.get(param)).append("\"");
		}
		if (!parms.isEmpty()) {
			sb.setLength(sb.length() - 2);
		}
		sb.append(")");
		return sb.toString();
	}

	protected String getDefaultPropertyType() {
		return getContext().getConfig().getDefaultPropertyType();
	}

	/**
	 * Sets a property of the default type.
	 *
	 * @param name  The name of the property.
	 * @param value The value of the property.
	 */
	public void setWebtestProperty(final String name, final String value) {
		setWebtestProperty(name, value, null);
	}

	/**
	 * Sets a property of the default type.
	 *
	 * @param name		 The name of the property.
	 * @param value		The value of the property.
	 * @param propertyType The kind of property desired. One of {@link #PROPERTY_TYPE_ANT},
	 *                     {@link #PROPERTY_TYPE_ANT_STRICT}, {@link #PROPERTY_TYPE_DYNAMIC} or {@link #PROPERTY_TYPE_DEFAULT}.
	 */
	public void setWebtestProperty(final String name, final String value, final String propertyType) {
		final String thisPropType = propertyType == PROPERTY_TYPE_DEFAULT ? getDefaultPropertyType() : propertyType;

		LOG.debug("setWebtestProperty: " + name + "=" + value + " [" + thisPropType + "]");
		if (StringUtils.isEmpty(thisPropType) || PROPERTY_TYPE_DYNAMIC.equals(thisPropType)) {
			getContext().getWebtest().setDynamicProperty(name, value);
			return;
		}

		if (PROPERTY_TYPE_ANT.equals(thisPropType)) {
			checkSetup();
			getProject().setProperty(name, value);
			return;
		}

		if (PROPERTY_TYPE_ANT_STRICT.equals(thisPropType)) {
			checkSetup();
			getProject().setNewProperty(name, value);
			return;
		}

		throw new StepExecutionException("Unknown propertyType: " + thisPropType, this);
	}

	/**
	 * Gets a property of the default type.
	 *
	 * @param name The name of the property.
	 * @return The value of the property.
	 */
	public String getWebtestProperty(final String name) {
		return getWebtestProperty(name, PROPERTY_TYPE_DEFAULT);
	}

	/**
	 * Gets a property of the specified type
	 *
	 * @param name		 The name of the property.
	 * @param propertyType The kind of property desired. One of {@link #PROPERTY_TYPE_ANT},
	 *                     {@link #PROPERTY_TYPE_DYNAMIC} or {@link #PROPERTY_TYPE_DEFAULT}.
	 * @return The value of the property.
	 */
	public String getWebtestProperty(final String name, final String propertyType) {
		final String thisPropType = propertyType == PROPERTY_TYPE_DEFAULT ? getDefaultPropertyType() : propertyType;

		LOG.debug("getWebtestProperty(" + name + ") [" + thisPropType + "]");
		if (StringUtils.isEmpty(thisPropType) || PROPERTY_TYPE_DYNAMIC.equals(thisPropType)) {
			return getContext().getWebtest().getDynamicProperty(name);
		}

		if (thisPropType.startsWith(PROPERTY_TYPE_ANT)) {
			checkSetup();
			return getProject().getProperty(name);
		}
		throw new StepExecutionException("Unknown propertyType: " + thisPropType, this);
	}

	public Map getWebtestProperties() {
		return getWebtestProperties(null);
	}

	public Map getWebtestProperties(final String propertyType) {
		final String thisPropType = propertyType == null ? getDefaultPropertyType() : propertyType;

		if (StringUtils.isEmpty(thisPropType) || PROPERTY_TYPE_DYNAMIC.equals(thisPropType)) {
			return getContext().getWebtest().getDynamicProperties();
		}

		if (thisPropType.startsWith(PROPERTY_TYPE_ANT)) {
			checkSetup();
			return getProject().getProperties();
		}
		throw new StepExecutionException("Unknown propertyType: " + thisPropType, this);
	}

	/**
	 * This creates a bitwise copy of the receiver. Since we do not reference
	 * any complex objects as attributes, the default implementation of
	 * object will do.
	 * The mere relay to the super implementation is left in the code as a
	 * reminder that this needs to be updated as soon as complex objects
	 * are aggregated right here or in a subclass.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	protected static IStringVerifier getVerifier(final boolean useRegex) {
		return useRegex ? RegExStringVerifier.INSTANCE : EqualsStringVerfier.INSTANCE;
	}

	/**
	 * Called if {@link #doExecute()} throws an exception
	 * @param t the thrown exception
	 */
	protected void handleException(final Throwable t) {
		LOG.debug("Handling exception " + t.getClass().getName() + ": " + t.getMessage(), t);
		StepUtil.handleException(t);
	}

	/**
	 * Throw an exception if the condition holds.
	 *
	 * @param condition If true, throws the exception.
	 * @param message   The error message.
	 */
	protected void paramCheck(final boolean condition, final String message) {
		if (condition) {
			throw new StepExecutionException(message, this);
		}
	}

	protected void nullParamCheck(final Object param, final String paramName) {
		paramCheck(param == null, "Required parameter \"" + paramName + "\" not set!");
	}

	protected void emptyParamCheck(final String param, final String paramName) {
		paramCheck(StringUtils.isEmpty(param),
				"Required parameter \"" + paramName + "\" not set or set to empty string!");
	}

	/**
	 * Checks that the parameter's value is non negative
	 *
	 * @param paramName the name of the parameter
	 * @param value	 the parameter value
	 * @throws StepExecutionException if the value is negative
	 */
	protected void positiveOrZeroParamCheck(final int value, final String paramName) {
		if (value < 0) {
			throw new StepExecutionException(paramName + " parameter with value '" + value + "' must not be negative", this);
		}
	}

	protected void integerParamCheck(final String param, final String paramName, final boolean nonNegative) {
		try {
			final int value = Integer.parseInt(param);
			if (nonNegative && value < 0) {
				throw new StepExecutionException(paramName + " parameter with value '" + param + "' must not be negative", this);
			}
		} catch (NumberFormatException e) {
			throw new StepExecutionException("Can't parse " + paramName + " parameter with value '" + param + "' as an integer.", this);
		}
	}

	protected void optionalIntegerParamCheck(final String param, final String paramName, final boolean nonNegative) {
		if (!StringUtils.isEmpty(param)) {
			integerParamCheck(param, paramName, nonNegative);
		}
	}

	protected void nullResponseCheck() {
		paramCheck(getContext() == null || getContext().getCurrentResponse() == null,
				"No current response available! Is previous invoke missing?");
	}

	/**
	 * Gets a snapshot of the values.
	 * As the value of the attributes can change over time,
	 * it is not possible to fill and cache the Map.
	 * Either fill the Map everytime, or skip the fields and use only the Map.
	 * <p>This method returns all the parameters that were discovered at build time and stored in the <em>.attributes</em> resource.
	 * Overwrite this method if your step doesn't have a  <em>.attributes</em> resource
	 *
	 * @return A Map of (attribute name, attribute value) for this step.
	 */
	public Map getParameterDictionary() {
		final Map parameterDictionary = new TreeMap(); // to ensure order and make report comparison easier

		addComputedParameters(parameterDictionary);
		addInternalParameters(parameterDictionary);

		return parameterDictionary;
	}

	/**
	 * Adds parameters that are not issued from the config file but computed at runtime by the step
	 *
	 * @param map the map in which the parameters should be added
	 */
	protected void addComputedParameters(final Map map) {
		if (this instanceof IComputeValue) {
			final String value = ((IComputeValue) this).getComputedValue();
			MapUtil.putIfNotNull(map, "=> value", value);
		}
	}


	/**
	 * TODO: would be cleaner to notify the result listener and to give him this information
	 *
	 * @return the "results" parameter of the step
	 */
	protected Map getComputedParameters() {
		final Map map = new HashMap();
		addComputedParameters(map);
		return map;
	}

	private void addInternalParameters(final Map map) {
		// add internal parameters
		MapUtil.putIfNotNull(map, "taskName", getTaskName());
	}
}
