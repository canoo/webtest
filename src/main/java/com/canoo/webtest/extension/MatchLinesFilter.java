// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.canoo.webtest.steps.AbstractFilter;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Removes or keeps lines which match a regular expression.<p>
 *
 * @author Paul King
 * @webtest.step category="Filter"
 * name="matchLinesFilter"
 * description="Removes or keeps lines which match a <key>regex</key>. For use with steps which take a <em>ContentFilter</em>."
 */
public class MatchLinesFilter extends AbstractFilter
{
    private static final String LS = System.getProperty("line.separator");
    private String fRegex;
    private String fRemove;

    /**
     * @param value
     * @webtest.parameter required="yes"
     * description="The regex to match against lines."
     */
    public void setRegex(final String value) {
        fRegex = value;
    }

    public String getRegex() {
        return fRegex;
    }

    /**
     * @param flag
     * @webtest.parameter
     *   required="no"
     *   default="false"
     *   description="Indicates that lines which match will be removed (true) or kept (false)."
     */
    public void setRemove(final String flag) {
        fRemove = flag;
    }

    public String getRemove() {
        return fRemove;
    }

    public void doExecute() throws Exception {
        final WebResponse webResponse = getContext().getCurrentResponse().getWebResponse();
        final String orig = webResponse.getContentAsString();
        final String origType = webResponse.getContentType();
        final boolean remove = ConversionUtil.convertToBoolean(getRemove(), false);
        final String lineStr = "(^.*$)";
        final Pattern linePattern = Pattern.compile(lineStr, Pattern.MULTILINE);
        final Matcher matcher = linePattern.matcher(orig);
        final StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            final String line = matcher.group(0);
            if (line.matches(fRegex) != remove) {
                buf.append(line).append(LS);
            }
        }
        defineAsCurrentResponse(buf.toString(), origType);
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getRegex(), "regex");
        nullResponseCheck();
    }
}
