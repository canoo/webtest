// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.WebClientContext;
import com.canoo.webtest.steps.AbstractStepContainer;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Selects the WebClient to use as current, creating a new one if this WebClient doesn't exist.
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="selectWebClient"
 * description="Selects the WebClient to navigate with, creating it if none exists with this name.
 * Useful when a test sequence requires actions to be performed by different users (e.g. an admin and an normal user).
 * If used with nested steps, the previously selected WebClient is selected again when the execution of the nested tests ends."
 */
public class SelectWebClient extends AbstractStepContainer {
    private static final Logger LOG = Logger.getLogger(SelectWebClient.class);
    private String fName;

    public String getName() {
        return fName;
    }

    /**
     * @webtest.parameter required="yes"
     * description="The name of the WebClient to select (will be created if it doesn't yet exist). 
     * 'default' designates the WebClient available at test startup.
     * This name has nothing to do with the simulated browser and just serve for organisation within the test
     * like 'administrator' or 'normal user'"
     */
    public void setName(final String newName) {
        fName = newName;
    }

    public void doExecute() throws Exception {
        LOG.debug("Selecting WebClient " + getName());

        final WebClientContext wcc = getContext().getCurrentWebClientContext();
        getContext().defineCurrentWebClientContext(getName());

        final Page currentResponse = getContext().getCurrentResponse();
        final String message;
        if (currentResponse == null)
        {
        	message = "No current response";
        }
        else
        {
        	message = "Current response is now: " + currentResponse.getUrl();
        }
        LOG.debug(message);

        if (!getSteps().isEmpty())
        {
        	try
        	{
        		executeContainedSteps();
        	}
        	finally
        	{
                LOG.debug("Restoring WebClientContext " + wcc.getName());
        		getContext().defineCurrentWebClientContext(wcc.getName());
        	}
        }
    }

    protected void verifyParameters() {
        super.verifyParameters();
        emptyParamCheck(getName(), "name");
    }

    public boolean isPerformingAction() {
    	return false;
    }
}
