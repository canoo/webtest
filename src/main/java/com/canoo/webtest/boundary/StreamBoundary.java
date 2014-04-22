// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.boundary;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.Step;
import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;

/**
 * Helper class for working with streams.
 *
 * @author Paul King
 */
public final class StreamBoundary {
    private static final Logger LOG = Logger.getLogger(StreamBoundary.class);
    private StreamBoundary() {}

    /**
     * Close an InputStream ignoring an errors.
     *
     * Wraps IOException's.
     *
     * @param inStream
     */
    public static void closeInputStream(final InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                LOG.warn("Error closing stream: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close an OutputStream ignoring an errors.
     *
     * Wraps IOException's.
     *
     * @param outStream
     */
    public static void closeOutputStream(final OutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                LOG.warn("Error closing stream: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close an InputStream and abort step if an error occur.
     *
     * Wraps IOException's.
     *
     * @param is
     * @param step
     */
    public static void tryCloseInputStreamWithFail(final InputStream is, final Step step) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                throw new StepExecutionException("Error closing stream: " + e.getMessage(), step);
            }
        }
    }

    /**
     * Close an OutputStream and abort step if an error occur.
     *
     * Wraps IOException's.
     *
     * @param os
     * @param step
     */
    public static void tryCloseOutputStream(final OutputStream os, final Step step) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                throw new StepExecutionException("Error closing stream: " + e.getMessage(), step);
            }
        }
    }

    public static Object tryReadObject(final ObjectInputStream ois, final Step step) throws IOException {
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new StepExecutionException("Error reading object from file: " + e.getMessage(), step);
        }
    }

    public static byte[] tryGetBytes(final Context context, final Step step) {
        try {
            return IOUtils.toByteArray(context.getCurrentResponse().getWebResponse().getContentAsStream());
        } catch (IOException e) {
            throw new StepExecutionException("Error processing content: " + e.getMessage(), step);
        }
    }
}
