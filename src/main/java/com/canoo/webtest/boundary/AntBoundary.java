// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.boundary;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;

/**
 * Boundary class for interacting with ANT.
 *
 * @author Paul King
 */
public final class AntBoundary
{
	private static final Logger LOG = Logger.getLogger(AntBoundary.class);
    private AntBoundary() {}

    public static Task maybeConfigure(final Task newTask) {
		Task task = newTask;
		// to work with ant 1.6
		if (task instanceof UnknownElement) {
			task.maybeConfigure();
			task = ((UnknownElement) task).getTask();
		}
        if (task == null) {
            LOG.warn("Ant returned a null task from maybeConfigure!!");
        } else {
            LOG.debug("Task '" + task.getTaskName() + "' configured with ant (" + System.identityHashCode(task) + ")");
        }
		return task;
	}
}
