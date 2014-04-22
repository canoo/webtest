// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link ExcelFindRow}.<p>
 *
 * @author Rob Nielsen
 */
public class ExcelFindRowTest extends BaseExcelStepTestCase {
    protected Step createStep() {
        return new ExcelFindRow();
    }

    public void testRequiredFields() {
        final ExcelFindRow step = (ExcelFindRow) getStep();
        step.setProperty("xxx");
        step.setCol("A");
        assertStepRejectsNullParam("text", step);
        step.setText("blah");
        step.setProperty(null);
        assertStepRejectsNullParam("property", step);
        step.setProperty("xxx");
        step.setCol(null);
        assertStepRejectsNullParam("col", step);
    }

    public void testInvalidCol() {
        final ExcelFindRow step = (ExcelFindRow) getStep();
        step.setProperty("xxx");
        step.setText("TOTAL");
        step.setCol("InvalidColumn");
        assertErrorOnExecute(step, "Invalid Column", "Can't parse 'InvalidColumn' as a column reference (eg. 'A' or '1')");
        step.setCol("0");
        assertErrorOnExecute(step, "Invalid Column", "Can't parse '0' as a column reference (eg. 'A' or '1')");
    }

    public void testInvalidStartRow() {
        final ExcelFindRow step = (ExcelFindRow) getStep();
        step.setProperty("xxx");
        step.setCol("A");
        step.setStartRow("InvalidRow");
        step.setText("xxx");
        assertErrorOnExecute(step, "Invalid Row", "Can't parse startRow parameter with value 'InvalidRow' as an integer.");
        step.setStartRow("0");
        assertErrorOnExecute(step, "Invalid Row", "Can't parse '0' as a integer row reference.");
    }

    public void testCantFindRow() {
        final ExcelFindRow step = (ExcelFindRow) getStep();
        step.setCol("A");
        step.setText("blarg");
        step.setProperty("test");
        assertFailOnExecute(step, "FindRow", "No cells were found matching 'blarg' starting from A1");
    }

}
