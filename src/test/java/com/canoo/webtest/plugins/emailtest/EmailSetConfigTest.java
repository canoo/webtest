// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailSetConfig}.
 *
 * @author Paul King, ASERT
 */
public class EmailSetConfigTest extends BaseStepTestCase
{
    protected Step createStep() {
        return new EmailSetConfig();
    }

    public void testEmailConfigInfoSetOnExecute() throws Exception {
        final Step step = getStep();
        step.doExecute();
        assertNotNull(step.getContext().get("EmailConfigInfo"));
    }

    public void testDefaultType() throws Exception {
        final EmailSetConfig step = (EmailSetConfig) getStep();
        assertEquals("pop3", step.getType());
    }
}
