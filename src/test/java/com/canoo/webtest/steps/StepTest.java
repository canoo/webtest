// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Target;
import org.apache.xerces.xni.XNIException;
import org.xml.sax.SAXException;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.ErrorStepStub;
import com.canoo.webtest.self.FailStepStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;

/**
 * Tests for {@link Step}.
 * @author unknown
 * @author Marc Guillemot
 * @author Denis N. Antonioli
 * @author Paul King
 */
public class StepTest extends BaseStepTestCase
{
    private Step fFailSpec;
    private Step fErrorSpec;

    protected void setUp() throws Exception {
        super.setUp();
        fFailSpec = new FailStepStub();
        configureTask(fFailSpec);
        fErrorSpec = new ErrorStepStub();
        configureTask(fErrorSpec);
    }

    protected Step createStep() {
        return new StepStub();
    }

    public void testGetDescription() {
        final Step step = getStep();
        final String prefix = "PRE";
        final String desc = "desc";
        final String suffix = "POST";
        assertEquals("", step.getDescription(prefix, suffix));
        step.setDescription(desc);
        assertEquals(prefix + desc + suffix, step.getDescription(prefix, suffix));
    }

    /**
     * Test that expandDynamicProperties just uses what it needs from the properties map
     * and not the rest allowing tricky use of this map to save (non String) objects
     * from groovy (or any other script step) for reuse between 2 steps.
     */
    @SuppressWarnings("unchecked")
	public void testExpandDynamicProperties() {
    	final Step step = getStep(); 
        step.getWebtestProperties().put("myProp", "something");
        step.getWebtestProperties().put("myTrickyUseOfProps", new Object());
        assertEquals("something", step.getProject().replaceProperties("#{myProp}"));
    }

    public void testLifecycle() {
        assertStatus(getStep(), false, false, false, "before started notification");
        getStep().notifyStarted();
        assertStatus(getStep(), true, false, false, "after started notification");
        getStep().notifyCompleted();
        assertStatus(getStep(), true, true, false, "after completed notification");
        getStep().notifySuccess();
        assertStatus(getStep(), true, true, true, "after success notification");
    }

    public void testIsStartCompletedOnFailure() {
        assertStatus(fFailSpec, false, false, false, "before execute");
        ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                fFailSpec.execute();
            }
        });
        assertStatus(fFailSpec, true, true, false, "after execute with failure");
    }

    public void testIsStartCompletedOnError() {
        assertStatus(fErrorSpec, false, false, false, "before execute");
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Exception {
                fErrorSpec.execute();
            }
        });
        assertStatus(fErrorSpec, true, true, false, "after execute with error");
    }

    public void testWebtestProperties() throws Exception {
        final String propName = "count";
        final String propValue = "2";

        final RevealingStepStub openingStep = new RevealingStepStub();
        configureStep(openingStep);
        executeStep(openingStep);
        openingStep.setWebtestProperty(propName, propValue);

        assertEquals("empty string", "", openingStep.getProject().replaceProperties(""));
        assertNull("null string", openingStep.getProject().replaceProperties(null));

        assertEquals("prop only", propValue, openingStep.getProject().replaceProperties("#{count}"));
        assertEquals("prop 1", "X" + propValue + "X",
        		openingStep.getProject().replaceProperties("X#{count}X"));
        assertEquals("prop 2", propValue + propValue,
        		openingStep.getProject().replaceProperties("#{count}#{count}"));
        assertEquals("no prop", "X", openingStep.getProject().replaceProperties("X"));
    }

    public void testExceptions() {
        // dummy statements for coverage purposes: test default forms
        // of exceptions which are catered for but not generally used
        new StepFailedException("message").toString();
        new StepExecutionException("message", (Throwable) null).toString();
    }

    public void testHandleUnexpectedException() {
        checkUnexpectedException(fFailSpec, new StepFailedException("message", fFailSpec), StepFailedException.class);
        checkExecutionException(new StepExecutionException("message", (Throwable) null));
        final Logger log = Logger.getLogger(Step.class.getName());
        final Level oldlevel = log.getLevel();
        log.setLevel(Level.FATAL);
        checkExecutionException(new NullPointerException("message"));
        log.setLevel(oldlevel);
        checkExecutionException(new SAXException("message"));
        checkExecutionException(new XNIException("message", new RuntimeException("Nested")));
        checkExecutionException(new XNIException("message", new XNIException("message", new RuntimeException("Double Nested"))));
        checkExecutionException(new XNIException("message", new InvocationTargetException(new RuntimeException("Double Nested"))));
    }

    private void checkExecutionException(final Exception exception) {
        checkUnexpectedException(fErrorSpec, exception, StepExecutionException.class);
    }

    private static void checkUnexpectedException(final Step step, final Exception exception, final Class throwable) {
        ThrowAssert.assertThrows(throwable,
            new TestBlock()
            {
                public void call() throws Exception {
                    step.handleException(exception);
                }
            });
    }

    public void testHandlesThrowableWithNoMessage() {
        final String message = ThrowAssert.assertThrows(RuntimeException.class, new TestBlock()
        {
            public void call() throws Throwable {
            	getStep().handleException(new RuntimeException());
            }
        });
        assertEquals("Unexpected exception caught: java.lang.RuntimeException", message);
    }

    public void testExecute() throws Exception {
        assertExecute(new StepStub());
    }

    public void testDoExecute() throws Exception {
        assertDoExecute(new StepStub());
    }

    private static void assertStatus(final Step step, final boolean started, final boolean completed,
                                     final boolean successful, final String when) {
        assertEquals("isStarted() should be " + started + " " + when, started, step.isStarted());
        assertEquals("isCompleted() should be " + completed + " " + when, completed, step.isCompleted());
        assertEquals("isSuccessful() should be " + successful + " " + when, successful, step.isSuccessful());
    }

    private void assertExecute(final Step step) {
    	configureStep(step);
        step.execute();
        assertTrue(((AbstractStepVerifierStub) step).isVerified());
        assertTrue(((AbstractStepVerifierStub) step).isExecuted());
    }

    private void assertDoExecute(final Step step) throws Exception {
    	configureStep(step);
        step.verifyParameters();
        step.doExecute();
        assertTrue(((AbstractStepVerifierStub) step).isVerified());
        assertTrue(((AbstractStepVerifierStub) step).isExecuted());
    }

    public static class RevealingStepStub extends Step
    {
        public void doExecute() {
        }

    }

    private abstract static class AbstractStepVerifierStub extends RevealingStepStub
    {
        private boolean fExecuted;
        private boolean fVerified;

        public boolean isExecuted() {
            return fExecuted;
        }

        public boolean isVerified() {
            return fVerified;
        }

        public void setExecuted(final boolean executed) {
            fExecuted = executed;
        }

        public void setVerified(final boolean verified) {
            fVerified = verified;
        }
    }

    private static final class StepStub extends AbstractStepVerifierStub
    {
        public void doExecute() {
            setExecuted(true);
        }

        protected void verifyParameters() {
            setVerified(true);
        }
    }

    public void testClone() throws Exception {
        getStep().setLocation(new Location("myFile.xml", 15, 34));
    	getStep().setDescription("foo");
    	getStep().setOwningTarget(new Target());
    	getStep().setTaskType("myType");
    	getStep().setTaskName("myTask");

    	final Step clone = (Step) getStep().clone();
    	assertSameAndNotNull(getStep().getDescription(), clone.getDescription());
    	assertSameAndNotNull(getStep().getLocation(), clone.getLocation());
    	assertSameAndNotNull(getStep().getOwningTarget(), clone.getOwningTarget());
    	assertSameAndNotNull(getStep().getProject(), clone.getProject());
    	assertSameAndNotNull(getStep().getTaskType(), clone.getTaskType());
    	assertSameAndNotNull(getStep().getTaskType(), clone.getTaskType());

    	// don't need to duplicate because it can be the share between a step an its clone
    	// but in a first time, just test equals
    	assertEquals(getStep().getParameterDictionary(), clone.getParameterDictionary());
    }

    /**
     * Test that expected object is not null and is the same as actual object.
     */
    protected void assertSameAndNotNull(final Object expected, final Object actual) {
        assertNotNull(expected);
        assertSame(expected, actual);
    }
}
