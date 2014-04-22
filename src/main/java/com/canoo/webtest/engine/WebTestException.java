package com.canoo.webtest.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Base class for WebTest exceptions.
 * @author Marc Guillemot
 */
public class WebTestException extends BuildException {
    private Step fFailedStep;
	private final List details = new ArrayList();
	private final String fShortMessage;
	private final String urlCurrentResponse_;

	protected WebTestException(final String message, final Throwable cause) {
        super(message, cause);
        fShortMessage = message;
        urlCurrentResponse_ = readUrlCurrentResponse();
    }

	private static String readUrlCurrentResponse() {
		final Page currentResponse = WebtestTask.getThreadContext().getCurrentResponse();
        if (currentResponse == null)
        	return "-- none --";
        else
        	return currentResponse.getUrl().toExternalForm();
	}
    
    /**
     * @param failedStep may only be null for testing purposes *
     */
	protected WebTestException(final String message, final Step failedStep, final Throwable cause) {
        this(message, cause);
        fFailedStep = failedStep;
    }

    /**
     * @param failedStep may only be null for testing purposes *
     */
	protected WebTestException(final String message, final Step failedStep) {
        this(message, failedStep, null);
    }

    /**
     * @param failedStep may only be null for testing purposes *
     */
	protected WebTestException(final String shortMessage, final String messageEnd, final Step failedStep) {
		super(shortMessage + messageEnd);
		fShortMessage = shortMessage;
		fFailedStep = failedStep;
        urlCurrentResponse_ = readUrlCurrentResponse();
    }

	/**
     * Gets details of the exception
     * @return a list of {@link NameValuePair}
     */
    public List getDetails()
    {
    	return details;
    }
    
    public void addDetail(final String name, final String value)
    {
    	details.add(new NameValuePair(name, value));
    }
    
    /**
     * Gets the short form of the message. This is what will be displayed as
     * error cause in the report. The detailed information from {@link #getDetails()}
     * receives a special formatting. This may be the same than {@link #getMessage()} when for instance
     * no detail information is available.
     * @return the short message
     */
    public String getShortMessage()
    {
    	return fShortMessage;
    }

	public String toString() {
		if (fFailedStep == null) {
			return super.toString();
		} else {
			return super.toString() + ", Step: " + fFailedStep.toString();
		}
	}
	
	/**
	 * Gets the url of the current response when the exception occurred
	 * @return "-- none --" when no current response was available
	 */
	public String getUrlCurrentResponse()
	{
		return urlCurrentResponse_;
	}
}
