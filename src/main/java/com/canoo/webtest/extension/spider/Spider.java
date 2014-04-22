/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.StepUtil;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Denis N. Antonioli
 */
public class Spider {
	private static final Logger LOG = Logger.getLogger(Spider.class);
	public static final IVisitorStrategy ALWAYS_ACCEPT_VISITOR_STRATEGY = new AlwaysAcceptVisitorStrategy();
	public static final IReporter NO_OP_REPORTER = new NoOpReporter();
	public static final IValidator NO_OP_VALIDATOR = new NoOpValidator();

	private final Map fVisitedLinks = new HashMap();

	private IReporter fReporter;
	private IVisitorStrategy fVisitorStrategy;
	private IValidator fValidator;

	private String fFileName;
	private int fDepth;
	private boolean fFailOnError;
	private Context fContext;

	public void setFailOnError(boolean failOnError) {
		fFailOnError = failOnError;
	}

	public void setFileName(final String filename) {
		fFileName = filename;
	}

	public String getFileName() {
		return fFileName;
	}

	public void setDepth(final int depth) {
		fDepth = depth;
	}

	public void setReporter(final IReporter reporter) {
		fReporter = reporter;
	}

	public IReporter getReporter() {
		return fReporter;
	}

	public void setVisitorStrategy(final IVisitorStrategy visitorStrategy) {
		fVisitorStrategy = visitorStrategy;
	}

	public IVisitorStrategy getVisitorStrategy() {
		return fVisitorStrategy;
	}

	public void setValidator(final IValidator validator) {
		fValidator = validator;
	}

	public IValidator getValidator() {
		return fValidator;
	}

	Writer getWriter() throws IOException {
		final Writer writer;
		if (fFileName != null) {
			final File file = new File(fContext.getConfig().getWebTestResultDir(), fFileName);
			LOG.info("Writing in " + file);
			writer = new FileWriter(file);
		} else {
			LOG.info("Writing in standard output");
			writer = new OutputStreamWriter(System.out);
		}
		return writer;
	}

    void setContext(Context context) {
        fContext = context;
    }

    public void execute(final Context context) {
		validate();
		fVisitedLinks.clear();
		setContext(context);
        doExecute();
    }

    boolean doExecute() {
        Writer writer = null;
        boolean success = false;
        try {
            writer = getWriter();
            fReporter.setWriter(writer);
            fReporter.writeHeader();
            visit((HtmlPage) fContext.getCurrentResponse(), fDepth);
            fReporter.writeFooter();
            success = true;
        } catch (final Throwable e) {
            LOG.error("Problems during write: " + e.getMessage(), e);
        } finally {
            // doing this to Stdout will cause interuption
            IOUtils.closeQuietly(writer);
        }
        return success;
    }

    void validate() {
        if (fDepth < 0) {
            throw new IllegalArgumentException("depth must be >= 0");
        }
        if (fFileName == null) {
            LOG.info("No file name defined, will output to console");
        }
        if (fReporter == null) {
            LOG.info("No reporter defined, using noop reporter");
            fReporter = NO_OP_REPORTER;
        }
        if (fValidator == null) {
            LOG.info("No validator defined, using noop validator");
            fValidator = NO_OP_VALIDATOR;
        }
        if (fVisitorStrategy == null) {
            LOG.info("No visitor strategy set, using noop strategy");
            fVisitorStrategy = ALWAYS_ACCEPT_VISITOR_STRATEGY;
        }
    }

	void visit(final HtmlPage currentResponse, final int depth) throws IOException {
		LOG.debug("report depth " + depth);
		for (final Iterator iter = currentResponse.getAnchors().iterator(); iter.hasNext();) {
			final HtmlAnchor link = (HtmlAnchor) iter.next();

			final Properties linkInfo = fValidator.validate(fDepth - depth, currentResponse, link);
			fReporter.write(linkInfo);
			if (depth > 0 && needsReport(link)) {
                processLink(link, depth);
            }
		}
	}

    void processLink(final HtmlAnchor link, final int depth) throws IOException {
        try {
            follow(link);
            final Page page = fContext.getCurrentResponse();
            if (page instanceof HtmlPage)
            {
            	visit((HtmlPage) page, depth - 1);
            }
            else
            {
            	final WebResponse response = page.getWebResponse();
            	LOG.info("Don't going deeper in response for " + response.getWebRequest().getUrl()
            			+ " as it isn't an html page (content type: "
            			+ response.getContentType() + ", page" + page + ")");
            }
        }
        catch (final StepFailedException e) {
            LOG.error(e.getMessage(), e);
            if (fFailOnError) {
                throw e;
            }
        }
    }

    void follow(final HtmlAnchor link) {
        LOG.debug("Clicking on link with href: " + link.getHrefAttribute());
        try
        {
        	link.click();
        }
		catch (final Exception ex) {
			StepUtil.handleException(ex);
		} 
    }

	boolean needsReport(final HtmlAnchor link) {
		if (fVisitedLinks.containsKey(link.getHrefAttribute())) {
			LOG.info(link.getHrefAttribute() + " skipped: already visited");
			return false;
		}
		if (!fVisitorStrategy.accept(link)) {
			LOG.info(link.getHrefAttribute() + " skipped: rejected by visitor");
			return false;
		}
		fVisitedLinks.put(link.getHrefAttribute(), Boolean.TRUE);
		return true;
	}

	private static class AlwaysAcceptVisitorStrategy implements IVisitorStrategy {
		public boolean accept(HtmlAnchor link) {
			return true;
		}
	}

	private static class NoOpReporter implements IReporter {
		public void writeHeader() {
		}

		public void write(Properties linkInfo) {
		}

		public void setWriter(Writer writer) {
		}

		public void writeFooter() {
		}
	}

	private static class NoOpValidator implements IValidator {
		private static final Properties EMPTY_PROPERTIES = new Properties();
		public Properties validate(final int depth, final HtmlPage webResponse, final HtmlAnchor link) {
			return EMPTY_PROPERTIES;
		}
	}
}
