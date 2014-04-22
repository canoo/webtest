// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.CellReference;
import org.apache.log4j.Logger;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.steps.Step;

/**
 * Util class for looking up string values of various parts of an Excel spreadsheet.<p>
 *
 * @author Rob Nielsen
 */
public class ExcelCellUtils {
    private static final Logger LOG = Logger.getLogger(ExcelCellUtils.class);
    public static final int TWELVE_POINT_FIVE_GRAY = 17;
    public static final int SIX_POINT_TWO_FIVE_GRAY = 18;

    public static HSSFCell getExcelCellAt(final AbstractExcelSheetStep step, final int row, final short col) {
        if (row == -1) {
            return null;
        }
        final HSSFRow excelRow = step.getExcelSheet().getRow(row);
        if (excelRow == null) {
            return null;
        }
        return excelRow.getCell(col);
    }

    public static String getCellValueAt(final HSSFCell cell) {
        if (null == cell) {
            return "";
        }
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            case HSSFCell.CELL_TYPE_NUMERIC:
                return asStringTrimInts(cell.getNumericCellValue());
            case HSSFCell.CELL_TYPE_BLANK:
                return "";
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case HSSFCell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            case HSSFCell.CELL_TYPE_ERROR:
                return "Error Code "+String.valueOf(cell.getErrorCellValue() & 0xFF);
            ///CLOVER:OFF there are currently no other types.  Potentially more in future?
            default:
                LOG.warn("Cell Type not supported: " + cell.getCellType());
                return "";
            ///CLOVER:ON
        }
    }

    private static String asStringTrimInts(final double value) {
        if (value == (int)value) {
            return String.valueOf((int)value);
        }
        return String.valueOf(value);
    }

    public static CellReference getCellReference(final Step step, final String cell,
                                                 final String rowStr, final String colStr) {
        if (cell != null) {
            return getCellReference(step, cell);
        } else {
            try {
                final int row = Integer.parseInt(rowStr);
                if (row > 0) {
                    try
                    {
                        final int col = Short.parseShort(colStr);
                        if (col > 0) {
                            return new CellReference(row-1, col-1, true, true);
                        }
                    }
                    catch(NumberFormatException e) {
                        if (colStr.matches("[A-Z]+")) {
                           return new CellReference(colStr + rowStr);
                        }
                    }
                    throw new StepExecutionException("Can't parse '"+colStr +"' as a column reference (eg. 'A' or '1')", step);

                }
            } catch (NumberFormatException e) {
                // fallthrough
            }
            throw new StepExecutionException("Can't parse '"+rowStr +"' as a integer row reference.", step);
        }
    }

    public static CellReference getCellReference(final Step step, final String cell) {
        if (!cell.matches("[A-Z]+[0-9]+")) {
            throw new StepExecutionException("Invalid cell reference: " + cell, step);
        }
        return new CellReference(cell);
    }

    static String getCellType(final int cellType) {
        switch(cellType) {
            case HSSFCell.CELL_TYPE_BLANK: return "blank";
            case HSSFCell.CELL_TYPE_BOOLEAN: return "boolean";
            case HSSFCell.CELL_TYPE_ERROR: return "error";
            case HSSFCell.CELL_TYPE_FORMULA: return "formula";
            case HSSFCell.CELL_TYPE_NUMERIC: return "numeric";
            case HSSFCell.CELL_TYPE_STRING: return "string";
            default: return "unknown";
        }
    }

    public static String getAlignmentString(final short alignment) {
        switch(alignment) {
            case HSSFCellStyle.ALIGN_CENTER: return "center";
            case HSSFCellStyle.ALIGN_CENTER_SELECTION: return "center-selection";
            case HSSFCellStyle.ALIGN_FILL: return "fill";
            case HSSFCellStyle.ALIGN_GENERAL: return "general";
            case HSSFCellStyle.ALIGN_JUSTIFY: return "justify";
            case HSSFCellStyle.ALIGN_LEFT: return "left";
            case HSSFCellStyle.ALIGN_RIGHT: return "right";
            default: return "unknown";
        }
    }

    public static String getVerticalAlignmentString(final short verticalAlignment) {
        switch(verticalAlignment) {
            case HSSFCellStyle.VERTICAL_BOTTOM: return "bottom";
            case HSSFCellStyle.VERTICAL_CENTER: return "center";
            case HSSFCellStyle.VERTICAL_JUSTIFY: return "justify";
            case HSSFCellStyle.VERTICAL_TOP: return "top";
            default: return "unknown";
        }
    }

    public static String getFillPattern(final short fillPattern) {
        switch(fillPattern) {
            case HSSFCellStyle.NO_FILL: return "none";
            case HSSFCellStyle.SOLID_FOREGROUND: return "solid";
            case HSSFCellStyle.FINE_DOTS: return "50% gray";
            case HSSFCellStyle.ALT_BARS: return "75% gray";
            case HSSFCellStyle.SPARSE_DOTS: return "25% gray";
            case HSSFCellStyle.THICK_HORZ_BANDS: return "horizontal stripe";
            case HSSFCellStyle.THICK_VERT_BANDS: return "vertical stripe";
            case HSSFCellStyle.THICK_BACKWARD_DIAG: return "reverse diagonal stripe";
            case HSSFCellStyle.THICK_FORWARD_DIAG: return "diagonal stripe";
            case HSSFCellStyle.BIG_SPOTS: return "diagonal crosshatch";
            case HSSFCellStyle.BRICKS: return "thick diagonal crosshatch";
            case HSSFCellStyle.THIN_HORZ_BANDS: return "thin horizontal stripe";
            case HSSFCellStyle.THIN_VERT_BANDS: return "thin vertical stripe";
            case HSSFCellStyle.THIN_BACKWARD_DIAG: return "thin reverse diagonal stripe";
            case HSSFCellStyle.THIN_FORWARD_DIAG: return "thin diagonal stripe";
            case HSSFCellStyle.SQUARES: return "thin horizontal crosshatch";
            case HSSFCellStyle.DIAMONDS: return "thin diagonal crosshatch";
            case TWELVE_POINT_FIVE_GRAY: return "12.5% gray";
            case SIX_POINT_TWO_FIVE_GRAY: return "6.25% gray";
            default: return "unknown";
        }
    }

    public static String getBorder(final short border) {
        switch(border) {
            case HSSFCellStyle.BORDER_DASH_DOT: return "dash dot";
            case HSSFCellStyle.BORDER_DASH_DOT_DOT: return "dash dot dot";
            case HSSFCellStyle.BORDER_DASHED: return "dashed";
            case HSSFCellStyle.BORDER_DOTTED: return "dotted";
            case HSSFCellStyle.BORDER_DOUBLE: return "double";
            case HSSFCellStyle.BORDER_HAIR: return "hair";
            case HSSFCellStyle.BORDER_MEDIUM: return "medium";
            case HSSFCellStyle.BORDER_MEDIUM_DASH_DOT: return "medium dash dot";
            case HSSFCellStyle.BORDER_MEDIUM_DASH_DOT_DOT: return "medium dash dot dot";
            case HSSFCellStyle.BORDER_MEDIUM_DASHED: return "medium dashed";
            case HSSFCellStyle.BORDER_NONE: return "none";
            case HSSFCellStyle.BORDER_SLANTED_DASH_DOT: return "slanted dash dot";
            case HSSFCellStyle.BORDER_THICK: return "thick";
            case HSSFCellStyle.BORDER_THIN: return "thin";
            default: return "unknown";
        }
    }
    
    public static String getCellRef(int col, int row)
    {
		return "" + ((char) ('A' + col)) + (row + 1);
    }
}
