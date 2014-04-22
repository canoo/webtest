// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.steps;

import java.util.List;

import org.apache.tools.ant.Task;

import com.canoo.webtest.self.StepStub;
import com.canoo.webtest.steps.request.InvokePage;

/**
 * Unit tests for {@link AbstractStepContainer}.
 * @author Denis N. Antonioli
 * @author Marc Guillemot
 */
public class AbstractStepContainerTest extends BaseStepTestCase {
	private AbstractStepContainer fAbstractStepContainer;

	/**
	 * Dummy concrete extension for tests
	 */
	static class AbstractStepContainerStub extends AbstractStepContainer {
		public void doExecute() {
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		fAbstractStepContainer = (AbstractStepContainerStub) getStep();
	}

	protected Step createStep()
	{
		return new AbstractStepContainerStub();
	}

	public void testStubCreation() throws Exception {
		assertNotNull(fAbstractStepContainer);
		executeStep(fAbstractStepContainer); // coverage
	}

	public void testClone() throws Exception {
		final InvokePage step1 = new InvokePage();
		step1.setDescription("blah blah");
		step1.setTaskName("invoke");
		step1.setUrl("/my/Super/page.html");
		step1.setProject(fAbstractStepContainer.getProject());
		fAbstractStepContainer.addStep(step1);
		
		final AbstractStepContainer clonedContainer = (AbstractStepContainer) fAbstractStepContainer.clone();
		final List clonedSteps = clonedContainer.getSteps();
		
		assertEquals(1, clonedSteps.size());
		assertEquals(InvokePage.class, clonedSteps.get(0).getClass());
		final InvokePage clonedStep = (InvokePage) clonedSteps.get(0);
		assertEquals(step1.getUrl(), clonedStep.getUrl());
		assertEquals(step1.getDescription(), clonedStep.getDescription());
		assertEquals(step1.getTaskName(), clonedStep.getTaskName());
		assertSame(step1.getProject(), clonedStep.getProject());
	}

	public void testAddTask() {
		final Task task = new Task(){};
		configureTask(task); // would have been done by ant in normal run
        fAbstractStepContainer.addTask(task);

        final Task firstTask = (Task) fAbstractStepContainer.getSteps().get(0);
        assertSame(fAbstractStepContainer.getProject(), firstTask.getProject());
        assertSame(fAbstractStepContainer.getOwningTarget(), firstTask.getOwningTarget());
    }

	public void testAddStep() {
		final Step step = new StepStub();
		configureTask(step); // would have been done by ant in normal run
        fAbstractStepContainer.addTask(step);

        final Step firstStep = (Step) fAbstractStepContainer.getSteps().get(0);
        assertSame(fAbstractStepContainer.getProject(), firstStep.getProject());
    }
}
