// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.Evaluator;
import com.canoo.webtest.util.Expression;

/**
 * Step which stores a value into a property.<p>
 *
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step
 *   category="Core"
 *   name="storeProperty"
 *   alias="storeDynamicProperty"
 *   description="Provides the ability to store a given value into a property. It supports both dynamic properties and traditional ant properties."
 */
public class StorePropertyStep extends BaseStoreStep implements Evaluator {
    private String fPropertyValue;
    private String fEval;
    private String fName;
    private File fFile;


    public void doExecute() {
    	if (fFile == null)
    		addProperty(getName(), getValue());
    	else
    		loadProperties(fFile);
    }

    private void addProperty(final String name, String value) {
    	value = isEval() ? doEvaluate(value) : value;
        storeProperty(value, name);
	}

	String doEvaluate(final String propertyValue) {
        final double d = Expression.evaluateExpression(propertyValue, this);
        if (d == (int)d) {
            return Integer.toString((int)d);
        }
        return Double.toString(d);
    }

    public String getName() {
        return fName;
    }

    /**
     * Sets the property name
     *
     * @param name
     * @webtest.parameter
     *   required="yes/no"
     *   description="The property name. Must be set if <em>property</em> is not set (unless <em>file</em> is used)."
     */
    public void setName(final String name) {
    	fName = name;
    }

    public String getValue() {
        return fPropertyValue;
    }

    /**
     * Sets the property value
     *
     * @param value
     * @webtest.parameter
     *   required="yes/no"
     *   description="The property value. Must be set unless <em>file</em> is used."
     */
    public void setValue(final String value) {
        fPropertyValue = value;
    }


    public boolean isEval() {
        return ConversionUtil.convertToBoolean(getEval(), false);
    }

    public String getEval() {
        return fEval;
    }

    /**
     * Sets the eval flag
     *
     * @param eval
     * @webtest.parameter
     *   required="no"
     *   default="false"
     *   description="A flag which determines if the value is to be treated as a mathematical expression to be evaluated."
     */
    public void setEval(final String eval) {
        fEval = eval;
    }

    protected void verifyParameters() {
    	if (fFile == null) {
	        nullParamCheck(StringUtils.defaultString(getProperty(), fName), "name");
	        nullParamCheck(getValue(), "value");
    	}
    	else {
    		paramCheck(fName != null, "Parameters \"name\" and \"file\" can't be set together");
    		paramCheck(getValue() != null, "Parameters \"value\" and \"file\" can't be set together");
    	}
    }

    public double evaluate(final String s) {
        final String result = getProject().replaceProperties(s);
        try {
            return Double.parseDouble(result);
        }
        catch (final NumberFormatException e) {
            throw new StepFailedException("Attempted to evaluate non-numeric property '"
                    + s + "': " + e.getMessage(), this);
        }
    }

    /**
     * The property value if inlined.
     *
     * @param text The new value
     * @webtest.nested.parameter
     *    required="no"
     *    description="An alternative to the attribute value."
     */
    public void addText(final String text) {
        fPropertyValue = text;
    }

    /**
     * Load properties from a file
     * @param file file to load
     * @throws BuildException on error
     */
    protected void loadProperties(final File file) {
        final Properties props = new Properties();
        log("Loading properties from " + file.getAbsolutePath(), Project.MSG_VERBOSE);

        FileInputStream fis = null;
        try {
        	fis = new FileInputStream(file);
            props.load(fis);
        }
        catch (final IOException e) {
        	throw new BuildException(e);
        }
        finally {
        	IOUtils.closeQuietly(fis);
        }
        
        for (final Iterator iter = props.entrySet().iterator(); iter.hasNext(); ) {
        	final Map.Entry entry = (Entry) iter.next();
            addProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }

    /**
     * 
     * @webtest.parameter
     *   required="yes/no"
     *   description="The name of the file from which properties should be loaded.
     *   It is similar to Ant's <property file='...'/> with the difference that this task allows to define dynamic properties
     *   that can be overwritten."
     */
	public void setFile(final File propertiesFile) {
		fFile = propertiesFile;
	}
}
