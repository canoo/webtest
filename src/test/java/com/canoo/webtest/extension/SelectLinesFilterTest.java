// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.AbstractFilter;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link MatchLinesFilter}.
 *
 * @author Paul King
 */
public class SelectLinesFilterTest extends BaseFilterTestCase
{
    private static final String LS = System.getProperty("line.separator");
    private static final String SOURCE = "The quick" + LS + "brown fox" + LS + "jumped over the" + LS + "lazy dog" + LS;
    private static final String START_REGEX_1 = ".*quick.*";
    private static final String STOP_REGEX_1 = ".*over.*";
    private static final String START_REGEX_2 = "[^e]*e.*";
    private static final String STOP_REGEX_2 = "[^ ]* .*";
    private static final String EXPECTED_1A = "The quick" + LS + "brown fox" + LS;
    private static final String EXPECTED_1B = "brown fox" + LS + "jumped over the" + LS;
    private static final String EXPECTED_1C = "The quick" + LS + "lazy dog" + LS;
    private static final String EXPECTED_1D = "The quick" + LS;
    private static final String EXPECTED_1E = "The quick" + LS + "jumped over the" + LS;
    private SelectLinesFilter fFilter;

    protected void setUp() throws Exception
    {
        super.setUp();
        fFilter = (SelectLinesFilter) getStep();
        fFilter.setStartRegex(START_REGEX_1);
        fFilter.setStopRegex(STOP_REGEX_1);
        fFilter.setIncludeStart("true");
        fFilter.setIncludeStop("false");
        checkFilter(EXPECTED_1A, SOURCE);
        fFilter.setIncludeStart("false");
        fFilter.setIncludeStop("true");
        checkFilter(EXPECTED_1B, SOURCE);
        fFilter.setRemove("true");
        checkFilter(EXPECTED_1C, SOURCE);
        fFilter.setStartRegex(null);
        fFilter.setIncludeStart(null);
        fFilter.setRemove(null);
        fFilter.setIncludeStop("false");
        checkFilter(EXPECTED_1A, SOURCE);
        fFilter.setStartRegex(START_REGEX_2);
        fFilter.setStopRegex(STOP_REGEX_2);
        checkFilter(EXPECTED_1D, SOURCE);
        fFilter.setRepeat("true");
        checkFilter(EXPECTED_1E, SOURCE);
    }

    public void testFailsIfInsufficientParams() {
        final Step step = createAndConfigureStep();
        final String msg = ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Throwable {
                step.execute();
            }
        });
        assertEquals("One of 'startRegex' or 'stopRegex' must be set!", msg);
    }

    public void testFailsIfNoResponse() {
        fFilter.setStartRegex("dummy");
        checkFailsIfNoResponse();
    }

    protected AbstractFilter getFilter() {
        return fFilter;
    }

    protected Step createStep() {
        return new SelectLinesFilter();
    }
}
