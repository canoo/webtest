// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;

/**
 * Util class for converting a color index to a string in an Excel Spreadsheet.<p>
 *
 * @author Rob Nielsen
 */
public class ExcelColorUtils {
    private static final int AUTOMATIC_COLOR = 64;
    private static final Map<String, String> STANDARD_COLORS = new HashMap<String, String>();

    static {
        STANDARD_COLORS.put("#33cccc", "aqua");
        STANDARD_COLORS.put("#000000", "black");
        STANDARD_COLORS.put("#0000ff", "blue");
        STANDARD_COLORS.put("#666699", "blue gray");
        STANDARD_COLORS.put("#00ff00", "bright green");
        STANDARD_COLORS.put("#993300", "brown");
        STANDARD_COLORS.put("#ff8080", "coral");
        STANDARD_COLORS.put("#000080", "dark blue");
        STANDARD_COLORS.put("#003300", "dark green");
        STANDARD_COLORS.put("#660066", "dark purple");
        STANDARD_COLORS.put("#800000", "dark red");
        STANDARD_COLORS.put("#003366", "dark teal");
        STANDARD_COLORS.put("#808000", "dark yellow");
        STANDARD_COLORS.put("#ffcc00", "gold");
        STANDARD_COLORS.put("#008000", "green");
        STANDARD_COLORS.put("#c0c0c0", "25% gray");
        STANDARD_COLORS.put("#969696", "40% gray");
        STANDARD_COLORS.put("#808080", "50% gray");
        STANDARD_COLORS.put("#333333", "80% gray");
        STANDARD_COLORS.put("#ccccff", "ice blue");
        STANDARD_COLORS.put("#333399", "indigo");
        STANDARD_COLORS.put("#ffffcc", "ivory");
        STANDARD_COLORS.put("#cc99ff", "lavender");
        STANDARD_COLORS.put("#3366ff", "light blue");
        STANDARD_COLORS.put("#ccffcc", "light green");
        STANDARD_COLORS.put("#ff9900", "light orange");
        STANDARD_COLORS.put("#ccffff", "light turquoise");
        STANDARD_COLORS.put("#ffff99", "light yellow");
        STANDARD_COLORS.put("#99cc00", "lime");
        STANDARD_COLORS.put("#993366", "maroon");
        STANDARD_COLORS.put("#0066cc", "ocean blue");
        STANDARD_COLORS.put("#333300", "olive green");
        STANDARD_COLORS.put("#ff6600", "orange");
        STANDARD_COLORS.put("#99ccff", "pale blue");
        STANDARD_COLORS.put("#9999ff", "periwinkle");
        STANDARD_COLORS.put("#ff00ff", "pink");
        STANDARD_COLORS.put("#993366", "plum");
        STANDARD_COLORS.put("#ff0000", "red");
        STANDARD_COLORS.put("#ff99cc", "rose");
        STANDARD_COLORS.put("#339966", "sea green");
        STANDARD_COLORS.put("#00ccff", "sky blue");
        STANDARD_COLORS.put("#ffcc99", "tan");
        STANDARD_COLORS.put("#008080", "teal");
        STANDARD_COLORS.put("#00ffff", "turquoise");
        STANDARD_COLORS.put("#800080", "violet");
        STANDARD_COLORS.put("#ffffff", "white");
        STANDARD_COLORS.put("#ffff00", "yellow");
    }

    public static String getColorName(final AbstractExcelStep step, final short borderColor) {
        if (borderColor == AUTOMATIC_COLOR) {
            return "auto";
        }
        final HSSFColor color = step.getExcelWorkbook().getCustomPalette().getColor(borderColor);
        if (color == null) {
            return "none";
        }
        final short[] triplet = color.getTriplet();
        final String colorString = "#"+toHex(triplet[0]) +toHex(triplet[1])+toHex(triplet[2]);
        return lookupStandardColorName(colorString);
    }

    public static String lookupStandardColorName(final String colorString) {
        final String colorName = (String) STANDARD_COLORS.get(colorString);
        if (colorName != null) {
            return colorName;
        }
        return colorString;
    }

    private static String toHex(final short value) {
        String ret = Integer.toHexString(value & 0xFF);
        if (ret.length() == 1) {
            ret = "0" + ret;
        }
        return ret;
    }
}
