// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * Helper to test that the implementation of steps complies to the way
 * ANT instantiatiates its nested elements.
 * <p/>
 * There must be an addXXX method for each Element XXX with
 * exactly one parameter that can be instantiated via dynamic invocation.
 */
public class AntTest extends TestCase {
    public AntTest(String string) {
        super(string);
    }

    public static void nested(Class taskClass, String nested) {
        String methodName = "add" + nested;
        Method[] methods = taskClass.getDeclaredMethods();
        boolean foundMethod = false;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(methodName)) {
                if (foundMethod) {
                    Assert.fail("more than one method" + at(taskClass, methodName));
                }
                foundMethod = true;
                Class[] parms = method.getParameterTypes();
                Assert.assertEquals("parameter count in" + at(taskClass, methodName), 1, parms.length);
                try {
                    parms[0].newInstance();
                } catch (Exception e) {
                    Assert.fail("cannot instantiate parameter in" + at(taskClass, methodName, parms[0].getName()));
                }
            }
        }
        Assert.assertTrue("no such method" + at(taskClass, methodName, "?"), foundMethod);
    }

    private static String at(Class taskClass, String expectedMethodName, String param) {
        return " " + taskClass.getName() + "." + expectedMethodName + "(" + param + ")";
    }

    private static String at(Class taskClass, String expectedMethodName) {
        return at(taskClass, expectedMethodName, "?");
    }

    public void testNoMethodFails() {
        String message = ThrowAssert.assertThrows(AssertionFailedError.class, new TestBlock() {
            public void call() throws Exception {
                nested(Object.class, "NoSuchNestedElement");
            }
        });
        assertEquals("no such method java.lang.Object.addNoSuchNestedElement(?)", message);
    }

    public void addTwoMethods(Object o) {
    }

    public void addTwoMethods(String x) {
    }

    public void testMoreThanOneMethodFails() {
        String message = ThrowAssert.assertThrows(AssertionFailedError.class, new TestBlock() {
            public void call() throws Exception {
                nested(AntTest.class, "TwoMethods");
            }
        });
        assertEquals("more than one method com.canoo.webtest.self.AntTest.addTwoMethods(?)", message);
    }

    public void addNoEmptyConstructor(AntTest x) {
    }

    public void testNoEmptyCtorForParamFails() {
        String message = ThrowAssert.assertThrows(AssertionFailedError.class, new TestBlock() {
            public void call() throws Exception {
                nested(AntTest.class, "NoEmptyConstructor");
            }
        });
        assertEquals("cannot instantiate parameter in com.canoo.webtest.self.AntTest.addNoEmptyConstructor(com.canoo.webtest.self.AntTest)", message);
    }

    public void addTooManyParams(String x, String y) {
    }

    public void testTooManyParamsFails() {
        String message = ThrowAssert.assertThrows(AssertionFailedError.class, new TestBlock() {
            public void call() throws Exception {
                nested(AntTest.class, "TooManyParams");
            }
        });
        assertEquals("parameter count in com.canoo.webtest.self.AntTest.addTooManyParams(?) expected:<1> but was:<2>", message);
    }

    public void testAddMethodDummyCanBeCalled() {
        addNoEmptyConstructor(null);
        addTooManyParams(null, null);
        addTwoMethods(new Object());
        addTwoMethods(new String());
    }

}
