// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathException;

import org.junit.Test;
import org.w3c.dom.Attr;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class VerifyLinkedContentTest extends BaseStepTestCase {
	private VerifyLinkedContent fStep;
	private static final List<NameValuePair> emptyHeadersList = Collections.emptyList();


	protected void setUp() throws Exception {
		super.setUp();
		fStep = (VerifyLinkedContent) getStep();
	}

	protected Step createStep() {
		return new VerifyLinkedContent();
	}

	public void testRejectsNullXpath() {
		assertStepRejectsNullParam("xpath", new TestBlock() {
			public void call() throws Exception {
				executeStep(fStep);
			}
		});
	}

	public void testIterateAllMatchingElementsWithXpathError() {
		fStep.setXpath("//img()/@src");
		final String content = "<html><body><h1>hello</h1></body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");
		ThrowAssert.assertThrows(XPathException.class, new TestBlock() {
			public void call() throws Throwable {
				fStep.iterateAllMatchingElements(htmlPage);
			}
		});
	}

	/**
	 * Empty Xpath iteration. Security against bug in htmlunit.
	 */
	public void testIterateAllMatchingElementsWithoutMatch() throws Exception {
		fStep.setXpath("//img/@src");
		final String content = "<html><head></head><body><h1>hello</h1></body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		assertEquals(0, fStep.iterateAllMatchingElements(htmlPage).size());
	}

	/**
	 * Xpath iteration with one element. Security against bug in htmlunit.
	 */
	public void testIterateAllMatchingElementsWithOneMatch() throws Exception {
		fStep.setXpath("//img/@src");
		final String content = "<html><head></head><body><img src=\"resource/ok.png\" /></body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		final Iterator iter = fStep.iterateAllMatchingElements(htmlPage).iterator();
		assertTrue(iter.hasNext());

		final Attr ob = (Attr) iter.next();
		assertEquals("src", ob.getLocalName());
		assertEquals("resource/ok.png", ob.getValue());
	}

	/**
	 * Xpath iteration with multiple elements. Security against bug in htmlunit.
	 */
	@Test
	public void testIterateAllMatchingElementsWithMultipleMatch() throws Exception {
		fStep.setXpath("//img/@src");
		final String content = "<html><head></head><body>" +
			"<img src=\"1.png\" />" +
			"<img src=\"2.png\" />" +
			"<img src=\"3.png\" />" +
			"</body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		@SuppressWarnings("unchecked")
		final List<? extends Object> nodes = fStep.iterateAllMatchingElements(htmlPage);

		Attr me = (Attr) nodes.get(0);
		assertEquals("src", me.getLocalName());
		assertEquals("1.png", me.getValue());

		me = (Attr) nodes.get(1);
		assertEquals("src", me.getLocalName());
		assertEquals("2.png", me.getValue());

		me = (Attr) nodes.get(2);
		assertEquals("src", me.getLocalName());
		assertEquals("3.png", me.getValue());
		assertEquals(3, nodes.size());
	}

	/**
	 * Complex Xpath iteration with multiple elements. Security against bug in htmlunit.
	 */
	public void testIterateAllMatchingElementsWithComplexExpression() throws Exception {
		fStep.setXpath("//img/@src | //input[@type='image']/@src");
		final String content = "<html><head></head><body>" +
			"<img src=\"1.png\" />" +
			"<input type=\"image\" src=\"2.png\" />" +
			"</body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		final Iterator iter = fStep.iterateAllMatchingElements(htmlPage).iterator();
		assertTrue(iter.hasNext());

		Attr me = (Attr) iter.next();
		assertEquals("src", me.getLocalName());
		assertEquals("1.png", me.getValue());

		me = (Attr) iter.next();
		assertEquals("src", me.getLocalName());
		assertEquals("2.png", me.getValue());

		assertFalse(iter.hasNext());
	}

	public void testVerifyLinksOnPage() throws Exception {
		fStep.setXpath("//img/@src");
		final String content = "<html><head></head><body>" +
			"<img src=\"1.png\" />" +
			"</body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		final WebClient webClient = getContext().getWebClient();
		final MockWebConnection webConnection = (MockWebConnection) webClient .getWebConnection();
		webConnection.setResponse(htmlPage.getFullyQualifiedUrl("1.png"), new byte[0], 200, "OK", "image/png", emptyHeadersList);

		final boolean previousThrow = webClient.isThrowExceptionOnFailingStatusCode();		
		StringBuffer sb = fStep.verifyLinksOnPage(htmlPage);
		assertEquals(previousThrow, webClient.isThrowExceptionOnFailingStatusCode());
		assertNotNull(sb);
		assertEquals(0, sb.length());
	}

	public void testVerifyParametersWithWrongType() throws Exception {
		fStep.setXpath("//");
		getContext().setDefaultResponse("", "foo/foo");
		ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
			public void call() throws Exception {
				executeStep(fStep);
			}
		});
	}

	public void testVerifyParametersWithoutXpath() throws Exception {
		getContext().setDefaultResponse("", "foo/foo");
		ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
			public void call() throws Exception {
				executeStep(fStep);
			}
		});
	}

	public void testExecuteWithMissingLinks() throws Exception {
		fStep.setXpath("//img/@src");
		fStep.setAccept("image/png");

		final String content = "<html><head></head><body>" +
			"<img src=\"1.png\" />" +
			"<img src=\"2.png\" />" +
			"</body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		final MockWebConnection webConnection = (MockWebConnection) getContext().getWebClient().getWebConnection();
		webConnection.setResponse(htmlPage.getFullyQualifiedUrl("1.png"), new byte[0], 200, "OK", "image/png", emptyHeadersList);
		webConnection.setResponse(htmlPage.getFullyQualifiedUrl("2.png"), new byte[0], 404, "Resource not found", "image/png", emptyHeadersList);

		getContext().saveResponseAsCurrent(htmlPage);

		ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
			public void call() throws Exception {
				executeStep(fStep);
			}
		});
	}

	public void testExecute() throws Exception {
		fStep.setXpath("//img/@src");
		fStep.setAccept("image/png");

		final String content = "<html><head></head><body>" +
			"<img src=\"1.png\" />" +
			"</body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		final MockWebConnection webConnection = (MockWebConnection) getContext().getWebClient().getWebConnection();
		webConnection.setResponse(htmlPage.getFullyQualifiedUrl("1.png"), new byte[0], 200, "OK", "image/png", emptyHeadersList);

		getContext().saveResponseAsCurrent(htmlPage);

		executeStep(fStep);
		assertEquals(htmlPage, fStep.getContext().getCurrentResponse());
	}


	public void testVerifyOneLink() throws Exception {
		fStep.setXpath("//img/@src");
		fStep.setAccept("image/png");
		fStep.verifyParameters();

		final String content = "<html><head></head><body>" +
			"<img src=\"1.png\" />" +
			"<img src=\"2.png\" />" +
			"<img src=\"3.png\" />" +
			"</body></html>";
		final HtmlPage htmlPage = (HtmlPage) getDummyPage(content, "text/html");

		final WebClient webClient = new WebClient();
		final MockWebConnection webConnection = new MockWebConnection();
		webClient.setWebConnection(webConnection);
		webConnection.setResponse(htmlPage.getFullyQualifiedUrl("1.png"), new byte[0], 200, "OK", "image/png", emptyHeadersList);
		webConnection.setResponse(htmlPage.getFullyQualifiedUrl("2.png"), new byte[0], 404, "Resource not found", "image/png", emptyHeadersList);
		webConnection.setResponse(htmlPage.getFullyQualifiedUrl("3.png"), new byte[0], 200, "OK", "application/octet-stream", emptyHeadersList);

		String result;
		result = fStep.verifyOneLink(webClient, htmlPage, new SimpleSrcAttr(htmlPage, "1.png"));
		assertNotNull(result);
		assertEquals(0, result.length());

		result = fStep.verifyOneLink(webClient, htmlPage, new SimpleSrcAttr(htmlPage, "2.png"));
		assertNotNull(result);
		assertTrue(-1 < result.indexOf(" 404 "));
		assertTrue(-1 < result.indexOf("2.png"));

		result = fStep.verifyOneLink(webClient, htmlPage, new SimpleSrcAttr(htmlPage, "3.png"));
		assertNotNull(result);
		assertTrue(-1 < result.indexOf(" application/octet-stream "));
		assertTrue(-1 < result.indexOf("3.png"));
	}


	public void testMakeCloverHappy() {
		final Attr attributeEntry = new SimpleSrcAttr(null, "1.png");
		assertEquals("1.png", attributeEntry.getValue());
		attributeEntry.setValue("2.png");
		assertEquals("2.png", attributeEntry.getValue());
	}

	static class SimpleSrcAttr extends DomAttr {
		SimpleSrcAttr(final HtmlPage page, final String value) {
			super(page, null, "src", value, true);
		}
	}
}