// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.AbstractFilter;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;

import org.w3c.dom.Document;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.log4j.Logger;

/**
 * Normalizes XML content.
 *
 * @author Paul King
 * @webtest.step category="Filter"
 * name="normalizeXml"
 * description="Normalizes XML content."
 */
public class NormalizeXmlFilter extends AbstractFilter
{
    private static final Logger LOG = Logger.getLogger(NormalizeXmlFilter.class);

    public void doExecute() throws Exception {
        final Page currentResponse = getContext().getCurrentResponse();
        if (currentResponse instanceof XmlPage) {
        	final Document doc = ((XmlPage) currentResponse).getXmlDocument();
        	doc.getDocumentElement(); // do it here as normalize is not yet supported by HtmlPage
            processXml(doc, currentResponse.getWebResponse().getContentType());
        }
        else if (currentResponse instanceof HtmlPage) {
            processXml(((HtmlPage) currentResponse), "text/xml");
        }
        else {
            throw new StepFailedException("Current response is not XML or HTML but: " 
            		+ currentResponse.getClass().getSimpleName()
            		+ " " + currentResponse.getWebResponse().getContentType());
        }
    }

    private void processXml(final Document doc, final String contentType) throws Exception {
        defineAsCurrentResponse(serializeXml(doc), contentType);
    }

    private static String serializeXml(Document rootDoc) throws IOException {
        final StringBuffer buf = new StringBuffer();
        Writer writer = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(writer, buildFormatter(rootDoc));
        serializer.serialize(rootDoc);
        buf.append(writer.toString());
        return buf.toString();
    }

    private static OutputFormat buildFormatter(Document doc) {
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        format.setIndent(2);
        return format;
    }

}
