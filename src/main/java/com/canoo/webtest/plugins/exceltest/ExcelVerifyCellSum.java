// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.StepExecutionException;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * Verifies that a cell represents the sum of a range of cells in an Excel spreadsheet file, either
 * as a "=SUM(<range>)" formula or numeric value.<p>
 *
 * @author Rob Nielsen
 * @webtest.step category="Excel"
 *   name="excelVerifyCellSum"
 *   alias="verifyCellSum"
 * description="This step verifies that a cell represents the sum of a range of cells, either as a formula (=SUM(<range>)) or numeric value."
 */
public class ExcelVerifyCellSum extends AbstractExcelCellStep {
    private String fRange;

    public String getRange() {
        return fRange;
    }

    /**
     * @param range
     * @webtest.parameter
     * 	 required="yes"
     *   description="The range of cells to verify sum against. (eg 'A1:A5')"
     */
    public void setRange(final String range) {
        fRange = range;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getRange(), "range");
        if (!getRange().matches("[A-Za-z]+[0-9]+:[A-Za-z]+[0-9]+")) {
            throw new StepExecutionException("Cannot parse \""+getRange()+"\" as a spreadsheet range. eg \"A10:A20\"", this);
        }
    }

    public void doExecute() throws Exception {
        final HSSFCell excelCell = getExcelCell();
        checkFormula(excelCell);
        checkLiteralValue(excelCell);
    }

    private void checkFormula(final HSSFCell excelCell) {
        if (excelCell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
            final String expectedValue = "SUM(" + getRange() + ")".toUpperCase();
            final String actualValue = excelCell.getCellFormula().toUpperCase();
            if (verifyStrings(expectedValue, actualValue)) {
                return;
            }
            throw new StepFailedException("Unexpected formula in cell " + getCellReferenceStr(), expectedValue, actualValue);
        }
        else if (excelCell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC) {
            throw new StepFailedException("Cell " + getCellReferenceStr() + " does not contain a formula or a numeric value.");
        }
    }

    private void checkLiteralValue(final HSSFCell excelCell) {
        final double cellValue = excelCell.getNumericCellValue();
        final int colon = getRange().indexOf(':');
        final CellReference start = ExcelCellUtils.getCellReference(this, getRange().substring(0, colon));
        final CellReference end = ExcelCellUtils.getCellReference(this, getRange().substring(colon + 1));
        double sum = 0;
        for(int row = start.getRow() ; row <= end.getRow() ; row++ ) {
            for(short col = start.getCol(); col <= end.getCol(); col++) {
                final HSSFCell excelCellAt = ExcelCellUtils.getExcelCellAt(this, row, col);
                if (excelCellAt == null || excelCellAt.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
                    continue;
                }
                if (excelCellAt.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                    sum += excelCellAt.getNumericCellValue();
                } else {
                    throw new StepFailedException("Cell " + ((char) ('A' + col)) + (row + 1) + " does not contain a numeric value.");
                }
            }
        }
        if (Math.abs(cellValue - sum) > 0.01) {
            throw new StepFailedException("Unexpected sum of cells from range " + fRange + " in cell " + getCellReferenceStr(), 
            		String.valueOf(sum), String.valueOf(cellValue));
        }
    }

}