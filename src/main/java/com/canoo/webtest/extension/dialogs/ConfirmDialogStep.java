// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.engine.Context;
import org.apache.log4j.Logger;

/**
 * Stores away properties related to a JavaScript dialog response in preparation for subsequent click.<p>
 *
 * @author Paul King, ASERT
 * @webtest.step category="Extension"
 * name="confirm"
 * description="Provides the ability to set expectations in relation to user responses to <key>javascript</key> Confirm Dialog boxes. Must be nested within the <stepref name='expectDialogs'/> step."
 */
public class ConfirmDialogStep extends AbstractDialogStep
{
    private static final Logger LOG = Logger.getLogger(ConfirmDialogStep.class);

    {
        setOptionalText(true);
    }

    // used when nesting this step
    public ConfirmDialogStep() {
        super();
    }

    // used by prepareDialogResponse legacy step
    public ConfirmDialogStep(final boolean response, final String text, final String regex,
                             final String saveProperty, final String savePropertyType) {
        super(response ? "true" : "false", text, regex, saveProperty, savePropertyType);
    }

    public void doExecute() {
        final Context context = getContext();
        LOG.debug("Dialog expectation saved - Number of expected dialogs now = "
                + DialogHelper.getExpectedDialogsCount(context));

    }
}
