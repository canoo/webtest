// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import java.io.IOException;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link AbstractExcelStep}.<p>
 *
 * @author Rob Nielsen
 */
public class AbstractExcelStepTest extends BaseExcelStepTestCase {

    protected Step createStep() {
        return new AbstractExcelStepStub();
    }

    public void testVerifyParametersWithoutPreviousPage() {
        final Step step = getStep();
        assertStepRejectsNullResponse(step);
    }

    public void testNonExcel() throws IOException {
        final Step step = getStep();
        WebtestTask.setThreadContext(new ExcelContextStub("text/html"));
        assertErrorOnExecute(step, "Non .xls file", "File does not have correct content type (not a '.xls' file?): text/html");
    }

    public void testNoFile() throws IOException {
        final Step step = getStep();
        WebtestTask.setThreadContext(new ExcelContextStub("application/vnd.ms-excel"));
        assertErrorOnExecute(step, "Non .xls file", "Could not open Excel file.");
    }

    public void testToString() {
    }

    private class AbstractExcelStepStub extends AbstractExcelStep {
        public void doExecute() throws Exception {
            getExcelWorkbook();
        }
    }
}
