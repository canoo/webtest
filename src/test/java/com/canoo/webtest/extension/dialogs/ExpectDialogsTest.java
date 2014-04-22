// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.control.BaseWrappedStepTestCase;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.engine.StepFailedException;
import org.apache.tools.ant.taskdefs.Echo;

/**
 * Test class for {@link com.canoo.webtest.extension.dialogs.ExpectDialogs}.<p>
 *
 * @author Paul King, ASERT
 */
public class ExpectDialogsTest extends BaseWrappedStepTestCase
{
    protected Step createStep() {
        return new ExpectDialogs();
    }

    public void testNonDialogThrowsException() {
        ThrowAssert.assertThrows(StepFailedException.class, "Not a dialog step", new TestBlock()
        {
            public void call() throws Throwable {
                ((ExpectDialogs)createStep()).addTask(new Echo());
            }
        });
    }
}
