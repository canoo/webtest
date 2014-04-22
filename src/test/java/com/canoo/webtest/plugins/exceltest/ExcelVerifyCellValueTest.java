// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.steps.Step;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * Test class for {@link ExcelVerifyCellValue}.<p>
 *
 * @author Rob Nielsen
 */
public class ExcelVerifyCellValueTest extends BaseExcelStepTestCase {
    protected Step createStep() {
        return new ExcelVerifyCellValue();
    }

    public void testWrong() {
        final ExcelVerifyCellValue step = (ExcelVerifyCellValue) getStep();
        step.setText("xxx");
        step.setCell("B10");
        assertFailOnExecute(step, "wrong", 
        		"Wrong cell value found for cell B10. Expected value \"xxx\" but got \"This is a very long string of text that can be searched for.\"");
    }

    public void testError() throws Exception {
        final ExcelVerifyCellValue step = (ExcelVerifyCellValue) getStep();
        step.setCell("B2");
        step.setText("Error Code 42");
        step.getExcelCell().setCellErrorValue((byte)42);
        step.getExcelCell().setCellType(HSSFCell.CELL_TYPE_ERROR);
        executeStep(step);
    }

}
