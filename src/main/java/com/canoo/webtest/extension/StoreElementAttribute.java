// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.store.BaseStoreStep;
import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;

/**
 * StoreElementAttribute
 *
 * @author Paul King
 * @webtest.step category="Extension"
 * name="storeElementAttribute"
 * description="Retrieves a particular attribute value of an element. E.g. the <em>style</em> attribute could be retrieved for an <key>HTML</key> element such as <TD style='color:green' ...>"
 */
public class StoreElementAttribute extends BaseStoreStep {
    private static final Logger LOG = Logger.getLogger(StoreElementAttribute.class);
    private String fHtmlId;
    private String fXPath;
    private String fAttributeName;

    /**
     * Locate all applicable html elements, check their number (size == 1) and
     * store the value of the attribute.
     *
     * @throws com.canoo.webtest.engine.StepFailedException
     *          if no applicable button was found
     */
    public void doExecute() throws Exception {
        nullResponseCheck();
        final HtmlElement element = findElement(getContext().getCurrentResponse(), getHtmlId(), getXpath(), LOG, this);
        String retval = element.getAttribute(getAttributeName());
        if (retval == HtmlElement.ATTRIBUTE_NOT_DEFINED) {
            retval = "undefined";
        } else if (retval == HtmlElement.ATTRIBUTE_VALUE_EMPTY || retval.length() == 0) {
            retval = "empty";
        }
        
        storeProperty(retval);
    }

    /**
     * Search for the element by id or xpath.<p>
     *
     * @param currentResp the html page in which to search
     * @param id
     * @param xpathStr
     * @param log
     * @param step
     * @return the element
     * @throws com.canoo.webtest.engine.StepFailedException if no element is found
     */
    public static HtmlElement findElement(final Page currentResp, final String id, final String xpathStr,
                                          final Logger log, final Step step) throws StepFailedException {
        if (id != null) {
            return findElementById(currentResp, id, log, step);
        }

        return findElementByXpath(currentResp, xpathStr, log, step);
    }

    static HtmlElement findElementById(final Page currentResp, final String id,
                                       final Logger log, final Step step) throws StepFailedException {
        log.debug("Looking for element with id \"" + id + "\"");
        try {
            if (!(currentResp instanceof HtmlPage)) {
                throw new StepExecutionException("Current response is not an HTML page but of type "
                    + currentResp.getWebResponse().getContentType(), step);
            }
            HtmlPage lastHtmlResp = (HtmlPage) currentResp;
            final HtmlElement element = lastHtmlResp.getHtmlElementById(id);
            log.debug("found element with id \"" + id + "\": " + element);
            return element;
        } catch (final ElementNotFoundException e) {
            throw new StepFailedException("No element found with id \"" + id + "\".", step);
        }
    }

    static HtmlElement findElementByXpath(final Page currentResp, final String xpathStr,
                                          final Logger log, final Step step) throws StepFailedException {
        log.debug("Looking for element with xpath \"" + xpathStr + "\"");
        Object node;
        node = HtmlUnitBoundary.trySelectSingleNodeByXPath(xpathStr, currentResp, step);

        if (node == null) {
            throw new StepFailedException("No element found with xpath \"" + xpathStr + "\".", step);
        }

        try {
            return (HtmlElement) node;
        } catch (ClassCastException cce) {
            throw new StepFailedException("The xpath doesn't select an Element: '" + node.getClass() + "'");
        }

    }

    /**
     * Sets the id attribute of the element of interest.<p>
     *
     * @param str the new value
     * @webtest.parameter required="yes/no"
     * description="The id of the html element to click on."
     */
    public void setHtmlId(final String str) {
        fHtmlId = str;
    }

    public String getHtmlId() {
        return fHtmlId;
    }

    /**
     * Sets the XPath used to identify the element of interest.<p>
     *
     * @param path the new value
     * @webtest.parameter required="yes/no"
     * description="The XPath identifying the html element to click on."
     */
    public void setXpath(final String path) {
        fXPath = path;
    }

    public String getXpath() {
        return fXPath;
    }

    /**
     * Sets the name of the attribute of interest.<p>
     *
     * @param name Sets the name of the attribute.
     * @webtest.parameter required="yes"
     * description="The name of the attribute of interest, e.g. \"disabled\" or \"checked\"."
     */
    public void setAttributeName(final String name) {
        fAttributeName = name;
    }

    public String getAttributeName() {
        return fAttributeName;
    }

    /**
     * Sets the Name of the Property.<p>
     *
     * @param name The Property Name
     * @webtest.parameter 
     * required="no" 
     * description="Deprecated. Same as property."
     * @deprecated since 03.2007. Use {@link #setProperty(String)}
     */
    public void setPropertyName(final String name) {
    	LOG.warn("'propertyName' is deprecated. Use 'property' instead");
        setProperty(name);
    }

    /**
     * Verifies the parameters.<p>
     */
    protected void verifyParameters() {
        super.verifyParameters();
        paramCheck(getHtmlId() == null && getXpath() == null, "\"htmlId\" or \"xPath\" must be set!");
        paramCheck(getHtmlId() != null && getXpath() != null, "Only one from \"htmlId\" and \"xPath\" can be set!");
        nullParamCheck(getProperty(), "property");
        nullParamCheck(getAttributeName(), "attributeName");
    }

}
