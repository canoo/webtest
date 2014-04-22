// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepFailedException;

/**
 * Verifies the title of an HTML document.
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="verifyTitle"
 *   alias="verifytitle"
 *   description="This step verifies whether the text enclosed by the <key>HTML</key> title tag (<TITLE> ... </TITLE>) matches some specified text. The specified text can represent a <key>regex</key>."
 */
public class VerifyTitle extends AbstractVerifyTextStep
{
    /**
     * Does the verification work.
     * @see com.canoo.webtest.steps.Step#doExecute()
     */
    public void doExecute() throws Exception {
        final String strTitle = getContext().getCurrentHtmlResponse(this).getTitleText();

        if (!verifyText(strTitle)) {
            throw new StepFailedException("Wrong document title found!", getText(), strTitle, this);
        }
    }


    /**
    * Called by Ant to set the text nested between opening and closing tags.
    * @param text the text to set
    * @webtest.nested.parameter
    *    required="no"
    *    description="Alternative way to set the 'text' attribute."
    */
   public void addText(final String text) {
	   if (getText() == null)
		   setText(getProject().replaceProperties(text));
	}
}
