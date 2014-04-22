// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Appender conserving the received log messages what allows test to ask him
 * after execution of test code.
 * @author Marc Guillemot
 */
public class BufferingAppender extends AppenderSkeleton
{
	private List<LoggingEvent> fEvents = new Vector<LoggingEvent>();

	public boolean requiresLayout()
	{
		return false;
	}

	public void close()
	{
		// nothing to do
	}

	protected void append(final LoggingEvent event)
	{
		fEvents.add(event);
	}

	/**
	 * Gets the list of received events
	 * @return a list of {@link LoggingEvent}
	 */
	public List getEvents()
	{
		return fEvents;
	}

	/**
	 * Gets a string containing all messages received.
	 */
	public String allMessagesToString()
	{
		final List<LoggingEvent> copy = new ArrayList<LoggingEvent>(fEvents);
		final StringBuffer sb = new StringBuffer();
		for (final Iterator<LoggingEvent> iter = copy.iterator(); iter.hasNext();)
		{
			final LoggingEvent elt = iter.next();
			sb.append(elt.getMessage());
			if (iter.hasNext())
				sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Indicates if one of the received events has the given message
	 */
	public boolean containsMessage(final String message)
	{
		final List<LoggingEvent> copy = new ArrayList<LoggingEvent>(fEvents);
		for (final LoggingEvent elt : copy)
		{
			if (message.equals(elt.getMessage()))
				return true;
		}
		return false;
	}
}