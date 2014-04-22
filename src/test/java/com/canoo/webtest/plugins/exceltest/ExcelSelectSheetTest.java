// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link ExcelSelectSheet}.<p>
 *
 * @author Rob Nielsen
 */
public class ExcelSelectSheetTest extends BaseExcelStepTestCase {
    protected Step createStep() {
        return new ExcelSelectSheet();
    }

    public void testSelectSheetByIndex() throws Exception {
        final ExcelSelectSheet step = (ExcelSelectSheet) getStep();
        step.setSheetIndex("2");
        executeStep(step);
        assertSame(step.getExcelWorkbook().getSheetAt(2), step.getExcelSheet());
    }

    public void testSelectSheetByName() throws Exception {
        final ExcelSelectSheet step = (ExcelSelectSheet) getStep();
        step.setSheetName("Sheet2");
        executeStep(step);
        assertSame(step.getExcelWorkbook().getSheetAt(1), step.getExcelSheet());
    }

    public void testInvalidParameters() {
        checkInvalidParameters(null, null, "Either sheet number or sheet name should be specified");
        checkInvalidParameters("2", "X", "One of sheet number or sheet name must be specified");
        checkInvalidParameters("X", null, "Can't parse sheetIndex parameter with value 'X' as an integer.");
    }

    private void checkInvalidParameters(final String sheetIndex, final String sheetName, final String error) {
        final ExcelSelectSheet step = (ExcelSelectSheet) getStep();
        step.setSheetIndex(sheetIndex);
        step.setSheetName(sheetName);
        assertErrorOnExecute(step, "invalid parameters", error);
    }
}
