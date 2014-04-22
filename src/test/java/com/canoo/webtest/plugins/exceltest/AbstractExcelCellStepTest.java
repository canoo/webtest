// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import org.apache.poi.hssf.util.CellReference;

import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link AbstractExcelCellStep}.<p>
 *
 * @author Rob Nielsen
 */
public class AbstractExcelCellStepTest extends BaseExcelStepTestCase
{
    public void testCellByReference() throws Exception {
        checkCellReference(0,0, "A1", null, null);
    }

    public void testTrickyCellByReference() throws Exception  {
        checkCellReference(122, 106, "ABC123", null, null);
    }

    public void testCellByRowCol() throws Exception  {
        checkCellReference(4,2, null, "5", "3");
    }

    public void testCellByNamedCol() throws Exception  {
        checkCellReference(9,5, null, "10", "F");
    }

    public void testInvalidCellReference() {
        checkErrorOnExecute("ThisIsNotValid", null, null, "Invalid cell reference: ThisIsNotValid");
    }


    public void testInvalidRowReference() {
        checkErrorOnExecute(null, "ThisIsNotValid", "1", "Can't parse 'ThisIsNotValid' as a integer row reference.");
    }

    public void testInvalidColReference() {
        checkErrorOnExecute(null, "1", "ThisIsNotValid", "Can't parse 'ThisIsNotValid' as a column reference (eg. 'A' or '1')");
        checkErrorOnExecute(null, "1", "0", "Can't parse '0' as a column reference (eg. 'A' or '1')");
    }

    public void testNoColumn() {
        checkErrorOnExecute(null, "1", null, "You must specify a row and column or a cell reference.");
    }

    public void testNoRow() {
        checkErrorOnExecute(null, null, "1", "You must specify a row and column or a cell reference.");
    }

    public void testNoFieldsSet() {
        checkErrorOnExecute(null, null, null, "You must specify a row and column or a cell reference.");
    }

    public void testAllFieldsSet() {
        checkErrorOnExecute("A4", "4", "1", "You must specify either a row and column or a cell reference.");
    }

    private void checkErrorOnExecute(final String cell, final String row, final String col,
                                     final String exceptionMessagePrefix) {
        final AbstractExcelCellStep step = (AbstractExcelCellStep) getStep();
        step.setCell(cell);
        step.setRow(row);
        step.setCol(col);
        assertErrorOnExecute(step, "Parameter Error", exceptionMessagePrefix);
    }

    private void checkCellReference(final int expRow, final int expCol, final String cell,
                                    final String row, final String col) throws Exception {
        final AbstractExcelCellStep step = (AbstractExcelCellStep) getStep();
        step.setCell(cell);
        step.setRow(row);
        step.setCol(col);
        executeStep(step);
        final CellReference cellReference = step.getCellReference();
        assertEquals("Row", expRow, cellReference.getRow());
        assertEquals("Column", expCol, cellReference.getCol());
    }

    protected Step createStep() {
        return new AbstractExcelCellStepStub();
    }

    public void testToString() {
    }

    private static class AbstractExcelCellStepStub extends AbstractExcelCellStep {
        public void doExecute() throws Exception {
            getCellReference();
        }
    }
}
