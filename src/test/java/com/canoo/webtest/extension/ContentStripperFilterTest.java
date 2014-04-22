// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;


/**
 * ContentStripperFilter Tester.
 *
 * @author Paul King
 */
public class ContentStripperFilterTest extends ResponseFilterTestCase
{
    private static final String SOURCE_1 = "The <b>tags</b> including these <p/><p/> should be removed.";
    private static final String SOURCE_2 = "<b>inside should be ok</b>";
    private static final String SOURCE_3 = "no tags should be ok";
    private static final String SOURCE_4 = "<tag_only_should_be_ok/>";
    private static final String SOURCE_5 = "<html>\n<head>\r<title>abc123</title>\r\n</head></html>";
    private static final String SOURCE_5A = "<HTML>\n<head>\r<Title>abc123</TITLE>\r\n</head></html>";
    private static final String SOURCE_6 = "<html>\n<head>\r<title>abc123</title>\r\n</head></html>def456";
    private static final String SOURCE_7 = "no tags \nshould be ok";
// note html parser adds tags if needed
    private static final String EXPECTEDHTML_1 = "<html><head></head><body><b></b><p></p><p></p></body></html>";
    private static final String EXPECTEDHTML_2 = "<html><head></head><body><b></b></body></html>";
    private static final String EXPECTEDHTML_3 = "<html><head></head><body></body></html>";
    private static final String EXPECTEDHTML_4 = "<html><head></head><body><tag_only_should_be_ok></tag_only_should_be_ok></body></html>";
    private static final String EXPECTEDHTML_5 = "<html><head><title></title></head><body></body></html>";
    private static final String EXPECTEDHTML_6 = "<html><head><title></title></head><body></body></html>";
    private static final String EXPECTEDHTML_7 = "<html><head></head><body></body></html>";
    // note XML parser doesn't add tags
    private static final String EXPECTEDXML_2 = "<b></b>";
    private static final String EXPECTEDXML_4 = "<tag_only_should_be_ok></tag_only_should_be_ok>";
    private static final String EXPECTEDXML_5 = "<html><head><title></title></head></html>";

    public void testStripsContent() {

        final ContentStripperFilter filter = new ContentStripperFilter();
        checkFilterContentAsHtml(filter, SOURCE_1, EXPECTEDHTML_1);
        checkFilterContentAsHtml(filter, SOURCE_2, EXPECTEDHTML_2);
        checkFilterContentAsHtml(filter, SOURCE_3, EXPECTEDHTML_3);
        checkFilterContentAsHtml(filter, SOURCE_4, EXPECTEDHTML_4);
        checkFilterContentAsHtml(filter, SOURCE_5, EXPECTEDHTML_5);
        checkFilterContentAsHtml(filter, SOURCE_5A, EXPECTEDHTML_5);
        checkFilterContentAsHtml(filter, SOURCE_6, EXPECTEDHTML_6);
        checkFilterContentAsHtml(filter, SOURCE_7, EXPECTEDHTML_7);

        checkFilterContentAsXml(filter, SOURCE_2, EXPECTEDXML_2);
        checkFilterContentAsXml(filter, SOURCE_4, EXPECTEDXML_4);
        checkFilterContentAsXml(filter, SOURCE_5, EXPECTEDXML_5);
        checkFilterContentAsXml(filter, SOURCE_5A, ""); // bad XML will not be processed

        assertEquals(0, getFilterContent(filter, "dummy", "text/plain").length());
    }

}
