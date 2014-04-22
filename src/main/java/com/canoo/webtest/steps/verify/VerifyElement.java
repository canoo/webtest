// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.Iterator;
import java.util.List;

/**
 * @author unknown
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="verifyElement"
 * alias="verifyelement"
 * description="This step verifies the existence of a particular <key>HTML</key> element (tag) in the result page. It is identified by its <key>HTML</key> element type (e.g. TITLE, HEAD, etc.) and the <em>text</em> value of the NAME attribute."
 */
public class VerifyElement extends AbstractVerifyTextStep {
    private String fType;

    public String getType() {
        return fType;
    }

    /**
     * @webtest.parameter required="yes"
     * description="The <key>HTML</key> element type, e.g. "INPUT" for a <INPUT.../>  tag or "title" for <title>...</title>."
     */
    public void setType(final String newType) {
        fType = newType;
    }

    public void doExecute() {
        int numberOfHits = getNumberOfHits();

        if (numberOfHits > 1) {
            throw new StepFailedException("More than 1 element with type \""
                    + fType + "\" and name \"" + getText() + "\" found!", this);
        }
        if (numberOfHits == 0) {
            throw new StepFailedException("No element of type \"" + fType
                    + "\" and name \"" + getText() + "\" found!", this);
        }
    }

    /**
     * Gets the number of hits in the current response.
     * @deprecated Use {@link #getNumberOfHits()}.
     * @param context The execution context.
     * @return The number of hits.
     */
    protected int getNumberOfHits(final Context context) {
        return getNumberOfHits();
    }

    protected int getNumberOfHits() {
        Context context = getContext();
        int numberOfHits = findElementAttributeValue(context, getType(), ELEMENT_ATTRIBUTE_NAME, getText());

        if (numberOfHits == 0) {
            numberOfHits = findElementAttributeValue(context, getType(), ELEMENT_ATTRIBUTE_ID, getText());
        }
        return numberOfHits;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getType(), "type");
    }

    private int findElementAttributeValue(final Context context, final String elementType, final String attributeName, String value) {
        final List li = getAllElementsOfType(context, elementType.toLowerCase());
        return getNumberOfOccurances(li, value, attributeName);
    }

    /**
     * Gets all elements of the given type from the current html response
     *
     * @param context
     * @param type
     * @return a list of {@link com.gargoylesoftware.htmlunit.html.HtmlElement}
     */
    protected static List getAllElementsOfType(final Context context, final String type) {
        return ((HtmlPage) context.getCurrentResponse()).getDocumentElement().getHtmlElementsByTagName(type);
    }

    /**
     * Gets the number of elements having the value of the specified attribute matching the test
     * @param li
     * @param expectedString
     * @param attributeName
     */
    protected int getNumberOfOccurances(final List li, final String expectedString, final String attributeName) {
        int numberOfHits = 0;
        for (Iterator iter = li.iterator(); iter.hasNext();) {
            HtmlElement elt = (HtmlElement) iter.next();
            if (verifyStrings(expectedString, elt.getAttribute(attributeName))) {
                ++numberOfHits;
            }
        }
        return numberOfHits;
	}
}
