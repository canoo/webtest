package com.canoo.ant.table;

import java.io.File;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;

public class BaseTestCase extends TestCase {

    public BaseTestCase(String name) {
        super(name);
    }

    protected File getPackageResource(final String fileName) throws Exception
    {
    	final String resourceName = getClass().getPackage().getName().replace('.', '/') + "/" + fileName;
    	final URL resource = getClass().getClassLoader().getResource(resourceName);
    	URI uri = new URI(resource.toExternalForm());
        final File file = new File(uri);
        assertTrue("File doesn't exist: " + file.getName(), file.exists());
        return file;
    }

}