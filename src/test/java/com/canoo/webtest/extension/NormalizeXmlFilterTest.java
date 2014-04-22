// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepFailedException;


/**
 * NormalizeXmlFilter Tester.
 *
 * @author Paul King
 */
public class NormalizeXmlFilterTest extends ResponseFilterTestCase
{
    private static final String SOURCE_1 = "<xml><tag attribute='123'>\r\n</tag></xml>\r\n";
    private static final String EXPECTEDXML_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xml>\n" +
            "  <tag attribute=\"123\"/>\n" +
            "</xml>\n";

    public void testNormalizesContentXML() {
        final NormalizeXmlFilter filter = new NormalizeXmlFilter();
        checkFilterContentAsXml(filter, SOURCE_1, EXPECTEDXML_1);
    }

    public void testNormalizesContentNoXML() {
        final NormalizeXmlFilter filter = new NormalizeXmlFilter();
        try {
        	getFilterContent(filter, SOURCE_1, "text/plain");
        	fail();
        }
        catch (StepFailedException e) {
        	// ok
        }
    }

    public void testNormalizesContentHtml() {
        final String source = "<html><body class='foo'><div><span id='pl'>hello</div></body></html>";
        final String normalized = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<html>\n"
			+ "  <head/>\n"
			+ "  <body class=\"foo\">\n"
			+ "    <div>\n"
			+ "      <span id=\"pl\">hello</span>\n"
			+ "    </div>\n"
			+ "  </body>\n"
			+ "</html>\n";
        final NormalizeXmlFilter filter = new NormalizeXmlFilter();
        checkFilterContentAsHtml(filter, source, normalized);
    }
}
