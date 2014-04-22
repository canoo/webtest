// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.steps.AbstractFilter;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link MatchLinesFilter}.
 *
 * @author Paul King
 */
public class MatchLinesFilterTest extends BaseFilterTestCase
{
    private static final String LS = System.getProperty("line.separator");
    private static final String SOURCE_1 = "The quick brown" + LS + "fox jumped over" + LS + "the lazy dog" + LS;
    private static final String REGEX_1 = ".*\\W[df]o[gx]\\W?.*|.*\\W[df]o[gx]|[df]o[gx]\\W?.*";
    private static final String EXPECTED_1A = "fox jumped over" + LS + "the lazy dog" + LS;
    private static final String EXPECTED_1B = "The quick brown" + LS;

    private MatchLinesFilter fFilter;

    protected AbstractFilter getFilter() {
        return fFilter;
    }

    public void testFilter() throws Exception {
        fFilter.setRegex(REGEX_1);
        checkFilter(EXPECTED_1A, SOURCE_1);
        fFilter.setRemove("true");
        checkFilter(EXPECTED_1B, SOURCE_1);
    }

    protected void setUp() throws Exception {
        super.setUp();
        fFilter = (MatchLinesFilter) getStep();
    }

    public void testFailsIfNoRegex() {
        checkFailsIfNoParam("regex");
    }

    public void testFailsIfNoResponse() {
        fFilter.setRegex("dummy");
        checkFailsIfNoResponse();
    }

    protected Step createStep() {
        return new MatchLinesFilter();
    }
}
