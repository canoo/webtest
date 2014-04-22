// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import org.apache.log4j.Logger;

/**
 * Check that all expected dialogs have been 'consumed' by JavaScript.<p>
 *
 * @author Paul King
 * @webtest.step category="Extension"
 * name="verifyNoDialogs"
 * alias="verifyNoDialogResponses"
 * description="Used in conjunction with the <stepref name='expectDialog' category='Extension'/> and <stepref name='expectDialogs' category='Extension'/> steps when dealing with <key>javascript</key> Dialog boxes. This step checks that all user responses expected were in fact \"consumed\" by <key>javascript</key>."
 */
public class VerifyNoDialogs extends Step {
    private static final Logger LOG = Logger.getLogger(VerifyNoDialogs.class);

    public void doExecute() {
        final int count = DialogHelper.getExpectedDialogsCount(getContext());
        LOG.debug("Number of expected dialogs = " + count);
        if (count > 0) {
            throw new StepFailedException("Dialogs found but none expected!", this);
        }
    }

}
