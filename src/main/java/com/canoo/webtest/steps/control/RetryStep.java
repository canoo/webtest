// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.control;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @webtest.step
 *   category="Extension"
 *   name="retry"
 *   description="This step encapsulates one or more test steps that should pass eventually but may fail initially. Any kind of step can be nested."
 */
public class RetryStep extends MultipleExecutionStepContainer {
	private static final Logger LOG = Logger.getLogger(RetryStep.class);
	private static final String DEFAULT_COUNTERNAME = "count";
	private String fMaxcount;
	private String fCounterName = DEFAULT_COUNTERNAME;
	private int fCount;

    public String getCounterName() {
        return fCounterName;
    }

    /**
	 * @param counterName
	 * @webtest.parameter
	 *   required="no"
	 *   default="count"
	 *   description="The name that shall be used to reference the current repetition counter."
	 */
	public void setCounterName(final String counterName) {
		fCounterName = counterName;
	}

	/**
	 * @param maxCount
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The number of times to try running the included steps. Counter values start at 0 and go up to count-1."
	 */
	public void setMaxcount(final String maxCount) {
		fMaxcount = maxCount;
	}

	public String getMaxcount() {
		return fMaxcount;
	}

	public void doExecute() throws Exception {
		boolean allOk = false;

		for (int i = 0; i < fCount && !allOk; i++) {
			LOG.debug("count = " + i + "/" + (fCount - 1));
			setWebtestProperty(getCounterName(), Integer.toString(i));
			
			final Task iteration = createIterationWrapper("Retry " + i + "/" + (fCount - 1));

			try {
				iteration.perform();
				allOk = true;
			} catch (final BuildException accepted) {
				// ok.
			}
		}
		if (!allOk) {
			throw new StepFailedException("Failed - retried the nested steps " + fCount + " time(s) without success",
			   this);
		}
	}

	protected void verifyParameters() {
		super.verifyParameters();
		fCount = ConversionUtil.convertToIntOrReject("Retry Count", getMaxcount(), this);
		if (fCount < 0) {
			throw new StepExecutionException("Retry count must be set and greater than or equal to 0!", this);
		}
	}
}
