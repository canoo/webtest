// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.boundary;

import java.io.IOException;
import java.net.URL;

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Boundary class for interacting with HtmlUnit.
 *
 * @author Paul King
 * @author Marc Guillemot
 */
public final class HtmlUnitBoundary
{
    private static final Logger LOG = Logger.getLogger(HtmlUnitBoundary.class);
    private HtmlUnitBoundary() {}

    public static Page tryGetPage(final URL url, final WebClient webClient) {
        return tryGetPage(url, webClient, true);
    }

    public static Page tryGetPageNoFail(final URL url, final WebClient webClient) {
        return tryGetPage(url, webClient, false);
    }

    private static Page tryGetPage(final URL url, final WebClient webClient, final boolean shouldFail) {
        try {
			LOG.debug("Visiting: " + url + " (fail=" + shouldFail + ")");
			return webClient.getPage(url);
		} catch (final FailingHttpStatusCodeException e) {
			LOG.debug("Testing " + url + " failed: status code " + e.getStatusCode() + "("
			   + e.getStatusMessage() + ")");
            return returnNullOrFail(e, shouldFail);
		} catch (final IOException e) {
			LOG.debug("Testing " + url + " failed: IOException " + e.getMessage());
            return returnNullOrFail(e, shouldFail);
		}
    }

    private static Page returnNullOrFail(final Exception e, final boolean shouldFail) {
        if (shouldFail) {
            throw new RuntimeException(e);
        } else {
            return null;
        }
    }

    // TODO: smell boundary class call back into non-boundary class (xpathHelper) extract out and supply tests
    public static Object trySelectSingleNodeByXPath(final String xpathStr, final Page currentResp, final Step step) 
    throws StepExecutionException {
    	try
    	{
    		return step.getContext().getXPathHelper().selectFirst(currentResp, xpathStr);
    	} 
    	catch (final XPathException e) {
    		throw new StepExecutionException("Error processing xpath \"" + xpathStr + "\".", step, e);
    	}
    }
}
