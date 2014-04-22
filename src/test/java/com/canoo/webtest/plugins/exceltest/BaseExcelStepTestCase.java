// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Base Test case for Excel steps.<p>
 *
 * @author Rob Nielsen
 */
public abstract class BaseExcelStepTestCase extends BaseStepTestCase {

    public ContextStub createContext() {
        return new ExcelContextStub(ExcelTestResources.DEFAULT_FILE);
    }

    protected static void assertStepRejectsNullParam(final String param, final Step step) {
        assertStepRejectsNullParam(param, new TestBlock()
        {
            public void call() throws Throwable {
                executeStep(step);
            }
        });
    }
}
