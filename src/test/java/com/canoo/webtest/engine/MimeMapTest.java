package com.canoo.webtest.engine;

import junit.framework.TestCase;

/**
 * Unit tests for {@link MimeMap}.
 * @author Denis N. Antonioli
 * @author Marc Guillemot
 */
public class MimeMapTest extends TestCase {
    public void testAnyXmlType() {
        assertEquals("xml", MimeMap.getExtension("application/anything+xml"));
    }

    public void testAnyTextSubtype() {
        assertEquals("txt", MimeMap.getExtension("text/anything"));
    }

    public void testUnknownType() {
        assertEquals("unknown", MimeMap.getExtension("this is not a mime type"));
    }
    
    public void testXHtmlType() {
        assertEquals("html", MimeMap.getExtension("application/xhtml+xml"));
        assertEquals("html", MimeMap.getExtension("application/vnd.wap.xhtml+xml"));
    }

    public void testAdjustMimeTypeIfNeeded()
    {
    	assertEquals("text/xml", MimeMap.adjustMimeTypeIfNeeded("text/xml", "http://foo/fii"));
    	assertEquals("text/xml", MimeMap.adjustMimeTypeIfNeeded("text/xml", "http://foo/fii.xls"));
    	assertEquals(MimeMap.EXCEL_MIME_TYPE, 
    			MimeMap.adjustMimeTypeIfNeeded(MimeMap.UNKNOWN_BINARY_MIME_TYPE, "http://foo/fii.xls"));
    	assertEquals(MimeMap.UNKNOWN_BINARY_MIME_TYPE, 
    			MimeMap.adjustMimeTypeIfNeeded(MimeMap.UNKNOWN_BINARY_MIME_TYPE, null));
    }
}
