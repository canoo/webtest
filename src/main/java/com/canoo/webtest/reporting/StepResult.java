package com.canoo.webtest.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;

/**
 * Result holder for the execution of a step (or task) and his children.
 *
 * @author Marc Guillemot
 */
public class StepResult {
	private static final Logger LOG = Logger.getLogger(StepResult.class);

	private static String getName(final Task task) {
		final String taskName = task.getTaskName();
		if (taskName == null) {
			return task.getTaskType() + " " + ClassUtils.getShortClassName(task.getClass());
		}
		return taskName;
	}

	private final Map<String, String> fAttributes = new TreeMap<String, String>();
	private final List<StepResult> fChildren = new ArrayList<StepResult>();
	private Date fEndDate;
	private List fHtmlParserMessages = Collections.EMPTY_LIST;
	private Location fLocation;
	private StepResult fParent;
	private final Date fStartDate = new Date();
	private boolean fSuccessfull;
	private String fTaskDescription;

	private final String fTaskName;

	/**
	 * Constructs a result for a non executed task (and his children).
	 * This allows to present information in report
	 * about configured tasks that haven't been executed due to an error in a step before them.
	 *
	 * @param taskWrapper the configurable for the task
	 */
	@SuppressWarnings("unchecked")
	protected StepResult(final RuntimeConfigurable taskWrapper) {
		this(taskWrapper.getElementTag());
		fAttributes.putAll(taskWrapper.getAttributeMap());
		fTaskDescription = (String) fAttributes.get("description");
		LOG.debug("Constructing result for non executed task: " + getTaskName());


		retrieveNestedText(taskWrapper);

		// HACK: Perhaps should we look if the element is a task or not
		if ("table".equals(taskWrapper.getElementTag())) {
			fEndDate = fStartDate;
			fSuccessfull = true;
		}

		// the task may have children
		addLostChildren(IteratorUtils.asIterator(taskWrapper.getChildren()));
	}

	public StepResult(final String taskName) {
		fTaskName = taskName;
	}

	/**
	 * Retrieves the nested text configured within the task, if any, taking care
	 * to remove "noise" as Ant does
	 *
	 * @param taskWrapper the current task wrapper
	 */
	protected void retrieveNestedText(final RuntimeConfigurable taskWrapper) {
		LOG.trace("In retrieveNestedText");
		final String nestedText = taskWrapper.getText().toString().trim();
		final IntrospectionHelper ih = IntrospectionHelper.getHelper(taskWrapper.getClass());
		if (nestedText.length() > 0 && ih.supportsCharacters()) {
			LOG.debug(taskWrapper.getElementTag() + " supports text: " + ih.getAddTextMethod());
			fAttributes.put("nested text", nestedText);
		}
	}

	/**
	 * Builds the result holder for the given task
	 *
	 * @param task the task (probably a {@link org.apache.tools.ant.UnknownElement}
	 */
	StepResult(final Task task) {
		this(getName(task));

		retrieveNestedText(task.getRuntimeConfigurableWrapper());

		// HACK: use WebtestPropertyHelper instead?
		final Map attributeMap = task.getRuntimeConfigurableWrapper().getAttributeMap();
		for (final Iterator iterator = attributeMap.entrySet().iterator(); iterator.hasNext();) {
			final Map.Entry entry = (Map.Entry) iterator.next();
			fAttributes.put((String) entry.getKey(), task.getProject().replaceProperties((String) entry.getValue()));
		}
//		fAttributes.putAll(task.getRuntimeConfigurableWrapper().getAttributeMap());
		fTaskDescription = (String) fAttributes.get("description");
		fAttributes.remove("description");

		fLocation = task.getLocation();
	}

	/**
	 * Receives notification that properties have been expanded in an attribute
	 */
	void propertiesExpanded(final String originalValue, final String expanded) {
		// write expanded values (if any) in place of original values
		// (would be interesting to have both)
		for (final Map.Entry<String, String> entry : fAttributes.entrySet()) {
			final String unexpandedValue = (String) entry.getValue();
			if (originalValue.equals(unexpandedValue)) {
				LOG.debug("Replacing value with expanded value: " + expanded);
				entry.setValue(expanded);
			}
		}
	}

	/**
	 * @param result the child to add
	 */
	public void addChild(final StepResult result) {
		if (result == null)
			throw new IllegalArgumentException("child can't be null");

		result.fParent = this;
		fChildren.add(result);
	}

	/**
	 * Adds report information for non executed child tasks
	 *
	 * @param iter the iterator over {@link RuntimeConfigurable} child task
	 */
	protected void addLostChildren(final Iterator iter) {
		while (iter.hasNext()) {
			final RuntimeConfigurable child = (RuntimeConfigurable) iter.next();
			addChild(new StepResult(child));
		}
	}

	/**
	 * Tries to find the child tasks that haven't been executed and adds them to the current result.
	 *
	 * @param task the failing task
	 */
	protected void addNotExecutedChildren(final Task task) {
		final RuntimeConfigurable taskWrapper = task.getRuntimeConfigurableWrapper();
		// may happens that no wrapper is available when the task has not been created by ant
		// like for some special WebTest tasks (that should probably disappear)
		if (taskWrapper == null) {
			LOG.debug("No wrapper found for task " + task + ", skipping lost children search");
			return;
		}

		final int iNbKnownChildren = getChildren().size();

		final List children = EnumerationUtils.toList(taskWrapper.getChildren());
		if (iNbKnownChildren < children.size()) {
			addLostChildren(children.listIterator(iNbKnownChildren));
		}

	}

	/**
	 * Gets the attributes of the task
	 *
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return fAttributes;
	}

	/**
	 * @return the children.
	 */
	public List<StepResult> getChildren() {
		return fChildren;
	}

	/**
	 * Gets the duration of the task execution.
	 *
	 * @return the task duration, <code>-1</code> if the task hasn't be
	 *         executed
	 */
	public long getDuration() {
		if (isCompleted()) {
			return getEndDate().getTime() - getStartDate().getTime();
		}
		return -1;
	}

	/**
	 * Gets the date at which the execution of the task finished
	 *
	 * @return <code>null</code> if the task has not been completed
	 */
	public Date getEndDate() {
		return fEndDate;
	}

	public List getHtmlParserMessages() {
		return fHtmlParserMessages;
	}

	/**
	 * Gets the location of the task
	 *
	 * @return <code>null</code> if unknown
	 */
	public Location getLocation() {
		return fLocation;
	}

	/**
	 * @return the parent result, <code>null</code> for the top most result
	 */
	StepResult getParent() {
		return fParent;
	}

	/**
	 * Gets the date at which the execution of the task started
	 *
	 * @return <code>null</code> if the task has not been started
	 */
	public Date getStartDate() {
		return fStartDate;
	}

	/**
	 * Gets the description of the task
	 *
	 * @return <code>null</code> if unknown or no description set
	 */
	public String getTaskDescription() {
		return fTaskDescription;
	}

	/**
	 * @return the task name.
	 */
	public String getTaskName() {
		return fTaskName;
	}

	/**
	 * Indicates if the step has been executed (may be successfull or failed)
	 *
	 * @return <code>true</code> if executed
	 */
	public boolean isCompleted() {
		return getEndDate() != null;
	}

	/**
	 * Indicates if the step has been successfully completed
	 *
	 * @return <code>true</code> if successful
	 */
	public boolean isSuccessful() {
		return fSuccessfull;
	}

	/**
	 * Informs the result that the task if finished, allowing to stop the timer
	 *
	 * @param throwable			the exception thrown by the task (if any)
	 * @param liHtmlParserMessages the list of html parser messages associated to the just finished task
	 */
	void taskFinished(final Task task, final Throwable throwable, final List liHtmlParserMessages) {
		fEndDate = new Date();
		fSuccessfull = throwable == null;
		fHtmlParserMessages = liHtmlParserMessages;

		addNotExecutedChildren(task);
	}

	/**
	 * Adds results of the step execution
	 *
	 * @param results
	 */
	void addStepResults(final Map<String, String> results) {
		fAttributes.putAll(results);
	}
	
	/**
	 * Gets the task attribute value
	 * @param key the attribute name (case insensitive)
	 * @return the value, <code>null</code> if task doesn't have this attribute
	 */
	String getAttribute(final String key)
	{
		if (key == null)
			return null;

		for (final Iterator iter=fAttributes.entrySet().iterator(); iter.hasNext();)
		{
			final Map.Entry entry = (Map.Entry) iter.next();
			if (key.equalsIgnoreCase((String) entry.getKey()))
				return (String) entry.getValue();
		}
		return null;
	}
}
