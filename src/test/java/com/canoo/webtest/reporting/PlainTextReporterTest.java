// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import org.apache.tools.ant.Project;

import junit.framework.TestCase;

import com.canoo.webtest.ant.TestStepSequence;
import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.self.StepStub;

/**
 * Unit tests for {@link PlainTextReporter}.
 * @author Unknown
 * @author Marc Guillemot
 */
public class PlainTextReporterTest extends TestCase
{
    private RootStepResult fResultInfo;
    private StringWriter fStringWriter;
    private PrintWriter fPrintWriter;
    private PlainTextReporter fReporter;

    protected void setUp() throws Exception {
        super.setUp();
        final Project project = new Project();
        final WebtestTask webtest = new WebtestTask();
        webtest.setProject(project);
        webtest.addSteps(new TestStepSequence());
        final Context context = new Context(webtest);
        WebtestTask.setThreadContext(context);
        
        fResultInfo = new RootStepResult(webtest.getStepSequence());
        fStringWriter = new StringWriter();
        fPrintWriter = new PrintWriter(fStringWriter);
        fReporter = new PlainTextReporter();
    }

    public void testPrintNoSteps() {
        fReporter.print(fPrintWriter, fResultInfo);
        assertTrue("Summary header",
                fStringWriter.toString().startsWith("Test step summary for specification:"));
    }

    public void testPrintCompletedStep() {

    	final StepResult result = new StepResult("myTask");
    	result.taskFinished(new StepStub(), null, Collections.EMPTY_LIST);
    	fResultInfo.addChild(result);

        fReporter.print(fPrintWriter, fResultInfo);
        final String str = fStringWriter.toString();
        assertTrue(str.startsWith("Test"));
        assertTrue(str.indexOf("myTask") > 0);
        assertTrue(str.indexOf("Duration") > 0);
    }

    public void testPrintNotCompleted() {

    	final StepResult result = new StepResult("myTask");
    	fResultInfo.addChild(result);

        fReporter.print(fPrintWriter, fResultInfo);
        final String str = fStringWriter.toString();
        assertTrue(str.startsWith("Test"));
        assertTrue(str.indexOf("myTask") > 0);
        assertTrue(str.indexOf("not completed") > 0);
    }

    public void testPrintNotSuccessful() {

    	final StepResult result = new StepResult("myTask");
    	fResultInfo.addChild(result);
    	fResultInfo.setLastFailingTaskResult(result, new IllegalArgumentException("dummy error"));

        fReporter.print(fPrintWriter, fResultInfo);
        final String str = fStringWriter.toString();
        assertTrue(str.startsWith("Test"));
        assertTrue(str.indexOf("PlainTextReporter") > 0);
        assertTrue(str.indexOf("Test failed.") > 0);
        assertTrue(str.indexOf("IllegalArgumentException") > 0);
        assertTrue(str.indexOf("dummy error") > 0);
    }
}
