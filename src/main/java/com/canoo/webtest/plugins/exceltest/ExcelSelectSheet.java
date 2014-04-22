// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.engine.StepExecutionException;

/**
 * @author Rob Nielsen
 * @webtest.step category="Excel"
 *   name="excelSelectSheet"
 *   alias="selectSheet"
 * description="Provides the ability to select the current sheet.  Any subsequent calls to excel steps will use the selected sheet if not overridden"
 */
public class ExcelSelectSheet extends AbstractExcelSheetStep {
    public void doExecute() {
        setCurrentSheet(getExcelSheet());
    }

    /**
     * @param name The Sheet Name
     * @webtest.parameter required="yes/no"
     * description="The name of the sheet to select.  Either <em>sheetName</em> or <em>sheetIndex</em> must be selected."
     */
    public void setSheetName(final String name) {
        super.setSheetName(name);
    }

    /**
     * @param index The Sheet Name
     * @webtest.parameter required="yes/no"
     * description="The index of the sheet to select, starting at zero.  Either <em>sheetName</em> or <em>sheetIndex</em> must be selected."
     */
    public void setSheetIndex(final String index) {
        super.setSheetIndex(index);
    }

    protected void verifyParameters() {
        super.verifyParameters();
        if (getSheetIndex() == null && getSheetName() == null) {
            throw new StepExecutionException("Either sheet number or sheet name should be specified", this);
        }
        if (getSheetIndex() != null && getSheetName() != null) {
            throw new StepExecutionException("One of sheet number or sheet name must be specified", this);
        }
    }

}