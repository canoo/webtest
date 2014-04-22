// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.io.IOException;

import com.canoo.webtest.steps.Step;

/**
 * @author Paul King
 */
public class PdfVerifyBookmarkStepTest extends AbstractBaseVerifyPdfTestCase
{
    protected Step createStep() {
        return new PdfVerifyBookmarkStep();
    }

    public void testAttributes() throws IOException {
        assertStepRejectsNullParam("name", getExecuteStepTestBlock());
    }

    public void testNoCurrentResponse() throws IOException {
    	PdfVerifyBookmarkStep step = (PdfVerifyBookmarkStep) getStep();
    	step.setName("dummy");
        assertStepRejectsNullResponse(getStep());
    }

}
