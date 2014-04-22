// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.canoo.webtest.steps.AbstractFilter;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Removes all content leaving only tags. Assumes the content is well-formed.
 *
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step category="Filter"
 * name="contentStripper"
 * description="Removes all content within well-formed XML/XHTML leaving only tags."
 */
public class ContentStripperFilter extends AbstractFilter
{
    private static final Logger LOG = Logger.getLogger(ContentStripperFilter.class);

	public void doExecute() throws Exception {
        final Page currentResponse = getContext().getCurrentResponse();
        final String origType = currentResponse.getWebResponse().getContentType();
        LOG.info("Filtering response of type " + origType);

        final StringBuffer buf = new StringBuffer();
        if (currentResponse instanceof HtmlPage) 
        {
            final HtmlPage page = (HtmlPage) currentResponse;
            buf.append("<html>");
            processHtml(page.getDocumentElement().getChildren(), buf);
            buf.append("</html>");
        }
        else if (currentResponse instanceof XmlPage) 
        {
            final XmlPage page = (XmlPage) currentResponse;
            final Document xmlDocument = page.getXmlDocument();
            if (xmlDocument != null) {
                processXml(xmlDocument.getChildNodes().item(0), buf);
            }
        }
        else
        {
        	LOG.warn("Unexpected current response: " + origType
        			+ ", filter result will be empty");
        }
        defineAsCurrentResponse(buf.toString(), origType);
    }

    private static void processHtml(final Iterable iter, final StringBuffer buf) {
    	final Iterator children = iter.iterator();
        while (children.hasNext()) {
            final DomNode node = (DomNode) children.next();
            if (!(node instanceof DomText)) {
                buf.append("<").append(node.getNodeName()).append(">");
                processHtml(node.getChildren(), buf);
                buf.append("</").append(node.getNodeName()).append(">");
            }
        }
    }

    private static void processXml(final Node node, final StringBuffer buf) {
        buf.append("<").append(node.getNodeName()).append(">");
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node n = childNodes.item(i);
            if (n.hasChildNodes()) {
                processXml(n, buf);
            }
        }
        buf.append("</").append(node.getNodeName()).append(">");
    }
}
