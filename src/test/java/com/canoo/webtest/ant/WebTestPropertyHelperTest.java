// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.ant;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Echo;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.BaseStepTestCase;

/**
 * Unit tests for {@link WebtestPropertyHelper}.
 * @author Marc Guillemot
 */
public class WebTestPropertyHelperTest extends TestCase
{
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

	private void testReplacement(final String expected, final String original) {
		
		final RuntimeConfigurable task = BaseStepTestCase.parseStep(project, Echo.class, "description='" + original + "'");
		task.maybeConfigure(project);
		final UnknownElement elt = (UnknownElement) task.getProxy();
		
		assertEquals(expected, elt.getDescription());
	}
}
