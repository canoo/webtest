// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Test cases for {@link ClickLink}.
 *
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King, ASERT
 * @author Denis N. Antonioli
 */
public class ClickLinkTest extends BaseStepTestCase
{
    private static final String NBSP = "\u00a0";
    private ClickLink fStep;

	protected Step createStep() {
        return new ClickLink();
    }
	
	protected void setUp() throws Exception
	{
		super.setUp();
		fStep = (ClickLink) getStep();
	}

    public static void testNbsp() {
        ClickLink step = new ClickLink();

        step.setLabel("a" + NBSP + "b");
        // TODO migration: why should this nbsp be transformed to a normal space?
        // assertEquals("a b", step.getLabel());
    }

    // <clickLink htmlId="notExisting" />
    public void testClickNonExistingLink() throws Exception {
        final String htmlContent = wrapContent("No link");
        getContext().setDefaultResponse(htmlContent);
        fStep.setHtmlId("notExisting");
        final HtmlPage page = getDummyPage(htmlContent);
        assertNull(fStep.findClickableElement(page));
    }

    // <clickLink htmlId="dummyLink" />
    public void testLocateWebLinkById() throws Exception {
        final String htmlContent = wrapContent("<a id='dummyLink' href='dummy.html'>dummy</a>");
        fStep.setHtmlId("dummyLink");
        final HtmlPage page = getDummyPage(htmlContent);
        assertNotNull(fStep.findClickableElement(page));
    }

    // <clickLink htmlId="idNotAnchor" />
    public void testLocateNonLinkFails() throws Exception {
        final String htmlContent = wrapContent("<span id='idNotAnchor'>dummy</span>");
        final HtmlPage page = getDummyPage(htmlContent);
        fStep.setHtmlId("idNotAnchor");
        String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                fStep.findClickableElement(page);
            }
        });
        assertTrue(msg.indexOf("not a link") != -1);
    }

    // <clickLink label="dummy" />
    public void testLocateWebLinkByLabel() throws Exception {
        final String htmlContent = wrapContent("<a id='dummyLink' href='dummy.html'>dummy</a>");
        
        final HtmlPage page = getDummyPage(htmlContent);
        fStep.setLabel("dummy");
        assertNotNull(fStep.findClickableElement(page));
    }

    // <clickLink label="dummy" href="foo"/>
    public void testLocateWebLinkByLabelAndHref() throws Exception {
        final String htmlContent = wrapContent("<a href='foo.html'>dummy</a>, <a href='foo2.html'>foo</a>");
        final HtmlPage page = getDummyPage(htmlContent);

        final ClickLink step = (ClickLink) getStep();

        step.setLabel("dummy");
        step.setHref("foo");
        assertEquals("foo.html", 
        		((HtmlAnchor) step.findClickableElement(page)).getHrefAttribute());
        step.setLabel("foo");
        step.setHref("foo");
        assertEquals("foo2.html", 
        		((HtmlAnchor) step.findClickableElement(page)).getHrefAttribute());
    }

    // <clickLink label="dummyalt" />
    public void testLocateWebLinkByMatchingImageAlt() throws Exception {
        final String htmlContent = wrapContent("<img src='dummy.gif' alt='dummyalt'/>"
                + "<table><tr><td><img src='dummy.gif' alt='dummyalt'/></td></tr></table>"
                + "<a href='dummy.html'><img src='dummy.gif' alt='dummyalt'/></a>");
        final HtmlPage page = getDummyPage(htmlContent);
        fStep.setLabel("dummyalt");
        assertNotNull(fStep.findClickableElement(page));
    }

    // <clickLink href="dummy.html" />
    public void testLocateWebLinkByHref() throws Exception {
        final String htmlContent = wrapContent("<a id='dummyLink' href='dummy.html'>dummy</a>");
        final HtmlPage page = getDummyPage(htmlContent);
        fStep.setHref("dummy.html");
        assertNotNull(fStep.findClickableElement(page));
    }

    // <clickLink href="param=value" />
    public void testLocateWebLinkByHrefParams() throws Exception {
        final String htmlContent = wrapContent("<a id='dummyLink' href='dummy.jsp?param=value&this=that'>dummy</a>");
        fStep.setHref("param=value");
        final HtmlPage page = getDummyPage(htmlContent);
        assertNotNull(fStep.findClickableElement(page));
    }


    // <clickLink xpath="//a[@class = 'foo']" />
    public void testLocateWebLinkByXPath() throws Exception {
        final String htmlContent = wrapContent("<a href='dummy.jsp'>dummy1</a> <a href='dummy.jsp' class='foo'>dummy2</a>");
        fStep.setXpath("//a[@class = 'foo']");
        final HtmlPage page = getDummyPage(htmlContent);
        assertEquals("dummy2", fStep.findClickableElement(page).asText());
    }

    // <clickLink xpath="//a[@class = 'foo']" />
    public void testExecuteXPath() throws Exception {
        final String htmlContent = wrapContent("<a href='dummy.jsp'>dummy1</a> <a href='dummy.jsp' class='foo' onclick='alert(\"foo\")'>dummy2</a>");
        fStep.setXpath("//a[@class = 'foo']");
        final HtmlPage page = getDummyPage(htmlContent);
        final WebClient client = page.getWebClient();
        final List collectedAlerts = new ArrayList();
        final AlertHandler alertHandler = new CollectingAlertHandler(collectedAlerts);
        client.setAlertHandler(alertHandler);
        
        fStep.execute();
        
        final List expectedAlerts = Collections.singletonList("foo");
        assertEquals(expectedAlerts, collectedAlerts);
       
    }

    // <clickLink />
    public void testNoAttributes() throws Exception {
        String msg = ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock()
        {
            public void call() throws Exception {
                fStep.verifyParameters();
            }
        });

        assertEquals("\"htmlId\" or \"xpath\" or \"label\" or \"href\" must be set!", msg);
    }

    /**
     * Tests that an exception is thrown when label and/or href is used combined with htmlid
     */
    public void testVerifyParameterHtmlIdNotCombined() {
        fStep.setHtmlId("idNotAnchor");
        fStep.setLabel("foo");

        TestBlock block = new TestBlock()
        {
            public void call() throws Exception {
                fStep.verifyParameters();
            }
        };

        // with htmlid and label
        ThrowAssert.assertThrows(StepExecutionException.class, block);

        // with htmlid, label and href
        fStep.setHref("foo");
        ThrowAssert.assertThrows(StepExecutionException.class, block);

        // with htmlid and href
        fStep.setLabel(null);
        ThrowAssert.assertThrows(StepExecutionException.class, block);

        // with htmlid and xpath
        fStep.setHref(null);
        fStep.setXpath("//a");
        ThrowAssert.assertThrows(StepExecutionException.class, block);
    }

    /**
     * Tests that an exception is thrown when label and/or href is used combined with xpath
     */
    public void testVerifyParametersXPath() {
        fStep.setXpath("//a");

        TestBlock block = new TestBlock()
        {
            public void call() throws Exception {
                fStep.verifyParameters();
            }
        };

        // with xpath and label
        fStep.setLabel("foo");
        ThrowAssert.assertThrows(StepExecutionException.class, block);

        // with xpath, label and href
        fStep.setHref("foo");
        ThrowAssert.assertThrows(StepExecutionException.class, block);

        // with xpath and href
        fStep.setLabel(null);
        ThrowAssert.assertThrows(StepExecutionException.class, block);
    }

    public void testErrorWithXmlPageUnknownLabel() throws Exception {
        fStep.setLabel("foo");
        assertErrorOnExecuteIfCurrentPageIsXml(fStep);
    }

    public void testErrorWithXmlPageInvalidXPath() throws Exception {
        final String htmlContent = wrapContent("<span>dummy</span>");
        final HtmlPage page = getDummyPage(htmlContent);
        fStep.setXpath("//span");
        String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                fStep.findClickableElement(page);
            }
        });
        assertTrue(msg.indexOf("not a link") != -1);
        assertTrue(msg.indexOf("span tag") != -1);
    }

    private static String wrapContent(String content) {
        return "<html><head><title>foo</title></head><body>" + content + "</body></html>";
    }

    public void testNestedText() throws Exception {
    	testNestedTextEquivalent(getStep(), "label");
    }
}
