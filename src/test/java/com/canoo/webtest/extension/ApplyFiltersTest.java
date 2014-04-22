// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.control.BaseWrappedStepTestCase;

/**
 * Test for {@link ApplyFilters}.
 *
 * @author Paul King
 */
public class ApplyFiltersTest extends BaseWrappedStepTestCase
{
    protected Step createStep() {
        return new ApplyFilters();
    }
}
