// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

import com.canoo.webtest.interfaces.IStepSequence;

/**
 * Common abstract base class for all container steps.<p>
 * <p/>
 * This is a container class for a list of AbstractTestSpecificationSteps. It is used for
 * the <code>&lt;steps&gt;</code> element in ant build scripts as well as for the
 * <code>&lt;not&gt;</code> test step elements.
 * <p/>
 * <p>An instance of this class is usually created by ant if it encounters the nested
 * <code>&lt;steps&gt;</code> element within a <code>&lt;webtest&gt;</code> element.
 * <p/>
 * <p><em>Note: The nested element object <code>ClickButton</code> is instantiated and "added" to the
 * <code>AbstractStepSequence</code> before the nested element is completely parsed! No attributes
 * or nested elements of the <code>&lt;clickButton&gt;</code> elements are available at time
 * when it is added to <code>AbstractStepSequence</code>.</em></p>
 *
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @webtest.step
 */
public abstract class AbstractStepContainer extends Step implements TaskContainer, IStepSequence, Serializable, Cloneable
{
    private static final Logger LOG = Logger.getLogger(AbstractStepContainer.class);

    private List fSteps = new ArrayList();

    protected AbstractStepContainer() {
    }

    /**
     * Called by ant to add a nested task to this container.
     *
     * @see org.apache.tools.ant.TaskContainer#addTask(org.apache.tools.ant.Task)
     */
    public void addTask(final Task newTask) {
        fSteps.add(newTask);
    }

    /**
     * @param step
     * @webtest.nested.parameter required="yes"
     * description="A webtest step."
     */
    public void addStep(final Step step) {
        LOG.debug("Adding WebTest Step: " + step);
        addTask(step);
    }

    public List getSteps() {
        return fSteps;
    }

    /**
     * Executes the contained step taking care to setup needed properties before execution
     *
     * @param step the step to execute
     */
    protected void executeContainedStep(final Task step) {
        step.perform();
    }

    /**
     * Executes the contained steps
     */
    protected void executeContainedSteps() {
        for (final Iterator iter = getSteps().iterator(); iter.hasNext();) {
            executeContainedStep((Task) iter.next());
        }
    }
}
