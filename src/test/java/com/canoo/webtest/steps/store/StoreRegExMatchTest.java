// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;

import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.UnknownElement;

import com.canoo.webtest.ant.WebtestPropertyHelper;
import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * @author Carsten Seibert
 * @author Paul King
 */
public class StoreRegExMatchTest extends BaseStepTestCase
{
	public class StoreRegExMatchStub extends StoreRegExMatch
    {
		public StoreRegExMatchStub(String text, String group, String propertyName) {
			setText(text);
			setGroup(group);
			setProperty(propertyName);
		}
	}

	protected Step createStep() {
		return new StoreRegExMatch();
	}

	public void testVerifyParameters() throws Exception {
		final StoreRegExMatch step = new StoreRegExMatchStub("x", "-1", "X");
		configureStep(step);
		assertErrorOnExecute(step,
		   "Should raise StepExecutionException due to invalid parameter",
		   "group parameter with value '-1' must not be negative");
		step.setGroup("0");
		step.setProperty(null);
		checkStepRejectsEmptyParam(step, "property");
		step.setProperty("myProp");
		step.setText(null);
		checkStepRejectsEmptyParam(step, "Regular expression (text attribute)");
		String documentText = "XXX";
		step.setText(documentText);
		getContext().setDefaultResponse(documentText);
		step.execute();
	}

	private void checkStepRejectsEmptyParam(final StoreRegExMatch step, String param) {
		assertStepRejectsEmptyParam(param, new TestBlock() {
			public void call() throws Exception {
                step.execute();
			}
		});
	}

	public void testGrouping() throws Exception {
		StoreRegExMatchStub step = new StoreRegExMatchStub("X(.*)(X(.*)X)((?i:X\\d+))", "0", "myProp");
		getContext().setDefaultResponse("X11X22Xx33");

		executeStep(step);
		assertEquals("Group 0 (whole match)", "X11X22Xx33", step.getWebtestProperty("myProp"));

		step.setGroup("1");
		executeStep(step);
		assertEquals("Group 1", "11", step.getWebtestProperty("myProp"));

		step.setGroup("2");
		executeStep(step);
		assertEquals("Group 2 (outer nested)", "X22X", step.getWebtestProperty("myProp"));

		step.setGroup("3");
		executeStep(step);
		assertEquals("Group 3 (inner nested)", "22", step.getWebtestProperty("myProp"));

		step.setGroup("4");
		executeStep(step);
		assertEquals("Group 4 (last x...)", "x33", step.getWebtestProperty("myProp"));
	}

	public void testInvalidGroup() throws Exception {
		final StoreRegExMatchStub step = new StoreRegExMatchStub("X(.*)X(.*)X", "3", "myProp");
		getContext().setDefaultResponse("X11X22X");
		
		final Throwable t = ThrowAssert.assertThrows("referencing an invalid group",
		   StepFailedException.class, new TestBlock() {
			   public void call() throws Exception {
				   executeStep(step);
			   }
		   });
		assertTrue(t.getMessage().startsWith("Group not found:"));
	}

	public void testExceptionIfNoCurrentResponse() throws Exception {
		final StoreRegExMatchStub step = new StoreRegExMatchStub("X(.*)X(.*)X", "1", "myProp");

		assertStepRejectsNullResponse(step);
	}

	public void testExceptionIfNoMatch() throws Exception {
		final StoreRegExMatchStub step = new StoreRegExMatchStub("X(.*)X(.*)X", "1", "myProp");
		final Throwable t = ThrowAssert.assertThrows("no match available", StepFailedException.class, new TestBlock() {
			public void call() throws Exception {
				executeStep(step);
			}
		});
		assertTrue(t.getMessage().startsWith("No match for regular expression"));
	}

	public void testSetWebtestProperties() throws Exception {
		getProject().addTaskDefinition("storeRegExMatch", StoreRegExMatch.class);
        final UnknownElement task = new UnknownElement("storeRegExMatch");
        task.setTaskName("storeRegExMatch");

        RuntimeConfigurable wrapper = new RuntimeConfigurable(task, task.getTaskName());
        wrapper.setAttribute("text", "X(#{wildcard})(X(#{wildcard})X)");
        wrapper.setAttribute("group", "0");
        wrapper.setAttribute("property", "myProp");

		configureTask(task);
		final WebtestTask webtest = getContext().getWebtest();
		WebtestPropertyHelper.configureWebtestPropertyHelper(getProject());
		webtest.setDynamicProperty("wildcard", ".*");

		getContext().setDefaultResponse("X11X22X");
		task.perform();
		assertEquals("Group 0 (whole match)", "X11X22X", webtest.getDynamicProperty("myProp"));
	}
}

