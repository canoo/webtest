// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * Extracts all text content from within the current PDF document.
 *
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step category="PDF"
 * name="pdfToTextFilter"
 * description="Extracts all text content from within the current <key>pdf</key> document."
 */
public class PdfToTextFilter extends AbstractPdfFilter
{
	private static final Logger LOG = Logger.getLogger(PdfToTextFilter.class);
    private String fMode = PDFPage.MODE_NORMAL;
    private String fFragSep = " ";
    private String fLineSep = "\n";
    private String fPageSep;

    public String getPageSep() {
        return fPageSep;
    }

    /**
     * @param pageSep
     * @webtest.parameter
     *   required="no"
     *   default="[+++ NEW PAGE +++]\n"
     *   description="The page separator string to use, e.g. \"\n\" or \"------\"."
     */
    public void setPageSep(final String pageSep) {
        fPageSep = pageSep;
    }

    public String getLineSep() {
        return fLineSep;
    }

    /**
     * @param lineSep
     * @webtest.parameter
     *   required="no"
     *   default="platform line separator"
     *   description="The line separator string to use, e.g. \" \" or \"\n\"."
     */
    public void setLineSep(final String lineSep) {
        fLineSep = lineSep;
    }

    public String getFragSep() {
        return fFragSep;
    }

    /**
     * @param fragSep
     * @webtest.parameter
     *   required="no"
     *   default="a single space"
     *   description="The fragment separator string to use, e.g. \"\" or \" \" or \",\" or \" | \". Only used if <em>mode</em> is \"<em>groupByLines</em>\"."
     */
    public void setFragSep(final String fragSep) {
        fFragSep = fragSep;
    }

    public String getMode() {
        return fMode;
    }

    /**
     * @param mode
     * @webtest.parameter
     *   required="no"
     *   default="normal"
     *   description="Deprecated: doesn't do anything anymore."
     */
    public void setMode(final String mode) {
    	if (PDFPage.MODE_LINES.equals(mode)) {
    		LOG.warn("mode='" + PDFPage.MODE_LINES + "' is not supported anymore. Using mode='normal'.");
    	}
        fMode = mode;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        paramCheck(!PDFPage.MODE_NORMAL.equals(getMode()) && !PDFPage.MODE_LINES.equals(getMode()),
                "Invalid mode");
    }

    protected void doFilter(final PDFPage pdfPage)
    {
//        String defaultPageSep = (PDFPage.MODE_NORMAL.equals(getMode()) ? "\n" : "") + "[+++ NEW PAGE +++]\n";
      String defaultPageSep = "\n[+++ NEW PAGE +++]\n";
        if (getPageSep() == null) {
            fPageSep = defaultPageSep;
        } 
        else {
            fPageSep = fPageSep.replaceAll("\\\\n", "\n");
            fPageSep = fPageSep.replaceAll("\\\\r", "\r");
        }
        final String text = pdfPage.getText(getFragSep(), getLineSep(), getPageSep(), PDFPage.MODE_NORMAL);
        LOG.debug("Filter result: >" + text + "<");
        ContextHelper.defineAsCurrentResponse(getContext(), text, "text/plain", getClass());
    }
}
