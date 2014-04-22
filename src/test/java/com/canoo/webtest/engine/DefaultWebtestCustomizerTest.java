package com.canoo.webtest.engine;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Sleep;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.interfaces.IWebtestCustomizer;
import com.canoo.webtest.reporting.StepExecutionListener;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;

/**
 * @author Ardeshir Arfaian
 */
public class DefaultWebtestCustomizerTest extends TestCase {

	private Project project;
	private WebtestTask webTest;
	private Configuration config;
	private Context context;
	
	public void setUp() {
		project = new Project();
		webTest = new WebtestTask();
		webTest.setProject(project);
		webTest.setName("TestWebTest");
		config	= new Configuration(webTest);
		
        context = new Context(webTest);
        config.setProject(project);
        config.setContext(context);
	}
	
	public void testNoReference() {
		final Context context = new Context(webTest);
        
		config = new Configuration();
        config.setProject(project);
        config.setContext(context);
        config.execute();
        
        assertEquals(DefaultWebtestCustomizer.class, webTest.getWebtestCustomizer().getClass());
        assertEquals(WebClient.class, context.getWebClient().getClass());
        assertEquals(CSVTracingWebConnection.class, context.getWebClient().getWebConnection().getClass());
	}
	
	public void testCustomWebtestCustomizer() {
        project.addReference("wt.webtestCustomizer", new IWebtestCustomizer() {
			public StepExecutionListener createExecutionListener(WebtestTask wt) {
				return new StepExecutionListener(wt.getConfig().getContext());
			}
			public WebClient customizeWebClient(WebClient wc) {
				new WebConnectionWrapper(wc);
				return wc;
			}
		});
        
        webTest.setProject(project);
        config.execute();
        
        assertNotSame(DefaultWebtestCustomizer.class, webTest.getWebtestCustomizer().getClass());
        assertEquals(WebClient.class, context.getWebClient().getClass());
        assertEquals(CSVTracingWebConnection.class, context.getWebClient().getWebConnection().getClass());
	}
	
	public void testWrongTypeReference() {
        boolean isCatched = false;
        project.addReference("wt.webtestCustomizer", new Object());
        
        try {
          webTest.setProject(project);
        } catch (BuildException be) {
          isCatched = true;
        }
        assertTrue("Wrong reference didn't cause an exception", isCatched);
        
        config.execute();
        
        assertEquals(DefaultWebtestCustomizer.class, webTest.getWebtestCustomizer().getClass());
        assertEquals(WebClient.class, context.getWebClient().getClass());
	}
	
	public void testAllMethodsInvoked() {
		class DummyCustomizerImpl implements IWebtestCustomizer {

        	private boolean isCreateExecutionListenerInvoked = false;
    		private boolean isCustomizeWebClientInvoked = false;
    		
        	public StepExecutionListener createExecutionListener(WebtestTask wt) {
				isCreateExecutionListenerInvoked = true;
        		return new StepExecutionListener(wt.getConfig().getContext());
			}

			public WebClient customizeWebClient(WebClient wc) {
				isCustomizeWebClientInvoked = true;
				return wc;
			}
		};
		
		DummyCustomizerImpl dummyCustomizer = new DummyCustomizerImpl();
		project.addReference("wt.webtestCustomizer", dummyCustomizer);
        webTest.setProject(project);
        
        final Sleep dummyTask = new Sleep();
        dummyTask.setMilliseconds(0);
        dummyTask.setProject(project);
        webTest.addTask(dummyTask);
                
        Target target = new Target();
        target.setProject(project);
        
        webTest.setOwningTarget(target);
        webTest.execute();
        
        assertTrue(dummyCustomizer.isCreateExecutionListenerInvoked);
        assertTrue(dummyCustomizer.isCustomizeWebClientInvoked);
	}
	
	public void testNullReference() {
        project.addReference("wt.webtestCustomizer", null);
		webTest.setProject(project);
		
        config.execute();
        
        assertEquals(DefaultWebtestCustomizer.class, webTest.getWebtestCustomizer().getClass());
        assertEquals(WebClient.class, context.getWebClient().getClass());
	}
}
