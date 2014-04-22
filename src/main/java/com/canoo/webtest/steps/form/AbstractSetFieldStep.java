// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.canoo.webtest.engine.IStringVerifier;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.extension.StoreElementAttribute;
import com.canoo.webtest.steps.AbstractBrowserAction;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Abstract class for steps which update form fields.
 * This class handles the attributes name/formName, htmlId or xpath.
 *
 * @author Marc Guillemot
 * @author Paul King
 * @author Denis N. Antonioli
 */
public abstract class AbstractSetFieldStep extends AbstractBrowserAction {
	private static final Logger LOG = Logger.getLogger(AbstractSetFieldStep.class);

	public static final String MESSAGE_ARGUMENT_MISSING = "One of 'forLabel', 'htmlId', 'name', or 'xpath' must be set!";
	public static final String MESSAGE_ARGUMENT_REDUNDANT = "Only one of 'forLabel', 'htmlId', 'name', and 'xpath' should be set!";

	private String fName;
    private String fXPath;
    private String fFormName;
    private String fFieldIndex;
    private String fHtmlId;
    private String fForLabel;

    /**
     * Set the name.
     * @param name
     * @webtest.parameter required="yes/no"
     * description="The name of the input field of interest.
     * One of <em>forLabel</em>, <em>htmlId</em>, <em>name</em>, or <em>xpath</em> is required."
     */
    public void setName(final String name) {
        fName = name;
    }

    public String getName() {
        return fName;
    }

    /**
     * Set the text of the label associated with the field to set.
     * @param text the label text
     * @webtest.parameter required="yes/no"
     * description="The text of the label field associated with the input field of interest. 
     * One of <em>forLabel</em>, <em>htmlId</em>, <em>name</em>, or <em>xpath</em> is required."
     */
    public void setForLabel(final String text) {
        fForLabel = text;
    }

    public String getForLabel() {
        return fForLabel;
    }

    public String getXpath() {
        return fXPath;
    }

    /**
	 * Set the xpath.
	 *
	 * @param xpath
	 * @webtest.parameter required="yes/no"
	 * description="The xpath of the input field of interest. 
     * One of <em>forLabel</em>, <em>htmlId</em>, <em>name</em>, or <em>xpath</em> is required."
	 */
	public void setXpath(final String xpath) {
		fXPath = xpath;
	}

    /**
     * Set the form name.
     * @param formName
     * @webtest.parameter required="no"
     * default="the last form selected using 'selectForm', otherwise searches all forms"
     * description="The name of the form containing the field of interest. Ignored if <em>htmlId</em> is used."
     */
    public void setFormName(final String formName) {
        fFormName = formName;
    }

    public String getFormName() {
        return fFormName;
    }

    public String getHtmlId() {
        return fHtmlId;
    }

    /**
	 * Set the html id.
	 *
	 * @param htmlId
	 * @webtest.parameter required="yes/no"
	 * description="The id of the input field of interest. 
     * One of <em>forLabel</em>, <em>htmlId</em>, <em>name</em>, or <em>xpath</em> is required."
	 */
	public void setHtmlId(final String htmlId) {
		fHtmlId = htmlId;
	}

	/**
	 * Set the index.
	 *
	 * @param index
	 * @webtest.parameter required="no"
	 * default="the first field found that matches criteria"
	 * description="The index of the field of interest (starting at 0) if more than one field matches criteria. Ignored if <em>htmlId</em> or <em>xpath</em> is used."
	 */
	public void setFieldIndex(final String index) {
		fFieldIndex = index;
	}

	public String getFieldIndex() {
		return fFieldIndex;
	}

	public void doExecute() throws SAXException, IOException {
		if (fName != null) {
			final HtmlForm form = findForm();
			if (form == null) {
				throw new StepFailedException("No suitable form found having field named \"" + getName() + "\"", this);
			}
			LOG.debug("Found matching form " + form);
			setField(selectField(trimFields(findFields(form)), getFieldIndex(), this));
		}
		else if (getForLabel() != null)
		{
			setField(findFieldByLabel(getContext().getCurrentHtmlResponse(this), getForLabel()));
		}
		else { // htmlId, xpath
			setField(StoreElementAttribute.findElement(getContext().getCurrentHtmlResponse(this), getHtmlId(), getXpath(), LOG, this));
		}
	}

	/**
	 * Retrieves the (first) field associated with the label containing the provided text
	 * @param page the page to search in
	 * @param labelText the text of the label
	 * @return the associated form field
	 * @throws StepFailedException if no field is found
	 */
	HtmlElement findFieldByLabel(final HtmlPage page, final String labelText)
	{
		LOG.debug("Searching label tag with text: " + labelText);
		final List labels = page.getDocumentElement().getHtmlElementsByTagName("label");
		LOG.debug(labels.size() + " found in the page");
		
		final IStringVerifier verifier = getVerifier(false);
		for (final Iterator iter=labels.iterator(); iter.hasNext();)
		{
			final HtmlLabel label = (HtmlLabel) iter.next();
			if (verifier.verifyStrings(labelText, label.asText()))
			{
				LOG.debug("Found label with matching text, examining the associated field: " + label);
				final HtmlElement target = label.getReferencedElement();
				if (keepField(target))
				{
					LOG.debug("Found field: " + target);
					return target;
				}
				else
				{
					LOG.debug("Target doesn't match: " + target);
				}
			}
		}
		throw new StepFailedException("No label found with text \"" + labelText + "\"", this);
	}

	/**
	 * Sets a field according to the step.
	 * It is up to the step's implementation to decide how to set the step.
	 *
	 * @param field The field to set.
	 */
	protected abstract void setField(HtmlElement field) throws IOException;

	/**
	 * Finds the relevant form.
	 *
	 * @return The found form.
	 */
	protected abstract HtmlForm findForm();

	/**
	 * Finds all possible input fields. This is a generic implementation, sub-classes may want to take advantage
	 * of more specific functions.
	 *
	 * @param form The form to search.
	 * @return A list of candidate fields.
	 */
	protected List findFields(final HtmlForm form) {
		return form.getInputsByName(fName);
	}

	/**
	 * Apply {@link #keepField(com.gargoylesoftware.htmlunit.html.HtmlElement)} to trim the list of fields found.
	 *
	 * @param fields All fields found.
	 * @return A list of candidate fields.
	 */
	protected List trimFields(final List fields) {
		for (final Iterator iter = fields.iterator(); iter.hasNext();) {
			final HtmlElement elt = (HtmlElement) iter.next();
			LOG.debug("Considering element " + elt);
			if (!keepField(elt)) {
				iter.remove();
			}
		}
		LOG.debug("Found " + fields.size() + " field(s)");
		return fields;
	}

	/**
	 * Called by {@link #findFields(com.gargoylesoftware.htmlunit.html.HtmlForm)} to filter out elements
	 * with the correct name but not matching some other selection criteria.
	 * @param elt One of the elements with the correct name.
	 * @return True if the element is accepted.
	 */
	protected boolean keepField(HtmlElement elt) {
		return true;
	}

	/**
	 * Finds the desired field by selecting either a specific field designated by
	 * indexStr or the first one if indexStr is left blank
	 *
	 * @param fieldList A list of {@link HtmlElement fields}.
	 * @param indexStr The index of the desired field.
	 * @param step The calling step, for exception.
	 * @return The selected field
	 */
	public static HtmlElement selectField(final List fieldList, final String indexStr, final Step step) {
		if (fieldList.isEmpty()) {
			throw new StepFailedException("No suitable field(s) found", step);
		}

		int numFieldsFound = fieldList.size();
		int index;
		if (StringUtils.isEmpty(indexStr)) {
			LOG.info("Found " + numFieldsFound + " suitable fields, considering only the first one");
			index = 0;
		} else {
			index = ConversionUtil.convertToInt(indexStr, 0);
			if (index < 0 || index >= numFieldsFound) {
				throw new StepFailedException("Can't set field with index '" + index + "', valid range is 0.." + (numFieldsFound - 1), step);
			}
		}
		return (HtmlElement) fieldList.get(index);
	}

	protected void verifyParameters() {
		super.verifyParameters();

		nullResponseCheck();

		int count = 0;
		if (fXPath != null) {
			count++;
		}
		if (fName != null) {
			count++;
		}
		if (fHtmlId != null) {
			count++;
		}
		if (getForLabel() != null) {
			count++;
		}
		paramCheck(count == 0, MESSAGE_ARGUMENT_MISSING);
		paramCheck(count > 1, MESSAGE_ARGUMENT_REDUNDANT);
		if (fName == null) {
			paramCheck(fFieldIndex != null, "The attribute 'fieldIndex' is only valid with the attribute 'name'.");
		} else {
			optionalIntegerParamCheck(fFieldIndex, "fieldIndex", false);
		}
	}
}
