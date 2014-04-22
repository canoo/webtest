// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;

/**
 * Test class for {@link ExcelVerifyCellStyle}.<p>
 *
 * @author Rob Nielsen
 */
public class ExcelVerifyCellStyleTest extends BaseExcelStepTestCase {
    protected Step createStep() {
        return new ExcelVerifyCellStyle();
    }

    public void testVerifyWithEmpty() throws Exception {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setCell("B3");
        executeStep(step);
    }

    public void testVerifyStyleOnEmptyCell() throws Exception {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setCell("A1");
        step.setFormat("XXX");
        assertErrorOnExecute(step, "can't find cell", "Can't find cell for A1");
    }

    public void testInvalidSideOnBorder() throws Exception {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setCell("B3");
        step.setBorder("blah:blah");
        assertErrorOnExecute(step, "border unknown", "Border side 'blah' unknown.  Specify one of top, down, left right.");
    }

    public void testFailedAssertionInBorderColor() throws Exception {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setBorderColor("left:black;top:red");
        checkFailedAssertion("D4", "topBorderColor", "red", "black");
        step.setBorderColor("black;right:blue");
        checkFailedAssertion("D4", "rightBorderColor", "blue", "black");
        step.setBorderColor("bottom:black;bottom:#00ff00");
        checkFailedAssertion("D4", "bottomBorderColor", "bright green", "black");
        step.setBorderColor("left:xxx");
        checkFailedAssertion("D4", "leftBorderColor", "xxx", "black");
    }

    public void testFailedAssertionInFillColor() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setFillColor("blue");
        checkFailedAssertion("F20", "fillColor", "blue", "dark red");
    }

    public void testFailedAssertionInFillBackgroundColor() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setFillBackgroundColor("#ff0000");
        checkFailedAssertion("G22", "fillBackgroundColor", "red", "orange");
    }

    public void testFailedAssertionInFillPattern() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setFillPattern("horizontal strip");
        checkFailedAssertion("J16", "fillPattern", "horizontal strip", "horizontal stripe");
    }

    public void testFailedAssertionInFontStyle() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setFontStyle("bold underline strikethrough");
        checkFailedAssertion("L14", "fontStyle", "bold strikethrough underline", "bold italic strikethrough underline");
    }

    public void testFailedAssertionInFontName() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setFontName("arial");
        checkFailedAssertion("L20", "fontName", "arial", "Arial");
    }

    public void testFailedAssertionInFontSize() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setFontSize("16.0");
        checkFailedAssertion("L31", "fontSize", "16.0", "16");
    }

    public void testFailedAssertionInAlignment() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setAlign("left");
        checkFailedAssertion("M6", "align", "left", "center");
    }

    public void testFailedAssertionInVerticalAlignment() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setValign("left");
        checkFailedAssertion("M12", "valign", "left", "top");
    }

    public void testFailedAssertionInType() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setType("blah");
        checkFailedAssertion("N23", "type", "blah", "boolean");
    }

    public void testFailedAssertionInFormat() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setFormat("xxx");
        checkFailedAssertion("N31", "format", "xxx", "m/d/yy");
    }

    public void testFailedAssertionInLocked() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setLocked("xxx");
        checkFailedAssertion("O4", "locked", "xxx", "true");
    }

    public void testFailedAssertionInWrapped() throws Exception {
        ((ExcelVerifyCellStyle) getStep()).setWrap("xxx");
        checkFailedAssertion("O8", "wrap", "xxx", "false");
    }

    private void checkFailedAssertion(final String cell, final String property,
                                      final String expected, final String actual) {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setSheetIndex("1");
        step.setCell(cell);
        assertFailOnExecute(step, "border color", 
        		"Wrong cell style found for property '" + property + "' in cell " + cell
        		+ ". Expected value \"" + expected + "\" but got \"" +actual + "\"");
    }

    public void testErrorCellType() throws Exception {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setCell("B2");
        step.getExcelCell().setCellErrorValue((byte)42);
        step.getExcelCell().setCellType(HSSFCell.CELL_TYPE_ERROR);
        step.setType("error");
        executeStep(step);
    }

    public void testUnknownProperties() throws Exception {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setCell("B2");
        final HSSFCellStyle cellStyle = step.getExcelCell().getCellStyle();
        cellStyle.setBorderTop(Short.MAX_VALUE);
        cellStyle.setAlignment(Short.MAX_VALUE);
        cellStyle.setVerticalAlignment(Short.MAX_VALUE);
        cellStyle.setFillPattern(Short.MAX_VALUE);
        final HSSFFont fontAt = step.getExcelWorkbook().getFontAt(cellStyle.getFontIndex());
        fontAt.setUnderline(Byte.MAX_VALUE);
        step.setBorder("top:unknown");
        step.setAlign("unknown");
        step.setValign("unknown");
        step.setFillPattern("unknown");
        step.setFontStyle("bold underline-unknown");
        executeStep(step);
    }

    public void testUnknownCellType() {
        assertEquals("unknown", ExcelCellUtils.getCellType(6));
    }

    public void testInvalidSides() throws Exception {
        final ExcelVerifyCellStyle step = (ExcelVerifyCellStyle) getStep();
        step.setCell("A1");
        ThrowAssert.assertThrows(IllegalArgumentException.class, "Invalid side: 4", new TestBlock()
        {
            public void call() throws Exception {
                step.getBorder(null, 4);
            }
        });
        ThrowAssert.assertThrows(IllegalArgumentException.class, "Invalid side: 4", new TestBlock()
        {
            public void call() throws Exception {
                step.getBorderColor(null, 4);
            }
        });
        ThrowAssert.assertThrows(IllegalArgumentException.class, "Invalid side: 4", new TestBlock()
        {
            public void call() throws Exception {
                step.getAdjacentCell(4);
            }
        });
    }
}
