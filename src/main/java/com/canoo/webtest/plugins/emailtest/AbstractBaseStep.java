// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;

/**
 * Abstract class used by email steps.
 *
 * @author Paul King
 */
public abstract class AbstractBaseStep extends Step {
    private EmailHelper fHelper;

    protected EmailHelper getHelper() {
        return fHelper;
    }

    protected void setHelper(final EmailHelper helper) {
        fHelper = helper;
    }

    protected AbstractBaseStep() {
        setHelper(new EmailHelper());
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        emailContextCheck();
    }

    private void emailContextCheck() {
        if (!getContext().containsKey("EmailConfigInfo")) {
            throw new StepFailedException("No previous emailSetConfig!", this);
        }
    }
}
