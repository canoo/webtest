// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test class for {@link ExpectDialog}.<p>
 *
 * @author Paul King, ASERT
 */
public class ExpectDialogTest extends BaseStepTestCase
{
    private ExpectDialog fStep;

	protected Step createStep() {
        return new ExpectDialog();
    }

	protected void setUp() throws Exception
	{
		super.setUp();
		fStep = (ExpectDialog) getStep();
	}

    public void testStoresParams() throws Exception {
        fStep.setDialogType("prompt");
        fStep.setResponse("response");
        fStep.setSaveProperty("someProp");
        fStep.setSavePropertyType(Step.PROPERTY_TYPE_DYNAMIC);
        executeStep(fStep);
        assertEquals(1, DialogHelper.getExpectedDialogsCount(getContext()));
        final AbstractDialogStep dialogStep = DialogHelper.getNextExpectedDialog(getContext());
        assertEquals(PromptDialogStep.class, dialogStep.getClass());
        assertEquals("response", dialogStep.getResponse());
        assertEquals("someProp", dialogStep.getSaveProperty());
        assertEquals(Step.PROPERTY_TYPE_DYNAMIC, dialogStep.getSavePropertyType());
    }

//    public void testHtmlUnitAdapter() {
//        ExpectDialog.HtmlUnitAdapter hua = new ExpectDialog.HtmlUnitAdapter();
//        hua.handleAlert(null, null);
//        hua.handleConfirm(null, null);
//        hua.handlePrompt(null, null);
//    }
}
