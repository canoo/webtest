// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.io.File;

import org.apache.tools.ant.Project;

import com.canoo.webtest.boundary.ResetScriptRunner;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link ScriptStep}.<p>
 *
 * @author Paul King, ASERT
 */
public class ScriptStepTest extends BaseStepTestCase
{
    private ScriptStep fStep;

    protected Step createStep() {
        return new ScriptStep();
    }
    
    protected void setUp() throws Exception
    {
    	super.setUp();
    	fStep = (ScriptStep) getStep();
    }

    public void testVerifyParameterUsage() {
        assertStepRejectsEmptyParam("language", getExecuteStepTestBlock());
        fStep.setLanguage("javascript");
        final Throwable t = assertErrorOnExecute(fStep, "", "");
        assertEquals("Either \"src\" attribute or nested script text must be given.", t.getMessage());
    }

    public void testInvalidUsageBadFile() throws Exception {
        fStep.setLanguage("groovy");
        final File notExisting = new File("WillNotBeFound.groovy")
        {
        	public boolean exists()
        	{
        		return false;
        	}
        };
        fStep.setSrc(notExisting);
        final Throwable t = assertErrorOnExecute(fStep, "", "");
        assertTrue(t.getMessage().indexOf("Could not find") != -1);
    }

    public void testInvalidUsageBadLanguage() throws Exception {
        fStep.setLanguage("cobol");
        fStep.setSrc(new File("selftests/tests/GMacroSteps.groovy"));
        final Throwable t = assertErrorOnExecute(fStep, "", "");
        assertTrue(t.getMessage().indexOf("unsupported language") != -1);
    }

    private static final class ResetScriptRunnerStub extends ResetScriptRunner {
        private final String fLanguage;

        private ResetScriptRunnerStub(String language) {
            fLanguage = language;
        }

        public String getLanguage() {
            return fLanguage;
        }
    }

    public void testInvalidUsageSwapLanguageWithKeep() throws Exception {
        checkLanguageSwap("groovy", "javascript");
        checkLanguageSwap("ruby", "java");
    }

    private void checkLanguageSwap(String lang1, String lang2) {
        getContext().setRunner(new ResetScriptRunnerStub(lang1));
        fStep.setProject(new Project());
        fStep.setLanguage(lang2);
        fStep.setKeep("true");
        final Throwable t = assertErrorOnExecute(fStep, "", "");
        assertEquals("You may not change 'language' to '" + lang2 + "' after previously using the 'keep' attribute (was: " + lang1 + ")", 
        		t.getMessage());
    }

}
