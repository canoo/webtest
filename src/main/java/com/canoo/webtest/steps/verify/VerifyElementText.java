// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.extension.StoreElementAttribute;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This step verifies if the specified text (CDATA) is enclosed
 * by an HTML element (tag) of the specified type. A name can
 * be specified in case that the element in question appears
 * multiple times. The text can be specified as a regular expression.
 * <p/>
 * Example 1:<br/>
 * HTML source:
 * <pre>
 * &lt;title>The page title&lt;/title>
 * </pre>
 * <p/>
 * Possible statements:
 * <pre>
 * &lt;verifyElementText type="title" text="The page title"/>
 * &lt;verifyElementText type="title" text=".*page title.*" regex="true"/>
 * </pre>
 * <p/>
 * Example 2:
 * HTML source:
 * <pre>
 * &lt;textarea name="Hugo">
 * The very large text area named hugo.
 * &lt;/textarea>
 * </pre>
 * <p/>
 * Possible statements:
 * <pre>
 * &lt;verifyElementText type="textarea" name="Hugo" text="The very large text area named hugo."/>
 * &lt;verifyElementText type="textarea" name="Hugo" text=".*text area.*" regex="true"/>
 * </pre><p/>
 *
 * @author Unknown
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="verifyElementText"
 *   alias="verifyelementtext"
 *   description="This step verifies whether the <key>HTML</key> element specified by <em>htmlId</em> or <em>type</em> 
 *   and optional <em>name</em> encloses <em>text</em> between its opening and closing tags."
 */
public class VerifyElementText extends AbstractVerifyTextStep {
	private static final Logger LOG = Logger.getLogger(VerifyElementText.class);
	private String fType;
	private String fName;
    private String fHtmlId;

	public void doExecute() {
        final HtmlElement elt;
        if (StringUtils.isEmpty(getHtmlId())) {
            elt = findElementByTypeAndOptionalName();
        } 
        else {
            elt = StoreElementAttribute.findElement(getContext().getCurrentResponse(), getHtmlId(), null, LOG, this);
        }
        if (!verifyText(readText(elt))) {
            throw new StepFailedException(getFailedMessage(), getText(), readText(elt), this);
        }
	}

    private HtmlElement findElementByTypeAndOptionalName() {
        final List li = ((HtmlPage) getContext().getCurrentResponse()).getDocumentElement().getHtmlElementsByTagName(getType());
        LOG.debug(li.size() + " elts found of type \"" + getType() + "\"");
        if (li.isEmpty()) {
            throw new StepFailedException("No element found of type \"" + getType() + "\"", this);
        }
        if (li.size() > 1 && getName() == null) {
            throw new StepFailedException("More than 1 element with type \"" + getType()
               + "\" found! No name is specified.",
               this);
        }

        final HtmlElement elt;
        if (getName() != null) {
        	final List namedElements = findNodesWithAttribute(li, ELEMENT_ATTRIBUTE_NAME, getName());

            if (namedElements.isEmpty()) {
                throw new StepFailedException("No element of type \"" + getType() + "\" and name \"" + getName()
                   + "\" found!",
                   this);
            }
            if (namedElements.size() > 1) {
                throw new StepFailedException("More than 1 element of type \"" + getType() + "\" with name \"" + getName()
                   + "\" found!",
                   this);
            }
            elt = (HtmlElement) namedElements.get(0);
        } 
        else {
            elt = (HtmlElement) li.get(0);
        }
        return elt;
    }

    /**
     * Gets the text representation of the element
     * @param elt
     * @return the text for the element
     */
    protected String readText(final HtmlElement elt) {
        LOG.debug("Reading text for " + elt);
        return elt.asText();
    }

	protected static List findNodesWithAttribute(final List li, final String attributeName, final String attributeValue) {
		LOG.debug("Looking in list for elements with attribute \"" + attributeName + "\"'s value: \""
		   + attributeValue + "\"");
		List result = new ArrayList();
		for (Iterator iter = li.iterator(); iter.hasNext();) {
			final HtmlElement elt = (HtmlElement) iter.next();
			final String strValue = elt.getAttribute(attributeName);

			if (attributeValue.equals(strValue)) {
				result.add(elt);
			} else {
				LOG.debug("Value: \"" + strValue + "\" => not ok for " + elt);
			}
		}
		LOG.debug(result.size() + " element(s) found");
		return result;
	}

	protected String getFailedMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("Wrong contents found in HTML element (type=\"");
		sb.append(getType());
		sb.append("\", name=\"");
		sb.append(getName());
		sb.append("\", htmlId=\"");
		sb.append(getHtmlId());
		sb.append("\")!");
		return sb.toString();
	}

	public String getName() {
		return fName;
	}

	/**
	 * @webtest.parameter
	 *    required="no"
	 *    description="The name of the element, i.e. the value of the 'name' attribute. Ignored if using 'htmlId'."
	 */
	public void setName(String newName) {
		fName = newName;
	}

	public String getType() {
		return fType;
	}

	/**
	 * @webtest.parameter
	 *    required="yes/no"
	 *    description="The HTML element (tag) name. One of 'htmlId' or 'type' must be set."
	 */
	public void setType(String newType) {
		fType = newType;
	}

    public String getHtmlId() {
        return fHtmlId;
    }

    /**
     * @webtest.parameter
     *    required="yes/no"
     *    description="The HTML htmlId of the element. One of 'htmlId' or 'type' must be set."
     */
    public void setHtmlId(String htmlId) {
        this.fHtmlId = htmlId;
    }

	protected void verifyParameters() {
		super.verifyParameters();
        paramCheck(StringUtils.isEmpty(getType()) && StringUtils.isEmpty(getHtmlId()),
                "One of 'htmlId' or 'type' must be set.");
	}
}
