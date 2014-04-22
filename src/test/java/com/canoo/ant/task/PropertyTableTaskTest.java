package com.canoo.ant.task;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.NoBannerLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.ImportTask;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link PropertyTableTask}.
 * @author Marc Guillemot
 */
public class PropertyTableTaskTest
{
	@Test
	public void testFailASAP() throws Exception {
		
		final Project p = prepareProject("com/canoo/ant/table/dataDriven.xml");
		try
		{
			p.setProperty("failASAP", "true");
			p.executeTarget("testFailASAP");
			Assert.fail();
		}
		catch (final BuildException e)
		{
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("Wrong day!"));
		}
	}

	@Test
	public void testFailAtTheEnd() throws Exception {
		
		final Project p = prepareProject("com/canoo/ant/table/dataDriven.xml");
		try
		{
			p.executeTarget("testFailASAP");
			Assert.fail();
		}
		catch (final BuildException e)
		{
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("1/7 executions of nested tasks failed"));
		}
	}

	private Project prepareProject(final String resourceName) throws URISyntaxException, IOException {
		final URL url = getClass().getClassLoader().getResource(resourceName);
		Assert.assertNotNull("Resource not found: " + resourceName, url);
		final File f = new File(url.toURI());
		final Project p = createProject();
		final Target target = new Target();
		target.setProject(p);
		target.setName("");
		
		p.addTaskDefinition("dataDriven", PropertyTableTask.class);
		
		ImportTask importTask = new ImportTask();
		importTask.setFile(f.getAbsolutePath());
		importTask.setProject(p);
		importTask.setOwningTarget(target);

		final File tmpFile = File.createTempFile("webtest-unittest", ".xml");
		final Location location = new Location(tmpFile.getAbsolutePath());
		importTask.setLocation(location);
		importTask.execute();
		
		tmpFile.delete();
		return p;
	}


    /**
     * @return Factory method to create new Project instances
     */
    protected static Project createProject() {
        final Project project = new Project();

        final ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference(ProjectHelper.PROJECTHELPER_REFERENCE, helper);
        helper.getImportStack().addElement("AntBuilder"); // import checks that stack is not empty 

        final BuildLogger logger = new NoBannerLogger();

        logger.setMessageOutputLevel(org.apache.tools.ant.Project.MSG_INFO);
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);

        project.addBuildListener(logger);

        project.init();
        project.getBaseDir();
        return project;
    }
}
