package com.canoo.webtest.steps.control;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;

import com.canoo.webtest.steps.AbstractStepContainer;

/**
 * Base class for containers requiring multiple execution of their nested steps.
 * @author Marc Guillemot
 */
public abstract class MultipleExecutionStepContainer extends AbstractStepContainer {
    private static final Logger LOG = Logger.getLogger(MultipleExecutionStepContainer.class);

    /**
     * Create a new task wrapping the tasks of this wrapper to execute them
     * @param description the description for the wrapper task
     * @return the task holding the subtasks
     */
	protected Task createIterationWrapper(final String description)
	{
        final GroupStep group = new GroupStep();
		group.setProject(getProject());
		group.setTaskName("iteration wrapper");

		group.setLocation(getLocation());
		group.setOwningTarget(getOwningTarget());
		group.setDescription(description);

        final RuntimeConfigurable wrapper = new RuntimeConfigurable(group, "group");
        wrapper.setAttribute("description", description);

        // copy the children: both the UnknownElement and the associated RuntimeConfigurable
        final Enumeration e = getRuntimeConfigurableWrapper().getChildren();
        while (e.hasMoreElements()) 
        {
            final RuntimeConfigurable r = (RuntimeConfigurable) e.nextElement();
            final UnknownElement unknownElement = (UnknownElement) r.getProxy();
            final UnknownElement copy = copy(unknownElement);
            group.addTask(copy);
            wrapper.addChild(copy.getWrapper());
        }

        return group;
	}

	private UnknownElement copy(final UnknownElement ue) {
		final UnknownElement ret = new UnknownElement(ue.getTag());
		if (ue.getNamespace() != null)
			ret.setNamespace(ue.getNamespace());
		ret.setProject(getProject());
		ret.setQName(ue.getQName());
		ret.setTaskType(ue.getTaskType());
		ret.setTaskName(ue.getTaskName());
		ret.setLocation(ue.getLocation());
		ret.setOwningTarget(getOwningTarget());
		
		final RuntimeConfigurable rc = new RuntimeConfigurable(ret, ue.getTaskName());
		rc.setPolyType(ue.getWrapper().getPolyType());
		
		final Map m = ue.getWrapper().getAttributeMap();
		for (final Iterator i = m.entrySet().iterator(); i.hasNext();) 
		{
			final Map.Entry entry = (Map.Entry) i.next();
			rc.setAttribute((String) entry.getKey(), (String) entry.getValue());
		}
		rc.addText(ue.getWrapper().getText().toString());
		
		final Enumeration e = ue.getWrapper().getChildren();
		while (e.hasMoreElements()) 
		{
			final RuntimeConfigurable r = (RuntimeConfigurable) e.nextElement();
			final UnknownElement unknownElement = (UnknownElement) r.getProxy();
			final UnknownElement child = copy(unknownElement);
			rc.addChild(child.getWrapper());
			ret.addChild(child);
		}
		return ret;
	}
}
