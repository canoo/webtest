package com.canoo.webtest.interfaces;

import org.apache.tools.ant.Project;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.DefaultWebtestCustomizer;
import com.canoo.webtest.reporting.StepExecutionListener;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * This interface is intended for users that need to customize WebTest. When a {@link WebtestTask} is executed,
 * it will look at {@link Project#getReference(String)} with the key {@link #KEY} for a {@link IWebtestCustomizer}.
 * If a {@link IWebtestCustomizer} is found, its methods will be called (see methods descriptions). If now reference is found,
 * a {@link DefaultWebtestCustomizer} will be instantiated and used.<br>
 * <br>
 * <font color="red">Important note</font>: if you use this interface, please notify WebTest committers in order to document it
 * directly in this file. When changes are needed, this will allow to know who really uses which methods.  
 */
public interface IWebtestCustomizer {
	/**
	 * The key that should be used to place a {@link IWebtestCustomizer} as project reference.
	 */
	static final String KEY = "wt.webtestCustomizer";

	/**
	 * Called to create the execution listener that will be responsible for the collection of the results.
	 * @param wt the &lt;webtest&gt; task
	 * @return
	 */
	StepExecutionListener createExecutionListener(final WebtestTask wt);

	/**
	 * Called after the creation of the {@link WebClient}
	 * @param wc a standard instance
	 * @return the same instance with extra configuration or a new one (at your own risk, please respect the normal configuration
	 * possibilities)
	 */
	WebClient customizeWebClient(final WebClient wc);
}
