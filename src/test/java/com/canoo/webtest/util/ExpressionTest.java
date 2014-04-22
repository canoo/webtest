// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

import junit.framework.TestCase;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.self.TestBlock;

/**
 * Test for {@link Expression}.
 *
 * @author Paul King
 */
public class ExpressionTest extends TestCase
{
    public void testCanCreate() {
        assertNotNull(new Expression());
    }

    public void testDefaultValues() {
        final Expression expression = new Expression();
        assertEquals(0.0, expression.evaluate(null), 0.0);
        assertEquals(0.0, expression.evaluate(""), 0.0);
        assertEquals(0.0, expression.evaluate("() "), 0.0);
    }

    public void testNoEvaluator() {
        ThrowAssert.assertThrows(IllegalArgumentException.class, new TestBlock() {
            public void call() throws Throwable {
                Expression.evaluateExpression("x");
            }
        });
    }
}
