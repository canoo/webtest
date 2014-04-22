package com.canoo.webtest.boundary;
/*
 * Copyright  2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This is a MODIFIED VERSION of ScriptRunner from Ant.
 *  Modifications Copyright (c) ASERT 2005.
 *  Modifications released under the Canoo WebTest license.
 */
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is used to run BSF scripts
 */
public class ResetScriptRunner
{
    // Register Groovy ourselves, since BSF does not
    // natively support it (yet).
    // This "hack" can be removed once BSF has been
    // modified to support Groovy or more dynamic
    // registration.
    static {
        BSFManager.registerScriptingEngine(
                "groovy",
                "org.codehaus.groovy.bsf.GroovyEngine",
                new String[]{"groovy", "gy"});
    }

    /**
     * Script language
     */
    private String fLanguage;

    /**
     * Script content
     */
    private String fScript;

    /**
     * Beans to be provided to the script
     */
    private Map fBeans = new HashMap();
    private BSFManager fManager;

    {
        reset();
    }

    public void reset() {
        fScript = "";
    }

    /**
     * Add a list of named objects to the list to be exported to the script
     *
     * @param dictionary a map of objects to be placed into the script context
     *                   indexed by String names.
     */
    public void addBeans(final Map dictionary) {
        for (Iterator i = dictionary.keySet().iterator(); i.hasNext();) {
            final String key = (String) i.next();
            try {
                final Object val = dictionary.get(key);
                addBean(key, val);
            } catch (BuildException ex) {
                // The key is in the dictionary but cannot be retrieved
                // This is usually due to references that refer to tasks
                // that have not been taskdefed in the current run.
                // Ignore
            }
        }
    }

    /**
     * Add a single object into the script context.
     *
     * @param key  the name in the context this object is to stored under.
     * @param bean the object to be stored in the script context.
     */
    public void addBean(final String key, final Object bean) {
        boolean isValid = key.length() > 0
                && Character.isJavaIdentifierStart(key.charAt(0));

        for (int i = 1; isValid && i < key.length(); i++) {
            isValid = Character.isJavaIdentifierPart(key.charAt(i));
        }

        if (isValid) {
            fBeans.put(key, bean);
        }
    }

    /**
     * Do the work.
     *
     * @param execName the name that will be passed to BSF for this script
     *                 execution.
     * @throws BuildException if someting goes wrong exectuing the script.
     */
    public void executeScript(final String execName) throws BuildException {
        if (fLanguage == null) {
            throw new BuildException("script language must be specified");
        }
        try {
            if (fManager == null) {
                fManager = prepareManager();
            }
            // execute the script
            fManager.exec(fLanguage, execName, 0, 0, fScript);
        } catch (BSFException be) {
            throw createBuildException(be);
        }
    }

    /**
     * Do the work.
     *
     * @param execName the name that will be passed to BSF for this script
     *                 execution.
     * @throws BuildException if someting goes wrong exectuing the script.
     */
    public String evalScript(final String execName) throws BuildException {
        if (fLanguage == null) {
            throw new BuildException("script language must be specified");
        }
        try {
            if (fManager == null) {
                fManager = prepareManager();
            }
            // evaluate the expression
            return fManager.eval(fLanguage, execName, 0, 0, fScript).toString();
        } catch (BSFException be) {
            throw createBuildException(be);
        }
    }

    private BSFManager prepareManager() throws BSFException {
        final BSFManager manager = new BSFManager();
        for (Iterator i = fBeans.keySet().iterator(); i.hasNext();) {
            final String key = (String) i.next();
            final Object value = fBeans.get(key);
            if (value != null) {
                manager.declareBean(key, value, value.getClass());
            } else {
                // BSF uses a hashtable to store values
                // so cannot declareBean with a null value
                // So need to remove any bean of this name as
                // that bean should not be visible
                manager.undeclareBean(key);
            }
        }
        return manager;
    }

    private static BuildException createBuildException(final BSFException be) {
        Throwable t = be;
        final Throwable te = be.getTargetException();
        if (te != null) {
            if (te instanceof BuildException) {
                return (BuildException) te;
            } else {
                t = te;
            }
        }
        return new BuildException(t);
    }

    /**
     * Defines the language (required).
     *
     * @param lang the scripting language name for the script.
     */
    public void setLanguage(final String lang) {
        this.fLanguage = lang;
    }

    /**
     * Get the script language
     *
     * @return the script language
     */
    public String getLanguage() {
        return fLanguage;
    }

    /**
     * Load the script from an external file ; optional.
     *
     * @param file the file containing the script source.
     */
    public void setSrc(final File file) {
        if (!file.exists()) {
            throw new BuildException("file " + file.getPath() + " not found.");
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            fScript += IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Set the script text.
     *
     * @param text a component of the script text to be added.
     */
    public void addText(final String text) {
        fScript += text;
    }
}
