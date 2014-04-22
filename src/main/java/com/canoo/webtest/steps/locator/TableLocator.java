// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.locator;

import java.util.Iterator;

import org.xml.sax.SAXException;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.interfaces.ITableLocator;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

/**
 * @author Dierk König, Carsten Seibert
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.nested
 *   category="Core"
 *   name="table"
 *   description="This optional nested element can be used to locate a specific cell in a specific table on the page."
 */
public class TableLocator implements ITableLocator {
    private String fHtmlId;
    private String fDescription;
    private int fRow;
    private int fColumn;

    /**
     * @webtest.parameter
     * 	required="no"
     *  description="The table's id attribute, if present. It is used instead of the name attribute here, because HTML 4.0 does not support that attribute on tables."
     */
    public void setHtmlId(final String id) {
        fHtmlId = id;
    }

    public String getHtmlId() {
        return fHtmlId;
    }

    /**
     * @webtest.parameter
     * 	required="no"
     *  description="Deprecated. Same as htmlid."
     * @deprecated use {@link #setHtmlId(String)} instead
     */
    public void setId(final String id) {
        setHtmlId(id);
    }

    /**
      * @deprecated use {@link #setHtmlId(String)} instead
    */
    public String getId() {
        return getHtmlId();
    }

    /**
     * @webtest.parameter
     * 	required="yes"
     *  description="The cell's row index in the table, starting at 0."
     */
    public void setRow(final int row) {
        fRow = row;
    }

    public int getRow() {
        return fRow;
    }

    /**
     * @webtest.parameter
     * 	required="yes"
     *  description="The cell's column index in the table, starting at 0."
     */
    public void setColumn(final int column) {
        fColumn = column;
    }

    public int getColumn() {
        return fColumn;
    }

    /**
     * @webtest.parameter
     * 	required="no"
     *  description="Description for this locator."
     */
    public void setDescription(final String description) {
        this.fDescription = description;
    }

    public String getDescription() {
        return fDescription;
    }

    public String locateText(final Context context, Step step) throws TableNotFoundException, IndexOutOfBoundsException, SAXException {
        try {
            final HtmlElement htmlElement;
            if (getHtmlId() == null) {
                htmlElement = findFirstTable(context, step);
            } else {
                htmlElement = context.getCurrentHtmlResponse(step).getHtmlElementById(getHtmlId());
            }
            if (!(htmlElement instanceof HtmlTable)) {
                throw new StepFailedException("Found '" + htmlElement.getTagName() +
                        "' element when looking for 'table' element using htmlId " + getHtmlId(), step);
            }
            final HtmlTable table = (HtmlTable) htmlElement;
            return table.getRow(getRow()).getCell(getColumn()).asText();
        } 
        catch (final ElementNotFoundException e) {
            throw new TableNotFoundException(getHtmlId());
        }
    }

    private static HtmlElement findFirstTable(final Context context, final Step step) {
        final Iterator allHtmlChildElements =
	            context.getCurrentHtmlResponse(step).getHtmlElementDescendants().iterator();
	    while (allHtmlChildElements.hasNext()) {
	    	final HtmlElement element = (HtmlElement) allHtmlChildElements.next();
	        if (element instanceof HtmlTable) {
	            return element;
	        }
	    }
        throw new ElementNotFoundException("*", "*", "*");
    }
}
