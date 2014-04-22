// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.boundary.ResetScriptRunner;
import com.canoo.webtest.engine.xpath.XPathHelper;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Represents context information for a webtest.
 *
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 */
public class Context {
	private static final Logger LOG = Logger.getLogger(Context.class);
	/**
	 * The name identifiying the default {@link WebClientContext} that is the current one at test startup.
	 */
	public static final String KEY_DEFAULT_WEBCLIENTCONTEXT = "default";

	private int fCurrentStepIndex;
	private WebtestTask fWebtest;
	private boolean fPrepared;
	private Map<String, Object> fContextStore = new HashMap<String, Object>();
	private ResetScriptRunner fSavedRunner;

	private final XPathHelper fXPathHelper = new XPathHelper();

	private WebClientContext fCurrentWebClientContext = new WebClientContext(KEY_DEFAULT_WEBCLIENTCONTEXT);
	private final Map<String, WebClientContext> fWebClientContexts = new HashMap<String, WebClientContext>();
	private final AtomicReference<ScriptException> backgroundJSError_ = new AtomicReference<ScriptException>();

	public Context(final WebtestTask webtest) {
		fWebtest = webtest;
		fWebClientContexts.put(KEY_DEFAULT_WEBCLIENTCONTEXT, fCurrentWebClientContext);
	}

	public boolean containsKey(final String key) {
		return fContextStore.containsKey(key);
	}

	public Object get(final String key) {
		return fContextStore.get(key);
	}

	/**
	 * Shortcut method to get the configuration of the
	 *
	 * @return the configuration
	 * @see WebtestTask
	 */
	public Configuration getConfig() {
		return getWebtest().getConfig();
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#getCurrentForm
	 */
	public HtmlForm getCurrentForm() {
		return getCurrentWebClientContext().getCurrentForm();
	}

	/**
	 * /**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#getCurrentHtmlResponse(Step)
	 */
	public HtmlPage getCurrentHtmlResponse(final Step step) {
		return getCurrentWebClientContext().getCurrentHtmlResponse(step);
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#getCurrentResponse()
	 */
	public Page getCurrentResponse() {
		return getCurrentWebClientContext().getCurrentResponse();
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentResponseFile()
	 * @see WebClientContext#getCurrentResponseFile()
	 */
	public String getCurrentResponseFile() {
		return getCurrentWebClientContext().getCurrentResponseFile();
	}

	public int getCurrentStepNumber() {
		return fCurrentStepIndex + 1;
	}

	/**
	 * Gets the currently active {@link WebClientContext}.
	 *
	 * @return the web client context
	 */
	public WebClientContext getCurrentWebClientContext() {
		return fCurrentWebClientContext;
	}

	public int getNumberOfSteps() {
		return getWebtest().getStepSequence().getSteps().size();
	}

	/**
	 * Gets the currently available {@link WebClientContext}
	 *
	 * @return an unmodifiable Map of (key, {@link WebClientContext})
	 */
	public Map<String, WebClientContext> getWebClientContexts() {
		return Collections.unmodifiableMap(fWebClientContexts);
	}

	/**
	 * Defines the current {@link WebClientContext} creating it if none was previously registered
	 * under this name.
	 *
	 * @param name the name of the {@link WebClientContext} to activate.
	 *             The default {@link WebClientContext} is registered under the key {@link #KEY_DEFAULT_WEBCLIENTCONTEXT}.
	 * @return the new current context
	 */
	public WebClientContext defineCurrentWebClientContext(final String name) {
		WebClientContext webClientContext = (WebClientContext) fWebClientContexts.get(name);
		if (webClientContext == null) {
			webClientContext = new WebClientContext(name);
			webClientContext.setWebClient(getConfig().createWebClient());
			fWebClientContexts.put(name, webClientContext);
			LOG.info("Created new WebClientContext for \"" + name + "\"");
		}
		fCurrentWebClientContext = webClientContext;
		return webClientContext;
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#getResponses()
	 */
	public WebClientContext.StoredResponses getResponses() {
		return getCurrentWebClientContext().getResponses();
	}

	public ResetScriptRunner getRunner() {
		return fSavedRunner;
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#getSavedPassword()
	 */
	public String getSavedPassword() {
		return getCurrentWebClientContext().getSavedPassword();
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#getSavedUserName()
	 */
	public String getSavedUserName() {
		return getCurrentWebClientContext().getSavedUserName();
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#getWebClient()
	 */
	public WebClient getWebClient() {
		return getCurrentWebClientContext().getWebClient();
	}

	/**
	 * Gets the &lt;webtest&gt; for this context.
	 *
	 * @return the task
	 */
	public WebtestTask getWebtest() {
		return fWebtest;
	}

	/**
	 * Gets the XPath helper used for this test. It will be responsible to
	 * create the xpath objects with the right custom functions and variables.
	 *
	 * @return the helper
	 */
	public XPathHelper getXPathHelper() {
		return fXPathHelper;
	}

	public void increaseStepNumber() {
		fCurrentStepIndex += 1;
	}

	public boolean isPrepared() {
		return fPrepared;
	}

	// generic mechanism to extend context information
	public void put(final String key, final Object value) {
		LOG.debug("put(" + key + ", " + value + ")");
		fContextStore.put(key, value);
	}

	public void remove(final String key) {
		fContextStore.remove(key);
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#restorePreviousResponse()
	 */
	public void restorePreviousResponse() {
		getCurrentWebClientContext().restorePreviousResponse();
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#restoreResponses(com.canoo.webtest.engine.WebClientContext.StoredResponses)
	 */
	public void restoreResponses(
			final WebClientContext.StoredResponses savedResponses) {
		getCurrentWebClientContext().restoreResponses(savedResponses);
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#restoreWindowListener()
	 */
	public void restoreWindowListener() {
		getCurrentWebClientContext().restoreWindowListener();
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#saveResponseAsCurrent(Page)
	 */
	public void saveResponseAsCurrent(final Page page) {
		getCurrentWebClientContext().saveResponseAsCurrent(page);
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#setCurrentForm(HtmlForm)
	 */
	public void setCurrentForm(final HtmlForm form) {
		getCurrentWebClientContext().setCurrentForm(form);
	}

	public void setPrepared(final boolean prepared) {
		fPrepared = prepared;
	}

	public void setRunner(final ResetScriptRunner sr) {
		fSavedRunner = sr;
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#setSavedPassword(String)
	 */
	public void setSavedPassword(final String password) {
		getCurrentWebClientContext().setSavedPassword(password);
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#setSavedUserName(String)
	 */
	public void setSavedUserName(final String userName) {
		getCurrentWebClientContext().setSavedUserName(userName);
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#setWebClient(WebClient)
	 */
	public void setWebClient(final WebClient webClient) {
		getCurrentWebClientContext().setWebClient(webClient);
	}

	/**
	 * Delegates to current {@link WebClientContext}
	 *
	 * @see #getCurrentWebClientContext()
	 * @see WebClientContext#suspendWindowListener()
	 */
	public void suspendWindowListener() {
		getCurrentWebClientContext().suspendWindowListener();
	}

	/**
	 * Gets the last background JS exception (if any).
	 * This method should disappear when HtmlUnit offer better control possibilities for that.
	 * This consumes the exception and next call will return <code>null</code> (unless a new error occurs)
	 * @return the JS error
	 */
	public ScriptException getBackgroundJSError() {
		return backgroundJSError_.getAndSet(null);
	}

	/**
	 * Sets the last background JS exception.
	 * This method should disappear when HtmlUnit offer better control possibilities for that
	 * @return the JS error
	 */
	public void setBackgroundJSError(final ScriptException e) {
		backgroundJSError_.set(e);
	}
}
