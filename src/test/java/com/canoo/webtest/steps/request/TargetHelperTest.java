// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.net.SocketTimeoutException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;

import com.agical.rmock.extension.junit.RMockTestCase;
import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.interfaces.IConnectionInitializer;
import com.canoo.webtest.interfaces.IPropertyHandler;
import com.canoo.webtest.security.ConnectionInitializationException;
import com.canoo.webtest.self.BufferingAppender;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.AbstractBrowserAction;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.StepUtil;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;

/**
 * Tests for {@link TargetHelper}.
 * @author Paul King
 * @author Marc Guillemot
 */
public class TargetHelperTest extends RMockTestCase
{
    private static final String OK_CUSTOM_INITIALIZER_CLASS_NAME = "com.canoo.webtest.steps.request.TargetHelperTest$NullConnectionInitializer";
    private static final String BAD_CUSTOM_INITIALIZER_CLASS_NAME = "com.canoo.webtest.steps.request.TargetHelperTest$ExceptionConnectionInitializer";

    private AbstractBrowserAction fBrowserAction = new AbstractBrowserAction()
    {
        public void doExecute() {}
    };
    private TargetHelper fTargetHelper = new TargetHelper(fBrowserAction);
    private final Context fTestContext = createContextStupWithoutSteps();
    private final BufferingAppender fBuffAppender = new BufferingAppender();

    static ContextStub createContextStupWithoutSteps()
    {
    	return new ContextStub()
    	{
    		protected void initTestStepSequence(Project project) {
    			// nothing, we want to skip it here
    		}
    	};
    }
    protected void setUp() throws Exception {
        final Configuration testConfig = new Configuration();
        testConfig.setShowhtmlparseroutput(false);
        final IPropertyHandler handler = (IPropertyHandler) mock(IPropertyHandler.class, "handler");

        testConfig.setPropertyHandler(handler);
        fTestContext.getWebtest().addConfig(testConfig);

        BaseStepTestCase.executeStep(fBrowserAction); // coverage

        Logger.getRootLogger().setLevel(Level.INFO); // ensure that the logged event we want to catch is not discarded
        Logger.getRootLogger().addAppender(fBuffAppender);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        Logger.getRootLogger().removeAppender(fBuffAppender);
    }

    public void testGotoTargetByUrl() throws Exception {
    	fTestContext.getWebtest().getConfig().getExternalProperty(TargetHelper.CONNECTION_INITIALIZER_KEY);
        modify().returnValue(null);
        startVerification();
        fBuffAppender.getEvents().clear();
        try {
            fTargetHelper.getResponse(fTestContext, "targetUrl");
        } catch (final Exception e) {
            /* ignore */
        }
        assertTrue("should be called by url", fBuffAppender.allMessagesToString().indexOf("url") > -1);
    }

    /**
     * Tests that SocketTimeoutException is catched and that a StepFailedException is rethrown
     * @throws Exception
     */
    public void testSocketTimeoutException() throws Exception {
        final String message = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
            	final Throwable t = new SocketTimeoutException("blah SocketTimeoutException message");
            	StepUtil.handleException(t);
            }
        });
        assertEquals("Server took to long to answer: blah SocketTimeoutException message", message);
    }

    public void testInvokeCustomInitializer() throws Exception {
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock()
        {
            public void call() throws Exception {
                fTargetHelper.invokeCustomInitializer(fTestContext, "no.such.class");
            }
        });
        final String message = ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock()
        {
            public void call() throws Exception {
                fTargetHelper.invokeCustomInitializer(fTestContext, BAD_CUSTOM_INITIALIZER_CLASS_NAME);
            }
        });
        assertTrue(message.startsWith("ConnectionInitializer raised exception"));
    }

    public void testPrepareConversationWithProperCustomInitializer() throws Exception {
        final IPropertyHandler customHandler = (IPropertyHandler) mock(IPropertyHandler.class, "customHandler");
        customHandler.getProperty(TargetHelper.CONNECTION_INITIALIZER_KEY);
        modify().returnValue(OK_CUSTOM_INITIALIZER_CLASS_NAME);
        startVerification();
        fTestContext.getConfig().setPropertyHandler(customHandler);
        ThrowAssert.assertPasses("normal pass", new TestBlock()
        {
            public void call() throws Exception {
                fTargetHelper.prepareConversationIfNeeded(fTestContext);
            }
        });
    }

    public static class ExceptionConnectionInitializer implements IConnectionInitializer
    {
        public void initializeConnection(final Configuration config) throws ConnectionInitializationException {
            throw new ConnectionInitializationException("xxx");
        }
    }

    public static class NullConnectionInitializer implements IConnectionInitializer
    {
        public void initializeConnection(final Configuration config) throws ConnectionInitializationException {
            // just pass
        }
    }
    
    /**
     * There is a bug in HtmlUnit where addCredentials doesn't necessary overwrite old ones.
     * Once this has been fixed, we can remove TargetHelper.WTCredentials.
     * @throws Exception
     */
    public void testDefaultCredentialsProvider() throws Exception {
    	final DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
    	credentialsProvider.addCredentials("userName", "password");

    	final UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentialsProvider.getCredentials(new AuthScope("myHost", 80));
    	assertEquals("userName", cred.getUserName());
    	assertEquals("password", cred.getPassword());

    	credentialsProvider.addCredentials("userName", "new password");
    	final UsernamePasswordCredentials cred2 = (UsernamePasswordCredentials) credentialsProvider.getCredentials(new AuthScope("myHost", 80));
    	assertEquals("userName", cred2.getUserName());
    	assertEquals("new password", cred2.getPassword());
    }
}
