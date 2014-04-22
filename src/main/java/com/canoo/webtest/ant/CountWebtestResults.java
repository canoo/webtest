package com.canoo.webtest.ant;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Ant task that reads the WebTest results from a WebTest results file.
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 *   name="countWebtestResults"
 *   description="This <key>ANT</key> task counts the WebTest results in the provided result file
 *   and can produce a build failure when some webtests have failed"
 */
public class CountWebtestResults extends Task
{
	private boolean failOnError = true;
	private String failureProperty;
	private String successProperty;
	private int nbSuccessful = 0;
	private int nbFailed = 0;
	private File fResultFile;
	
	/**
	 * Minimal SAX event handler counting the results
	 */
	private class WebTestResultCounter extends DefaultHandler
	{
		public void startElement(final String _uri, final String _localName, final String _name,
				final Attributes _attributes) throws SAXException
		{
			if ("summary".equals(_name))
			{
				final boolean success = "yes".equals(_attributes.getValue("successful"));
				if (success)
					++nbSuccessful;
				else
					++nbFailed;
			}
		}
	}

	public void execute() throws BuildException
	{
		readResults();
		
		setProperties();

		final int nbTests = nbFailed + nbSuccessful;
		if (failOnError && nbFailed != 0)
		{
			throw new BuildException(nbFailed + " of " + nbTests 
					+ " webtests have failed (" + nbSuccessful + " successful)!");
		}

		// log at warn level as it is displayed by default
		log(nbTests + " webtests run (successful: " + nbSuccessful 
				+ ", failed: " + nbFailed + ")", Project.MSG_WARN);
	}

	/**
	 * Sets the Ant properties with the number of failed / successful results
	 */
	void setProperties()
	{
		if (!StringUtils.isEmpty(failureProperty) && getNbFailed() != 0)
		{
			getProject().setNewProperty(failureProperty, String.valueOf(getNbFailed()));
		}
		if (!StringUtils.isEmpty(successProperty) && getNbSuccessful() != 0)
		{
			getProject().setNewProperty(successProperty, String.valueOf(getNbSuccessful()));
		}
	}

	/**
	 * Reads the result from the xml file
	 */
	void readResults()
	{
		if (fResultFile == null)
			throw new BuildException("Mandatory attribute >resultFile< not set!");
		if (!fResultFile.exists())
			throw new BuildException("Can't find result file >" + fResultFile.getAbsolutePath() + "<");

		final WebTestResultCounter counter = new WebTestResultCounter(); 
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser saxParser;
		try
		{
			saxParser = factory.newSAXParser();
		}
		catch (final Exception e)
		{
			throw new BuildException("Failed to create SAX parser", e);
		}

        try
		{
			saxParser.parse(fResultFile, counter);
		}
		catch (final Exception e)
		{
			throw new BuildException("Failed to parse result file >" + fResultFile + "<", e);
		}
	}

	/**
	 * @return the failOnError.
	 */
	public boolean isFailOnError()
	{
		return failOnError;
	}

	/**
	 * @param _failOnError the failOnError to set
     * @webtest.parameter required="no"
     * default="true"
     * description="Stops the build process if a webtest failed."
	 */
	public void setFailOnError(boolean _failOnError)
	{
		failOnError = _failOnError;
	}

	/**
	 */
	public String getFailureProperty()
	{
		return failureProperty;
	}

	/**
     * @webtest.parameter required="no"
     * description="The name of the Ant property that will be set with the numbers of failures.
     * The property is not set if no failure was found."
	 */
	public void setFailureProperty(final String _failuresProperty)
	{
		failureProperty = _failuresProperty;
	}

	/**
	 * @return the successProperty.
	 */
	public String getSuccessProperty()
	{
		return successProperty;
	}

	/**
	 * @param _successProperty the successProperty to set
     * @webtest.parameter required="no"
     * description="The name of the Ant property that will be set with the numbers of successful webtests.
     * The property is not set if no successful test was found."
	 */
	public void setSuccessProperty(final String _successProperty)
	{
		successProperty = _successProperty;
	}

	/**
	 * @return the fResultFile.
	 */
	public File getResultFile()
	{
		return fResultFile;
	}

	/**
	 * @param _resultFile the fResultFile to set
     * @webtest.parameter required="yes"
     * description="the file containing the WebTest results"
	 */
	public void setResultFile(final File _resultFile)
	{
		fResultFile = _resultFile;
	}

	/**
	 * @return the nbSuccessful.
	 */
	protected int getNbSuccessful()
	{
		return nbSuccessful;
	}

	/**
	 * @return the nbFailed.
	 */
	protected int getNbFailed()
	{
		return nbFailed;
	}

}
