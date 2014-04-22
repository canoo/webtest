package com.canoo.webtest.engine;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

/**
 * {@link org.apache.tools.ant.BuildListener} implementation that does nothing. 
 * It does absolutely nothing but is useful as base class for classes that want to implement
 * only some of the methods of {@link org.apache.tools.ant.BuildListener}.
 * @author Marc Guillemot
 */
public class NOPBuildListener implements BuildListener
{
	/**
	 * Does nothing
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(final BuildEvent event)
	{
		// does nothing
	}

	/**
	 * Does nothing
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(final BuildEvent event)
	{
		// does nothing
	}

	/**
	 * Does nothing
	 * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
	 */
	public void messageLogged(final BuildEvent event)
	{
		// does nothing
	}

	/**
	 * Does nothing
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(final BuildEvent event)
	{
		// does nothing
	}

	/**
	 * Does nothing
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(final BuildEvent event)
	{
		// does nothing
	}

	/**
	 * Does nothing
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(final BuildEvent event)
	{
		// does nothing
	}

	/**
	 * Does nothing
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(final BuildEvent event)
	{
		// does nothing
	}
}
