// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import java.io.InputStream;
import java.util.WeakHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.canoo.webtest.engine.MimeMap;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Base class for Excel steps.
 *
 * @author Rob Nielsen
 * @author Paul King
 */
public abstract class AbstractExcelStep extends Step
{
    private static final Logger LOG = Logger.getLogger(AbstractExcelStep.class);

    private static final Map sMapWorkbooks = new WeakHashMap(); // needed until we plug a "ExcelPage" in htmlunit
    protected static final String KEY_CURRENT_SHEET = AbstractExcelStep.class.getName() + "#CurrentSheet";

    protected AbstractExcelStep() {
    }

    /**
     * @throws StepExecutionException
     *          if pdf analyzer cannot be initialized correctly
     */
    protected HSSFWorkbook getExcelWorkbook() {
    	final Page currentPage = getContext().getCurrentResponse();
    	HSSFWorkbook workbook = (HSSFWorkbook) sMapWorkbooks.get(currentPage);
    	if (workbook == null) {
    		workbook = createWorkbook(currentPage);
    		sMapWorkbooks.put(currentPage, workbook); // weak map, analyser garbage collected together with the page
    	}
    	return workbook;
    }

    /**
     * Creates an HSSFWorkbook for the page. This method should not be used directly, 
     * use {@link #getExcelWorkbook()} instead.
     * @param currentPage the page containing the Excel document
     * @return the analyzer
     */
    private HSSFWorkbook createWorkbook(final Page currentPage) {
        InputStream is = null;
        try {
            getContext().put(KEY_CURRENT_SHEET, null);
            is = currentPage.getWebResponse().getContentAsStream();

            final POIFSFileSystem excelFile = new RetryWithCapsPOIFSFileSystem(is);
            return new HSSFWorkbook(excelFile);
        } 
        catch (final Exception e) {
            final String message = "Could not open Excel file.";
            LOG.debug(message, e);
            throw new StepExecutionException(message, this, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullResponseCheck();
        final Page currentResponse = getContext().getCurrentResponse();

        String contentType = currentResponse.getWebResponse().getContentType();
        if (!MimeMap.EXCEL_MIME_TYPE.equals(contentType)) {
            throw new StepExecutionException("File does not have correct content type (not a '.xls' file?): "
                    + currentResponse.getWebResponse().getContentType(), this);
        }
    }

    protected boolean verifyStrings(final String expectedValue, final String actualValue) {
        boolean regex=false;
        String newExpectedValue = expectedValue;
        if (expectedValue != null && expectedValue.length() >= 2 && expectedValue.charAt(0)=='/' && expectedValue.charAt(expectedValue.length() - 1)=='/') {
            regex = true;
            newExpectedValue = expectedValue.substring(1, expectedValue.length() - 1);
        }
        return getVerifier(regex).verifyStrings(newExpectedValue, actualValue);
    }
}
