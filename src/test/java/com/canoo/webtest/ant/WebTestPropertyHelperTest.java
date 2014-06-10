// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.ant;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Echo;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.BaseStepTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link WebtestPropertyHelper}.
 * @author Marc Guillemot
 */
public class WebTestPropertyHelperTest extends TestCase
{
    private static final Logger LOG = Logger.getLogger(WebTestPropertyHelperTest.class);
	private Project project;

	public void testReplaceProperties()
	{
		final WebtestTask webtest = new WebtestTask();
		WebtestTask.setThreadContext(new Context(webtest));
		project = new Project();

		WebtestPropertyHelper.configureWebtestPropertyHelper(project);
		testReplacement("testtool", "testtool");
		testReplacement("testtool browser", "testtool browser");
		testReplacement("${testtool} #{browser}", "${testtool} #{browser}");
		testReplacement("${testtool.built.on.#{browser}}", "${testtool.built.on.#{browser}}");
		testReplacement("#{browser.of.${testtool}}", "#{browser.of.${testtool}}");

		// define ant property "testtool"
		project.setProperty("testtool", "WebTest");
		testReplacement("testtool", "testtool");
		testReplacement("testtool browser", "testtool browser");
		testReplacement("WebTest #{browser}", "${testtool} #{browser}");
		testReplacement("${testtool.built.on.#{browser}}", "${testtool.built.on.#{browser}}");
		testReplacement("#{browser.of.${testtool}}", "#{browser.of.${testtool}}");

		// define dynamic property "browser"
		webtest.setDynamicProperty("browser", "HtmlUnit");
		testReplacement("testtool", "testtool");
		testReplacement("testtool browser", "testtool browser");

		testReplacement("HtmlUnit", "#{browser}");
		testReplacement("WebTest HtmlUnit", "${testtool} #{browser}");
		testReplacement("${testtool.built.on.#{browser}}", "${testtool.built.on.#{browser}}");
		testReplacement("#{browser.of.${testtool}}", "#{browser.of.${testtool}}");

		// define dynamic property "browser.of.WebTest"
		webtest.setDynamicProperty("browser.of.WebTest", "embedded HtmlUnit");
		testReplacement("embedded HtmlUnit", "#{browser.of.${testtool}}");

		// define ant property "testtool.built.on.HtmlUnit"
		project.setNewProperty("testtool.built.on.HtmlUnit", "WebTest");
		testReplacement("WebTest", "${testtool.built.on.#{browser}}");
		
		// misc tests
		webtest.setDynamicProperty("foo", "#{foo}");
		testReplacement("#{foo}", "#{foo}");

		webtest.setDynamicProperty("foo2", "#{foo}");
		testReplacement("#{foo}", "#{foo2}");

		project.setProperty("bla", "${bla}");
		testReplacement("${bla}", "${bla}");

		project.setProperty("bla", "#{foo}");
		testReplacement("#{foo}", "${bla}");
    }

    public void testMultipleInvocations()
    {
        final WebtestTask webtest = new WebtestTask();
        WebtestTask.setThreadContext(new Context(webtest));
        project = new Project();

        project.setProperty("foo", "bar");
        webtest.setDynamicProperty("foo", "bar");

        testReplacement("bar", "${foo}");
        testReplacement("#{foo}", "#{foo}");

        // Set up the Webtest Property Helper
        WebtestPropertyHelper.configureWebtestPropertyHelper(project);
        testReplacement("bar", "${foo}");
        testReplacement("bar", "#{foo}");

        // Set it up a second time to simulate multiple tests within the same project
        WebtestPropertyHelper.configureWebtestPropertyHelper(project);
        testReplacement("bar", "${foo}");
        testReplacement("bar", "#{foo}");

        // Clean up the property helper once to simulate one of the tests finishing
        WebtestPropertyHelper.cleanWebtestPropertyHelper(project);

        // Dynamic and Ant property replacement should continue to work
        testReplacement("bar", "${foo}");
        testReplacement("bar", "#{foo}");

        // Clean up the property helper again to simulate the second test finishing
        WebtestPropertyHelper.cleanWebtestPropertyHelper(project);

        // Ant property replacement should continue to work
        testReplacement("bar", "${foo}");

        // Dynamic property replacement should not work anymore
        testReplacement("#{foo}", "#{foo}");
    }

    public void testParallelUse() throws Exception {
        project = new Project();
        project.setProperty("foo", "bar");

        final Thread thread1 = new Thread("WebTest1")
        {
            @Override
            public void run() {
                final WebtestTask webtest = new WebtestTask();
                WebtestTask.setThreadContext(new Context(webtest));
                webtest.setDynamicProperty("foo", "bar");

                try { Thread.sleep(500); } catch (InterruptedException ignore) {}
                // Set up the Webtest Property Helper
                WebtestPropertyHelper.configureWebtestPropertyHelper(project);

                // Property Replacement should work
                testReplacement("bar", "${foo}");
                testReplacement("bar", "#{foo}");

                // Clean up the property helper once to simulate one of the tests finishing
                WebtestPropertyHelper.cleanWebtestPropertyHelper(project);
            }
        };

        final Thread thread2 = new Thread("WebTest2")
        {
            @Override
            public void run() {
                final WebtestTask webtest = new WebtestTask();
                WebtestTask.setThreadContext(new Context(webtest));
                webtest.setDynamicProperty("foo", "bar");

                // Set up the Webtest Property Helper
                WebtestPropertyHelper.configureWebtestPropertyHelper(project);

                // Property Replacement should work
                testReplacement("bar", "${foo}");
                testReplacement("bar", "#{foo}");

                try { Thread.sleep(500); } catch (InterruptedException ignore) {}

                // Clean up the property helper once to simulate one of the tests finishing
                WebtestPropertyHelper.cleanWebtestPropertyHelper(project);
            }
        };

        final List<Throwable> uncaugtExceptions = new ArrayList<Throwable>();
        final Thread.UncaughtExceptionHandler collectingExceptionHandler = new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error(e);
                uncaugtExceptions.add(e);
            }
        };
        thread1.setUncaughtExceptionHandler(collectingExceptionHandler);
        thread1.start();

        thread2.setUncaughtExceptionHandler(collectingExceptionHandler);
        thread2.start();
        thread2.join(5000);
        thread1.join(5000);

        if (!uncaugtExceptions.isEmpty())
        {
            uncaugtExceptions.get(0).printStackTrace(System.err);
            assertTrue("Found exceptions: " + uncaugtExceptions, uncaugtExceptions.isEmpty());
        }

        // Ant property replacement should continue to work
        testReplacement("bar", "${foo}");

        // Dynamic property replacement should not work anymore
        testReplacement("#{foo}", "#{foo}");
    }

    public void testMultipleProjects()
    {
        final WebtestTask webtest = new WebtestTask();
        WebtestTask.setThreadContext(new Context(webtest));
        Project project1 = new Project();
        Project project2 = new Project();

        project1.setProperty("foo", "bar");
        project2.setProperty("bar", "foo");
        webtest.setDynamicProperty("foo", "bar");

        testReplacement(project1, "bar", "${foo}");
        testReplacement(project1, "#{foo}", "#{foo}");

        testReplacement(project2, "foo", "${bar}");
        testReplacement(project2, "#{foo}", "#{foo}");

        // Set up the Webtest Property Helper
        WebtestPropertyHelper.configureWebtestPropertyHelper(project1);
        testReplacement(project1, "bar", "${foo}");
        testReplacement(project1, "bar", "#{foo}");

        // Ensure Project 2 still can't use dynamic properties
        testReplacement(project2, "foo", "${bar}");
        testReplacement(project2, "#{foo}", "#{foo}");

        // Set it up a second time to simulate multiple tests in different projects
        WebtestPropertyHelper.configureWebtestPropertyHelper(project2);
        testReplacement(project1, "bar", "${foo}");
        testReplacement(project1, "bar", "#{foo}");

        // Ensure Project 2 can use dynamic properties
        testReplacement(project2, "foo", "${bar}");
        testReplacement(project2, "bar", "#{foo}");

        // Clean up the property helper once to simulate one of the tests finishing
        WebtestPropertyHelper.cleanWebtestPropertyHelper(project1);

        // Ant property replacement should continue to work for project 1
        testReplacement(project1, "bar", "${foo}");

        // Dynamic property replacement should not work anymore for project1
        testReplacement(project1, "#{foo}", "#{foo}");

        // Dynamic and Ant property replacement should continue to work for project 2
        testReplacement(project2, "foo", "${bar}");
        testReplacement(project2, "bar", "#{foo}");

        // Clean up the property helper again to simulate the second test finishing
        WebtestPropertyHelper.cleanWebtestPropertyHelper(project2);

        // Ant property replacement should continue to work for project1
        testReplacement(project1, "bar", "${foo}");

        // Dynamic property replacement is still not availalbe for project1
        testReplacement(project2, "#{foo}", "#{foo}");

        // Ant property replacement should continue to work for project2
        testReplacement(project2, "foo", "${bar}");

        // Dynamic property replacement should not work anymore for project2
        testReplacement(project2, "#{foo}", "#{foo}");
    }

	private void testReplacement(final String expected, final String original) {
		testReplacement(project, expected, original);
	}

    private void testReplacement(final Project project, final String expected, final String original) {
        final RuntimeConfigurable task = BaseStepTestCase.parseStep(project, Echo.class, "description='" + original + "'");
        task.maybeConfigure(project);
        final UnknownElement elt = (UnknownElement) task.getProxy();

        assertEquals(expected, elt.getDescription());
    }
}
