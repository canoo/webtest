// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.File;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.WebTestException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.MockWebConnection;

/**
 * Unit tests for {@link InvokePage}.
 * @author Unknown
 * @author Marc Guillemot
 */
public class InvokePageTest extends BaseStepTestCase
{
	protected Step createStep() {
		return new InvokePage();
	}

	public void testExecuteFailsWithoutUrl() {
		assertErrorOnExecute(getStep(), "no Url", "");
	}

	public void testExecuteFailsWithTooManyParams() {
        InvokePage step = (InvokePage) getStep();
        step.setMethod("POST");
        assertErrorOnExecute(step, "post with no content or contentFile", "");
        step.setContent("dummy");
        step.setContentFile(new File("dummy"));
        assertErrorOnExecute(step, "both content and contentFile", "");
	}

	/**
	 * Tests that javascript errors are caught and correctly reported
	 * @throws Exception if the test fails
	 */
	public void testCatchRuntimeScriptError() throws Exception {
        final String url = "http://myhost.mydomain/myPage.html";
		final String html = "<html><head><script>notExisting()</script></head></html>";
		final String message = checkCodeWithScriptError(url, html).getMessage();
    	assertTrue("message doesn't contain page url: " + message, message.indexOf(url) != -1);
    	assertTrue("message doesn't contain information about failing function: " + message, message.indexOf("notExisting") != -1);
	}

	/**
	 * Tests that javascript parsing errors are caught and correctly reported
	 * @throws Exception if the test fails
	 */
	public void testCatchParseScriptError() throws Exception {
		final String url = "http://myhost.mydomain/myPage.html";
		final String html = "<html><head><script>var a = 1;\nnotExisting(;</script></head></html>";
		final WebTestException t = (WebTestException) checkCodeWithScriptError(url, html);
    	assertTrue("message doesn't contain page url: " + t.getMessage(), t.getMessage().indexOf(url) != -1);
    	assertTrue("message doesn't contain information about non parseable code: " + t.getDetails(), 
    			t.getDetails().toString().indexOf("notExisting(;") != -1);
	}

	/**
	 * Loads the given html as answer from the url,
	 * checks that a StepFailedException is thrown and return the error message
	 * @return the error message
	 */
	private Throwable checkCodeWithScriptError(final String url, final String html) {
		final InvokePage step = (InvokePage) getStep();
		((MockWebConnection) getContext().getWebClient().getWebConnection()).setDefaultResponse(html);

        step.setUrl(url);
        final TestBlock block = new TestBlock() {
        	public void call() throws Throwable
        	{
                step.execute();
        	}
        };
        return ThrowAssert.assertThrows("", StepFailedException.class, block);
	}

    public void testNestedText() throws Exception {
    	InvokePage invokeStep = (InvokePage) getStep();
    	testNestedTextEquivalent(invokeStep, "url");
    	
    	invokeStep = (InvokePage) createAndConfigureStep();
    	invokeStep.setUrl("http://my.web.site/foo");
    	testNestedTextEquivalent(invokeStep, "content");
    	
    }

    /**
     * Test that credentials gets updated.
     * @throws Exception
     */
    public void testCredentials() throws Exception {
    	final InvokePage invokeStep = (InvokePage) getStep();
    	
    	final TargetHelper th = new TargetHelper(invokeStep);
    	th.setUsername("userName");
    	th.setPassword("password");
    	th.prepareConversationIfNeeded(invokeStep.getContext());
    	final CredentialsProvider credentialsProvider = invokeStep.getContext().getWebClient().getCredentialsProvider();
    	final AuthScope authScope = new AuthScope("myhost", 80);
    	UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentialsProvider.getCredentials(authScope);
    	assertEquals("userName", cred.getUserName());
    	assertEquals("password", cred.getPassword());

    	th.setUsername("userName");
    	th.setPassword("new password");
    	th.prepareConversationIfNeeded(invokeStep.getContext());
    	cred = (UsernamePasswordCredentials) invokeStep.getContext().getWebClient().getCredentialsProvider().getCredentials(authScope);
    	assertEquals("userName", cred.getUserName());
    	assertEquals("new password", cred.getPassword());
    }
}
