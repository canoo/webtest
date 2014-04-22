// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.engine.StepFailedException;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * @author Rob Nielsen
 * @webtest.step category="Excel"
 *   name="excelFindRow"
 *   alias="findRow"
 * description="This step allows you to find a specific row on a sheet.  This is useful for using with repeat when you don't know how many rows there will be on a table.  It starts from a specified cell and searches down for a row matching the given text in an Excel spreadsheet and saves the resulting row as a property."
 */
public class ExcelFindRow extends AbstractExcelSheetStep {
    private String fText;
    private String fPropertyName;
    private String fPropertyType;
    private String fStartRow = "1";
    private String fCol;

    /**
     * @param index The row to start from
     * @webtest.parameter required="no"
     * default="1"
     * description="The row to start searching from"
     */
    public void setStartRow(final String index) {
        fStartRow = index;
    }

    public String getStartRow() {
        return fStartRow;
    }

    /**
     * @param index The index of the sheet to select
     * @webtest.parameter required="no"
     * description="The column reference (eg. 'B' or '2') to search in."
     */
    public void setCol(final String index) {
        fCol = index;
    }

    public String getCol() {
        return fCol;
    }

    /**
     * @param name The Property Name
     * @webtest.parameter required="yes"
     * description="The name of the property in which to store the value."
     */
    public void setProperty(final String name) {
        fPropertyName = name;
    }

    public String getProperty() {
        return fPropertyName;
    }

    /**
     * Sets the Type of the Property.<p>
     *
     * @param type The Property type
     * @webtest.parameter required="no"
     * description="The type of the property in which to store the value. Either \"ant\" or \"dynamic\"."
     * default="the \"defaultPropertyType\" as specified in the \"config\" element is used."
     */
    public void setPropertyType(final String type) {
        fPropertyType = type;
    }

    public String getPropertyType() {
        return fPropertyType;
    }

    public String getText() {
        return fText;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="yes"
     *   description="The text value to search for.  Will be treated as a regex if surrounded with '/'s (eg '/test\d+/')"
     */
    public void setText(final String text) {
        fText = text;
    }

    protected boolean verifyText(final String actualValue) {
        return verifyStrings(getText(), actualValue);
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getText(), "text");
        nullParamCheck(getProperty(), "property");
        nullParamCheck(getCol(), "col");
        optionalIntegerParamCheck(getStartRow(), "startRow", true);
    }

    public void doExecute() {
        final CellReference cellReference = ExcelCellUtils.getCellReference(this, null, getStartRow(), getCol());
        final HSSFSheet excelSheet = getExcelSheet();
        int row = cellReference.getRow();
        while(row <= excelSheet.getLastRowNum()) {
            final HSSFCell excelCellAt = ExcelCellUtils.getExcelCellAt(this, row, cellReference.getCol());
            if (verifyText(ExcelCellUtils.getCellValueAt(excelCellAt))) {
                setWebtestProperty(getProperty(), String.valueOf(row + 1), getPropertyType());
                return;
            }
            row++;
        }
        throw new StepFailedException("No cells were found matching '"+getText()+"' starting from " + cellReference.formatAsString(), this);
    }
}