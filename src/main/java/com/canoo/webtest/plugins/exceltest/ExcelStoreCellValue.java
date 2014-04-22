// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

/**
 * @author Rob Nielsen
 * @webtest.step category="Excel"
 *   name="excelStoreCellValue"
 *   alias="storeCellValue"
 * description="Provides the ability to store the value of a cell in a property for later checking.  Note that this step tests the actual stored value of the cell, not what is displayed in the spreadsheet.  In particular, formulas will not be evaluated."
 */
public class ExcelStoreCellValue extends AbstractExcelCellStep {
    private String fPropertyName;
    private String fPropertyType;

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

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(fPropertyName, "property");
    }

    public void doExecute() {
        setWebtestProperty(getProperty(), getCellValue(), getPropertyType());
    }
}