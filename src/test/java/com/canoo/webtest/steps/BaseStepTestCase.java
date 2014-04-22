// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;

import com.agical.rmock.extension.junit.RMockTestCase;
import com.canoo.webtest.ant.WebtestPropertyHelper;
import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.canoo.webtest.boundary.UrlBoundary;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.reporting.IResultReporter;
import com.canoo.webtest.reporting.RootStepResult;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests that are common for all Steps, especially parameter handling.
 *
 * @author Marc Guillemot
 * @author Denis N. Antonioli
 * @author Paul King
 */
public abstract class BaseStepTestCase extends RMockTestCase {
    protected static final String NO_CURRENT_RESPONSE = "No current response available! Is previous invoke missing?";
    private static final String NO_CURRENT_RESPONSEFILE = "No current response file available!";

    private ContextStub fContext;
    private Step fStep;
    private Project fProject;
    private Target fTarget;

    /**
     * The minimal string to have the verification of toString pass.
     * Useful for simplistic implmentation of abstract class.
     */
    public static final String MOCK_TO_STRING = ")";


    /**
     * Creates the context and the Step under test calling {@link #createStep()}.
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        fProject = new Project();
        fProject.init();
    	WebtestPropertyHelper.configureWebtestPropertyHelper(fProject);
        fContext = createContext();
        fTarget = new Target();
        fTarget.setProject(fProject);
        fStep = createAndConfigureStep();
    }
    
    protected void tearDown() throws Exception {
    	super.tearDown();
    	// Junit releases test instances only at the end, we need to release resources earlier
        fProject = null;
        fContext = null;
        fTarget = null;
        fStep = null;
    }

    protected ContextStub getContext() {
        return fContext;
    }

    /**
     * Creates the context that will be created in {@link #setUp()}, available through {@link #getContext()}
     * and set on the step created with {@link #createStep()}
     *
     * @return the context
     */
    protected ContextStub createContext() {
        final ContextStub cx = new ContextStub();
        cx.getWebtest().setProject(getProject());
        return cx;
    }

    protected void setFakedContext(final ContextStub context) throws IOException
    {
        if (fContext.getCurrentResponse() != null)
        	fContext.getCurrentResponse().cleanUp();
        fContext = context;
        WebtestTask.setThreadContext(context);
    }
    /**
     * Gets a html page with a very basic content
     *
     * @return the page
     */
    protected HtmlPage getDummyPage() {
        return getDummyPage("<html></html>");
    }

    /**
     * Gets a html page with the given content
     */
    protected HtmlPage getDummyPage(final String content) {
        return (HtmlPage) getDummyPage(content, "text/html");
    }

    /**
     * Gets a page with the given content and content type
     */
    protected Page getDummyPage(final String content, final String contentType) {
        fContext.setDefaultResponse(content, contentType);
        final URL url = UrlBoundary.tryCreateUrl("http://webtest.canoo.com");
        return HtmlUnitBoundary.tryGetPage(url, getContext().getWebClient());
    }

    public void testUnknownPropertyType() {
        final Step step = getStep();
        try {
            step.getWebtestProperty("antProp", "unknown");
            ///CLOVER:OFF will not occur if we write steps correctly
            fail("Should raise a StepExecutionException");
            ///CLOVER:ON
        } catch (StepExecutionException expected) {
            assertTrue(true);
        }
    }

    public void testToString() {
        final Step step = getStep();

        final String result = step.toString();
        assertNotNull(result);
        assertTrue("toString() should end with ')' but found: " + result, result.endsWith(")"));
    }

    /**
     * Gets the step beeing tested. This should not be called during class initialisation because
     * a new instance is created before each test execution.
     *
     * @return the currently tested step created during {@link #setUp()} by {@link #createStep()}.
     */
    protected final Step getStep() {
        return fStep;
    }

    /**
     * Concrete test classes should return the Step they want to test.
     * The step is created during {@link #setUp()} and the context is set on it to {@link #getContext()}
     *
     * @return the tested step.
     */
    protected abstract Step createStep();

    /**
     * Creates a new step using {@link #createStep()} and sets the current context on it
     *
     * @return the step
     */
    protected final Step createAndConfigureStep() {
        return configureStep(createStep());
    }

    /**
     * Configures the step a la webtest, setting the properties ant/webtest would have set on it before executing
     * it (Project, Context, ...).
     *
     * @param step the step to configure
     * @return the configured step
     */
    protected Step configureStep(final Step step) {
        configureTask(step);
        return step;
    }

    /**
     * Configures the task a la ant, setting the properties ant would have set on it before executing
     * it (Project, Target, ...).
     *
     * @param task the task to configure
     * @return the configured task
     */
    protected Task configureTask(final Task task) {
        task.setProject(fProject);
        task.setOwningTarget(fTarget);
        return task;
    }
    
    /**
     * Gets the project used for the current test
     * @return the project
     */
    protected Project getProject()
    {
    	return fProject;
    }

    public static void assertStepRejectsNullResponse(final Step step) {
    	((ContextStub) step.getContext()).fakeLastResponse(null);
    	assertErrorOnExecute(step, "currentResponse == null", NO_CURRENT_RESPONSE);
    }

    protected void assertStepRejectsNullResponseFile(final Step step) throws Exception {
    	getContext().fakeLastResponse(null);
    	assertErrorOnExecute(step, "currentResponseFile == null", NO_CURRENT_RESPONSEFILE);
    }

    protected static void checkResponseMessage(final String expectedMessage, final String message) {
        ///CLOVER:OFF will not occur if we write steps correctly
        final String actualMessage = null == message ? "null" : message;
        if (!actualMessage.startsWith(expectedMessage)) {
            fail("expected start <" + expectedMessage + "> but was <" + actualMessage + ">");
        }
        ///CLOVER:ON
    }

    protected static void assertStepRejectsNullParam(final String param, final TestBlock b) {
        final Throwable t = ThrowAssert.assertThrows("param == null", StepExecutionException.class, b);
        assertEquals("Required parameter \"" + param + "\" not set!", t.getMessage());
    }

    public static void assertStepRejectsEmptyParam(final String param, final TestBlock b) {
        final Throwable t = ThrowAssert.assertThrows("param == null or zero length", StepExecutionException.class, b);
        assertEquals("Required parameter \"" + param + "\" not set or set to empty string!", t.getMessage());
    }

    /**
     * Gets a test block calling {@link #executeStep(Step)} with the provided step.
     * @return the test block
     */
    protected static TestBlock getExecuteStepTestBlock(final Step step)
    {
    	return new TestBlock() {
            public void call() throws Exception {
                executeStep(step);
            }
        };
    }


    /**
     * Gets a test block calling {@link #executeStep(Step)} on {@link #getStep()}.
     * @return the test block
     */
    protected TestBlock getExecuteStepTestBlock()
    {
    	return getExecuteStepTestBlock(getStep());
    }

    /**
     * @return the thrown exception
     */
    protected static Throwable assertThrowOnExecute(final Step step, final String failMessage, final String exceptionMessagePrefix,
                                             final Class<?> throwable) {
        final Throwable t = ThrowAssert.assertThrows(failMessage, throwable, getExecuteStepTestBlock(step));
        final String message = t.getMessage();
        if (!message.startsWith(exceptionMessagePrefix)) {
            fail("expected start <" + exceptionMessagePrefix + "> but was <" + message + ">");
        }
        return t;
    }

    /**
     * @param failMessage
     * @param exceptionMessagePrefix
     * @return the thrown exception
     */
    protected static Throwable assertFailOnExecute(final Step step, final String failMessage, final String exceptionMessagePrefix) {
        return assertThrowOnExecute(step, failMessage, exceptionMessagePrefix, StepFailedException.class);
    }
    /**
     * @return the thrown exception
     */
    protected Throwable assertFailOnExecute(final Step step) {
        return assertThrowOnExecute(step, "", "", StepFailedException.class);
    }

    /**
     * @return the thrown exception
     */
    public static Throwable assertErrorOnExecute(final Step step, final String failMessage, final String exceptionMessagePrefix) {
        return assertThrowOnExecute(step, failMessage, exceptionMessagePrefix, StepExecutionException.class);
    }

    /**
     * @return the thrown exception
     */
    protected Throwable assertErrorOnExecute(final Step step) {
        return assertThrowOnExecute(step, "", "", StepExecutionException.class);
    }

    protected void assertErrorOnExecuteIfCurrentPageIsXml(final Step step) {
        step.getContext().saveResponseAsCurrent(getDummyPage("<xml></xml>", "text/xml"));
        ThrowAssert.assertThrows(StepExecutionException.class,
                "Current response is not an HTML page", new TestBlock() {
            public void call() throws Exception {
                executeStep(step);
            }
        });
    }

    public static void assertInstanceOf(final Class expected, final Object actual) {
        if (!expected.isInstance(actual)) {
            fail("expected: <" + expected.getName() + "> but was <" + actual.getClass().getName() + ">.");
        }
    }

    public static void testAssertInstanceOf() {
        assertInstanceOf(Boolean.class, Boolean.TRUE);
        ThrowAssert.assertThrows(AssertionFailedError.class, new TestBlock() {
            public void call() throws Throwable {
                assertInstanceOf(Integer.class, Boolean.TRUE);
            }
        });
    }

    /**
     * Use this method to execute a step.
     * The method duplicates the content of {@link Step#execute()}, but without error
     * handling and without notification. It assumes the context is already set.
     *
     * @param step The step to execute.
     * @throws Exception
     */
    public static void executeStep(final Step step) throws Exception {
        step.verifyParameters();
        step.doExecute();
    }

    public static ContextStub getContextForDocument(final String documentText) {
        return new ContextStub(documentText);
    }


    /**
     * Test that calling addText(String) on the step sets the specified property.
     * @param step the step
     * @param propertyName the property that addText is expected to fill
     * @throws Exception
     */
    public static void testNestedTextEquivalent(final Step step, final String propertyName) throws Exception {
    	final String textToSet = "blabla";
        final Method addText = step.getClass().getMethod("addText", String.class);
        addText.invoke(step, textToSet);

        final Method getter = step.getClass().getMethod("get" + StringUtils.capitalize(propertyName));
    	final String value = (String) getter.invoke(step);
    
    	assertEquals("test value of property " + propertyName, textToSet, value);
    }

    protected RuntimeConfigurable parseStep(final Class aClass, final String attributes) {
    	return parseStep(getProject(), aClass, attributes);
    }

    /**
     * 
     * @param aClass the class of the step to create
     * @param attributes the attributes
     * @return a RuntimeConfigurable as would Ant do it while parsing
     */
    public static RuntimeConfigurable parseStep(final Project project, final Class aClass, final String attributes) {
        final String tagName = ClassUtils.getShortClassName(aClass);
    	final UnknownElement task = new UnknownElement(tagName);
        task.setQName(tagName);
        task.setTaskType(tagName);
        task.setTaskName(tagName);
        task.setProject(project);
        
        project.addTaskDefinition(tagName, aClass);

        final RuntimeConfigurable wrapper = new RuntimeConfigurable(task, task.getTaskName());
        final Pattern p = Pattern.compile("(\\w+)='([^']*)'");
        
        final Matcher m = p.matcher(attributes);
        while (m.find()) {
            final String name = m.group(1);
        	final String value = m.group(2);
        	wrapper.setAttribute(name, value);
        }

        return wrapper;
    }

	public static File getTemporaryResultPathFolder() throws IOException {
		final File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
    	File tmpWebTestFolder = new File(tmpFolder, "webtest-unittest-tmp");
    	tmpWebTestFolder.deleteOnExit();
    	FileUtils.forceMkdir(tmpWebTestFolder);
    	assertTrue(tmpFolder.isDirectory());
		return tmpWebTestFolder;
	}

	public static void registerDummyResultReporter(final Project project) {
		project.setProperty(WebtestTask.REPORTER_CLASSNAME_PROPERTY, DummyResultReporter.class.getName());
	}

	/**
	 * An implementation of {@link IResultReporter} doing nothing just to avoid error with the default result reporter.
	 */
	public static class DummyResultReporter implements IResultReporter {
		@Override
		public void generateReport(final RootStepResult result) throws Exception {
			// nothing
		}
	}
}
