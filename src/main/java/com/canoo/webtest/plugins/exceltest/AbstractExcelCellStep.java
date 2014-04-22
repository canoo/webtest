// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.util.CellReference;

import com.canoo.webtest.engine.StepExecutionException;

/**
 * Base class for steps that deal with an individual cell of an Excel spreadsheet.<p>
 *
 * @author Rob Nielsen
 */
public abstract class AbstractExcelCellStep extends AbstractExcelSheetStep {
    private String fRow;
    private String fCol;
    private String fCell;

    /**
     * @param cell The index of the sheet to select
     * @webtest.parameter required="yes/no"
     * description="The spreadsheet cell to select.  eg. A5.  Either <em>cell</em> or <em>row</em> and <em>col</em> must be specified."
     */
    public void setCell(final String cell) {
        fCell = cell;
    }

    public String getCell() {
        return fCell;
    }

    /**
     * @param row The row of the sheet to select
     * @webtest.parameter required="yes/no"
     * description="The number of the row to select, starting at 1.  Either <em>cell</em> or <em>row</em> and <em>col</em> must be specified."
     */
    public void setRow(final String row) {
        fRow = row;
    }

    public String getRow() {
        return fRow;
    }

    /**
     * @param col The column to select
     * @webtest.parameter required="no"
     * description="The name or number (starting at 1) of the column to select. eg 1 or A.  Either <em>cell</em> or <em>row</em> and <em>col</em> must be specified."
     */
    public void setCol(final String col) {
        fCol = col;
    }

    public String getCol() {
        return fCol;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        if (getCell() == null && (getRow() == null || getCol() == null)) {
            throw new StepExecutionException("You must specify a row and column or a cell reference.", this);
        }
        if (getCell() != null && (getRow() != null || getCol() != null)) {
            throw new StepExecutionException("You must specify either a row and column or a cell reference.", this);
        }
    }

    protected HSSFCell getExcelCell() {
        final CellReference cellReference = ExcelCellUtils.getCellReference(this, getCell(), getRow(), getCol());
        return ExcelCellUtils.getExcelCellAt(this, cellReference.getRow(), cellReference.getCol());
    }

    protected String getCellValue() {
        return ExcelCellUtils.getCellValueAt(getExcelCell());
    }

    protected CellReference getCellReference() {
        return ExcelCellUtils.getCellReference(this, getCell(), getRow(), getCol());
    }

    protected String getCellReferenceStr() {
    	return getCellReference().formatAsString();
    }
}
