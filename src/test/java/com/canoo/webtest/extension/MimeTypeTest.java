// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import junit.framework.TestCase;

/**
 * @author Denis N. Antonioli
 */
public class MimeTypeTest extends TestCase {

	public void testMediaRangeCreate() {
		final MimeType.MediaRange mediaRange = MimeType.MediaRange.create("img/png");
		assertEquals("img/", mediaRange.getType());
		assertEquals("/png", mediaRange.getSubtype());
		ThrowAssert.assertThrows(IllegalArgumentException.class, "'*/png' is not a valid syntax.",
		   new TestBlock() {
			   public void call() throws Exception {
				   MimeType.MediaRange.create("*/png");
			   }
		   });
		ThrowAssert.assertThrows(IllegalArgumentException.class, "'' is not a valid syntax.",
		   new TestBlock() {
			   public void call() throws Exception {
				   MimeType.MediaRange.create("");
			   }
		   });
	}

	public void testMediaRangeMatch() {
		assertTrue(MimeType.MediaRange.create("img/png").match("img/png"));
		assertTrue(MimeType.MediaRange.create("img/*").match("img/png"));
		assertTrue(MimeType.MediaRange.create("*/*").match("img/png"));

		assertFalse(MimeType.MediaRange.create("img/png").match("img/gif"));
		assertTrue(MimeType.MediaRange.create("img/*").match("img/gif"));
		assertTrue(MimeType.MediaRange.create("*/*").match("img/gif"));

		assertFalse(MimeType.MediaRange.create("img/png").match("text/xml"));
		assertFalse(MimeType.MediaRange.create("img/*").match("text/xml"));
		assertTrue(MimeType.MediaRange.create("*/*").match("text/xml"));
	}

	public void testMimeType() {
		assertTrue("Default MimeType accepts everything", MimeType.ALL_MEDIA.match("img/gif"));

		MimeType mimeType = new MimeType("img/png");
		assertTrue(mimeType.match("img/png"));
		assertFalse(mimeType.match("text/xml"));
		assertFalse(mimeType.match("application/pdf"));

		mimeType = new MimeType("img/png;text/xml");
		assertTrue(mimeType.match("img/png"));
		assertTrue(mimeType.match("text/xml"));
		assertFalse(mimeType.match("application/pdf"));

		mimeType = new MimeType("img/png;text/xml;*/*");
		assertTrue(mimeType.match("img/png"));
		assertTrue(mimeType.match("text/xml"));
		assertTrue(mimeType.match("application/pdf"));
	}

	public void testToString() {
		assertEquals("*/*", MimeType.ALL_MEDIA.toString());
		assertEquals("text/html", new MimeType("text/html").toString());
		assertEquals("text/*", new MimeType("text/*").toString());
		assertEquals("*/*", new MimeType("*/*").toString());
		assertEquals("text/html;text/plain", new MimeType("text/html;text/plain").toString());
	}

	public void testSimplify() {
		assertEquals("text/plain;text/xml", new MimeType("text/xml;text/plain").toString());
		assertEquals("*/*", new MimeType("text/html;text/*;*/*").toString());
		assertEquals("text/*", new MimeType("text/html;text/*").toString());
		assertEquals("*/*", new MimeType("text/html;*/*").toString());
	}

	public void testCompareMediaRange(){
		final MimeType.MediaRange imageAny = MimeType.MediaRange.create("img/*");
		final MimeType.MediaRange textAny = MimeType.MediaRange.create("text/*");
		final MimeType.MediaRange textHtml = MimeType.MediaRange.create("text/html");
		final MimeType.MediaRange textXml = MimeType.MediaRange.create("text/xml");

		assertTrue(0 > MimeType.MediaRange.ALL_MEDIA_RANGES.compareTo(textAny));
		assertTrue(0 > MimeType.MediaRange.ALL_MEDIA_RANGES.compareTo(textHtml));
		assertEquals(0, MimeType.MediaRange.ALL_MEDIA_RANGES.compareTo(MimeType.MediaRange.ALL_MEDIA_RANGES));

		assertTrue(0 > imageAny.compareTo(textAny));
		assertTrue(0 > textAny.compareTo(textXml));
		assertEquals(0, imageAny.compareTo(MimeType.MediaRange.create("img/*")));
		assertTrue(0 < textAny.compareTo(imageAny));

		assertTrue(0 < textHtml.compareTo(MimeType.MediaRange.ALL_MEDIA_RANGES));
		assertTrue(0 < textHtml.compareTo(textAny));
		assertEquals(0, textHtml.compareTo(MimeType.MediaRange.create("text/html")));
		assertTrue(0 > textHtml.compareTo(textXml));
		assertTrue(0 > textHtml.compareTo(textXml));
	}
}