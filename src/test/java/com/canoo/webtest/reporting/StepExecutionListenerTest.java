// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import com.canoo.webtest.ant.TestStepSequence;
import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.DefaultWebtestCustomizer;
import com.canoo.webtest.interfaces.IComputeValue;
import com.canoo.webtest.steps.Step;

/**
 * Unit tests for {@link StepExecutionListener}.
 * @author Unknown
 * @author Marc Guillemot
 * @author Ardeshir Arfaian
 */
public class StepExecutionListenerTest extends TestCase {
    private static final Logger LOG = Logger.getLogger(StepExecutionListenerTest.class);
    
    public static class StringBufferReporter implements IResultReporter
    {
    	static Map<String, String> reports = Collections.synchronizedMap(new HashMap<String, String>());
    	
    	public void generateReport(final RootStepResult result) throws IOException {
    		final StringBuilder sb = new StringBuilder();
    		
            for (final StepResult res : result.getChildren())
            {
            	sb.append(res.getTaskName() + ":" + res.getAttributes() + "\n");
            }
            		
            reports.put(result.getWebtestName(), sb.toString());
    	}
    }
    
    /**
     * Task that waits until the "status" has reached
     * the given state to increase it by 1.
     */
    public static class WaitingTask extends Step
    {
		private static final long serialVersionUID = 4919608524778827568L;
		public static int currentState = 1;
    	private final int state;
    	WaitingTask(final int _state, final Project _project)
    	{
    		state = _state;
    		setProject(_project);
    	}

    	@Override
    	public void doExecute() throws Exception {
			for (int i=0; i<10; ++i)
			{
				synchronized (WaitingTask.class)
				{
					if (currentState < state)
					{
						try {
							WaitingTask.class.wait(100);
						}
						catch (final InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
					else
					{
						currentState = state + 1;
						WaitingTask.class.notifyAll();
					}
				}
			}
		}
    	
    	/**
    	 * Don't rely on {link {@link IComputeValue#getComputedValue()} for this
    	 * test as all results will have the same key and no synchronization issue
    	 * will occur
    	 */
    	@Override
    	protected void addComputedParameters(final Map map) {
    		map.put(getTaskName(), state);
    	}
		
    	public String getComputedValue() {
    		return "compute value for " + state;
    	}

    	@Override
		public String getTaskName() {
			return "wait" + state;
		}
    }
    
    /**
     * Test that only the right result listener gets its results when tests run in parallel
     * @see <a href="http://webtest-community.canoo.com/jira/browse/WT-477">WT-477</a>
     */
    public void testParallelUse() throws Exception
    {
    	final Project project = new Project();
    	final String reporterClassName = StringBufferReporter.class.getName();
		project.setProperty(WebtestTask.REPORTER_CLASSNAME_PROPERTY, reporterClassName);
    	assertEquals(reporterClassName, project.getProperty(WebtestTask.REPORTER_CLASSNAME_PROPERTY));

    	final WebtestTask webtest1 = new WebtestTask();
    	webtest1.setName("test 1");
    	webtest1.setProject(project);
    	webtest1.addTask(new WaitingTask(1, project));
    	webtest1.addTask(new WaitingTask(3, project));
    	
    	final WebtestTask webtest2 = new WebtestTask();
    	webtest2.setName("test 2");
    	webtest2.setProject(project);
    	webtest2.addTask(new WaitingTask(2, project));
    	webtest2.addTask(new WaitingTask(4, project));

    	
    	final Thread thread1 = new Thread("WebTest1")
    	{
    		@Override
    		public void run() {
    			webtest1.execute();
    		}
    	};

    	final Thread thread2 = new Thread("WebTest2")
    	{
    		@Override
    		public void run() {
    			webtest2.execute();
    		}
    	};

    	final List<Throwable> uncaugtExceptions = new ArrayList<Throwable>();
    	final UncaughtExceptionHandler collectingExceptionHandler = new Thread.UncaughtExceptionHandler()
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
    	
    	assertEquals("wait1:{wait1=1}\nwait3:{wait3=3}\n", StringBufferReporter.reports.get("test 1"));
    	assertEquals("wait2:{wait2=2}\nwait4:{wait4=4}\n", StringBufferReporter.reports.get("test 2"));
    	
    }

	private class DummyStepExecutionListener extends StepExecutionListener {
		
		private boolean isWebtestFinishedInvoked;
		
		public DummyStepExecutionListener(Context context) {
			super(context);
			
			isWebtestFinishedInvoked = false;
		}
		
		public void webtestFinished() {
			isWebtestFinishedInvoked = true;
		}
		
		public boolean isWebtestFinishedInvoked() {
			return isWebtestFinishedInvoked;
		}
		
	}
	
	public void testAllMethodsInvoked() {
		Project project = new Project();
		WebtestTask webTest = new WebtestTask();
		webTest.setProject(project);
		webTest.setName("TestWebTest");
		Configuration config = new Configuration(webTest);
		
		Context context = new Context(webTest);
        config.setProject(project);
        config.setContext(context);

        class DummyCustomizerImpl extends DefaultWebtestCustomizer {

			private DummyStepExecutionListener dummyListener;
			
			public StepExecutionListener createExecutionListener(WebtestTask wt) {
				return dummyListener = new DummyStepExecutionListener(wt.getConfig().getContext());
			}
			
			public DummyStepExecutionListener getDummyListener() {
				return dummyListener;
			}

		};
		
		DummyCustomizerImpl dummyCustomizer = new DummyCustomizerImpl();
		project.addReference("wt.webtestCustomizer", dummyCustomizer);
        webTest.setProject(project);
        
        final TestStepSequence steps = new TestStepSequence();
        steps.setProject(project);
        webTest.addSteps(steps);
                
        Target target = new Target();
        target.setProject(project);
        
        webTest.setOwningTarget(target);
        webTest.execute();
        
        assertNotNull(dummyCustomizer.getDummyListener());
        assertTrue(dummyCustomizer.getDummyListener().isWebtestFinishedInvoked());
	}
}
