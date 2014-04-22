// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.ant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.Project;

import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.reporting.RootStepResult;
import com.canoo.webtest.self.ErrorStepStub;
import com.canoo.webtest.self.FailStepStub;
import com.canoo.webtest.self.StepStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.request.InvokePage;

/**
 * Unit tests for {@link TestStepSequence}.
 * @author Unknown
 * @author Marc Guillemot
 */
public class TestStepSequenceTest extends BaseStepTestCase
{
    public void testCreation() {
        final TestStepSequence sequence = new TestStepSequence();
        assertNotNull(sequence);
        try {
            sequence.doExecute(); // coverage
        } catch (Exception ex) {
            // ignore
        }
    }

    protected Step createStep()
    {
    	final Project project = new Project();
    	final WebtestTask webtest = new WebtestTask();
    	webtest.setProject(project);
    	final TestStepSequence step = new TestStepSequence();
    	step.setProject(project);
    	webtest.addSteps(step);
    	return step;
    }

    public void testAddStep() {
    	final TestStepSequence steps = (TestStepSequence) getStep();

        final InvokePage step = new InvokePage();
        step.setDescription("aStepName");
        step.setUrl("aRelativeUrl");
        steps.addStep(step);
        assertEquals("#steps", 1, steps.getSteps().size());
    }

    /**
	 * Creates a dummy step
	 * @return the initialized step
	 */
	protected StepStub createStepStub()
	{
		final StepStub step = new StepStub();
		configureStep(step);
		return step;
	}
	
    public void testNoSteps() {
    	final StepExecutionException exception = (StepExecutionException) 
    		ThrowAssert.assertThrows("", StepExecutionException.class, new TestBlock()
        {
            public void call() throws Exception {
                getStep().execute();
            }
        });
    	
    	assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    public void testExecutionOneEntry() throws IOException {
    	final List<Step> testSteps = new ArrayList<Step>();
        testSteps.add(createStepStub());
        final RootStepResult result = executeStepList(testSteps);
        checkResult(result, true, false, false);
        final StepStub step = (StepStub) testSteps.get(0);
        assertOneExecution(step);
        assertEquals("context step counter", 1 + 1, step.getContext().getCurrentStepNumber());
    }

    public void testExecutionMultipleEntries() throws IOException {
    	final List<Step> testSteps = new ArrayList<Step>();
        testSteps.add(createStepStub());
        testSteps.add(createStepStub());
        final RootStepResult result = executeStepList(testSteps);
        checkResult(result, true, false, false);
        final StepStub step0 = (StepStub) testSteps.get(0);
        assertOneExecution(step0);
        assertOneExecution((StepStub) testSteps.get(1));
        assertEquals("context step counter", 2 + 1, step0.getContext().getCurrentStepNumber());
    }

    public void testFailure() throws IOException {
    	final StepStub failStep = new FailStepStub();
    	configureStep(failStep);
    	final RootStepResult result = executeAndAssertNoSuccess(failStep);
        checkResult(result, false, true, false);
    }

    public void testError() throws IOException {
    	final StepStub errorStep = new ErrorStepStub();
    	configureStep(errorStep);
    	final RootStepResult result = executeAndAssertNoSuccess(errorStep);
        checkResult(result, false, false, true);
    }

    private static void checkResult(final RootStepResult result, final boolean success, final boolean failure, final boolean error) {
        assertEquals("success", success, result.isSuccessful());
        assertEquals("failure", failure, result.isFailure());
        assertEquals("error", error, result.isError());
    }

    private RootStepResult executeAndAssertNoSuccess(final StepStub unsuccessfulStep) throws IOException {
    	final List<Step> testSteps = new ArrayList<Step>();
        testSteps.add(unsuccessfulStep);
        final StepStub goodStep = createStepStub();
        testSteps.add(goodStep);
        final RootStepResult result = executeStepList(testSteps);
        assertNotExecuted(goodStep);
//        assertSame(unsuccessfulStep, result.getStep()); // FIXME: needed?
        assertNotNull("exception not null", result.getException());
        return result;
    }

    private RootStepResult executeStepList(final List testSteps) throws IOException {
    	final WebtestTask webtest = new WebtestTask();
    	webtest.setName("my simple test");
    	configureTask(webtest);
    	registerDummyResultReporter(getProject());
    	webtest.addConfig(getConfigurationStub());
    	webtest.getConfig().setHaltonerror(false);
    	webtest.getConfig().setHaltonfailure(false);
    	webtest.getConfig().setResultpath(getTemporaryResultPathFolder());
    	final TestStepSequence steps = (TestStepSequence) getStep();
    	webtest.addSteps(steps);
    	for (final Iterator iter = testSteps.iterator(); iter.hasNext();)
    	{
    		steps.addStep((Step) iter.next());
    	}
    	webtest.execute();
    	final RootStepResult result = webtest.getResultBuilderListener().getRootResult();
        assertNotNull(result);
        return result;
    }

    private static void assertOneExecution(final StepStub step) {
        assertEquals("Execution counter", 1, step.getCallCount());
    }

    private static void assertNotExecuted(StepStub step) {
        assertEquals("Execution counter", 0, step.getCallCount());
    }

    private Configuration getConfigurationStub() {
    	final Configuration config = new Configuration();
    	configureTask(config);
        return config;
    }
}
