// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link StorePropertyStep}.
 *
 * @author Paul King
 * @author Marc Guillemot
 */
public class StorePropertyStepTest extends BaseStepTestCase
{
    private StorePropertyStep fStep;

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (StorePropertyStep) getStep();
    }

    protected Step createStep() {
        return new StorePropertyStep();
    }

    // <storeProperty name="MyName" value="MyValue" />
    public void testExecute() {
        fStep.setName("MyName");
        fStep.setValue("MyValue");
        fStep.execute();
        assertEquals("MyValue", fStep.getWebtestProperty("MyName"));
    }

    // <storeProperty name="MyName" value="MyValue" propertyType="ant" />
    public void testExecuteAnt() {
        fStep.setName("MyName");
        fStep.setValue("MyValue");
        fStep.setPropertyType(Step.PROPERTY_TYPE_ANT);
        fStep.execute();
        assertEquals("MyValue", fStep.getWebtestProperty("MyName", Step.PROPERTY_TYPE_ANT));
    }

    // <storeProperty name="MyName" value="MyValue" propertyType="dynamic" />
    public void testExecuteDynamic() {
        fStep.setName("MyName");
        fStep.setValue("MyValue");
        fStep.setPropertyType(Step.PROPERTY_TYPE_DYNAMIC);
        fStep.execute();
        assertEquals("MyValue", fStep.getWebtestProperty("MyName", Step.PROPERTY_TYPE_DYNAMIC));
    }

    // <storeProperty name="MyName" value="3+4" eval="true" />
    public void testEval() {
        fStep.setName("MyName");
        fStep.setValue("3+4");
        fStep.setEval("true");
        fStep.execute();
        assertEquals("7", fStep.getWebtestProperty("MyName"));
    }

    public void testEvalDirect() {
        assertEquals("7", fStep.doEvaluate("3+4"));
        assertEquals("14", fStep.doEvaluate("3.5 * 4"));
        assertEquals("4.5", fStep.doEvaluate("14 / 4 + 1"));
        assertEquals("8", fStep.doEvaluate("199 % 5 * 2"));
        assertEquals("9", fStep.doEvaluate("199 % (5 * 2)"));
        assertEquals("2.5", fStep.doEvaluate("(20 - 6) / 4 - 1"));
        fStep.setWebtestProperty("six", "6");
        assertEquals("2.5", fStep.doEvaluate("(20 - #{six}) / 4 - 1"));
        fStep.setWebtestProperty("eight", "8");
        assertEquals("48", fStep.doEvaluate("#{eight} * #{six}"));
        fStep.setWebtestProperty("six", "six");
        String message = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
            public void call() throws Throwable {
                assertEquals("2.5", fStep.doEvaluate("(20 - #{six}) / 4 - 1"));
            }
        });
        assertEquals("Attempted to evaluate non-numeric property '#{six}': For input string: \"six\"", message);
    }

    // <storeProperty name="MyName" />
    public void testNoNameAttribute() throws Exception {
        fStep.setName("MyName");
        assertStepRejectsNullParam("value", getExecuteStepTestBlock());
    }

    // <storeProperty name="MyName" value="MyValue" propertyType="unknown"/>
    public void testUnknownPropertyType() {
        fStep.setName("MyName");
        fStep.setValue("MyValue");
        fStep.setPropertyType("unknown");
        String msg = ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock());
        assertTrue(msg.indexOf("Unknown propertyType") != -1);
    }

    // <storeProperty value="MyValue" />
    public void testNoValueAttribute() throws Exception {
        fStep.setValue("MyValue");
        assertStepRejectsNullParam("name", getExecuteStepTestBlock());
    }

    // <storeProperty file="myPropertyFile" /> with not existing file
    public void testFileNotExisting() throws Exception {
    	final File tmpFile = File.createTempFile("foo", "foo");
    	tmpFile.delete();
    	assertFalse(tmpFile.exists());

    	fStep.setFile(tmpFile);
        final Throwable t = ThrowAssert.assertThrows("", BuildException.class, getExecuteStepTestBlock());
        assertInstanceOf(FileNotFoundException.class, t.getCause());
    }

    // <storeProperty file="myPropertyFile" />
    public void testFile() throws Exception {
    	// ensure that the properties don't exist
        assertNull(fStep.getWebtestProperty("prop1"));
        assertNull(fStep.getWebtestProperty("prop2"));

        final File tmpFile = File.createTempFile("foo", "foo");
    	tmpFile.deleteOnExit();
    	FileUtils.writeStringToFile(tmpFile, "prop1=hello\nprop2=bye bye");
    	assertTrue(tmpFile.exists());

    	fStep.setFile(tmpFile);
        fStep.execute();
        assertEquals("hello", fStep.getWebtestProperty("prop1"));
        assertEquals("bye bye", fStep.getWebtestProperty("prop2"));
    }
}
