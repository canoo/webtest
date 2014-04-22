package com.canoo.webtest.reporting;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.NoBannerLogger;
import org.apache.tools.ant.Project;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the generation of single HTML reports from single WebTestReport.xml.
 * The idea is that each single HTML report can be generated in the background as soon as the WebTestReport.xml has been written.
 * This should make a whole build faster because reports don't need anymore to be generated at the end.
 * As WebTest looses most of its time waiting for answers of HTTP servers
 * and therefore generating reports in the background shouldn't have any noticeable performance impact.
 */
class HTMLReportGenerator {
	private static final Logger LOG = Logger.getLogger(HTMLReportGenerator);
	private static final HTMLReportGenerator INSTANCE = new HTMLReportGenerator();
	
	private final ExecutorService service = Executors.newFixedThreadPool(1, new HTMLReportGeneratorThreadFactory());

	/**
	 * Registers a background task to create the HTML report from the provided xml report file. 
	 */
	public static void registerReportToFormat(final Project project, final File reportFile)
	{
		INSTANCE.addReportToGenerate(project, reportFile);
	}
	
	/**
	 * Terminates the remaining formatting, if any.
	 */
	public static void shutdown()
	{
		LOG.debug("Waiting for executor to shutdown: ${INSTANCE.hashCode()}")
		INSTANCE.service.shutdown()
		INSTANCE.service.awaitTermination(10 * 60, TimeUnit.SECONDS) // caution: TimeUnit.MINUTES seems to be introduce in Java 6!!!
		LOG.debug("Finished waiting for executor to shutdown: ${INSTANCE.hashCode()}")
	}

	private void addReportToGenerate(final Project project, final File reportFile) {
		final Callable task = new FormatTask(reportGenerator: this, project: project, reportFile: reportFile);
		LOG.debug("Adding task for $reportFile: ${this}")
		service.submit(task);
	}

	
	private void format(final Project project, final File reportFile) {
		final AntBuilder antBuilder = new AntBuilder(project)

        LOG.debug("Formatting $reportFile: ${this.hashCode()}")
		
		try {
			antBuilder."wt.htmlReports.single"(xmlFile: reportFile, folder: reportFile.getParentFile())
		}
		catch (Throwable e) {
			LOG.error("Error formatting $reportFile done", e)
		}
		LOG.debug("Formatting $reportFile done: ${this.hashCode()}")
	}
	
	public String toString() {
		"" + this.hashCode()
	}
}

class FormatTask implements Callable
{
	Project project
	File reportFile
	HTMLReportGenerator reportGenerator
	
	public Object call() throws Exception {
		reportGenerator.format(project, reportFile);
	}
}

/**
 * Thread factory creating threads with a priority inferior to the one of the current thread.
 * The idea is that the generation of HTML reports should occur while WebTest has nothing to do,
 * waiting for answers from an HTTP server.
 */
class HTMLReportGeneratorThreadFactory implements ThreadFactory
{
	public Thread newThread(final Runnable r) {
		final Thread t = Executors.defaultThreadFactory().newThread(r)
		t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()))
		t.setDaemon(false)

		return t;
	}	
}