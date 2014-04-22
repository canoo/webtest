// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link AbstractExcelSheetStep}.<p>
 *
 * @author Rob Nielsen
 */
public class AbstractExcelSheetStepTest extends BaseExcelStepTestCase
{

    public void testGetFirstSheet() {
        checkGetExcelSheet(0, null, null);
    }
    public void testGetSheetByName() {
        checkGetExcelSheet(1, "Sheet2", null);
    }

    public void testGetSheetByIndex() {
        checkGetExcelSheet(2, null, "2");
    }

    public void testUnknownSheetName() {
        final AbstractExcelSheetStep step = (AbstractExcelSheetStep) getStep();
        step.setSheetName("not a sheet");
        assertErrorOnExecute(step, "unknown sheet name", "A sheet named 'not a sheet' was not found in the file.");
    }

    public void testUnknownSheetIndex() {
        final AbstractExcelSheetStep step = (AbstractExcelSheetStep) getStep();
        step.setSheetIndex("3");
        assertErrorOnExecute(step, "unknown sheet index", "Invalid sheet index: 3. This workbook contains sheets with indexes from 0 to 2.");
        step.setSheetIndex("-1");
        assertErrorOnExecute(step, "unknown sheet index", "sheetIndex parameter with value '-1' must not be negative");
        step.setSheetIndex("pork");
        assertErrorOnExecute(step, "unknown sheet index", "Can't parse sheetIndex parameter with value 'pork' as an integer.");
    }

    public void testNoSheets() {
        final AbstractExcelSheetStep step = (AbstractExcelSheetStep) getStep();
        WebtestTask.setThreadContext(new ExcelContextStub(ExcelTestResources.MINIMAL_FILE));
        step.getExcelWorkbook().removeSheetAt(0);
        assertErrorOnExecute(step, "no sheets", "This spreadsheet has no sheets");
    }

    private void checkGetExcelSheet(final int expectedSheetIndex, final String sheetName,
                                    final String sheetIndex) {
        final AbstractExcelSheetStep step = (AbstractExcelSheetStep) getStep();
        step.setSheetName(sheetName);
        step.setSheetIndex(sheetIndex);
        final HSSFSheet excelSheet = step.getExcelSheet();
        final HSSFSheet expectedSheet = step.getExcelWorkbook().getSheetAt(expectedSheetIndex);
        assertSame(expectedSheet, excelSheet);
    }

    public void testGetSheetStoredInContext() {
        final AbstractExcelSheetStep step = (AbstractExcelSheetStep) getStep();
        final HSSFSheet currentSheet = step.getExcelWorkbook().getSheetAt(2);
        step.setCurrentSheet(currentSheet);
        assertSame(currentSheet, step.getExcelSheet());
    }

    protected Step createStep() {
        return new AbstractExcelSheetStepStub();
    }

    public void testToString() {
    }

    private static class AbstractExcelSheetStepStub extends AbstractExcelSheetStep {
        public void doExecute() throws Exception {
            getExcelSheet();
        }
    }
}
