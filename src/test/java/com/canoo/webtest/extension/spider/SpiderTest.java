// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension.spider;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.request.ClickLink;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SpiderTest extends TestCase {
    private Spider fSpider;
    private static final IVisitorStrategy ALWAYS_REJECT_VISITOR_STRATEGY = new IVisitorStrategy() {
        public boolean accept(HtmlAnchor link) {
            return false;
        }
    };
    private static final String TEMP_FILENAME1 = "dummy";
    private static final String TEMP_FILENAME2 = "spiderman.txt";

    protected void setUp() throws Exception {
        super.setUp();
        fSpider = new Spider();
    }

    public void testNeedsReportCheckMulitpleVisit() {
        fSpider.setVisitorStrategy(Spider.ALWAYS_ACCEPT_VISITOR_STRATEGY);

        final HtmlAnchor link = newLink("bla");
        assertTrue("Accept first time", fSpider.needsReport(link));
        assertFalse("Reject second time", fSpider.needsReport(link));
    }

    static HtmlAnchor newLink(final String href) {
		final WebClient client = new WebClient();
		final HtmlPage[] page = {null};
		ThrowAssert.assertPasses("", new TestBlock(){
			public void call() throws Throwable
			{
				page[0] = (HtmlPage) client.getPage(WebClient.URL_ABOUT_BLANK);
			}
		});

		final HtmlAnchor anchor = (HtmlAnchor) page[0].createElement("a");
		anchor.setAttribute("href", href);
        return anchor;
    }

    public void testNeedsReportCheckVisitorStrategy() {
        fSpider.setVisitorStrategy(ALWAYS_REJECT_VISITOR_STRATEGY);

        final HtmlAnchor link = newLink("bla");
        assertFalse("Reject first time", fSpider.needsReport(link));
    }

    public void testValidateNoNegativeDepth() {
        fSpider.setDepth(-1);
        ThrowAssert.assertThrows(IllegalArgumentException.class, new TestBlock() {
            public void call() throws Exception {
                fSpider.validate();
            }
        });
    }

    public void testValidateSetDefault() {
        assertNull(fSpider.getFileName());
        assertNull(fSpider.getReporter());
        assertNull(fSpider.getValidator());
        assertNull(fSpider.getVisitorStrategy());
        fSpider.validate();
        assertNull(fSpider.getFileName());
        assertEquals(Spider.NO_OP_REPORTER, fSpider.getReporter());
        assertEquals(Spider.NO_OP_VALIDATOR, fSpider.getValidator());
        assertEquals(Spider.ALWAYS_ACCEPT_VISITOR_STRATEGY, fSpider.getVisitorStrategy());
    }

    public void testValidatePreserveValue() {
        final String filename = "filename";
        final IReporter reporter = new SeparatedValueReporter(new String[0]);
        final IValidator validator = new SimpleLinksValidator();
        final IVisitorStrategy visitorStrategy = new PatternVisitorStrategy(null);

        fSpider.setFileName(filename);
        fSpider.setReporter(reporter);
        fSpider.setValidator(validator);
        fSpider.setVisitorStrategy(visitorStrategy);
        fSpider.validate();
        assertEquals(filename, fSpider.getFileName());
        assertEquals(reporter, fSpider.getReporter());
        assertEquals(validator, fSpider.getValidator());
        assertEquals(visitorStrategy, fSpider.getVisitorStrategy());
    }

    public void testReporter() {
        assertNull(fSpider.getReporter());
        fSpider.setReporter(Spider.NO_OP_REPORTER);
        assertEquals(Spider.NO_OP_REPORTER, fSpider.getReporter());
    }

    public void testValidator() {
        assertNull(fSpider.getValidator());
        fSpider.setValidator(Spider.NO_OP_VALIDATOR);
        assertEquals(Spider.NO_OP_VALIDATOR, fSpider.getValidator());
    }

    public void testVisitorStrategy() {
        assertNull(fSpider.getVisitorStrategy());
        fSpider.setVisitorStrategy(Spider.ALWAYS_ACCEPT_VISITOR_STRATEGY);
        assertEquals(Spider.ALWAYS_ACCEPT_VISITOR_STRATEGY, fSpider.getVisitorStrategy());
    }

    public void testGetWriter() throws IOException {
        ContextStub context = new ContextStub();
        Spider spider = new Spider();
        spider.setContext(context);
        // file case
        spider.setFileName(TEMP_FILENAME1);
        Writer writer = spider.getWriter();
        assertEquals("java.io.FileWriter", writer.getClass().getName());
        // stdout case
        spider.setFileName(null);
        writer = spider.getWriter();
        assertEquals("java.io.OutputStreamWriter", writer.getClass().getName());
    }

    public void testFailScenarios() throws IOException, SAXException {
        final Step dummyStep = new ClickLink();
        final Spider spider = new Spider() {
            void follow(final HtmlAnchor link) {
                throw new StepFailedException("Forced stop by SpiderTest", dummyStep);
            }
        };
        spider.setContext(new ContextStub());
        // should finish normally
        spider.processLink(null, 0);
        // should fail
        spider.setFailOnError(true);
        ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
            public void call() throws Throwable {
                spider.processLink(null, 0);
            }
        });
    }

    /**
     * Spider should just not go into a non html page but without throwing
     */
    public void testNonHtmlPage() throws Exception {
        final Spider spider = new Spider() {
            void follow(final HtmlAnchor link) {
                // nothing
            }
        };
        final WebClient client = new WebClient();
        final MockWebConnection connection = new MockWebConnection();
        client.setWebConnection(connection);
        connection.setDefaultResponse("foo", 200, "ok", "text/plain");
        final Page page = client.getPage(new URL("http://foo"));
        
        final Context context = new ContextStub()
        {
        	public Page getCurrentResponse()
        	{
        		return page;
        	}
        };
        spider.setContext(context);
        spider.processLink(null, 12);
    }

    public void testWriteProblemsAreIgnored() {
        final Spider goodspider = new Spider() {
            void visit(final HtmlPage currentResponse, final int depth) throws IOException {
                // ignore
            }
        };
        goodspider.setContext(ContextStub.CONTEXT_STUB_NOCURRENTRESPONSE);
        goodspider.validate();
        goodspider.setFileName(TEMP_FILENAME2);
        assertTrue(goodspider.doExecute());
        final Spider badspider = new Spider() {
            void visit(final HtmlPage currentResponse, final int depth) throws IOException {
                throw new IOException("Fake write problem by SpiderTest");
            }
        };
        badspider.setContext(ContextStub.CONTEXT_STUB_NOCURRENTRESPONSE);
        badspider.validate();
        badspider.setFileName(TEMP_FILENAME2);
        assertFalse(badspider.doExecute());
    }

    public void testMakeCloverHappy() throws IOException {
        Spider.NO_OP_VALIDATOR.validate(0, null, null);
        Spider.NO_OP_REPORTER.setWriter(null);
        Spider.NO_OP_REPORTER.writeHeader();
        Spider.NO_OP_REPORTER.write(null);
        Spider.NO_OP_REPORTER.writeFooter();
        Spider.ALWAYS_ACCEPT_VISITOR_STRATEGY.accept(null);
    }

    protected void tearDown() throws Exception {
        File tmp = new File(TEMP_FILENAME1);
        tmp.deleteOnExit();
        tmp = new File(TEMP_FILENAME2);
        tmp.deleteOnExit();
    }

}