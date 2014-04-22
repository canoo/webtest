package com.canoo.webtest.extension.groovy;

import groovy.lang.GroovyRuntimeException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import com.canoo.webtest.self.BufferingAppender;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.request.InvokePage;
import com.canoo.webtest.steps.verify.VerifyXPath;

/**
 * Tests for {@link GroovyStep}.
 *
 * @author Marc Guillemot
 * @author Denis N. Antonioli
 */
public class GroovyStepTest extends BaseStepTestCase {

    protected Step createStep() {
        return new GroovyStep();
    }

    public void testInvocationException() {
        final GroovyStep step = (GroovyStep) getStep();

        step.addText("something invalid");

        final Throwable t = assertErrorOnExecute(step, "should not compile", "Error invoking groovy");
        final Throwable cause = t.getCause();
        assertInstanceOf(GroovyRuntimeException.class, cause);
    }

    public void testCompilationException() {
        final GroovyStep step = (GroovyStep) getStep();

        step.addText("(");

        final Throwable t = assertErrorOnExecute(step, "should not compile", "Cannot compile groovy code: (");
        final Throwable cause = t.getCause();
        assertInstanceOf(CompilationFailedException.class, cause);
    }

    /**
     * Test that print(ln) done in Groovy is correctly "forwarded" to the step's log
     */
    public void testPrint() {
        final GroovyStep step = (GroovyStep) getStep();

        step.addText("println 1234; print 'a'; println 'b'; print 'c'");


        final Logger logger = Logger.getRootLogger();
        final BufferingAppender spoofAppender = new BufferingAppender();
        final Level originalLogLevel = logger.getLevel();
        logger.addAppender(spoofAppender);
        logger.setLevel(Level.DEBUG);

        step.execute();

        assertTrue(spoofAppender.containsMessage("1234"));
        assertTrue(spoofAppender.containsMessage("ab"));
        assertTrue(spoofAppender.containsMessage("c"));

        Logger.getRootLogger().removeAppender(spoofAppender);
        Logger.getRootLogger().setLevel(originalLogLevel);
    }
    
    public void testMacroStepBuilder()
    {
    	final String script = 
    		"def ant = new com.canoo.webtest.extension.groovy.MacroStepBuilder(step)\n"
    		+ "ant.invoke(url: 'about:blank')\n"
    		+ "ant.verifyXPath(xpath: 'true()', description: 'bla')";

        final GroovyStep step = (GroovyStep) getStep();
        step.getProject().addTaskDefinition("invoke", InvokePage.class);
        step.getProject().addTaskDefinition("verifyXPath", VerifyXPath.class);
        step.addText(script);
        step.execute();
    }
}
