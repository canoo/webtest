// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import junit.framework.TestCase;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

/**
 * Response Filter Test Helper.
 *
 * @author Paul King
 */
public class ResponseFilterTestCase extends TestCase
{
    static void checkFilterContentAsHtml(final Step filter, final String source, final String expectedHtml) {
        final ContextStub context = new ContextStub(source);
        WebtestTask.setThreadContext(context);
        filter.setProject(context.getWebtest().getProject());
        filter.execute();
        assertEquals(expectedHtml, context.getCurrentResponse().getWebResponse().getContentAsString());
    }

    static void checkFilterContentAsXml(final Step filter, final String source, final String expectedXml) {
        final String actualXml = getFilterContent(filter, source, "text/xml");
        assertEquals(expectedXml, actualXml);
    }

    static String getFilterContent(final Step filter, final String source, final String contentType) {
        final ContextStub context = new ContextStub(source, contentType);
        WebtestTask.setThreadContext(context);
        filter.setProject(context.getWebtest().getProject());
        filter.execute();
        return context.getCurrentResponse().getWebResponse().getContentAsString();
    }
}
