// Copyright (c) 2002-2007 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.verify.AbstractVerifyTextStep;
import org.apache.log4j.Logger;

/**
 * @author Denis N. Antonioli
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step
 *   category="Extension"
 *   name="verifyDocumentURL"
 *   description="This step verifies that the document URL matches the supplied text (potentially using a <key>regex</key>)."
 */
public class VerifyDocumentURL extends AbstractVerifyTextStep {
	private static final Logger LOG = Logger.getLogger(VerifyDocumentURL.class);

	public void doExecute() throws Exception {
        final String url = getContext().getCurrentResponse().getUrl().toExternalForm();
		LOG.info("Response URL is: '" + url + "'");
		if (!verifyText(url)) {
			throw new StepFailedException("The url '" + url + "' didn't match '" + getText() + "'.", this);
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
	    setText(getProject().replaceProperties(text));
	}
}
