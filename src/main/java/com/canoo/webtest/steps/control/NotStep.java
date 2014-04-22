// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.control;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.AbstractStepContainer;

/**
 * A NotStep accepts a single test step or a sequence of test steps
 * and expects upon execution that <em>each</em> of these steps fails.
 * <p/>
 * It serves basically as container and simply forwards the execution
 * to each of the contained steps preserving the initial order in which
 * the steps were provided. Each of the steps <em>must</em> raise a
 * StepFailedException. As soon as a step does not do that, the NotStep
 * step is considered to have failed and raises itself a TestStepFailedError.
 *
 * @author Carsten Seibert
 * @webtest.step category="Core"
 * name="not"
 * description="This step encapsulates one or more test steps that are ALL expected to fail. Any kind of step can be nested."
 */
public class NotStep extends AbstractStepContainer {
    private static final Logger LOG = Logger.getLogger(NotStep.class);
    public static final String DEFAULT_DESCRIPTION = "not";

    /**
     * Forward the execution to each of the wrapped steps. Each step must raise a StepFailedException.
     * As soon as the first step passes, i.e. does not raise StepFailedException, execution is
     * terminated.
     *
     * @throws com.canoo.webtest.engine.StepFailedException
     *          Raises this exception if one of the wrapped steps not fails
     */
    public void doExecute() throws Exception {

        boolean allStepsFailed = true;
        Task currentStep = null;
        for (final Iterator iter = getSteps().iterator(); iter.hasNext() && allStepsFailed;) {
            try {
                currentStep = (Task) iter.next();
                executeContainedStep(currentStep);
                allStepsFailed = false;
            }
            catch (final BuildException e) {
            	if (StepFailedException.isCausedByStepFailedException(e))
            	{
                    LOG.debug("Ignoring expected exception: " + e.getMessage());
            	}
            	else
            	{
            		LOG.debug("Rethrowing exception");
            		throw e;
            	}
            }
            catch (final Exception e)
            {
            	LOG.debug("Step failed", e);
            	throw e;
            }
            finally
            {
            	LOG.debug("finished NOT step");
            }
        }
        if (!allStepsFailed) {
            final StringBuffer message = new StringBuffer("Wrapped step did not fail");
            if (currentStep.getDescription() != null)
            {
            	message.append(": ");
            	message.append(currentStep.getDescription());
            }
            throw new StepFailedException(message.toString(), this);
        }
    }
}
