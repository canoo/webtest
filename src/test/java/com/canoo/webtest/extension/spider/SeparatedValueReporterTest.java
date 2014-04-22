// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension.spider;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class SeparatedValueReporterTest extends TestCase {
	public static final String KEY_LABEL = "Label";
	public static final String KEY_HREF = "Href";
	public static final String[] HEADER = new String[]{KEY_LABEL, KEY_HREF};

	private SeparatedValueReporter fSeparatedValueReporter;
	private StringWriter fWriter;

	protected void setUp() throws Exception {
        super.setUp();
		fSeparatedValueReporter = new SeparatedValueReporter(HEADER);
		fWriter = new StringWriter();
		fSeparatedValueReporter.setWriter(fWriter);
	}

	public void testSeparatedValueReporter() {
		ThrowAssert.assertThrows(IllegalArgumentException.class, new TestBlock() {
			public void call() throws Exception {
				new SeparatedValueReporter(null);
			}
		});
	}

	public void testSetWriter() throws Exception {
		ThrowAssert.assertThrows(IllegalArgumentException.class, new TestBlock() {
			public void call() throws Exception {
				fSeparatedValueReporter.setWriter(null);
			}
		});
	}

	public void testWriteHeader() throws IOException {
		fSeparatedValueReporter.writeHeader();
		assertEquals(KEY_LABEL + SeparatedValueReporter.VALUE_SEPARATOR + KEY_HREF + SeparatedValueReporter.LINE_SEPARATOR, fWriter.toString());
	}

	public void testWriteFooter() throws Exception {
		fSeparatedValueReporter.writeFooter();
		assertEquals("", fWriter.toString());
	}

	public void testWriteNoProperty() throws Exception {
		Properties linkInfo = new Properties();
		fSeparatedValueReporter.write(linkInfo);
		assertEquals(SeparatedValueReporter.VALUE_SEPARATOR + SeparatedValueReporter.LINE_SEPARATOR, fWriter.toString());
	}

	public void testWriteOneProperty() throws Exception {
		Properties linkInfo = new Properties();
		linkInfo.setProperty(KEY_HREF, "href");
		fSeparatedValueReporter.write(linkInfo);
		assertEquals(SeparatedValueReporter.VALUE_SEPARATOR + "href" + SeparatedValueReporter.LINE_SEPARATOR, fWriter.toString());
	}

	public void testWriteAllProperties() throws Exception {
		Properties linkInfo = new Properties();
		linkInfo.setProperty(KEY_HREF, "href");
		linkInfo.setProperty(KEY_LABEL, "label");
		fSeparatedValueReporter.write(linkInfo);
		assertEquals("label" + SeparatedValueReporter.VALUE_SEPARATOR + "href" + SeparatedValueReporter.LINE_SEPARATOR, fWriter.toString());
	}

	public void testFinishLine() {
		assertEquals(SeparatedValueReporter.LINE_SEPARATOR,
		   SeparatedValueReporter.finishLine(new StringBuffer()));
		assertEquals(SeparatedValueReporter.LINE_SEPARATOR,
		   SeparatedValueReporter.finishLine(new StringBuffer(SeparatedValueReporter.VALUE_SEPARATOR)));
		assertEquals("a" + SeparatedValueReporter.LINE_SEPARATOR,
		   SeparatedValueReporter.finishLine(new StringBuffer("a"+ SeparatedValueReporter.VALUE_SEPARATOR)));
	}
}