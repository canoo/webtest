// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link ExcelVerifyCellSum}.<p>
 *
 * @author Rob Nielsen
 */
public class ExcelVerifyCellSumTest extends BaseExcelStepTestCase {
    protected Step createStep() {
        return new ExcelVerifyCellSum();
    }

    public void testSuccess() throws Exception {
        final ExcelVerifyCellSum step = (ExcelVerifyCellSum) getStep();
        step.setCell("C8");
        step.setRange("C4:C7");
        executeStep(step);
    }

    public void testFailVerifyCellSumWithNumber() throws Exception {
        final ExcelVerifyCellSum step = (ExcelVerifyCellSum) getStep();
        step.setCell("C8");
        step.setRange("C5:C7");
        assertFailOnExecute(step, "verify with number",
        		"Unexpected sum of cells from range C5:C7 in cell C8. Expected value \"15.0\" but got \"16.0\"");
    }

    public void testFailVerifyCellSumWithFormula() throws Exception {
        final ExcelVerifyCellSum step = (ExcelVerifyCellSum) getStep();
        step.setCell("E8");
        step.setRange("F5:F9");
        assertFailOnExecute(step, "verify with formula", 
        		"Unexpected formula in cell E8. Expected value \"SUM(F5:F9)\" but got \"SUM(E4:E7)\"");
    }

    public void testFailVerifyCellSumWithTextInTarget() throws Exception {
        final ExcelVerifyCellSum step = (ExcelVerifyCellSum) getStep();
        step.setCell("B8");
        step.setRange("C4:C7");
        assertFailOnExecute(step, "verify text target", "Cell B8 does not contain a formula or a numeric value.");
    }

    public void testFailVerifyCellSumWithTextInRange() throws Exception {
        final ExcelVerifyCellSum step = (ExcelVerifyCellSum) getStep();
        step.setCell("C8");
        step.setRange("B4:B8");
        assertFailOnExecute(step, "verify text range", "Cell B4 does not contain a numeric value.");
    }

    public void testInvalidRange() throws Exception {
        final ExcelVerifyCellSum step = (ExcelVerifyCellSum) getStep();
        step.setCell("C8");
        step.setRange("blah");
        assertErrorOnExecute(step, "verify text range", "Cannot parse \"blah\" as a spreadsheet range. eg \"A10:A20\"");
    }
}
