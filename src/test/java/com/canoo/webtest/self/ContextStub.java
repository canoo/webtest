// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.tools.ant.Project;

import com.canoo.webtest.ant.TestStepSequence;
import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.boundary.FileBoundary;
import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.canoo.webtest.boundary.UrlBoundary;
import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.interfaces.IPropertyHandler;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Context implementation with facilities to use in unit tests.
 * @author Dierk König
 * @author Marc Guillemot
 * @author Paul King
 */

public class ContextStub extends Context
{
    private static final String DEFAULT_CONTENT_TYPE = "text/html";
    private static final String DEFAULT_HTML = "<HTML></HTML>";
	private boolean fFakeLastResponse;
	private Page fFakedLastReponse;

    public static final String SOME_BASE_URL = "http://webtest.canoo.com";
    public static final ContextStub CONTEXT_STUB_NOCURRENTRESPONSE = new ContextStub()
    {
        public Page getCurrentResponse() {
            return null;
        }
    };

    /**
     * Sets the default response to an empty html page
     */
    public ContextStub() {
        this(DEFAULT_HTML);
    }

    /**
     * Sets the current response text as text/html
     * @param responseText the response text
     */
    public ContextStub(final String responseText) {
        this(responseText, DEFAULT_CONTENT_TYPE);
    }

	/**
     * Sets the current response type and content to the provided values
	 * @param file the file to read the content from
     * @param contentType the response type
	 */
	public ContextStub(final File file, final String contentType) {
        this(FileBoundary.getBytes(file), contentType);
    }

	/**
     * Sets the current response text and content to the provided values
     * @param responseText the response text
     * @param contentType the response type
     */
    public ContextStub(final String responseText, final String contentType) {
        super(new WebtestTask());
        init();

        setDefaultResponse(responseText, contentType);
    }

    protected void init()
    {
        WebtestTask.setThreadContext(this);
        final Project project = new Project();
        getWebtest().setProject(project);
        initConfig(project);
        initTestStepSequence(project);

        final WebClient client = getWebtest().getConfig().createWebClient(); // to ensure that the webclient used is configured
        setWebClient(client);
        final MockWebConnection webConnection = new MockWebConnection();
        client.setWebConnection(webConnection);
    }

	protected void initConfig(final Project project) {
		final Configuration config = new Configuration();
        config.setProject(project);
        final IPropertyHandler noPropHandler = new IPropertyHandler() {
        	public String getProperty(final String propertyName)
        	{
        		return null;
        	}
        };
        getWebtest().addConfig(config);
        config.setPropertyHandler(noPropHandler);
	}

	protected void initTestStepSequence(final Project project) {
		final TestStepSequence steps = new TestStepSequence();
        steps.setProject(project);
        getWebtest().addSteps(steps);
	}

    public ContextStub(final byte[] responseBytes, final String contentType) {
        super(new WebtestTask());
        init();
        setDefaultResponse(responseBytes, contentType);
    }

    /*
     * Sets the text of the page for the next answer (as text/html)
     */
    public void setDefaultResponse(final String currentResponseText) {
        setDefaultResponse(currentResponseText, "text/html");
    }

    /*
     * Sets the text and the content type of the page for the next answer
     */
    public void setDefaultResponse(final String text, final String contentType) {
        setDefaultResponse(text.getBytes(), contentType);
    }

    /*
     * Sets the text and the content type of the page for the next answer
     */
    public void setDefaultResponse(final byte[] bytes, final String contentType) {
        ((MockWebConnection) getWebClient().getWebConnection()).setDefaultResponse(bytes, 200, "OK", contentType);
        final URL url = UrlBoundary.tryCreateUrl(SOME_BASE_URL);
        HtmlUnitBoundary.tryGetPage(url, getWebClient());
    }

    /**
     * Defines what should be answered as last reponse
     * @param page the page to return
     */
    public void fakeLastResponse(final Page page)
    {
    	final Page currentResponse = getCurrentResponse();
    	if (currentResponse != null) {
			try {
				currentResponse.cleanUp();
			}
    		catch (final IOException e) {
				throw new RuntimeException(e);
			}
    	}
    	fFakeLastResponse = true;
    	fFakedLastReponse = page;
    }
    
    /**
     * Gets the current response.
     * @return the faked response if any, otherwise the response provided by {@link com.canoo.webtest.engine.Context#getCurrentResponse()}
     * @see com.canoo.webtest.engine.Context#getCurrentResponse()
     */
    public Page getCurrentResponse()
    {
    	if (fFakeLastResponse)
    		return fFakedLastReponse;
    	else 
    		return super.getCurrentResponse();
    }
}
