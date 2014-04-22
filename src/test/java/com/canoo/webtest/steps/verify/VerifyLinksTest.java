// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * @author Dierk König, Urs-Peter Häss
 * @author Marc Guillemot, Paul King
 */

// todo: dk: test missing for already visited pages -> performance issue
// todo: dk: how about .doc .pdf links? -> stopHunting
// todo: StepFailedException for all Broken Links and processing does not stop

public class VerifyLinksTest extends BaseStepTestCase
{
    private static final String DUMMY_HREF = "any.htm";
    private static final String DUMMY_LABEL = "Homepage";
    private static final HtmlPage DUMMY_PAGE = getResponseForText(samplePageWithAnchor());

    private static final int THROW_NO_EXCEPTION = 0;
    private static final int THROW_MALFORMEDURLEXCEPTION = 1;
    private static final int THROW_IOEXCEPTION = 2;

    protected Step createStep() {
        return new VerifyLinks();
    }

	public static void testFindNoLinksOnEmptyPage() {
        checkLinkCountText("", 0);
        checkLinkCountText(wrapContent(""), 0);
    }

//    public void pendingTestFindNoLinksOnNameAttribute() {
//        checkLinkCountText(wrapContent("<A NAME='bla'>Content</A>"), 0);
//        checkLinkCountText(wrapContent("<A NAME='bla'></A>"), 0);
//    }

    public void testLinksValid() throws Exception {
        checkLinkCountDummyPage("<a href='#me'>dummy</a>" +
                "<a name='me' href='http://www.yahoo.com'>dummy</a>"
                + "<a href='http://www.google.com'>dummy</a>" + "<a href='/trafficlight.html'>dummy</a>", 4);
    }

    public void testLinksBroken() throws Exception {
        checkLinkCountDummyPage("<a href='notExist.jsp'>dummy</a>" + "<a href='ftp://dummy.org'>dummy</a>"
                + "<a href='crazy://badUrl.org'>dummy</a>", 1);
    }

//    public void pendingTestClientSideImageMap() {
//        checkLinkCountText(wrapContent("<MAP NAME=\"xxx\">" +
//                "<AREA shape=\"rect\" Coords=\"572,5,605,50\" HREF=\"bla\">" +
//                "<AREA shape=\"rect\" Coords=\"572,5,605,50\" HREF=\"bla\"></MAP>" +
//                "<IMG USEMAP=\"#XXX\" SRC=\"x.gif\">"), 2);
//    }

    public static void testFindOneLinkOnPage() {
        checkLinkCountText(samplePageWithAnchor(), 1);
        checkLinkCountText(wrapContent(anchor(DUMMY_HREF, "<IMG src=\"bla\">")), 1);
        checkLinkCountText(wrapContent("<A CLASS=\"x\" HREF=\"home.htm\"><IMG source=\"bla\"></A>"), 1);
        checkLinkCountText(wrapContent("<A HREF=\"home.htm\" TARGET=\"FRAME\">Homepage</A>"), 1);
        checkLinkCountText(wrapContent(anchor(DUMMY_HREF, DUMMY_LABEL) + anchor(DUMMY_HREF, DUMMY_LABEL)), 1);
    }

    public static void testIgnoreSpecialLinks() {
        checkLinkCountText(wrapContent(anchor("ftp://ftp.whateveritis.com", DUMMY_LABEL)), 0);
        checkLinkCountText(wrapContent(anchor("mailto:feedback-online@canoo.com", "Feedback")), 0);
        checkLinkCountText(wrapContent(anchor("www.ftp.com", "bla")), 1);
    }

    public static void testRelativeHrefExpansion() throws MalformedURLException {
        Set foundGoodLinks = getGoodLinks(samplePageWithAnchor());
        assertTrue(foundGoodLinks.contains(new URL(ContextStub.SOME_BASE_URL + "/" + DUMMY_HREF)));
    }

    private static class VerifyLinksExposeVisitsStub extends VerifyLinks
    {
        public void checkVisits(WebClient webClient, HtmlPage response) {
            super.checkVisits(webClient, response);
        }
    }

    public void testBrokenLinkDoesNotInterrupt() {
        WebClient client = new WebClient();
        VerifyLinks step = new VerifyLinksExposeVisitsStub();
        HtmlPage page = getResponseForText(wrapContent(anchor("a", "x") + anchor("b", "y")));
        step.checkVisits(client, page);
        assertEquals(2, step.getFailedVisits().size());
    }

    private static Set getGoodLinks(final String text) {
        HtmlPage response = getResponseForText(text);
        return VerifyLinks.getGoodLinks(response);
    }

    private static HtmlPage getResponseForText(final String text) {
        ContextStub context = new ContextStub(text);
        return (HtmlPage) context.getCurrentResponse();
    }

    public static void testSameHRefNotToBeFollowedTwice() {
        WebClient client = new WebClient();
        VerifyLinks step = new VerifyLinksExposeVisitsStub();
        String href = "a";
        HtmlPage page = getResponseForText(wrapContent(anchor(href, "LabelX") + anchor(href, "LabelY")));
        step.checkVisits(client, page);
        assertEquals(1, step.getFailedVisits().size());
    }

    private static class VerifyLinksExposeDepthStub extends VerifyLinks
    {
        private int fCount;

        protected void visit(HtmlPage page, URL ignoreUrl, WebClient client) {
            fCount++;
            followRecursively(page, client);
        }

        protected boolean stopHunting(HtmlPage htmlPage) {
            return false;
        }
    }

    public static void testDepth() {
        assertDepth(0);
        assertDepth(1);
        assertDepth(10);
    }

    private static void assertDepth(int depth) {
        VerifyLinksExposeDepthStub verifyLinks = new VerifyLinksExposeDepthStub();
        verifyLinks.setDepth(Integer.toString(depth));
        verifyLinks.verifyProperties();
        verifyLinks.followRecursively(DUMMY_PAGE, new WebClient());
        assertEquals(depth, verifyLinks.fCount);
    }

    public void testDepthWithBrokenUrl() {
        WebClient client = new WebClient();
        VerifyLinks verifyLinks = (VerifyLinks) getStep();
        HtmlPage page = getResponseForText(wrapContent(anchor("youwillnotfindthis", "x")));
        verifyLinks.setDepth("2");
        verifyLinks.verifyProperties();
        verifyLinks.checkVisits(client, page);
        boolean containsExactlyOneSemicolon =
                verifyLinks.brokenLinksToString().indexOf(";") == verifyLinks.brokenLinksToString().lastIndexOf(";") &&
                verifyLinks.brokenLinksToString().indexOf(";") != -1;
        assertTrue(containsExactlyOneSemicolon);
    }

    private static class VerifyLinksExposeForeignHostStub extends VerifyLinks
    {
        private boolean fIsforeignHost = true;

        protected boolean isForeignHost(URL ignore) {
            return fIsforeignHost;
        }
    }

    public static void testOnSiteOnly() {
        VerifyLinksExposeForeignHostStub verifyLinks = new VerifyLinksExposeForeignHostStub();
        verifyLinks.setOnsiteonly(true);
        verifyLinks.verifyProperties();
        assertTrue("onsiteonly and foreign host: stop", verifyLinks.stopHunting(DUMMY_PAGE));
        verifyLinks.fIsforeignHost = false;
        assertTrue("onsiteonly and same host: don't stop", !verifyLinks.stopHunting(DUMMY_PAGE));
        verifyLinks.fIsforeignHost = true;
        verifyLinks.setOnsiteonly(false);
        verifyLinks.verifyProperties();
        assertTrue("not onsiteonly and foreign host: don't stop", !verifyLinks.stopHunting(DUMMY_PAGE));
        verifyLinks.fIsforeignHost = false;
        assertTrue("not onsiteonly and same host: don't stop", !verifyLinks.stopHunting(DUMMY_PAGE));
    }

    private static void checkLinkCountText(final String text, final int expectedLinkCount) {
        ContextStub context = new ContextStub(text);
        assertEquals(expectedLinkCount, VerifyLinks.getLinkCount((HtmlPage) context.getCurrentResponse()));
    }

    private void checkLinkCountDummyPage(final String content, final int expectedLinkCount) {
        final ContextStub context = new ContextStub();
        final String htmlContent = wrapContent(content);
        final HtmlPage page = getDummyPage(htmlContent);
        context.saveResponseAsCurrent(page);
        assertEquals(expectedLinkCount, VerifyLinks.getLinkCount(page));
    }

    public void testNbLinksInReport() throws Exception {
        final String content = "<a href='page2.html'>dummy</a>"
                + "<a href='page3.html'>dummy</a>"
                + "<a href='/trafficlight.html'>dummy</a>"
        		+ "<a href='https://foo/secure.html'>secure</a>";

        final MockWebConnection webConnection = (MockWebConnection) getContext().getWebClient().getWebConnection();

        webConnection.setDefaultResponse("<html></html>");
        final URL startUrl = new URL("http://www.foo.foo/");
        webConnection.setResponse(startUrl, wrapContent(content));
        getContext().getWebClient().getPage(startUrl);

        final VerifyLinks step = (VerifyLinks) getStep();
        step.setDepth("5");
        step.execute();
        final Map properties = step.getParameterDictionary();
        assertEquals("4", properties.get("-> valid links"));

    }

    public void testNotBrokenStateRequest() {
        assertNotBrokenStateWithHttpReturnCode(399, true);
        assertNotBrokenStateWithHttpReturnCode(400, false);
        assertNotBrokenStateWithHttpReturnCode(401, false);
        assertNotBrokenStateWithHttpReturnCode(HttpURLConnection.HTTP_BAD_GATEWAY, false);
        assertNotBrokenStateWithHttpReturnCode(HttpURLConnection.HTTP_BAD_REQUEST, false);
        assertNotBrokenStateWithHttpReturnCode(HttpURLConnection.HTTP_NOT_FOUND, false);
        assertNotBrokenStateWithHttpReturnCode(HttpURLConnection.HTTP_OK, true);
    }

    public void testNotBrokenStateWithException() {
        assertNotBrokenStateWithException(THROW_MALFORMEDURLEXCEPTION, false);
        assertNotBrokenStateWithException(THROW_IOEXCEPTION, false);
    }

    private void assertNotBrokenStateWithHttpReturnCode(int returncode, boolean expected) {
        assertNotBrokenState(returncode, THROW_NO_EXCEPTION, expected);
    }

    private void assertNotBrokenStateWithException(int exceptionCode, boolean expected) {
        assertNotBrokenState(0, exceptionCode, expected);
    }

    private void assertNotBrokenState(int returncode, int exceptionCode, boolean expected) {
    	final VerifyLinks step = (VerifyLinks) createAndConfigureStep();
        final HtmlPage page = getResponseForText(samplePageWithAnchor());
        final WebClient badClient = makeClient(returncode, exceptionCode);
        getContext().setWebClient(badClient);
        step.checkVisits(badClient, page);
        assertEquals(expected, step.getFailedVisits().size() == 0);
    }

    private static WebClient makeClient(final int httpReturnCode, final int exceptionCode) {
        return new WebClient()
        {
            @SuppressWarnings("unchecked")
			public Page getPage(final URL url) throws IOException, FailingHttpStatusCodeException {
                switch (exceptionCode) {
                    case THROW_MALFORMEDURLEXCEPTION:
                        throw new MalformedURLException();
                    case THROW_IOEXCEPTION:
                        throw new IOException();
                    default:
                }
                if (httpReturnCode < 400) {
                	return super.getPage(WebClient.URL_ABOUT_BLANK);
                }
                final List<NameValuePair> emptyList = Collections.emptyList();
				final WebResponseData responseData = new WebResponseData(new byte[]{},
                		httpReturnCode, "blah", emptyList);
        		WebResponse webResponse = new WebResponse(responseData, url, HttpMethod.GET, 1);
                throw new FailingHttpStatusCodeException(webResponse);
            }
        };
    }

    private static String samplePageWithAnchor() {
        return wrapContent(anchor(DUMMY_HREF, DUMMY_LABEL));
    }

    private static String anchor(String href, String label) {
        return "<A HREF=\"" + href + "\">" + label + "</A>";
    }

    private static String wrapContent(String content) {
        return "<html><head><title>foo</title></head><body>" + content + "</body></html>";
    }

    public void testJSErrorsOnForeignPages() throws Exception {
        final String htmlContent = wrapContent("<a href='http://htmlunit.sf.net/jserror.html'>other</a>");
        final HtmlPage page = getDummyPage(htmlContent);
        getContext().saveResponseAsCurrent(page);
        
        final WebClient webClient = page.getWebClient();
        final MockWebConnection mockConnection = new MockWebConnection();
        mockConnection.setResponse(new URL("http://htmlunit.sf.net/jserror.html"), "<html><body onload='alert('></body></html>");
        webClient.setWebConnection(mockConnection);

    	final VerifyLinks step = (VerifyLinks) getStep();
    	
    	// throws exception as default is false
    	ThrowAssert.assertThrows(ScriptException.class, getExecuteStepTestBlock());

    	// ignore exceptions
    	step.setIgnoreForeignJSErrors(true);
    	executeStep(step);
    }

}
