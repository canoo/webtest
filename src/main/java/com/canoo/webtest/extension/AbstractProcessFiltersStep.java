// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;
import org.xml.sax.SAXException;

import com.canoo.webtest.boundary.AntBoundary;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.interfaces.IContentFilter;
import com.canoo.webtest.interfaces.IStepWithTableLocator;
import com.canoo.webtest.interfaces.ITableLocator;
import com.canoo.webtest.steps.AbstractStepContainer;
import com.canoo.webtest.steps.locator.TableLocator;
import com.canoo.webtest.steps.locator.TableNotFoundException;

/**
 * Abstract class used for processing content
 */
public abstract class AbstractProcessFiltersStep extends AbstractStepContainer implements IStepWithTableLocator
{
    private static final Logger LOG = Logger.getLogger(AbstractProcessFiltersStep.class);

    private ITableLocator fTableLocator;
    private String fHtmlId;
    private String fXpath;

    public String getHtmlId() {
        return fHtmlId;
    }

    /**
     * Sets the id attribute of the element to process.<p>
     *
     * @param str the new value
     * @webtest.parameter required="no"
     * description="The id of an <key>HTML</key> element. If present, processing will be limited to this element. Will be ignored if a nested <stepref name='table'/> step is used."
     */
    public void setHtmlId(final String str) {
        fHtmlId = str;
    }

    public String getXpath() {
        return fXpath;
    }

    /**
     * Sets the XPath used to identify the element to process.<p>
     *
     * @param path the new value
     * @webtest.parameter required="no"
     * description="An XPath expression identifying an <key>HTML</key> element. If present, processing will be limited to this element. Will be ignored if <em>htmlId</em> is set or if a nested <stepref name='table'/> step is used."
     */
    public void setXpath(final String path) {
        fXpath = path;
    }

    public ITableLocator getTableLocator() {
        return fTableLocator;
    }

    public void addTask(final Task newTask) {
        final Task task = AntBoundary.maybeConfigure(newTask);
        if (task instanceof IContentFilter) {
        	super.addTask(task);
        } else {
            LOG.warn("Ignoring inner step because it is not a content filter: " + task.getClass().getName());
        }
    }

    /**
     *
     * @param tableLocator
     * @webtest.nested.parameter
     *    required="no"
     *    description="To locate a specific cell in a specific table on the page."
     */
    public void addTable(final TableLocator tableLocator) {
        addTableInternal(tableLocator);
    }

    private void addTableInternal(final ITableLocator tableLocator) {
        fTableLocator = tableLocator;
    }

    protected void applyTableFilterIfNeeded(final Context context) {
        if (getTableLocator() != null) {
            tryLocateText(context);
        }
    }

    protected void applyExtractionIfNeeded(final Context context) {
        final String origType = context.getCurrentResponse().getWebResponse().getContentType();
        if (fXpath != null || fHtmlId != null) {
            ContextHelper.defineAsCurrentResponse(context, StoreElementAttribute.findElement(context.getCurrentResponse(),
                    getHtmlId(), getXpath(), LOG, this).asXml(), origType, getClass());
        }
    }

    private void tryLocateText(final Context context) {
        final String origType = context.getCurrentResponse().getWebResponse().getContentType();
        try {
            ContextHelper.defineAsCurrentResponse(context, getTableLocator().locateText(context, this), origType,
                    getClass());
        } catch (TableNotFoundException tableNotFound) {
            throw new StepFailedException("Cannot find table: " + tableNotFound.toString(), this);
        } catch (IndexOutOfBoundsException ioobe) {
            throw new StepFailedException("Cannot find cell with supplied index in table", this);
        } catch (SAXException se) {
            throw new StepFailedException("Can't parse table: " + se.getMessage(), this);
        }
    }
    
    public boolean isPerformingAction() {
    	return false;
    }
}
