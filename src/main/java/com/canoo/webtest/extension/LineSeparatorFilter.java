// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.steps.AbstractFilter;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Replaces all occurrences of a regular expression with some constant data.<p>
 *
 * @author Paul King
 * @webtest.step category="Filter"
 * name="lineSeparatorFilter"
 * description="Replaces all <em>line.separator</em> combinations within (part of) a response by a <em>LF</em> to support platform independent content processing. For use with steps which take a <em>ContentFilter</em>."
 */
public class LineSeparatorFilter extends AbstractFilter
{
    private static final String LF = "\n";

    public void doExecute() throws Exception {
        final WebResponse webResponse = getContext().getCurrentResponse().getWebResponse();
        final String content = webResponse.getContentAsString();
        final String origType = webResponse.getContentType();
        final String sep = System.getProperty("line.separator");
        final int sepSize = sep.length();
        final StringBuffer buf = new StringBuffer();
        int last = 0;
        int pos = content.indexOf(sep, last);
        while (pos != -1) {
            buf.append(content.substring(last, pos));
            buf.append(LF);
            last = pos + sepSize;
            pos = content.indexOf(sep, last);
        }
        if (last != content.length()) {
            buf.append(content.substring(last));
        }

        defineAsCurrentResponse(buf.toString(), origType);
    }
}
