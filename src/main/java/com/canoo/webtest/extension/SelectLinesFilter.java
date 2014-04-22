// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.canoo.webtest.steps.AbstractFilter;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Removes or keeps lines based on start and stop regular expressions.<p>
 *
 * @author Paul King
 * @webtest.step category="Filter"
 * name="selectLinesFilter"
 * description="Removes or keeps lines starting at a <em>start</em> <key>regex</key> until a <em>stop</em> <key>regex</key>. For use with steps which take a <em>ContentFilter</em>."
 */
public class SelectLinesFilter extends AbstractFilter
{
    private static final String LS = System.getProperty("line.separator");
    private static final String LINE_STR = "(^.*$)";

    private String fStartRegex;
    private String fIncludeStart;
    private String fStopRegex;
    private String fIncludeStop;
    private String fRemove;
    private String fRepeat;
    private static final Pattern LINE_PATTERN = Pattern.compile(LINE_STR, Pattern.MULTILINE);

    /**
     * @param value
     * @webtest.parameter
     *   required="yes/no"
     *   description="The regex to start selecting from. One of 'startRegex' or 'stopRegex' must be set."
     */
    public void setStartRegex(final String value) {
        fStartRegex = value;
    }

    public String getStartRegex() {
        return fStartRegex;
    }

    /**
     * @param flag
     * @webtest.parameter
     *   required="no"
     *   default="true"
     *   description="Whether to include the line the start regex was found on."
     */
    public void setIncludeStart(final String flag) {
        fIncludeStart = flag;
    }

    public String getIncludeStart() {
        return fIncludeStart;
    }

    /**
     * @param value
     * @webtest.parameter
     *   required="yes/no"
     *   description="The regex to stop selecting from. One of 'startRegex' or 'stopRegex' must be set."
     */
    public void setStopRegex(final String value) {
        fStopRegex = value;
    }

    public String getStopRegex() {
        return fStopRegex;
    }

    /**
     * @param flag
     * @webtest.parameter
     *   required="no"
     *   default="true"
     *   description="Whether to include the line the stop regex was found on."
     */
    public void setIncludeStop(final String flag) {
        fIncludeStop = flag;
    }

    public String getIncludeStop() {
        return fIncludeStop;
    }

    /**
     * @param flag
     * @webtest.parameter
     *   required="no"
     *   default="false"
     *   description="Indicates that selected lines will be removed (true) or kept (false)."
     */
    public void setRemove(final String flag) {
        fRemove = flag;
    }

    public String getRemove() {
        return fRemove;
    }

    /**
     * @param flag
     * @webtest.parameter
     *   required="no"
     *   default="false"
     *   description="Indicates that the first (false) or all (true) lines between start and stop <key>regex</key> patterns will be selected. Only relevant if the patterns occur multiple times throughout the response."
     */
    public void setRepeat(final String flag) {
        fRepeat = flag;
    }

    public String getRepeat() {
        return fRepeat;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        paramCheck(getStartRegex() == null && getStopRegex() == null, "One of 'startRegex' or 'stopRegex' must be set!");
        nullResponseCheck();
    }

    public void doExecute() throws Exception {
        final WebResponse webResponse = getContext().getCurrentResponse().getWebResponse();
        final String orig = webResponse.getContentAsString();
        final String origType = webResponse.getContentType();
        final boolean remove = ConversionUtil.convertToBoolean(getRemove(), false);
        final boolean repeat = ConversionUtil.convertToBoolean(getRepeat(), false);
        final boolean includeStart = ConversionUtil.convertToBoolean(getIncludeStart(), true);
        final boolean includeStop = ConversionUtil.convertToBoolean(getIncludeStop(), true);
        final Matcher matcher = LINE_PATTERN.matcher(orig);
        final StringBuffer buf = new StringBuffer();
        boolean selecting = StringUtils.isEmpty(getStartRegex());
        boolean done = false;
        while (matcher.find()) {
            boolean adding = false;
            final String line = matcher.group(0);
            if (selecting && includeStop) {
                adding = true;
            }
            if (selecting && !done && line.matches(getStopRegex())) {
                selecting = false;
                if (!repeat) {
                    done = true;
                }
            }
            if (!selecting && !done && line.matches(getStartRegex())) {
                selecting = true;
                if (includeStart) {
                    adding = true;
                }
            } else if (selecting) {
                adding = true;
            }
            if (adding != remove) {
                buf.append(line).append(LS);
            }
        }
        defineAsCurrentResponse(buf.toString(), origType);
    }
}
