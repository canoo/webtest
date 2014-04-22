// Copyright © 2005-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.steps.AbstractFilter;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link ReplaceContentFilter}.
 *
 * @author Paul King
 */
public class ReplaceContentFilterTest extends BaseFilterTestCase
{
    private static final String SOURCE_1 = "The time is 12:30PM.";
    private static final String SOURCE_2 = "Time for Lunch.";
    private static final String SOURCE_3 = "The time is 12:30PM. Time for Lunch.";
    private static final String REGEX_1 = "[0-9:]+[AP]M";
    private static final String REGEX_2 = "(?<=Time for )\\w+(?=\\.)";
    private static final String REGEX_3 = "(The time is )[0-9:]+[AP]M(. Time for )Lunch(\\.)";
    private static final String REPLACEMENT_3 = "$1[TIME_REMOVED]$2[MEAL_REMOVED]$3";
    private static final String EXPECTED_1 = "The time is [REMOVED].";
    private static final String EXPECTED_2 = "Time for [REMOVED].";
    private static final String EXPECTED_3 = "The time is [TIME_REMOVED]. Time for [MEAL_REMOVED].";
    private ReplaceContentFilter fFilter;

    protected void setUp() throws Exception
    {
        super.setUp();
        fFilter = (ReplaceContentFilter) getStep();
    }

    public void testRegexNoReplacement() throws Exception {
        fFilter.setRegex(REGEX_1);
        checkFilter(EXPECTED_1, SOURCE_1);
        fFilter.setRegex(REGEX_2);
        checkFilter(EXPECTED_2, SOURCE_2);
    }

    public void testRegexWithReplacement() throws Exception {
        fFilter.setRegex(REGEX_3);
        fFilter.setReplacement(REPLACEMENT_3);
        checkFilter(EXPECTED_3, SOURCE_3);
    }

    protected AbstractFilter getFilter() {
        return fFilter;
    }

    public void testFailsIfNoRegex() {
        checkFailsIfNoParam("regex");
    }

    public void testFailsIfNoResponse() {
        fFilter.setRegex("dummy");
        checkFailsIfNoResponse();
    }

    protected Step createStep() {
        return new ReplaceContentFilter();
    }
}
