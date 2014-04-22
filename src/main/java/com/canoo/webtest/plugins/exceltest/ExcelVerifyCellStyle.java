// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.CellReference;

import java.util.*;

/**
 * @author Rob Nielsen
 * @webtest.step category="Excel"
 *   name="excelVerifyCellStyle"
 *   alias="verifyCellStyle"
 * description="This step verifies the style of a specified cell in an Excel spreadsheet.  Only those properties set will be checked."
 */
public class ExcelVerifyCellStyle extends AbstractExcelCellStep {
    private static final int TOP = 0;
    private static final int RIGHT = 1;
    private static final int BOTTOM = 2;
    private static final int LEFT = 3;
    private static final String[] SIDES = {"top","right","bottom","left"};

    private String fType;
    private String fFormat;
    private String fAlign;
    private String fValign;
    private String fWrap;
    private String fLocked;
    private String fFontName;
    private String fFontSize;
    private String fFontStyle;
    private String fBorder;
    private String fBorderColor;
    private String fTextColor;
    private String fFillColor;
    private String fFillBackgroundColor;

    private String fFillPattern;


    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The format to check for. eg 0.00 for two decimal places."
     */
    public void setFormat(final String text) {
        fFormat = text;
    }

    public String getFormat() {
        return fFormat;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected cell type. One of <em>blank, numeric, formula, string, boolean or error.</em>"
     */
    public void setType(final String text) {
        fType = text;
    }

    public String getType() {
        return fType;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected horizontal alignment. One of center, center-selection, fill, general, justify, left, right"
     */
    public void setAlign(final String text) {
        fAlign = text;
    }

    public String getAlign() {
        return fAlign;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected vertical alignment.  One of bottom, center, justify, top"
     */
    public void setValign(final String text) {
        fValign = text;
    }

    public String getValign() {
        return fValign;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected wrap setting.  true or false"
     */
    public void setWrap(final String text) {
        fWrap = text;
    }

    public String getWrap() {
        return fWrap;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected font style.  A combination of bold, italic, strikethrough, subscript, superscript, underline, underline-accounting, underline-double, underline-double-accounting separated by spaces, or normal"
     */
    public void setFontStyle(final String text) {
        fFontStyle = text;
    }

    public String getFontStyle() {
        return fFontStyle;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected locked status. true or false"
     */
    public void setLocked(final String text) {
        fLocked = text;
    }

    public String getLocked() {
        return fLocked;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected font name. eg. Arial or Times"
     */
    public void setFontName(final String text) {
        fFontName = text;
    }

    public String getFontName() {
        return fFontName;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected font size in points. eg 10 or 12"
     */
    public void setFontSize(final String text) {
        fFontSize = text;
    }

    public String getFontSize() {
        return fFontSize;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected border style.  One of <em>dash dot, dash dot dot, dashed, dotted, double, hair, medium, medium dash dot, medium dash dot dot, medium dashed, slanted dash dot, thick, thin, none</em>.  See below for details on specifying sides."
     */
    public void setBorder(final String text) {
        fBorder = text;
    }

    public String getBorder() {
        return fBorder;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected border color. Specify as #rrggbb or use a standard name.  See below for details on specifying sides."
     */
    public void setBorderColor(final String text) {
        fBorderColor = text;
    }

    public String getBorderColor() {
        return fBorderColor;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected text color. Specify as #rrggbb or use a standard name."
     */
    public void setTextColor(final String text) {
        fTextColor = text;
    }

    public String getTextColor() {
        return fTextColor;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected foreground cell fill color. Specify as #rrggbb or use a standard name."
     */
    public void setFillColor(final String text) {
        fFillColor = text;
    }

    public String getFillColor() {
        return fFillColor;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected background cell fill color. Specify as #rrggbb or use a standard name."
     */
    public void setFillBackgroundColor(final String text) {
        fFillBackgroundColor = text;
    }

    public String getFillBackgroundColor() {
        return fFillBackgroundColor;
    }

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected fill pattern.  One of <em>none, solid, 50% gray, 75% gray, 25% gray, horizontal stripe, vertical stripe, reverse diagonal stripe, diagonal stripe, diagonal crosshatch, thick diagonal crosshatch, thin horizontal stripe, thin vertical stripe, thin reverse diagonal stripe, thin diagonal stripe, thin horizontal crosshatch, thin diagonal crosshatch, 12.5% gray, 6.25% gray</em>"
     */
    public void setFillPattern(final String text) {
        fFillPattern = text;
    }

    public String getFillPattern() {
        return fFillPattern;
    }

    public void doExecute() throws Exception {
        final String[] border = separateSides(getBorder());
        final String[] borderColor = separateSides(getBorderColor());
        for (int i = 0; i < SIDES.length; i++) {
            checkFormat(SIDES[i]+"Border", border[i], getCellBorder(i));
            checkFormat(SIDES[i]+"BorderColor", ExcelColorUtils.lookupStandardColorName(borderColor[i]), getCellBorderColor(i));
        }
        final HSSFCell excelCell = getExcelCell();
        checkFormat("type", getType(), ExcelCellUtils.getCellType(excelCell == null ? HSSFCell.CELL_TYPE_BLANK : excelCell.getCellType()));
        if (excelCell == null) {
            if (cellNotRequired()) {
                return;
            } else {
                throw new StepExecutionException("Can't find cell for " + getCellReferenceStr(), this);
            }
        }
        final HSSFCellStyle cellStyle = excelCell.getCellStyle();
        checkFormat("format", getFormat(), getExcelWorkbook().createDataFormat().getFormat(cellStyle.getDataFormat()));
        checkFormat("align", getAlign(), ExcelCellUtils.getAlignmentString(cellStyle.getAlignment()));
        checkFormat("valign", getValign(), ExcelCellUtils.getVerticalAlignmentString(cellStyle.getVerticalAlignment()));
        checkFormat("wrap", getWrap(), String.valueOf(cellStyle.getWrapText()));
        checkFormat("locked", getLocked(), String.valueOf(cellStyle.getLocked()));
        checkFormat("fontName", getFontName(), getFont(cellStyle).getFontName());
        checkFormat("fontSize", getFontSize(), String.valueOf(getFont(cellStyle).getFontHeightInPoints()));
        checkFormat("fontStyle", sortElements(getFontStyle()), getFontStyle(getFont(cellStyle)));
        checkFormat("fillColor", ExcelColorUtils.lookupStandardColorName(getFillColor()), ExcelColorUtils.getColorName(this, cellStyle.getFillForegroundColor()));
        checkFormat("fillBackgroundColor", ExcelColorUtils.lookupStandardColorName(getFillBackgroundColor()), ExcelColorUtils.getColorName(this, cellStyle.getFillBackgroundColor()));
        checkFormat("textColor", ExcelColorUtils.lookupStandardColorName(getTextColor()), ExcelColorUtils.getColorName(this, getFont(cellStyle).getColor()));
        checkFormat("fillPattern", getFillPattern(), ExcelCellUtils.getFillPattern(cellStyle.getFillPattern()));
    }

    private static String sortElements(final String elements) {
        if (elements == null) {
            return null;
        }
        final List list = new ArrayList();
        final StringTokenizer st = new StringTokenizer(elements, " ");
        while(st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        Collections.sort(list);
        final StringBuffer sb = new StringBuffer();
        for(Iterator it = list.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private static String getFontStyle(final HSSFFont font) {
        final StringBuffer sb = new StringBuffer();
        if (font.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) {
            sb.append("bold ");
        }
        if (font.getItalic()) {
            sb.append("italic ");
        }
        if (font.getStrikeout()) {
            sb.append("strikethrough ");
        }
        if (font.getTypeOffset() == HSSFFont.SS_SUB) {
            sb.append("subscript ");
        } else if (font.getTypeOffset() == HSSFFont.SS_SUPER) {
            sb.append("superscript ");
        }
        switch(font.getUnderline()) {
            case HSSFFont.U_NONE: break;
            case HSSFFont.U_SINGLE: sb.append("underline "); break;
            case HSSFFont.U_SINGLE_ACCOUNTING: sb.append("underline-accounting "); break;
            case HSSFFont.U_DOUBLE: sb.append("underline-double "); break;
            case HSSFFont.U_DOUBLE_ACCOUNTING: sb.append("underline-double-accounting "); break;
            default: sb.append("underline-unknown "); break;
        }
        if (sb.length() == 0) {
            return "normal";
        }
        return sb.substring(0, sb.length()-1);
    }

    private boolean cellNotRequired() {
        return fFormat ==null && fAlign ==null && fValign ==null && fWrap ==null && fLocked ==null
                && fFontName == null && fFontSize == null && fFontStyle == null && fFillColor == null
                && fFillBackgroundColor == null && fFillPattern == null && fTextColor == null;
    }

    public String getCellBorder(final int dir) {
        final HSSFCell excelCellAt = getExcelCell();
        short border = HSSFCellStyle.BORDER_NONE;
        if (excelCellAt != null) {
            border = getBorder(excelCellAt.getCellStyle(), dir);
        }
        if (border == HSSFCellStyle.BORDER_NONE) {
            final HSSFCell adjacentCell = getAdjacentCell(dir);
            if (adjacentCell != null) {
                border = getBorder(adjacentCell.getCellStyle(), (dir + 2) % 4);
            }
        }
        return ExcelCellUtils.getBorder(border);
    }

    public String getCellBorderColor(final int dir) {
        final HSSFCell excelCellAt = getExcelCell();
        short border = HSSFCellStyle.BORDER_NONE;
        if (excelCellAt != null) {
            border = getBorderColor(excelCellAt.getCellStyle(), dir);
        }
        if (border == HSSFCellStyle.BORDER_NONE) {
            final HSSFCell adjacentCell = getAdjacentCell(dir);
            if (adjacentCell != null) {
                border = getBorderColor(adjacentCell.getCellStyle(), (dir + 2) % 4);
            }
        }
        return ExcelColorUtils.getColorName(this, border);
    }

    HSSFCell getAdjacentCell(final int dir) {
        final CellReference cellReference = getCellReference();
        short yofs = 0;
        int xofs = 0;
        switch(dir) {
            case TOP: yofs=-1; break;
            case RIGHT: xofs=1; break;
            case BOTTOM: yofs=1; break;
            case LEFT: xofs=-1; break;
            default: throw new IllegalArgumentException("Invalid side: " + dir);
        }
        return ExcelCellUtils.getExcelCellAt(this, cellReference.getRow() + yofs, (short)(cellReference.getCol() + xofs));
    }

    short getBorder(final HSSFCellStyle cellStyle, final int index) {
         switch(index) {
             case TOP: return cellStyle.getBorderTop();
             case RIGHT: return cellStyle.getBorderRight();
             case BOTTOM: return cellStyle.getBorderBottom();
             case LEFT: return cellStyle.getBorderLeft();
             default: throw new IllegalArgumentException("Invalid side: "+index);
         }
    }

    short getBorderColor(final HSSFCellStyle cellStyle, final int index) {
         switch(index) {
             case TOP: return cellStyle.getTopBorderColor();
             case RIGHT: return cellStyle.getRightBorderColor();
             case BOTTOM: return cellStyle.getBottomBorderColor();
             case LEFT: return cellStyle.getLeftBorderColor();
             default: throw new IllegalArgumentException("Invalid side: "+index);
         }
    }

    private HSSFFont getFont(final HSSFCellStyle cellStyle) {
        return getExcelWorkbook().getFontAt(cellStyle.getFontIndex());
    }

    private void checkFormat(final String property, final String expected, final String actual) {
        if (expected == null || verifyStrings(expected, actual)) {
            return;
        }
        throw new StepFailedException("Wrong cell style found for property '"+property+"' in cell "+
                getCellReferenceStr(), expected, actual, this);
    }

    private String[] separateSides(final String border) {
        final String[] result = new String[SIDES.length];
        if (border != null) {
            final StringTokenizer st = new StringTokenizer(border, ";");
            while(st.hasMoreTokens()) {
                final String s = st.nextToken();
                final int colon = s.indexOf(':');
                if (colon != -1) {
                    final String sides = s.substring(0,colon);
                    final String value = s.substring(colon + 1).trim();
                    final StringTokenizer st2 = new StringTokenizer(sides, ",");
                    while(st2.hasMoreTokens()) {
                        final String side = st2.nextToken().trim();
                        int i = 0;
                        while (i < SIDES.length) {
                            if (side.equals(SIDES[i])) {
                                result[i] = value;
                                 break;
                            }
                            i++;
                        }
                        if (i == SIDES.length) {
                            throw new StepExecutionException("Border side '" + side + "' unknown.  Specify one of top, down, left right.", this);
                        }
                    }
                } else {
                    for(int i = 0; i < SIDES.length; i++) {
                        result[i] = s;
                    }
                }
            }
        }
        return result;
    }

}