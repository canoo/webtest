// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import junit.framework.TestCase;

import java.net.URL;

/**
 * @author Marc Guillemot
 */
public class HtmlParserMessageTest extends TestCase {
    private URL fUrl;
    private HtmlParserMessage fParserMessage;

    public HtmlParserMessageTest(String arg0) {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        fUrl = new URL("http://mypage");
        fParserMessage = new HtmlParserMessage(HtmlParserMessage.Type.ERROR,
                fUrl,
                "toto",
                15,
                20);
    }

    public static void testType() {
        assertEquals(HtmlParserMessage.Type.ERROR_NAME,
                HtmlParserMessage.Type.ERROR.getName());
        assertEquals(HtmlParserMessage.Type.ERROR_NAME,
                HtmlParserMessage.Type.ERROR.toString());

        assertEquals(HtmlParserMessage.Type.WARNING_NAME,
                HtmlParserMessage.Type.WARNING.getName());
        assertEquals(HtmlParserMessage.Type.WARNING_NAME,
                HtmlParserMessage.Type.WARNING.toString());

        assertTrue("warning < error",
                (HtmlParserMessage.Type.WARNING.compareTo(HtmlParserMessage.Type.ERROR)) < 0);
        assertTrue("error > warning",
                (HtmlParserMessage.Type.ERROR.compareTo(HtmlParserMessage.Type.WARNING)) > 0);

        assertTrue(HtmlParserMessage.Type.ERROR.isError());
        assertFalse(HtmlParserMessage.Type.ERROR.isWarning());
        assertFalse(HtmlParserMessage.Type.WARNING.isError());
        assertTrue(HtmlParserMessage.Type.WARNING.isWarning());
    }

    public static void testMessageCollector() {
        HtmlParserMessage.MessageCollector collector = new HtmlParserMessage.MessageCollector();
        assertEquals(0, collector.popAll().size());
        collector.error(null, null, 0, 0, null);
        assertEquals(1, collector.popAll().size());
        assertEquals(0, collector.popAll().size());
        collector.error(null, null, 0, 0, null);
        collector.warning(null, null, 0, 0, null);
        assertEquals(2, collector.popAll().size());
    }

    public void testHtmlParserMessage() {
        assertEquals(HtmlParserMessage.Type.ERROR, fParserMessage.getType());
        assertEquals(fUrl, fParserMessage.getURL());
        assertEquals("toto", fParserMessage.getMessage());
        assertEquals(15, fParserMessage.getLine());
        assertEquals(20, fParserMessage.getColumn());
    }

    public void testCompareTo() throws Exception {
        assertTrue("error is bigger",
                fParserMessage.compareTo(new HtmlParserMessage(HtmlParserMessage.Type.WARNING,
                        fParserMessage.getURL(),
                        fParserMessage.getMessage(),
                        fParserMessage.getLine(),
                        fParserMessage.getColumn()))
                > 0);
        assertTrue("url string compare",
                fParserMessage.compareTo(new HtmlParserMessage(fParserMessage.getType(),
                        new URL("http://mypage2"),
                        fParserMessage.getMessage(),
                        fParserMessage.getLine(),
                        fParserMessage.getColumn()))
                < 0);
        assertTrue("message is longer",
                fParserMessage.compareTo(new HtmlParserMessage(fParserMessage.getType(),
                        fParserMessage.getURL(),
                        fParserMessage.getMessage() + "a",
                        fParserMessage.getLine(),
                        fParserMessage.getColumn()))
                < 0);
        assertTrue("line",
                fParserMessage.compareTo(new HtmlParserMessage(fParserMessage.getType(),
                        fParserMessage.getURL(),
                        fParserMessage.getMessage(),
                        fParserMessage.getLine() + 1,
                        fParserMessage.getColumn()))
                < 0);
        assertTrue("column",
                fParserMessage.compareTo(new HtmlParserMessage(fParserMessage.getType(),
                        fParserMessage.getURL(),
                        fParserMessage.getMessage(),
                        fParserMessage.getLine(),
                        fParserMessage.getColumn() + 1))
                < 0);
    }

	public void testEquals() throws Exception {
		assertTrue("the same again",
				fParserMessage.equals(new HtmlParserMessage(fParserMessage.getType(),
						fParserMessage.getURL(),
						fParserMessage.getMessage(),
						fParserMessage.getLine(),
						fParserMessage.getColumn())));

		 assertFalse("with error type",
				 fParserMessage.equals(new HtmlParserMessage(HtmlParserMessage.Type.WARNING,
						 fParserMessage.getURL(),
						 fParserMessage.getMessage(),
						 fParserMessage.getLine(),
						 fParserMessage.getColumn())));
		assertFalse("with an other url",
				 fParserMessage.equals(new HtmlParserMessage(fParserMessage.getType(),
						 new URL("http://mypage2"),
						 fParserMessage.getMessage(),
						 fParserMessage.getLine(),
						 fParserMessage.getColumn())));
		assertFalse("with an other message",
				 fParserMessage.equals(new HtmlParserMessage(fParserMessage.getType(),
						 fParserMessage.getURL(),
						 fParserMessage.getMessage() + "a",
						 fParserMessage.getLine(),
						 fParserMessage.getColumn())));
		assertFalse("other line",
				 fParserMessage.equals(new HtmlParserMessage(fParserMessage.getType(),
						 fParserMessage.getURL(),
						 fParserMessage.getMessage(),
						 fParserMessage.getLine() + 1,
						 fParserMessage.getColumn())));
		assertFalse("other column",
				 fParserMessage.equals(new HtmlParserMessage(fParserMessage.getType(),
						 fParserMessage.getURL(),
						 fParserMessage.getMessage(),
						 fParserMessage.getLine(),
						 fParserMessage.getColumn() + 1)));
	 }

    public void testHashcode() {
        int hash1 = fParserMessage.hashCode();
        hash1 = fParserMessage.hashCode(); // coverage will tell if we recalculate unnecessarily
        int hash2 = new HtmlParserMessage(fParserMessage.getType(),
						 fParserMessage.getURL(),
						 fParserMessage.getMessage(),
						 fParserMessage.getLine(),
						 fParserMessage.getColumn() + 1).hashCode();
        assertFalse(hash1 == hash2);
    }
}
