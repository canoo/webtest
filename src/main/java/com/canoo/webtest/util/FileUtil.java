// Copyright © 2002-2007 Canoo Engineering AG, Switzerland. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.canoo.webtest.boundary.StreamBoundary;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.WebResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Utility class for working with files.
 *
 * @author Paul King
 */
public class FileUtil
{
    private static final Logger LOG = Logger.getLogger(FileUtil.class);

    /**
     * Reads a file into a String.
     *
     * @param file
     * @param step
     * @return the resulting String
     */
    public static String readFileToString(final File file, final Step step) {
        String canonicalPath = null;
        String result = null;
        InputStream inputStream = null;
        try {
            canonicalPath = file.getCanonicalPath();
            inputStream = new FileInputStream(file);
            result = IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new StepExecutionException("Could not find/read \"" + canonicalPath + "\".", step);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return result;
    }

    /**
     * Reads a file into a byte[].
     *
     * @param file
     * @param step
     * @return the resulting byte[]
     */
    public static byte[] readFileToByteArray(final File file, final Step step) {
        String canonicalPath = null;
        byte[] result = null;
        InputStream inputStream = null;
        try {
            canonicalPath = file.getCanonicalPath();
            inputStream = new FileInputStream(file);
            result = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new StepExecutionException("Could not find/read \"" + canonicalPath + "\".", step);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return result;
    }

    /**
     * Writes a String to a file.
     *
     * @param file
     * @param content
     * @param step
     * @throws StepExecutionException if something goes wrong
     */
    public static void writeStringToFile(final File file, final String content, final Step step) {
        String canonicalPath = null;
        FileOutputStream outputStream = null;
        try {
            canonicalPath = file.getCanonicalPath();
            outputStream = new FileOutputStream(file);
            IOUtils.write(content, outputStream);
        } catch (IOException e) {
            throw new StepExecutionException("Could not find/write \"" + canonicalPath + "\".", step);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Writes a Stream to a file.
     *
     * @param inputResponse
     * @param destfile
     * @param step
     * @throws StepExecutionException if something goes wrong
     */
    public static void writeResponseStreamToFile(final WebResponse inputResponse, final File destfile, final Step step) {
        String canonicalPath = null;
        FileOutputStream outputStream = null;
        try {
            canonicalPath = destfile.getCanonicalPath();
            outputStream = new FileOutputStream(destfile);
            IOUtils.copy(inputResponse.getContentAsStream(), outputStream);
        } catch (IOException e) {
            throw new StepExecutionException("Could not find/write \"" + canonicalPath + "\".", step);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Creates parent directory for file if required.
     *
     * @param file
     */
    public static void prepareDirs(final File file) {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            prepareDirs(file.getParentFile());
            file.getParentFile().mkdirs();
        }
    }

    /**
     * Helper method for reading objects from a file.
     *
     * @param file the file to read from
     * @param step step requesting the operation
     * @return the object from the file if everything worked correctly
     */
    public static Object tryReadObjectFromFile(final File file, final Step step) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        String message="finding";
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            message = "reading from";
            return StreamBoundary.tryReadObject(ois, step);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new StepExecutionException("Error " + message + " file: " + e.getMessage(), step);
        } finally {
            IOUtils.closeQuietly(ois);
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * Helper method for writing objects to a file.
     *
     * @param file the file to write to
     * @param object the object to write
     * @param step step requesting the operation
     */
    public static boolean tryWriteObjectToFile(final File file, final Object object, final Step step) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        String message = "creating";
        boolean success = false;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            message = "filling";
            oos.writeObject(object);
            success = true;
        } catch (IOException e) {
            LOG.error("Error during write: " + e.getMessage(), e);
            if (step != null) {
                throw new StepExecutionException("Error " + message + " file: " + e.getMessage(), step);
            }
        } finally {
            StreamBoundary.closeOutputStream(oos);
            StreamBoundary.closeOutputStream(fos);
        }
        return success;
    }

    /**
     * Creates a temporary file using a prefix and suffix and marks it for deletion on exit.
     *
     * @param prefix
     * @param suffix
     * @param step
     * @throws StepExecutionException if an error occurs while creating the file
     * @return the newly created temp file
     */
    public static File tryCreateTempFile(final String prefix, final String suffix, final Step step) {
        final File tmpFile;
        try {
            tmpFile = File.createTempFile(prefix, suffix);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new StepExecutionException("Error creating temporary file " + e.getMessage(), step);
        }
        tmpFile.deleteOnExit();
        return tmpFile;
    }
}
