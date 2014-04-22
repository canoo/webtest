// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Unit tests for conversion utilities.
 *
 * @author Paul King
 */
public class ConversionUtilTest extends TestCase
{
    public void testConversionHelpers() {
        int fInt = 3;
        long fLong = 30L;
        float fFloat = 300.0f;
        double fDouble = 3000.0d;

        // boolean
        Assert.assertEquals(true, ConversionUtil.convertToBoolean("yes", false));
        Assert.assertEquals(false, ConversionUtil.convertToBoolean(null, false));
        // int
        Assert.assertEquals(fInt, ConversionUtil.convertToInt("3", 4));
        Assert.assertEquals(fInt, ConversionUtil.convertToInt(null, fInt));
        // long
        Assert.assertEquals(fLong, ConversionUtil.convertToLong("30", 40));
        Assert.assertEquals(fLong, ConversionUtil.convertToLong(null, fLong));
        // float
        Assert.assertEquals(fFloat, ConversionUtil.convertToFloat("300.0", 400.0f), 0.00001f);
        Assert.assertEquals(fFloat, ConversionUtil.convertToFloat(null, 300.0f), 0.00001f);
        // double
        Assert.assertEquals(fDouble, ConversionUtil.convertToDouble("3000.0", 4000.0d), 0.00001d);
        Assert.assertEquals(fDouble, ConversionUtil.convertToDouble(null, fDouble), 0.00001d);
    }

    public void testExceptionsThrown() {
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Throwable {
                ConversionUtil.convertToIntOrReject(null, "XYZ not an integer", null);
            }
        });
    }
}
