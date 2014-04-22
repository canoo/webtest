// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.steps.AbstractFilter;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Replaces all occurrences of a regular expression with some constant data.<p>
 *
 * @author Paul King
 * @webtest.step category="Filter"
 * name="replaceFilter"
 * description="Replaces part of a response with some constant data. For use with steps which take a <em>ContentFilter</em>."
 */
public class ReplaceContentFilter extends AbstractFilter
{
    private String fRegex;
    private String fReplacement;

    /**
     * @param value
     * @webtest.parameter required="yes"
     * description="The regex matching content to replace."
     */
    public void setRegex(final String value) {
        fRegex = value;
    }

    public String getRegex() {
        return fRegex;
    }

    /**
     * @param value
     * @webtest.parameter required="no" default="[REMOVED]"
     * description="The regex replacement string."
     */
    public void setReplacement(final String value) {
        fReplacement = value;
    }

    public String getReplacement() {
        return fReplacement;
    }

    public void doExecute() throws Exception {
        final WebResponse response = getContext().getCurrentResponse().getWebResponse();
        final String orig = response.getContentAsString();
        final String origType = response.getContentType();
        final String resultStr = orig.replaceAll(getRegex(), getReplacement() == null ? "[REMOVED]" : getReplacement());
        defineAsCurrentResponse(resultStr, origType);
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getRegex(), "regex");
        nullResponseCheck();
    }
}
