// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.form;

import java.io.File;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.form.AbstractSetFieldStep;
import com.canoo.webtest.steps.form.SetFileField;

/**
 * Test class for {@link SetFileField}.
 * @author Paul King
 * @author Marc Guillemot
 */
public class SetFileFieldTest extends BaseStepTestCase
{
    private SetFileField fStep;

	protected Step createStep() {
        return new SetFileField();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (SetFileField) getStep();
    }

    public void testVerifyParameterUsage() {
        // <setFileField name="someName" />
        fStep.setFileName(null);
        fStep.setName("someName");
        assertStepRejectsNullParam("fileName", getExecuteStepTestBlock());
        // <setFileField fileName="someFileName" />
        fStep.setFileName(new File("someFileName"));
        fStep.setName(null);
        String msg = ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock());
        assertTrue(msg.indexOf(AbstractSetFieldStep.MESSAGE_ARGUMENT_MISSING) != -1);
        // bad index
        fStep.setName(null);
        fStep.setFieldIndex("blah");
        ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock());
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setFileName(new File("someFileName"));
        fStep.setName("someName");
        assertStepRejectsNullResponse(fStep);
    }

    public void testErrorWithXmlPage() throws Exception {
    	final File existingFile = new File("foo")
    	{
    		public boolean exists()
    		{
    			return true;
    		}
    	};
        fStep.setName("someName");
        fStep.setFileName(existingFile);
        assertErrorOnExecuteIfCurrentPageIsXml(fStep);
    }

}
