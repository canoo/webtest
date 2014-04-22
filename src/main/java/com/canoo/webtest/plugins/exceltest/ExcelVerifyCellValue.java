// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.engine.StepFailedException;

/**
 * @author Rob Nielsen
 * @webtest.step category="Excel"
 *   name="excelVerifyCellValue"
 *   alias="verifyCellValue"
 * description="This step verifies the value of a specified cell in an Excel spreadsheet.  Note that this step tests the actual stored value of the cell, not what is displayed in the spreadsheet.  In particular, formulas will not be evaluated."
 */
public class ExcelVerifyCellValue extends AbstractExcelCellStep {
    private String fText;

    public String getText() {
        return fText;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="yes"
     *   description="The text value to verify against."
     */
    public void setText(final String text) {
        fText = text;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(fText, "text");
    }

    public void doExecute() throws Exception {
        final String cellValue = getCellValue();
        if (verifyStrings(getText(), cellValue)) {
            return;
        }
        throw new StepFailedException("Wrong cell value found for cell "+
                getCellReferenceStr(), getText(), cellValue, this);
    }
}