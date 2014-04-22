// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.boundary;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

/**
 * Helper class for working with files.
 * @author Paul King
 * @author Marc Guillemot
 */
public final class FileBoundary
{
    private FileBoundary() {
    }

    /**
     * Helper method when creating files.
     *
     * @param filename the filename of the resource to get
     * @param relativeClass the class whose package contains the resource
     * @return the File if it exists
     * @throws java.lang.IllegalStateException if the file could not be found
     */
    public static File getFile(final String filename, final Class relativeClass) {
        final URL url = relativeClass.getResource(filename);
        if (url == null) {
            // only used for getting resources within test code
            throw new IllegalStateException("Could not find resource file '" + filename + "'");
        }
        return new File(url.getFile());
    }

    /**
     * Gets the bytes of the file
     * @param file the file to read
     * @return the file content
     * @throws RuntimeException if the content can't be read
     */
	public static byte[] getBytes(final File file) {
		try 
		{
			return FileUtils.readFileToByteArray(file);
		}
        catch (final IOException e) 
        {
			throw new RuntimeException(e);
		}
	}
}
