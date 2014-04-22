// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.io.File;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.control.BaseWrappedStepTestCase;

/**
 * Test for {@link VerifyContent}.
 * @author Marc Guillemot
 */
public class VerifyContentTest extends BaseWrappedStepTestCase
{
    protected Step createStep() {
        return new VerifyContent();
    }

    public void testVerifyParameter()
    {
    	final VerifyContent step = (VerifyContent) getStep();
    	final File existingFile = new File("blabla")
    	{
    		public boolean exists() {
    			return true;
    		}
    	};
    	step.setReferenceFile(existingFile);
    	step.setMode("foo");
        final Throwable t = ThrowAssert.assertThrows("", StepExecutionException.class, getExecuteStepTestBlock());
        assertEquals("Unallowed diff mode >foo<. Allowed modes are {auto,bin,text,regexperline}.", t.getMessage());
    	
    }
}
