// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.ant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.UnsupportedElementException;
import org.apache.tools.ant.taskdefs.Echo;

import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.reporting.IResultReporter;
import com.canoo.webtest.reporting.ReportCreationException;
import com.canoo.webtest.reporting.RootStepResult;
import com.canoo.webtest.reporting.StepResult;
import com.canoo.webtest.self.CollectingBuildListener;
import com.canoo.webtest.self.CollectingBuildListener.CollectedBuildEvent;
import com.canoo.webtest.self.LogCatchingTestCase;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Unit tests for {@link WebtestTask}
 * @author Unknown
 * @author Marc Guillemot
 */
public class WebtestTaskTest extends LogCatchingTestCase {
	private WebtestTask fTask;
	private Configuration fConfig;
	private Project fProject;
	private Target fTarget;

	protected void setUp() throws Exception {
		super.setUp();
		fProject = new Project();
		fTarget = new Target();
		fTask = new WebtestTask();
		fTask.setProject(fProject);
		fTask.setOwningTarget(fTarget);
		fTask.setName("foo");
		final Context context = new Context(fTask);
		WebtestTask.setThreadContext(context);
		fConfig = new Configuration();
		fConfig.setProject(fProject);
		fConfig.setOwningTarget(fTarget);
		fConfig.setResultpath(BaseStepTestCase.getTemporaryResultPathFolder());
		fTask.addConfig(fConfig);
		setUpCatchLoggerMessages();
	}

	public void testEvents()
	{
    	final CollectingBuildListener listener = new CollectingBuildListener()
    	{
    		public void messageLogged(final BuildEvent event)
    		{
    			// do nothing, ignore message;
    		}
    	};
    	fProject.addBuildListener(listener);
		fTask.addConfig(fConfig);
		final TestStepSequence steps = new TestStepSequence();
		steps.setProject(fProject);
		steps.setOwningTarget(fTarget);
		final Step step = dummyStep();
		steps.addStep(step);

		BaseStepTestCase.registerDummyResultReporter(fProject);
		fTask.addSteps(steps);
		fTask.perform();

		final List<CollectedBuildEvent> expectedEvents = new ArrayList<CollectedBuildEvent>();
		expectedEvents.add(new CollectedBuildEvent("taskStarted", new BuildEvent(fTask)));
		expectedEvents.add(new CollectedBuildEvent("taskStarted", new BuildEvent(fConfig)));
		expectedEvents.add(new CollectedBuildEvent("taskFinished", new BuildEvent(fConfig)));
		expectedEvents.add(new CollectedBuildEvent("taskStarted", new BuildEvent(steps)));
		expectedEvents.add(new CollectedBuildEvent("taskStarted", new BuildEvent(step)));
		expectedEvents.add(new CollectedBuildEvent("taskFinished", new BuildEvent(step)));
		expectedEvents.add(new CollectedBuildEvent("taskFinished", new BuildEvent(steps)));
		expectedEvents.add(new CollectedBuildEvent("taskFinished", new BuildEvent(fTask)));

        assertEquals(expectedEvents, listener.getCollectedEvents());
	}

	/**
	 * Test the equality of the content of the collections
	 * @param expected the expected elements
	 * @param actual the actual elements
	 */
	protected void assertEquals(final Collection<?> expected, final Collection<?> actual)
	{
		final Iterator<?> iterExpected = expected.iterator();
		final Iterator<?> iterActual = actual.iterator();
		int i = 0;
		while (iterExpected.hasNext())
		{
			if (!iterActual.hasNext())
				fail("Expected " + expected.size() + " elements, got " + actual.size() + "(the first " + i + " are equals)");

			assertEquals("Element " + i, iterExpected.next(), iterActual.next());
			++i;
		}

		if (iterActual.hasNext())
			fail("Expected " + expected.size() + " elements, got " + actual.size() + "(the first " + i + " are equals)");

	}

	/**
	 * Unit tests for {@link #assertEquals(Collection, Collection)}
	 */
	public void testAssertEqualsCollection() {
		assertEquals(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
		assertEquals(Collections.singleton("foo"), Collections.singletonList("foo"));

		testAssertEqualsCollectionFails(Collections.singleton("foo"), Collections.EMPTY_LIST);
		testAssertEqualsCollectionFails(Collections.EMPTY_LIST, Collections.singleton("foo"));
	}

	/**
	 * Utility method for {@link #testAssertEqualsCollection()}
	 */
	private void testAssertEqualsCollectionFails(final Collection<?> colExpected, final Collection<?> colActual)
	{
		ThrowAssert.assertThrows(AssertionFailedError.class, new TestBlock()
		{
			public void call()
			{
				assertEquals(colExpected, colActual);
			}
		});
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		tearDownCatchLoggerMessages();
	}

	private Step dummyStep() {
		final Step step = new Step()
		{
			public void doExecute() throws Exception
			{
				// nothing
			}
		};
		step.setProject(fProject);
		step.setOwningTarget(fTarget);
		return step;
	}

	protected EasyRootStepResult buildRootStepResult()
	{
		final TestStepSequence steps = new TestStepSequence();
        return new EasyRootStepResult(steps);
	}

	public void testStopOnError() {
		fConfig.setHaltonerror(true);
		final EasyRootStepResult result = buildRootStepResult();
		result.setLastFailingTaskResult(new StepResult("myTask"), new RuntimeException());
		ThrowAssert.assertThrows(BuildException.class, new TestBlock() {
			public void call() throws Exception {
				fTask.stopBuildIfNeeded(result, fConfig);
			}
		});
		fConfig.setHaltonerror(false);
		fTask.stopBuildIfNeeded(result, fConfig);
		// runs without exception
	}

	public void testSetsErrorProperty() {
		fConfig.setHaltonerror(false);
		fConfig.setErrorProperty("errorProp");
		final EasyRootStepResult result = buildRootStepResult();
		result.setLastFailingTaskResult(new StepResult("myTask"), new RuntimeException());
		fTask.stopBuildIfNeeded(result, fConfig);
        assertEquals("error property should be set to true", "true",
                fTask.getProject().getProperties().get("errorProp"));
	}

	public void testStopOnFailure() {
		fConfig.setHaltonfailure(true);
		final EasyRootStepResult result = buildRootStepResult();
		result.setLastFailingTaskResult(new StepResult("myTask"), new StepFailedException("bla"));
		
		final StepResult failingStepResult = new StepResult("myTask");
		result.addChild(failingStepResult);
		result.setLastFailingTaskResult(failingStepResult, new StepFailedException("bla"));
		ThrowAssert.assertThrows(BuildException.class, new TestBlock() {
			public void call() throws Exception {
				fTask.stopBuildIfNeeded(result, fConfig);
			}
		});
		fConfig.setHaltonfailure(false);
		fTask.stopBuildIfNeeded(result, fConfig);
		// runs without exception
	}

	public void testSetsFailureProperty() {
		fConfig.setHaltonfailure(false);
		fConfig.setFailureProperty("failureProp");
		final EasyRootStepResult result = buildRootStepResult();

		final StepResult failingStepResult = new StepResult("myTask");
		result.addChild(failingStepResult);
		result.setLastFailingTaskResult(failingStepResult, new StepFailedException("bla"));

		fTask.stopBuildIfNeeded(result, fConfig);
        assertEquals("failure property should be set", "true",
                fTask.getProject().getProperties().get("failureProp"));
	}

	public void testSetConfig() {
		fTask.addConfig(fConfig);
		assertEquals("checking config was set ok", fConfig, fTask.getConfig());
	}

	public void testNoReportIfNoSummary() {
		fTask.addConfig(fConfig);
		fTask.writeTestReportIfNeeded(null);
		// runs without NPE
	}

	public static void testInternalAssertions() {
		final WebtestTask task = new WebtestTask();
		ThrowAssert.assertThrows(BuildException.class, new TestBlock() {
			public void call() throws Exception {
				task.assertNotNull(null, new String[]{"", "", "", ""});
			}
		});
	}

	public void testReporterWithClassNotFoundException() {
		getSpoofAppender().getEvents().clear();
		fTask.callSelectedReporter("unknown.class", null);
		assertEquals("Exception caught while writing test report",
			getSpoofAppender().allMessagesToString());
	}

	public void testReporterWithReportCreationException() throws Exception {
		getSpoofAppender().getEvents().clear();
		final IResultReporter reporter = (IResultReporter) mock(IResultReporter.class, "reporter");
		reporter.generateReport(null);
		modify().throwException(new ReportCreationException(new RuntimeException("xxx")));
        startVerification();
		fTask.report(reporter, null);
		final LoggingEvent ev = (LoggingEvent) getSpoofAppender().getEvents().get(0);
		assertEquals("xxx", ev.getThrowableInformation().getThrowable().getMessage());
	}

	public void testHelperForCoverage() {
		// coverage hacks
		getSpoofAppender().getEvents().clear();
		// generate a message of any kind
		fTask.callSelectedReporter("unknown.class", null);
		/* ignore true case */ getSpoofAppender().containsMessage("Exception caught while writing test report");
		/* ignore false */ getSpoofAppender().containsMessage("dummy");
		/* ignore */ getSpoofAppender().close();
		/* ignore */ getSpoofAppender().requiresLayout();
		// generate a second message
		fTask.callSelectedReporter("unknown.class", null);
		/* ignore */ getSpoofAppender().allMessagesToString();
	}

	public void testAddTask()
	{
		final Configuration defaultConfig = new Configuration();
		final TestStepSequence steps = new TestStepSequence();

		final WebtestTask task = new WebtestTask()
		{
			public void addConfig(final Configuration config)
			{
				super.addConfig(config);
				assertSame(defaultConfig, config);
			}
			public void addSteps(final TestStepSequence steps)
			{
				super.addSteps(steps);
				assertSame(steps, steps);
			}
		};
		task.addTask(defaultConfig);
		task.addTask(steps);

		ThrowAssert.assertThrows(UnsupportedElementException.class, "", new TestBlock()
			{
				public void call()
				{
					task.addTask(new Echo());
				}
			});
	}

	/**
	 * Test that a default configuration is set if no one was specified in ant file
	 * @throws Exception
	 *
	 */
	public void testExecuteUsesDefaultConfiguration() throws Exception {
		final Configuration defaultConfig = new Configuration();
		final WebtestTask task = new WebtestTask() {
			protected Configuration createDefaultConfiguration() {
                return defaultConfig;
			}
		};
		task.setName("foo");
		task.setProject(fProject);

        task.addTask(new Echo());

        assertSame(defaultConfig, task.getConfig());
	}

	public void testExecuteSetWebtestVersion() {
		assertNull(fProject.getProperty("webtest.version"));
		fConfig.setSaveresponse(false);
		fConfig.setProject(fProject);
		fConfig.setOwningTarget(fTarget);
		fTask.addConfig(fConfig);

		final TestStepSequence steps = new TestStepSequence();
		steps.setProject(fProject);
		steps.setOwningTarget(fTarget);
		steps.addStep(dummyStep());
		fTask.addSteps(steps);
		fTask.execute();
		assertNotNull(fProject.getProperty("webtest.version"));
	}
	
	/**
	 * When <steps> is omitted, we should fill it as would Ant do
	 */
	public void testImplicitsSteps()
	{
		final WebtestTask webtest = new WebtestTask();
		webtest.setProject(new Project());
		final Step step = dummyStep();
		webtest.addTask(step);
		assertEquals(1, webtest.getStepSequence().getSteps().size());
		final RuntimeConfigurable stepsWrapper = webtest.getStepSequence().getRuntimeConfigurableWrapper();
		assertEquals(1, EnumerationUtils.toList(stepsWrapper.getChildren()).size());
	}
}

class EasyRootStepResult extends RootStepResult
{
	EasyRootStepResult(final TestStepSequence step)
	{
		super(step);
	}
	public void setLastFailingTaskResult(final StepResult stepResult, final Throwable cause)
	{
		super.setLastFailingTaskResult(stepResult, cause);
	}
}