// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.ant;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;

import com.canoo.webtest.steps.AbstractStepContainer;

/**
 * @webtest.step
 *   category="General"
 *   name="steps"
 *   description="This is a nested task of <stepref name='webtest' category='General'/> and is used to define the sequence of test steps to be executed."
 */
public class TestStepSequence extends AbstractStepContainer {
	private static final Logger LOG = Logger.getLogger(TestStepSequence.class);

	public TestStepSequence() {
		LOG.debug("Creating TestStepSequence");
	}

	public void doExecute() throws Exception {
	    LOG.debug("TestStepSequence: doExecute()");

	    if (getSteps().isEmpty()) {
            throw new IllegalArgumentException("At least one step is required in the WebTestSpec!");
        }

		executeSteps();
	}

	/**
	 */
	protected void executeSteps()
	{
    	Task currentStep = null;
    	LOG.debug("Executing steps...");
        for (final Iterator iterator = getSteps().iterator(); iterator.hasNext();) {
            currentStep = (Task) iterator.next();
        	LOG.debug("Executing step " + currentStep.getTaskName() + " " + currentStep);
            currentStep.perform();
            getContext().increaseStepNumber();
        }
        LOG.debug("Step execution finished");
	}
}
