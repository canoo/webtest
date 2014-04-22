// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.AbstractFilter;
import com.canoo.webtest.steps.BaseStepTestCase;

/**
 * Helper class for filter tests.
 *
 * @author Paul King
 */
public abstract class BaseFilterTestCase extends BaseStepTestCase
{
    protected abstract AbstractFilter getFilter();

    protected void checkFailsIfNoParam(final String param) {
        assertStepRejectsNullParam(param, new TestBlock()
        {
            public void call() throws Throwable {
                getFilter().execute();
            }
        });
    }

    protected void checkFailsIfNoResponse() {
    	getContext().fakeLastResponse(null);
        final TestBlock testBlock = new TestBlock()
        {
            public void call() throws Throwable {
                getFilter().execute();
            }
        };
        final Throwable t = ThrowAssert.assertThrows("currentResponse == null", StepExecutionException.class, testBlock);
        checkResponseMessage(NO_CURRENT_RESPONSE, t.getMessage());
    }

    protected void checkFilter(final String expected, final String target) throws Exception {
        getContext().setDefaultResponse(target, "text/plain");
        getFilter().doExecute();
        assertEquals(expected, getContext().getCurrentResponse().getWebResponse().getContentAsString());
    }
}
