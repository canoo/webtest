/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import org.apache.log4j.Logger;

import com.canoo.webtest.steps.AbstractStepContainer;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @author Denis N. Antonioli
 * @webtest.step category="Extension"
 * name="reportSite"
 * description="This step is used to test a complete site."
 */
public class ReportSiteStep extends AbstractStepContainer {
    private static final Logger LOG = Logger.getLogger(ReportSiteStep.class);
    public static final String[] HEADERS = {
        ExecuteStepValidator.KEY_DEPTH,
        ExecuteStepValidator.KEY_VERIFY
    };
    private String fFile;
    private String fDepth;
    private int fDepthCount;

    /**
     * @webtest.parameter required="no"
     * description="the name of the file that should contain the report. 
     * If not specified, report will be generated to standard output"
     */
    public void setFile(final String filename) {
        fFile = filename;
    }

    public String getFile() {
        return fFile;
    }

    protected void verifyParameters()
    {
    	super.verifyParameters();
        optionalIntegerParamCheck(getDepth(), "depth", true);
        fDepthCount = ConversionUtil.convertToInt(getDepth(), 0);
    }

    /**
     * @webtest.parameter required="no"
	 *   default="0"
     * description="the recursion depth"
     */
    public void setDepth(final String depth) {
        fDepth = depth;
    }

    public String getDepth() {
        return fDepth;
    }

    public void doExecute() throws CloneNotSupportedException {
    	LOG.debug("Creating spider");
        final Spider spider = new Spider();
        spider.setDepth(fDepthCount);
        spider.setFileName(getFile());

        spider.setReporter(getReporter());
        spider.setVisitorStrategy(getVisitorStrategy());
        spider.setValidator(getValidator());

        LOG.debug("Executing spider");
        spider.execute(getContext());
    }
    
    protected IReporter getReporter()
    {
    	return new SeparatedValueReporter(HEADERS);
    }

    protected IVisitorStrategy getVisitorStrategy()
    {
    	return new PatternVisitorStrategy("/.*/");
    }
    
    protected IValidator getValidator()
    {
    	return new ExecuteStepValidator(getContext(), this);
    }
}
