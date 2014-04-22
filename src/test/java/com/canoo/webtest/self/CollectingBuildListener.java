package com.canoo.webtest.self;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

/**
 * Simple implementation of {@link org.apache.tools.ant.BuildListener} that collects the received events.
 * @author Marc Guillemot
 */
public class CollectingBuildListener implements BuildListener
{
	/**
	 * Holds the information about a collected event.
	 */
	public static class CollectedBuildEvent
	{
		private final BuildEvent fEvent;
		private final String fEventName;

		public CollectedBuildEvent(final String eventName, final BuildEvent event)
		{
			fEvent = event;
			fEventName = eventName;
		}

		/**
		 * Gets the catched event
		 * @return the event.
		 */
		public BuildEvent getEvent()
		{
			return fEvent;
		}

		/**
		 * Gets the name of the event (names are given after the method names of
		 * {@link BuildListener}: "taskFinished", "taskStarted", ...)
		 * @return the name of the event
		 */
		public String getEventName()
		{
			return fEventName;
		}

		/**
		 * Tests equality for eventName and following properties on the collected event:
		 * task, target, project, exception
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(final Object obj)
		{
			if (obj instanceof CollectedBuildEvent)
			{
				final CollectedBuildEvent other = (CollectedBuildEvent) obj;
				final EqualsBuilder eb = new EqualsBuilder();
				eb.append(getEventName(), other.getEventName());

				final BuildEvent otherEvent = other.getEvent();
				eb.append(fEvent.getTask(), otherEvent.getTask());
				eb.append(fEvent.getTarget(), otherEvent.getTarget());
				eb.append(fEvent.getProject(), otherEvent.getProject());
				eb.append(fEvent.getException(), otherEvent.getException());
				return eb.isEquals();
			}
			return false;
		}

        /** Generate hashCode consistent with equals() method. */
        public int hashCode() {
            // seed with a hard-coded, randomly chosen, non-zero, odd number
            // ideally different to any other uses of this builder
            return new HashCodeBuilder(7, 47).
                    append(fEventName).
                    append(fEvent.getTask()).
                    append(fEvent.getTarget()).
                    append(fEvent.getProject()).
                    append(fEvent.getException()).
                    toHashCode();
        }

        /**
		 * Just for debugging
		 * @return a simple representation
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			final ToStringBuilder tsb = new ToStringBuilder(this);
			tsb.append(getEventName());
			tsb.append("task", getEvent().getTask());
			tsb.append("target", getEvent().getTarget());
			tsb.append("project", getEvent().getProject());
			return tsb.toString();
		}
	}

	private final List fCollectedEvents = new ArrayList();

	/**
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(final BuildEvent event)
	{
		fCollectedEvents.add(new CollectedBuildEvent("buildFinished", event));
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(final BuildEvent event)
	{
		fCollectedEvents.add(new CollectedBuildEvent("buildStarted", event));
	}

	/**
	 * Gets the collected events
	 * @return a list of {@link CollectedBuildEvent}s
	 */
	public List getCollectedEvents()
	{
		return fCollectedEvents;
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
	 */
	public void messageLogged(final BuildEvent event)
	{
		fCollectedEvents.add(new CollectedBuildEvent("messageLogged", event));
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(final BuildEvent event)
	{
		fCollectedEvents.add(new CollectedBuildEvent("targetFinished", event));
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(final BuildEvent event)
	{
		fCollectedEvents.add(new CollectedBuildEvent("targetStarted", event));
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(final BuildEvent event)
	{
		fCollectedEvents.add(new CollectedBuildEvent("taskFinished", event));
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(final BuildEvent event)
	{
		fCollectedEvents.add(new CollectedBuildEvent("taskStarted", event));
	}
}
