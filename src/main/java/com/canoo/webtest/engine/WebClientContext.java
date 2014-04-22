package com.canoo.webtest.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.request.SelectWebClient;
import com.canoo.webtest.util.Checker;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/*
 * Contains all context information bound to a WebClient.
 * Except if selectWebClient is used test runs with only 1 web client
 * and only one instance of this class is used.
 * @author Marc Guillemot
 */
public class WebClientContext {
	private static final Logger LOG = Logger.getLogger(WebClientContext.class);
	private WebClient fWebClient;
	private String fSavedUserName;
	private String fSavedPassword;
	private StoredResponse fPreviousResponse = NO_STORED_RESPONSE;
	private StoredResponse fCurrentResponse = NO_STORED_RESPONSE;
	private static final StoredResponse NO_STORED_RESPONSE = new StoredResponse(null);
	private final WebWindowListener fWindowListener = new CurrentWindowTracker();
	private final Stack fWindows = new Stack();
	private HtmlForm fCurrentForm;
	private final String fName;

	/**
	 * Used to capture the state of the (current and previous) responses to be able to
	 * restore it later. Uses the Memento pattern.
	 */
	public static final class StoredResponses {
		private final StoredResponse fPreviousResponse;
		private final StoredResponse fCurrentResponse;

		private StoredResponses(final WebClientContext context) {
			fPreviousResponse = context.fPreviousResponse;
			fCurrentResponse = context.fCurrentResponse;
		}
	}


	/**
	 * Keeps a Page and its stored file together.
	 */
	static class StoredResponse {
		private final Page fPage;
		private String fFile;

		StoredResponse(final Page page) {
			fPage = page;
		}

		public Page getPage() {
			return fPage;
		}

		public String getFile() {
			return fFile;
		}

		public void setFile(final String file) {
			fFile = file;
		}
	}

	/**
	 * Tracks window event to determine the "current" response
	 */
	class CurrentWindowTracker implements WebWindowListener {
		public void webWindowClosed(final WebWindowEvent event) {
			fWindows.remove(event.getWebWindow());
			// don't change currentResponse here else it causes problems with <previousResponse>
			LOG.debug("Window closed (contains: " + event.getWebWindow().getEnclosedPage().getUrl() + ")");
		}

		public void webWindowContentChanged(final WebWindowEvent event) {
			final WebWindow window = event.getWebWindow();
			final WebResponse webResp = event.getNewPage().getWebResponse();
            LOG.info("Content of window changed to " + webResp.getWebRequest().getUrl() + " (" + webResp.getContentType() + ")");

			final boolean takeItAsNew;
			if (window instanceof TopLevelWindow && event.getOldPage() == null) {
				takeItAsNew = true;
				LOG.info("Content loaded in newly opened window, its content will become current response");
			} else if (fCurrentResponse.getPage() != null
					&& fCurrentResponse.getPage().getEnclosingWindow() == window) {
				takeItAsNew = true;
				LOG.info("Content of current window changed, it will become current response");
			}
			// content loaded in an other window as the "current" one
			// by js becomes "current" only if new top window is opened
			else if (getWebClient().getJavaScriptEngine() == null
					|| !getWebClient().getJavaScriptEngine().isScriptRunning()) {
				if (window instanceof FrameWindow
						&& !HtmlPage.READY_STATE_COMPLETE.equals(
						((FrameWindow) window).getEnclosingPage().getDocumentElement().getReadyState())) {
					LOG.info("Content of frame window has changed without javascript while enclosing page is loading, "
							+ "it will NOT become current response");
					LOG.debug("Enclosing page's state: " + ((FrameWindow) window).getEnclosingPage().getDocumentElement().getReadyState());
					LOG.debug("Enclosing page's url: " + ((FrameWindow) window).getEnclosingPage().getUrl());
					takeItAsNew = false;
				} else {
					LOG.info("Content of window changed without javascript, it will become current response");
					takeItAsNew = true;
				}
			} else {
				LOG.info("Content of window changed with javascript, it will NOT become current response");
				takeItAsNew = false;
			}

			if (takeItAsNew) {
				saveResponseAsCurrent(window.getEnclosedPage());
			}
		}

		/**
		 * @see com.gargoylesoftware.htmlunit.WebWindowListener#webWindowOpened
		 */
		public void webWindowOpened(final WebWindowEvent event) {
			fWindows.push(event.getWebWindow());
			// page is not loaded yet, don't set it now as current window
		}
	}

	public WebClientContext(final String _name)
	{
		fName = _name;
	}
	
	/**
	 * Gets the name of this instance. Used by {@link SelectWebClient}.
	 */
	public String getName()
	{
		return fName;
	}

	public WebClient getWebClient() {
		return fWebClient;
	}

	public String getSavedUserName() {
		return fSavedUserName;
	}

	public String getSavedPassword() {
		return fSavedPassword;
	}

	/**
	 * Gets the current responses (currentResponse and previousResponse) of
	 * the context to be able to restore them later.
	 * <p/>
	 * MG: may probably be problematic (like previousResponse) when windows have been closed
	 *
	 * @return the status
	 */
	public StoredResponses getResponses() {
		return new StoredResponses(this);
	}

	/**
	 * Restore the responses to a previously saved value.
	 *
	 * @param savedResponses the responses to restore
	 */
	public void restoreResponses(final StoredResponses savedResponses) {
		fPreviousResponse = savedResponses.fPreviousResponse;
		fCurrentResponse = savedResponses.fCurrentResponse;
		LOG.info("Responses restored");
	}

	/**
	 * Gets the response on which actions and verifications will occur.
	 *
	 * @return the response
	 */
	public Page getCurrentResponse() {
		// test if window of current response has not been closed
		if (fCurrentResponse.getPage() != null
				&& !fWebClient.getWebWindows().contains(fCurrentResponse.getPage().getEnclosingWindow())) {
			LOG.info("The window containing current response has been closed, "
					+ "the content of the last opened window will become the current response");
			final WebWindow window = (WebWindow) fWindows.peek();
			saveResponseAsCurrent(window.getEnclosedPage());
		}
		return fCurrentResponse.getPage();
	}

	public String getCurrentResponseFile() {
		return fCurrentResponse.getFile();
	}

	public void setCurrentResponseFile(final String name) {
		fCurrentResponse.setFile(name);
	}


	/**
	 * Gets the current response as {@link com.gargoylesoftware.htmlunit.html.HtmlPage}
	 *
	 * @param step
	 * @throws StepExecutionException if the current response isn't an html page
	 */
	public HtmlPage getCurrentHtmlResponse(final Step step) {
		if (!(getCurrentResponse() instanceof HtmlPage)) {
			throw new StepExecutionException("Current response is not an HTML page but of type "
					+ getCurrentResponse().getWebResponse().getContentType(), step);
		}
		return (HtmlPage) getCurrentResponse();
	}

	public void restorePreviousResponse() {
		final WebWindow window = fPreviousResponse.getPage().getEnclosingWindow();
		if (!fWebClient.getWebWindows().contains(window)) {
			// register the window "back" to the browser
			fWebClient.registerWebWindow(window);
		}
		window.setEnclosedPage(fPreviousResponse.getPage());
		saveResponseAsCurrent(fPreviousResponse);
	}


	/**
	 * Sets the current and previous response for this context and this step.
	 *
	 * @param page The page to become the current response.
	 */
	public void saveResponseAsCurrent(final Page page) {
		saveResponseAsCurrent(new StoredResponse(page));
	}

	/**
	 * Sets the current and previous response for this context and this step
	 * with the associated files (if any)
	 *
	 * @param current The future current response.
	 */
	protected void saveResponseAsCurrent(final StoredResponse current) {
		Checker.assertFalse(current == null || current.getPage() == null, "Illegal new current response");
		setCurrentForm(null); // reset current form
		fPreviousResponse = fCurrentResponse;
		fCurrentResponse = current;

		LOG.info("Current response now: " + current.getPage().getUrl());
		LOG.debug("Previous response: " + (fPreviousResponse.getPage() != null ?
				fPreviousResponse.getPage().getUrl() : null));
	}

	public void setWebClient(final WebClient webClient) {
		fWebClient = webClient;
		restoreWindowListener();
		fWindows.push(webClient.getCurrentWindow());
	}

	public void suspendWindowListener() {
		fWebClient.removeWebWindowListener(fWindowListener);
	}

	public void restoreWindowListener() {
		fWebClient.addWebWindowListener(fWindowListener);
	}

	public void setSavedUserName(final String userName) {
		fSavedUserName = userName;
	}

	public void setSavedPassword(final String password) {
		fSavedPassword = password;
	}

	/**
	 * Gets the current form in the current response. This is the one that has been selected by setting the last form field.
	 *
	 * @return <code>null</code> if no current form available
	 */
	public HtmlForm getCurrentForm() {
		return fCurrentForm;
	}

	/**
	 * Sets the form that has to be used as the default one for setting fields.
	 *
	 * @param form new current form or if null then set current form to none (reset)
	 */
	public void setCurrentForm(final HtmlForm form) {
		fCurrentForm = form;
		if (form != null) {
			LOG.info("Current form set to (action=" + form.getActionAttribute() + ")");
		} else {
			LOG.info("Current form set to none");
		}
	}

	/**
	 * Closes the WebClient. This ensures that running js scripts are stopped.
	 * This instance should be used once this method has been called.
	 */
	public void destroy()
	{
		suspendWindowListener();
		// first get the top windows and then close them to avoid ConcurrentModificationException
		final List topWindows = new ArrayList();
		for (final Iterator iter=fWebClient.getWebWindows().iterator(); iter.hasNext();)
		{
			final WebWindow window = (WebWindow) iter.next();
			if (window instanceof TopLevelWindow)
			{
				topWindows.add(window);
			}
		}
		for (final Iterator iter=topWindows.iterator(); iter.hasNext();)
		{
			final TopLevelWindow window = (TopLevelWindow) iter.next();
			window.close();
		}
		
		fWebClient = null;
		fPreviousResponse = null;
		fCurrentResponse = null;
		fWindows.empty();
		fCurrentForm = null;
	}
}
