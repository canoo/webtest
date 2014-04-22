// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

/**
 * TagStripperFilter Tester.
 *
 * @author Paul King
 */
public class TagStripperFilterTest extends ResponseFilterTestCase
{
    private static final String SOURCE_1 = "The <b>tags</b> including these <p/><p/> should be removed.";
    private static final String SOURCE_2 = "<b>inside should be ok</b>";
    private static final String SOURCE_3 = "no tags should be ok";
    private static final String SOURCE_4 = "<tag_only_should_be_ok/>";
    private static final String SOURCE_5 = "<html> abc<head>def <title> ghi </title>jkl</head> mno </html>";

    private static final String EXPECTEDHTML_1 = "The tags including these  should be removed.";
    private static final String EXPECTED_2 = "inside should be ok";
    private static final String EXPECTEDHTML_3 = "no tags should be ok";
    private static final String EXPECTED_4 = "";
    private static final String EXPECTED_5 = " abcdef  ghi jkl mno ";

    public void testStripsTags() {
        final TagStripperFilter filter = new TagStripperFilter();
        checkFilterContentAsHtml(filter, SOURCE_1, EXPECTEDHTML_1);
        checkFilterContentAsHtml(filter, SOURCE_2, EXPECTED_2);
        checkFilterContentAsXml(filter, SOURCE_2, EXPECTED_2);
        checkFilterContentAsHtml(filter, SOURCE_3, EXPECTEDHTML_3);
        checkFilterContentAsHtml(filter, SOURCE_4, EXPECTED_4);
        checkFilterContentAsXml(filter, SOURCE_4, EXPECTED_4);
//        checkFilterContentAsHtml(filter, SOURCE_5, EXPECTED_5); // doesn't make sense as HTML!!!
        checkFilterContentAsXml(filter, SOURCE_5, EXPECTED_5);
        assertEquals(0, getFilterContent(filter, "dummy", "text/plain").length());
    }

}
