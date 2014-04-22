// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.self.ContextStub;
import junit.framework.TestCase;

/**
 * Tests for {@link DialogHelper}.
 * @author Paul King
 */
public class DialogHelperTest extends TestCase
{
    private final Context fContext = new ContextStub();

    public void testAddDialogTest() {
        assertEquals(0, DialogHelper.getExpectedDialogsCount(fContext));
        DialogHelper.addExpectedDialog(fContext, new AlertDialogStep(null, null, null, null));
        assertEquals(1, DialogHelper.getExpectedDialogsCount(fContext));
    }

    public void testGetDialogsTest() {
        assertNull("should be no dialogs initially", DialogHelper.getNextExpectedDialog(fContext));
        DialogHelper.addExpectedDialog(fContext, new AlertDialogStep(null, null, null, null));
        assertEquals("adding a dialog response should increase count", 1, DialogHelper.getExpectedDialogsCount(fContext));
        DialogHelper.getNextExpectedDialog(fContext);
        assertEquals("using a dialog response should decrease count", 0, DialogHelper.getExpectedDialogsCount(fContext));
    }

}
