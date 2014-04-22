package com.canoo.webtest.steps.control;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.AbstractStepContainer;

/**
 * @webtest.step
 *   category="Extension"
 *   name="timedGroup"
 *   description="This step allows nesting of other steps. Any kind of step can be nested. Execution of the group is deemed successful if all the nested steps are executed successfully and the total execution time for the group is less than the time given by the <em>maxSeconds</em> or <em>maxMillis</em> parameters."
 */
public class TimedGroupStep extends AbstractStepContainer {
	private static final Logger LOG = Logger.getLogger(TimedGroupStep.class);

	private String fSeconds;
	private String fMillis;

	/**
	 * Set the maximum number of seconds allowed for this group to execute.
	 *
	 * @param seconds  The new maximum time value
	 * @webtest.parameter
	 * 	required="yes/no"
	 *  description="The maximum number of seconds allowed for this group to execute. Either <em>maxSeconds</em> or <em>maxMillis</em> must be set but not both."
	 */
	public void setMaxSeconds(final String seconds) {
		fSeconds = seconds;
	}

	public String getMaxSeconds() {
		return fSeconds;
	}

	/**
	 * Set the maximum number of milliseconds allowed for this group to execute.
	 *
	 * @param milliseconds  The new maximum time value
	 * @webtest.parameter
	 * 	required="yes/no"
	 *  description="The maximum number of milliseconds allowed for this group to execute. Either <em>maxSeconds</em> or <em>maxMillis</em> must be set but not both."
	 */
	public void setMaxMillis(final String milliseconds) {
		fMillis = milliseconds;
	}

	public String getMaxMillis() {
		return fMillis;
	}

	public void doExecute() throws CloneNotSupportedException {
		notifyStarted(); // TODO: what is this doing here, it's already in Step#execute()
		executeContainedSteps();
		notifyCompleted(); // TODO: what is this doing here, it's already in Step#execute()

		long maxElapsedTimeMillis = getMaxElapsedMillis();
		LOG.debug("Max Time Allowed (millis): " + maxElapsedTimeMillis);
		LOG.debug("Time Taken (millis): " + getDuration());
		if (getDuration() > maxElapsedTimeMillis) {
			throw new StepFailedException("Group took " + getDuration() +
			   " ms to execute but maximum allowed was " + maxElapsedTimeMillis + " ms", this);
		}
		notifySuccess(); // TODO: what is this doing here, it's already in Step#execute()
	}

	long getMaxElapsedMillis() {
        return StringUtils.isEmpty(getMaxMillis()) ? Long.parseLong(getMaxSeconds()) * 1000 : Long.parseLong(getMaxMillis());
	}

	protected void verifyParameters() {
		super.verifyParameters();
        paramCheck(StringUtils.isEmpty(getMaxSeconds()) && StringUtils.isEmpty(getMaxMillis()), "One of 'maxSeconds' or 'maxMillis' must be set.");
        paramCheck(!StringUtils.isEmpty(getMaxSeconds()) && !StringUtils.isEmpty(getMaxMillis()), "Only one of 'maxSeconds' or 'maxMillis' must be set.");
		optionalIntegerParamCheck(getMaxSeconds(), "maxSeconds", true);
		optionalIntegerParamCheck(getMaxMillis(), "maxMillis", true);
	}
}
