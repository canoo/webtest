// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;


import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;


/**
 * Test class for {@link com.canoo.webtest.extension.dialogs.VerifyNoDialogs}.<p>
 *
 * @author Paul King, ASERT
 */
public class VerifyNoDialogsTest extends BaseStepTestCase
{
    protected Step createStep() {
        return new VerifyNoDialogs();
    }

    public void testSucceedsWhenNoDialogsPresent() throws Exception {
        executeStep(getStep());
    }

    public void testFailsWhenDialogsPresent() {
        final AbstractDialogStep dialogStep = new AlertDialogStep(null, null, null, null);
        DialogHelper.addExpectedDialog(getContext(), dialogStep);
        final String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(getStep());
            }
        });
        assertEquals("Dialogs found but none expected!", msg);
    }

}
