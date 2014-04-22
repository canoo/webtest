// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.interfaces.IContentFilter;

/**
 * @author Rob Nielsen
 * @webtest.step category="Excel"
 * name="excelStructureFilter"
 * alias="structureFilter"
 * description="Extracts the structure of the current <key>xls</key> document, as a xml file."
 */
public class ExcelStructureFilter extends AbstractExcelStep implements IContentFilter {

    public void doExecute() throws Exception {
        final HSSFWorkbook excelWorkbook = getExcelWorkbook();
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = builder.newDocument();
        final Element root = doc.createElement("excelWorkbook");
        doc.appendChild(root);
        root.setAttribute("backup", String.valueOf(excelWorkbook.getBackupFlag()));
        root.setAttribute("numberOfFonts", String.valueOf(excelWorkbook.getNumberOfFonts()));
        root.setAttribute("numberOfCellStyles", String.valueOf(excelWorkbook.getNumCellStyles()));
        root.setAttribute("numberOfNames", String.valueOf(excelWorkbook.getNumberOfNames()));
        final Element sheets = doc.createElement("sheets");
        for(int i=0; i < excelWorkbook.getNumberOfSheets(); i++) {
            final HSSFSheet sheetAt = excelWorkbook.getSheetAt(i);
            final Element sheetElement = doc.createElement("sheet");
            sheetElement.setAttribute("index", String.valueOf(i));
            sheetElement.setAttribute("name", excelWorkbook.getSheetName(i));
            sheetElement.setAttribute("firstRow", String.valueOf(sheetAt.getFirstRowNum()));
            sheetElement.setAttribute("lastRow", String.valueOf(sheetAt.getLastRowNum()));
            sheetElement.setAttribute("physicalRows", String.valueOf(sheetAt.getPhysicalNumberOfRows()));
            sheetElement.setAttribute("defaultRowHeight", String.valueOf(sheetAt.getDefaultRowHeight()));
            sheetElement.setAttribute("defaultColumnWidth", String.valueOf(sheetAt.getDefaultColumnWidth()));
            sheetElement.setAttribute("fitToPage", String.valueOf(sheetAt.getFitToPage()));
            sheets.appendChild(sheetElement);
        }
        root.appendChild(sheets);
        final StringWriter sw = new StringWriter();
        writeXmlFile(doc, sw);
        ContextHelper.defineAsCurrentResponse(getContext(), sw.toString(), "text/xml", getClass());
    }

    protected void writeXmlFile(final Document doc, final Writer writer) throws IOException
    {
        final OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        format.setEncoding("ISO-8859-1");
        format.setLineWidth(50);

        final XMLSerializer serializer = new XMLSerializer(writer, format);
        serializer.asDOMSerializer();
        serializer.serialize(doc.getDocumentElement());
        writer.close();
    }

}
