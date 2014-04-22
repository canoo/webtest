// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.verify;


import org.apache.tools.ant.Project;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;


/**
 * Tests the verifyProperty step.
 *
 * @author   Paul King, ASERT
 */
public class VerifyPropertyTest extends BaseStepTestCase
{
    private VerifyProperty fStep;

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (VerifyProperty) getStep();
        fStep.setWebtestProperty("dynProp", "The DynProp Value");
        fStep.setProject(new Project());
        fStep.getProject().setNewProperty("antProp", "The AntProp Value");
    }

	protected Step createStep() {
        return new VerifyProperty();
    }

    public void testAliases() throws Exception {
        fStep.setValue("foo");
        assertEquals("foo", fStep.getValue());
        assertEquals("foo", fStep.getText());
        fStep.setText("fii");
        assertEquals("fii", fStep.getValue());
        assertEquals("fii", fStep.getText());

        fStep.setProperty("foo");
        assertEquals("foo", fStep.getProperty());
        assertEquals("foo", fStep.getName());
        fStep.setName("fii");
        assertEquals("fii", fStep.getProperty());
        assertEquals("fii", fStep.getName());
    }

    /*
     * There is some overlap with the functional tests in these
     * unit tests but thought it was better to be safe than sorry
     */

    // <verifyProperty />
    public void testNoPropertyNameAttributes() throws Exception {
        assertStepRejectsNullParam("name", new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
    }

    // <verifyProperty name="dynProp" />
    public void testDynPropertyExists() throws Exception {
        fStep.setName("dynProp");
        executeStep(fStep);
    }

    // <verifyProperty name="antProp" propertyType="ant" />
    public void testAntPropertyExists() throws Exception {
        fStep.setName("antProp");
        fStep.setPropertyType(Step.PROPERTY_TYPE_ANT);
        executeStep(fStep);
    }

    // <verifyProperty name="dynProp" text="The DynProp Value"/>
    public void testDynPropertyHasCorrectValue() throws Exception {
        fStep.setName("dynProp");
        fStep.setText("The DynProp Value");
        executeStep(fStep);
    }

    // <verifyProperty name="antProp" propertyType="ant" text="The AntProp Value"/>
    public void testAntPropertyHasCorrectValue() throws Exception {
        fStep.setName("antProp");
        fStep.setPropertyType(Step.PROPERTY_TYPE_ANT);
        fStep.setText("The AntProp Value");
        executeStep(fStep);
    }

    // <verifyProperty name="dynProp" text="The DynProp Value"/>
    public void testDynPropertyValueWithRegex() throws Exception {
        fStep.setName("dynProp");
        fStep.setRegex("true");
        fStep.setText(".*Value");
        executeStep(fStep);
    }

    // <verifyProperty name="antProp" propertyType="ant" text="The AntProp Value"/>
    public void testAntPropertyValueWithRegex() throws Exception {
        fStep.setName("antProp");
        fStep.setPropertyType(Step.PROPERTY_TYPE_ANT);
        fStep.setRegex("true");
        fStep.setText(".*Value");
        executeStep(fStep);
    }

    // <verifyProperty name="dynProp" text="Something completely different" />
    public void testDynPropertyIncorrectValue() throws Exception {
        fStep.setName("dynProp");
        fStep.setText("Something completely different");
        String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
        assertTrue(msg.indexOf("Incorrect property value found!") != -1);
    }

    // <verifyProperty name="antProp" propertyType="ant" text="Something completely different" />
    public void testAntPropertyIncorrectValue() throws Exception {
        fStep.setName("antProp");
        fStep.setPropertyType(Step.PROPERTY_TYPE_ANT);
        fStep.setText("Something completely different");
        String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
        assertTrue(msg.indexOf("Incorrect property value found!") != -1);
    }

    // <verifyProperty name="someOtherProp" text="The DynProp Value" />
    public void testDynPropertyUnknownName() throws Exception {
        fStep.setName("someOtherProp");
        fStep.setText("The DynProp Value");
        String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
        assertTrue(msg.indexOf("Expected property \"someOtherProp\" to be defined!") != -1);
    }

    // <verifyProperty name="antProp" text="The AntProp Value" propertyType="unknown"/>
    public void testUnknownPropertyType() {
        fStep.setName("antProp");
        fStep.setPropertyType("unknown");
        fStep.setText("The AntProp Value");
        String msg = ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
        assertTrue(msg.indexOf("Unknown propertyType") != -1);
    }

    // <verifyProperty name="someOtherProp" propertyType="ant" text="The AntProp Value" />
    public void testAntPropertyUnknownName() throws Exception {
        fStep.setName("someOtherProp");
        fStep.setPropertyType(Step.PROPERTY_TYPE_ANT);
        fStep.setText("The AntProp Value");
        String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
        assertTrue(msg.indexOf("Expected property \"someOtherProp\" to be defined!") != -1);
    }

}
