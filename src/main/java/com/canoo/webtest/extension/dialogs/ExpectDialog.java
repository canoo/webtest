// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.PromptHandler;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Saves away a JavaScript dialog response in preparation for a subsequent automatic user response.<p>
 *
 * @author Paul King, ASERT
 * @webtest.step category="Extension"
 * name="expectDialog"
 * alias="prepareDialogResponse"
 * description="Provides the ability to set expectations in relation to user responses to <key>javascript</key> Dialog boxes (Alert, Confirm and Prompt dialogs)."
 */
public class ExpectDialog extends Step
{
    private String fSaveProperty;
    private String fSavePropertyType;
    private String fResponse;
    private String fDialogType;
    private static final Logger LOG = Logger.getLogger(ExpectDialog.class);

    /**
     * The name of the property used to save dialog text
     *
     * @param value The new saveProperty value
     * @webtest.parameter required="no"
     * description="The name of the property in which to store the dialog text for later checking with \"verifyProperty\"."
     */
    public void setSaveProperty(final String value) {
        fSaveProperty = value;
    }

    public String getSaveProperty() {
        return fSaveProperty;
    }

    /**
     * The type of the property used to save dialog text (ant or dynamic)
     *
     * @param value The new savePropertyType value
     * @webtest.parameter required="no"
     * description="The type of the property in which to store the dialog text for later checking. Either \"ant\" or \"dynamic\"."
     * default="the \"defaultPropertyType\" as specified in the \"config\" element is used."
     */
    public void setSavePropertyType(final String value) {
        fSavePropertyType = value;
    }

    public String getSavePropertyType() {
        return fSavePropertyType;
    }

    /**
     * The type of JavaScript dialog (alert, confirm or prompt)
     *
     * @param value The new dialogType value
     * @webtest.parameter required="no"
     * default="\"alert\""
     * description="One of \"alert\", \"confirm\" or \"prompt\"."
     */
    public void setDialogType(final String value) {
        fDialogType = value;
    }

    public String getDialogType() {
        return fDialogType;
    }

    /**
     * The response value returned to the JavaScript
     *
     * @param value The new response value
     * @webtest.parameter required="no"
     * description="simulate user response: ignored for alerts, converted to boolean for confirms - \"OK\" (true) or \"Cancel\" (false), contains typed text for prompts."
     * default="true"
     */
    public void setResponse(final String value) {
        fResponse = value;
    }

    public String getResponse() {
        return fResponse;
    }

    public void doExecute() {
        final Context context = getContext();
        final WebClient wc = context.getWebClient();

        final AbstractDialogStep dialogStep;
        if ("confirm".equalsIgnoreCase(getDialogType())) {
            dialogStep = new ConfirmDialogStep("true".equals(getResponse()), null, null, getSaveProperty(), getSavePropertyType());
        }
        else if ("prompt".equalsIgnoreCase(getDialogType())) {
            dialogStep = new PromptDialogStep(getResponse(), null, null, getSaveProperty(), getSavePropertyType());
        }
        else { // alert is default
            dialogStep = new AlertDialogStep(null, null, getSaveProperty(), getSavePropertyType());
        }
        DialogHelper.addExpectedDialog(context, dialogStep);
        final Object handler = new ExpectDialogs.CheckingDialogHandler(context, this);

        wc.setAlertHandler((AlertHandler) handler);
        wc.setConfirmHandler((ConfirmHandler) handler);
        wc.setPromptHandler((PromptHandler) handler);
        LOG.debug("Dialog expectation saved - now expect "
                + DialogHelper.getExpectedDialogsCount(context) + " dialog(s).");

	}

}
