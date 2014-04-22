// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.w3c.dom.Document;

import com.canoo.webtest.ant.TestStepSequence;
import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.self.ErrorStepStub;
import com.canoo.webtest.self.StepStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.self.VerifyParameterErrorStepStub;
import com.canoo.webtest.steps.Step;

/**
 * Unit tests for {@link XmlReporter}.
 * @author Unknown
 * @author Marc Guillemot
 */
public class XmlReporterTest extends TestCase {
    private static final Logger LOG = Logger.getLogger(XmlReporterTest.class);

    private RootStepResult fResultInfo;
	private Project fProject;
	private Target fTarget;

    public void setUp() {
    	fProject = new Project();
    	fTarget = new Target();

        final List steps = new ArrayList();
        final Step step1 = new StepStub();
        step1.setDescription("h\u00fcg\u00f6");
        step1.setProject(fProject);
        step1.setOwningTarget(fTarget);
        steps.add(step1);

        final Step step2 = new ErrorStepStub();
        step2.setProject(fProject);
        step2.setOwningTarget(fTarget);
        steps.add(step2);

        fResultInfo = createResultInfo(steps);
    }

    private RootStepResult createResultInfo(final List liSteps)
    {
    	final StepExecutionListener[] tabListener = {null};
    	final WebtestTask webtest = new WebtestTask()
    	{
    		protected void writeTestReportIfNeeded(RootStepResult result) {};
    		protected void stopBuildIfNeeded(RootStepResult webTestResult, Configuration config) {};
    		public void execute() throws BuildException
    		{
    			super.execute();
    			tabListener[0] = getResultBuilderListener();
    		}
    	};
    	webtest.setProject(fProject);
    	webtest.setOwningTarget(fTarget);
    	webtest.setName("Dummy Test Spec");

    	final Configuration config = new Configuration();
        config.setProject(fProject);
        config.setOwningTarget(fTarget);
        config.setHost("myHost");
        webtest.addConfig(config);

        // make access to result info, executeSteps should not be made public
        final TestStepSequence steps = new TestStepSequence();
        steps.setTaskName("steps");
        steps.setProject(fProject);
        steps.setOwningTarget(fTarget);
        webtest.addSteps(steps);

        for (final Iterator iter= liSteps.iterator(); iter.hasNext();)
        {
        	final Step step = (Step) iter.next();
            step.setProject(fProject);
        	steps.addStep(step);
        }

        webtest.execute();

        return tabListener[0].getRootResult();
    }


    public void testCreateNewFile() throws Exception {
    	final Document doc = getGeneratedDocument(new File("nonExisting.xml"), fResultInfo);
        assertNotNull("doc", doc);
        assertMergedDocument(doc);
    }

    private Document getGeneratedDocument(final File resultFile, final RootStepResult resultInfo) throws Exception
    {
    	final Document docs[] = {null};
    	final XmlReporter reporter = new XmlReporter()
    	{
    		protected File getResultFile(final RootStepResult resultInfo)
    		{
    			return resultFile;
    		}
    		protected void writeXmlFile(final Document doc, final File outfile) throws Exception {
    			docs[0] = doc;
    		}
    	};
    	reporter.generateReport(resultInfo);

    	return docs[0];
    }
    
    public void testOpenExistingFile() throws Exception {
    	final Document doc = getGeneratedDocument(new File("resources/testExistingNodes.xml"), fResultInfo);
        assertNotNull("doc", doc);
        
        assertMergedDocument(doc);
        assertTrue(generateXml(doc).indexOf("endtime") > -1);
    }

    /**
     * endtime should appear in report even if test no successfull (this is a change compared to
     * previous WebTest reporting, but a not successfull webtest still have an end!)
     * @throws Exception if the test fails
     */
    public void testOpenExistingFileWithNotCompletedStep() throws Exception {
        // add another step which won't be executed because second step failed
    	fResultInfo.addChild(new StepResult("myNotCompletedTask"));
    	final Document doc = getGeneratedDocument(new File("resources/testExistingNodes.xml"), fResultInfo);
        assertNotNull("doc", doc);
        assertTrue(generateXml(doc).indexOf("endtime") > -1);
    }

    public void testOpenExistingFileWithStepErrorInParameterValidation() throws Exception {
        final Step step1 = new VerifyParameterErrorStepStub();
        fResultInfo = createResultInfo(Collections.singletonList(step1));

    	final Document doc = getGeneratedDocument(new File("resources/testExistingNodes.xml"), fResultInfo);
        assertTrue(generateXml(doc).indexOf("endtime") > -1);
    }

    public void testOpenExistingFileWrongRootNode() throws Exception {
        final XmlReporter xmlReporter = new XmlReporter();
        ThrowAssert.assertThrows("bad root", ReportCreationException.class, new TestBlock() {
            public void call() throws Exception {
            	xmlReporter.readXmlFile(new File("resources/testWrongRootNode.xml"));
            }
        });
    }

    public void testReportException() {
        String message = "my message";
        Exception e = new RuntimeException(message);
        ReportCreationException reportCreationException = new ReportCreationException(e);
        assertEquals(message, reportCreationException.getMessage());
        assertSame(e, reportCreationException.getInitialThrowable());
    }

    private void assertMergedDocument(Document doc) throws IOException {
        assertTrue(generateXml(doc).indexOf("Dummy Test Spec") > -1);
    }

    private String generateXml(final Document doc) throws IOException {
        final StringWriter writer = new StringWriter();
        new XmlReporter().writeXmlFile(doc, writer);
        final String mergedDoc = writer.toString();
        LOG.debug(mergedDoc);
        return mergedDoc;
    }
}
