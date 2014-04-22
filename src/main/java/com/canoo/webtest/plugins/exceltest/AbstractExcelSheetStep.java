// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.canoo.webtest.engine.StepExecutionException;

/**
 * Base class for steps that deal with an individual sheet of an Excel spreadsheet.<p>
 *
 * @author Rob Nielsen
 */
public abstract class AbstractExcelSheetStep extends AbstractExcelStep {
    private String fSheetName;
    private String fSheetIndex;

    protected void setCurrentSheet(final HSSFSheet sheet) {
        getContext().put(KEY_CURRENT_SHEET, sheet);
    }

    protected HSSFSheet getExcelSheet() {
        final int numberOfSheets = getExcelWorkbook().getNumberOfSheets();
        if (numberOfSheets == 0) {
            throw new StepExecutionException("This spreadsheet has no sheets", this);
        }
        HSSFSheet sheet = null;
        if (fSheetName != null) {
            sheet = getExcelWorkbook().getSheet(fSheetName);
            if (sheet == null) {
                throw new StepExecutionException("A sheet named '"+ fSheetName + "' was not found in the file.", this);
            }
        }
        if (sheet == null && fSheetIndex != null) {
            final int sheetIndex = Integer.parseInt(fSheetIndex);
            if (sheetIndex < 0 || sheetIndex >= numberOfSheets) {
                throw new StepExecutionException("Invalid sheet index: "+fSheetIndex + ". This workbook contains sheets with indexes from 0 to "+ (numberOfSheets-1) + ".", this);
            }
            sheet = getExcelWorkbook().getSheetAt(sheetIndex);
        }
        if (sheet == null) {
            sheet = (HSSFSheet) getContext().get(KEY_CURRENT_SHEET);
        }
        if (sheet == null) {
            sheet = getExcelWorkbook().getSheetAt(0);
        }
        return sheet;
    }

    /**
     * @param name The Sheet Name
     * @webtest.parameter required="no"
     * description="The name of the sheet to select.  If no sheet is selected, the value of the last <em>excelSelectSheet</em> call is used, or defaults to the first sheet."
     */
    public void setSheetName(final String name) {
        fSheetName = name;
    }

    public String getSheetName() {
        return fSheetName;
    }

    /**
     * @param index The index of the sheet to select
     * @webtest.parameter required="no"
     * description="The index of the sheet to select, starting at zero.  If no sheet is selected, the value of the last <em>excelSelectSheet</em> call is used, or defaults to the first sheet."
     */
    public void setSheetIndex(final String index) {
        fSheetIndex = index;
    }

    public String getSheetIndex() {
        return fSheetIndex;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        optionalIntegerParamCheck(fSheetIndex, "sheetIndex", true);
    }
}
