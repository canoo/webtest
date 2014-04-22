// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.boundary;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.engine.StepExecutionException;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;

/**
 * Helper class for working with URLs.
 *
 * @author Paul King
 */
public final class UrlBoundary
{
    private static final Logger LOG = Logger.getLogger(UrlBoundary.class);
    private UrlBoundary() {}

    /**
     * Helper method to create URLs.
     *
     * @param urlStr
     * @return the created URL
     * @throws java.lang.RuntimeException if the URL could not be created
     */
    public static URL tryCreateUrl(final String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            LOG.error("Creating URL '" + urlStr + "' failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to create URLs.
     *
     * @param urlStr
     * @return the created URL
     * @throws java.lang.RuntimeException if the URL could not be created
     */
    public static URL tryCreateUrlWithError(final URL resource, final String urlStr, final Step step) {
        try {
            return new URL(resource, urlStr);
        } catch (MalformedURLException e) {
            LOG.error("Creating URL '" + urlStr + "' failed: " + e.getMessage());
            throw new StepExecutionException(e.getMessage(), step);
        }
    }

    /**
     * Helper method to create URLs.
     *
     * @param urlFile
     * @return the created URL
     */
    public static URL tryCreateUrlFromFileWithError(final File urlFile, final Step step) {
        try {
            return urlFile.toURI().toURL();
        } catch (MalformedURLException e) {
            LOG.error("Creating URL for File '" + urlFile.getName() + "' failed: " + e.getMessage());
            throw new StepExecutionException(e.getMessage(), step);
        }
    }
}
