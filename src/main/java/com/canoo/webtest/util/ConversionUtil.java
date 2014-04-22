// Released under the Canoo Webtest license.
package com.canoo.webtest.util;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.steps.Step;
import org.apache.commons.lang.StringUtils;

/**
 * Date: May 4, 2005
 * @author paulk
 */
public class ConversionUtil {
    /**
     * Convert a string value to a boolean according to ANT defintion of true. If the value is null or empty, return the
     * specified default value.
     *
     * @return parsed value. If value is null or empty, return default value.
     */
    public static boolean convertToBoolean(String value, boolean defaultValue) {
        if (StringUtils.isNotEmpty(value)) { // isNotBlank?
            return "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)
                    || "on".equalsIgnoreCase(value);
        }
        return defaultValue;
    }

    /**
     * Convert a string value to an integer. If the value is null, return the specified default value.
     *
     * @return parsed value. If value is null return default value.
     * @throws NumberFormatException
     */
    public static int convertToInt(String value, int defaultValue) {
        if (value != null) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    /**
     * Convert a string value to a long. If the value is null, return the specified default value.
     *
     * @return parsed value. If value is null return default value.
     * @throws NumberFormatException
     */
    public static long convertToLong(String value, long defaultValue) {
        if (value != null) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }

    /**
     * Convert a string value to a float. If the value is null, return the specified default value.
     *
     * @return parsed value. If value is null return default value.
     * @throws NumberFormatException
     */
    public static float convertToFloat(String value, float defaultValue) {
        if (value != null) {
            return Float.parseFloat(value);
        }
        return defaultValue;
    }

    /**
     * Convert a string value to a double. If the value is null, return the specified default value.
     *
     * @return parsed value. If value is null return default value.
     * @throws NumberFormatException
     */
    public static double convertToDouble(String value, double defaultValue) {
        if (value != null) {
            return Double.parseDouble(value);
        }
        return defaultValue;
    }

    /**
     * Parses a value as integer.
     * @param property the name of the property (used in the exception's message if parse fails)
     * @param value the value to parse
     * @return the parsed int
     * @throws com.canoo.webtest.engine.StepExecutionException if the value can't be parsed as int
     */
    public static int convertToIntOrReject(final String property, final String value, Step step) throws StepExecutionException {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            throw new StepExecutionException(
                    "Can't parse \"" + value + "\" as an int for property \"" + property + "\"",
                    step);
        }
    }
}
