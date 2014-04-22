package com.canoo.webtest.self;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import com.canoo.webtest.self.CollectingBuildListener.CollectedBuildEvent;

/**
 * Tests for {@link CollectingBuildListener}.
 * @author Marc Guillemot
 */
public class CollectingBuildListenerTest extends TestCase
{
	public void testCollectedBuildEventEquals()
	{
		final Project p = new Project();
		final CollectedBuildEvent event1 = new CollectedBuildEvent("buildStarted", new BuildEvent(p));

		assertFalse(event1.equals(new Object()));
		assertFalse(event1.equals(new CollectedBuildEvent("buildFinished", new BuildEvent(p))));
		assertEquals(event1, new CollectedBuildEvent("buildStarted", new BuildEvent(p)));
	}

	public void testCollectedBuildEventToString()
	{
		final Project p = new Project();
		final CollectedBuildEvent event = new CollectedBuildEvent("buildStarted", new BuildEvent(p));
		assertTrue(event.toString().indexOf("buildStarted") > 0);
	}

	public void testCollectedBuildEventHashCode()
	{
        final Project p = new Project();
        final CollectedBuildEvent event1 = new CollectedBuildEvent("buildStarted", new BuildEvent(p));

        assertTrue(event1.hashCode() != new CollectedBuildEvent("buildFinished", new BuildEvent(p)).hashCode());
        assertEquals(event1.hashCode(), new CollectedBuildEvent("buildStarted", new BuildEvent(p)).hashCode());
    }

	public void testEvents()
	{
		final Project project = new Project();
		final Target target = new Target();
		target.setProject(project);
		final Task task = new Task() {};
		task.setProject(project);
		task.setOwningTarget(target);

		final CollectingBuildListener listener = new CollectingBuildListener();
		int index = 0;

		// build started
		final BuildEvent buildStartedEvent = new BuildEvent(project);
		listener.buildStarted(buildStartedEvent);
		assertEquals(new CollectedBuildEvent("buildStarted", buildStartedEvent), listener.getCollectedEvents().get(index++));

		// target started
		final BuildEvent targetStartedEvent = new BuildEvent(target);
		listener.targetStarted(targetStartedEvent);
		assertEquals(new CollectedBuildEvent("targetStarted", targetStartedEvent), listener.getCollectedEvents().get(index++));

		// task started
		final BuildEvent taskStartedEvent = new BuildEvent(task);
		listener.taskStarted(taskStartedEvent);
		assertEquals(new CollectedBuildEvent("taskStarted", taskStartedEvent), listener.getCollectedEvents().get(index++));

		// task finished
		final BuildEvent taskFinishedEvent = new BuildEvent(task);
		listener.taskFinished(taskFinishedEvent);
		assertEquals(new CollectedBuildEvent("taskFinished", taskFinishedEvent), listener.getCollectedEvents().get(index++));

		// target finished
		final BuildEvent targetFinishedEvent = new BuildEvent(target);
		listener.targetFinished(targetFinishedEvent);
		assertEquals(new CollectedBuildEvent("targetFinished", targetFinishedEvent), listener.getCollectedEvents().get(index++));

		// message logged
		final BuildEvent messageLoggedEvent = new BuildEvent(project);
		listener.messageLogged(messageLoggedEvent);
		assertEquals(new CollectedBuildEvent("messageLogged", messageLoggedEvent), listener.getCollectedEvents().get(index++));

		// build finished
		final BuildEvent buildFinishedEvent = new BuildEvent(project);
		listener.buildFinished(buildStartedEvent);
		assertEquals(new CollectedBuildEvent("buildFinished", buildFinishedEvent), listener.getCollectedEvents().get(index++));
	}
}
