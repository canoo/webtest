// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.canoo.webtest.boundary.PackageBoundary;

/**
 * @author unknown
 * @author Marc Guillemot
 */
public class XmlReporter implements IResultReporter
{
    private static final Logger LOG = Logger.getLogger(XmlReporter.class);
    public static final String ROOT_ELEMENT = "summary";

    protected Document readXmlFile(final File file) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        LOG.debug("Reading file " + file.getPath());
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(file);
        
        // basic check that the file "is" a webtest report 
        if (!ROOT_ELEMENT.equals(doc.getFirstChild().getNodeName()))
        {
        	throw new ReportCreationException("Another root already exists: "
        			+ doc.getFirstChild().getNodeName() + "!");
        }
        
        return doc;
    }

    protected void writeXmlFile(final Document doc, final File outfile) throws Exception {
    	LOG.info("Writing report to " + outfile);
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), getEncoding()));
        writeXmlFile(doc, writer);
    }

    /**
     * Gets the encoding used to write the xml report file
     * @return "UTF-8"
     */
    protected String getEncoding()
	{
		return "UTF-8";
	}

	protected void writeXmlFile(final Document doc, final Writer writer) throws IOException {
        final OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        format.setEncoding(getEncoding());
        format.setLineWidth(100);

        final XMLSerializer serializer = new XMLSerializer(writer, format);
        serializer.asDOMSerializer();
        serializer.serialize(doc.getDocumentElement());
        writer.close();
    }

    public void generateReport(final RootStepResult result) throws Exception {
        final File resultFile = getResultFile(result);
        final Document doc = getDocument(resultFile);
        new XmlResultConverter(result).addToDocument(doc);
        writeXmlFile(doc, resultFile);
    }

	protected File getResultFile(final RootStepResult result)
	{
		return result.getConfig().getSummaryFile();
	}

	private Document getDocument(final File file) throws Exception {
        if (file.exists()) {
            return readXmlFile(file);
        }
        return createNewDocument();
    }

    /**
     * Creates a new document with the basic structure
     * @return the new document
     * @throws ParserConfigurationException if document factory doesn't work
     */
    protected Document createNewDocument() throws ParserConfigurationException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = builder.newDocument();

		final Element rootElement = doc.createElement(ROOT_ELEMENT);
		doc.appendChild(rootElement);
		addSummaryAttribute(rootElement);
        
        return doc;
    }

    private void addSummaryAttribute(final Element element) {
    	element.setAttribute("Implementation-Title", PackageBoundary.getImplementationTitle());
    	element.setAttribute("Implementation-Version", PackageBoundary.getImplementationVersion());
    }

}
