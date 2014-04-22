// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.util;

import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.net.URL;

/**
 * Unit tests for form utilities.
 *
 * @author Paul King
 */
public class FormUtilTest extends TestCase
{
    public void testHasTextField() throws Exception {
        final String htmlContent = "<html><head><title>foo</title></head>" + "<body>"
                + "<form name='testForm'>"
				+ "<input name='myInput1'>"
				+ "<input name='myInput2' type='TEXT'>"
                + "<input name='myInput3' type='password'>"
                + "<input name='myInput4' type='hidden'>"
				+ "</form>"
                + "No access</body></html>";

        final WebClient webClient = new WebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        webConnection.setDefaultResponse(htmlContent);
        webClient.setWebConnection(webConnection);
        final HtmlPage page = (HtmlPage) webClient.getPage(new URL("http://toto.to"));
        final HtmlForm form = page.getFormByName("testForm");
        Assert.assertTrue(FormUtil.hasTextField(form, "myInput1"));
        Assert.assertTrue(FormUtil.hasTextField(form, "myInput2"));
        Assert.assertTrue(FormUtil.hasTextField(form, "myInput3"));
        Assert.assertFalse(FormUtil.hasTextField(form, "myInput4"));
    }
}
