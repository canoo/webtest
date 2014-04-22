// Copyright (c) 2005-2006. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.canoo.webtest.steps.AbstractFilter;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Removes all tags within the content. Assumes the content is well-formed.
 *
 * @author Paul King
 * @author David Louvton
 * @webtest.step category="Filter"
 * name="tagStripper"
 * description="Removes all tags within well-formed XML/XHTML content."
 */
public class TagStripperFilter extends AbstractFilter
{

	private static void processHtml(final Iterable iter, final StringBuffer buf) {
      	final Iterator children = iter.iterator();
        while (children.hasNext()) {
            final DomNode node = (DomNode) children.next();
            if (node instanceof DomText) {
                buf.append(((DomText) node).getData());
            } else {
                processHtml(node.getChildren(), buf);
            }
        }
    }

    private static void processXml(final NodeList childNodes, final StringBuffer buf) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node n = childNodes.item(i);
            if (n.hasChildNodes()) {
                processXml(n.getChildNodes(), buf);
            } else {
                buf.append(n.getNodeValue() == null ? "" : n.getNodeValue());
            }
        }
    }

    public void doExecute() throws Exception {
        final StringBuffer buf = new StringBuffer();
        final Page currentResponse = getContext().getCurrentResponse();
        if (currentResponse instanceof HtmlPage) {
            final HtmlPage page = (HtmlPage) currentResponse;
            processHtml(page.getDocumentElement().getChildren(), buf);
        } else if (currentResponse instanceof XmlPage) {
            final XmlPage page = (XmlPage) currentResponse;
            processXml(page.getXmlDocument().getChildNodes(), buf);
        }
        defineAsCurrentResponse(buf.toString(), "text/plain");
    }
}
