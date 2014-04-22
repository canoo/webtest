// Copyright (c) 2004-2006. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.WebClientContext;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.FileUtil;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Compares the current response with a reference file.<p>
 * <p/>
 * @author Paul King, David Louvton
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="verifyContent"
 * description="Compares the current response with an expected file. 
 * Supports content filters such as <stepref name='lineSeparatorFilter'/> 
 * and <stepref name='replaceFilter'/> and the nested <stepref name='table' category='Core'/> step."
 */
public class VerifyContent extends AbstractProcessFiltersStep
{
    private static final Logger LOG = Logger.getLogger(VerifyContent.class);
    private File fReferenceFile;
    private boolean fReadFiltered = true;
    private static final String DIFF_MODE_AUTO = "auto";
    private static final String DIFF_MODE_BIN = "bin";
    private static final String DIFF_MODE_TEXT = "text";
    private static final String DIFF_MODE_REGEXPERLINE = "regexperline";
    private String fMode = DIFF_MODE_AUTO;

    public File getReferenceFile() {
        return fReferenceFile;
    }

    /**
     * @param mode the diff mode to use
     * @webtest.parameter required="no"
     * description="The diff mod to use for comparison. 
     * Possible values are 
     * \"auto\" (the default, uses \"text\" or \"bin\" depending on the type of the content to compare),
     * \"text\" (performs a unified diff like diff),
     * \"bin\" (compares byte per byte and displays differences in hex format), 
     * and \"regexPerLine\" (each line of the reference file is a regex that should match the actual content)."
     */
    public void setMode(final String mode) {
        fMode = mode;
    }

    public String getMode() {
        return fMode;
    }

    /**
     * @param file
     * @webtest.parameter required="yes"
     * description="The file to compare to"
     */
    public void setReferenceFile(final File file) {
        fReferenceFile = file;
    }

    public boolean isReadFiltered() {
        return fReadFiltered;
    }

    /**
     * @param readFiltered
     * @webtest.parameter
     *   required="no"
     *   default="true"
     *   description="Indicates that when reading <em>referenceFile</em> that the content should be filtered."
     */
    public void setReadFiltered(final boolean readFiltered) {
        fReadFiltered = readFiltered;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getReferenceFile(), "referenceFile");
        if (!getReferenceFile().exists()) {
            throw new StepFailedException("Reference file doesn't exist: " + getReferenceFile().getAbsolutePath());
        }
        
        final String mode = getMode().toLowerCase();
        final String[] allowedModes = {DIFF_MODE_AUTO, DIFF_MODE_BIN, DIFF_MODE_TEXT, DIFF_MODE_REGEXPERLINE};
        if (!ArrayUtils.contains(allowedModes, mode))
        {
            throw new StepExecutionException("Unallowed diff mode >" + getMode() + "<. "
            		+ "Allowed modes are "
            		+ ArrayUtils.toString(allowedModes)
            		+ ".", this);
        }
    }

    public void doExecute() throws Exception {
        final WebClientContext.StoredResponses origResponses = getContext().getResponses();
        final String result = diffContentWithExpected();
        getContext().restoreResponses(origResponses);

        if (!StringUtils.isEmpty(result)) {
        	final StepFailedException e = new StepFailedException("Current response and the reference differ.", this);
        	e.addDetail("diff", result);
            throw e;
        }
    }

    private String diffContentWithExpected() throws IOException {
        final List steps = getSteps();
        final Context context = getContext();
        applyTableFilterIfNeeded(context);
        applyExtractionIfNeeded(context);

        for (final Iterator iter = steps.iterator(); iter.hasNext();) {
            final Step step = (Step) iter.next();
            executeContainedStep(step);
		}
        final WebClientContext.StoredResponses srcResponses = context.getResponses();
        final Page actualPage = context.getCurrentResponse();
        final WebResponse actualResponse = actualPage.getWebResponse();

        LOG.debug("Processig reference file: " + getReferenceFile());
    	WebClientContext.StoredResponses referenceResponses = preProcessFiles(getReferenceFile(), context);
    	if (isReadFiltered()) {
            LOG.debug("Applying filter on reference file too");
            context.restoreResponses(referenceResponses);
            for (final Iterator iter = steps.iterator(); iter.hasNext();) {
                final Step step = (Step) iter.next();
                step.execute();
    		}
            referenceResponses = context.getResponses();
        	context.restoreResponses(srcResponses);
    	}

        LOG.debug("Source: " + actualResponse.getContentType() + " (" + actualResponse.getWebRequest().getUrl() + ")");
        context.restoreResponses(referenceResponses);
        final Page referencePage = context.getCurrentResponse();
        final WebResponse referenceResponse = referencePage.getWebResponse();
        LOG.debug("Reference: " + referenceResponse.getContentType() + " (" + referenceResponse.getWebRequest().getUrl() + ")");

        return produceDiffMessage(actualPage, referencePage);
    }

    private WebClientContext.StoredResponses preProcessFiles(final File destfile, final Context context) {
        LOG.debug("Loading reference file: " + destfile);
        final byte[] destBytes = FileUtil.readFileToByteArray(destfile, this);
        ContextHelper.defineAsCurrentResponse(context, destBytes,
                context.getCurrentResponse().getWebResponse().getContentType(), "http://" + getClass().getName());
        return context.getResponses();
    }

    private String produceDiffMessage(final Page actualPage, final Page referencePage) throws IOException {
		String labelReference = getReferenceFile().getAbsolutePath();
		String labelActual = "current response";
		if (!getSteps().isEmpty())
		{
			labelActual += " (filtered)";
			if (isReadFiltered())
				labelReference += " (filtered)";
		}
        
		final VerifyContentDiff diff = getDiff(actualPage, referencePage);
		LOG.debug("Mode >" + getMode() + "<, using: " + diff);
		return diff.compare(referencePage.getWebResponse(), actualPage.getWebResponse(), labelReference, labelActual);
    }

    /**
     * Gets the diff command to use for comparison. This may have been specified
     * otherwise a default diff is taken according to the content type of the pages
     * @param actualPage
     * @param referencePage
     */
    private VerifyContentDiff getDiff(final Page actualPage, final Page referencePage) {
    	String mode = getMode().toLowerCase();
    	if (DIFF_MODE_AUTO.equals(mode))
    	{
            if (isTextResponse(actualPage) && isTextResponse(referencePage))
            	mode = DIFF_MODE_TEXT;
            else
            	mode = DIFF_MODE_BIN;
    	}
    	if (DIFF_MODE_TEXT.equals(mode))
    		return new VerifyContentTextDiff();
    	else if (DIFF_MODE_REGEXPERLINE.equals(mode))
    		return new VerifyContentRegexPerLineDiff();
    	else
    		return new VerifyContentBinDiff();
	}

	/**
	 */
    static boolean isTextResponse(final Page _page) 
	{
    	// don't rely on mime type as not only text/* are "text" answers (for instance application/xhtml+xml)
    	return _page instanceof HtmlPage || _page instanceof TextPage || _page instanceof XmlPage || _page instanceof JavaScriptPage;
	}
}
