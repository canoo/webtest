// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Provides the ability to click on a submit button.
 *
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 * @author Denis N. Antonioli
 * @webtest.step category="Core"
 * name="clickButton"
 * alias="clickbutton"
 * description="This step is used to locate a form button and then click it."
 */
public class ClickButton extends AbstractIdOrLabelTarget {
	private static final Logger LOG = Logger.getLogger(ClickButton.class);
	private static final Set INPUT_BUTTONS_TYPES = new HashSet();
	private String fName;
	private String fFieldIndex;
	private String fX;
	private String fY;

	static {
		INPUT_BUTTONS_TYPES.add(HtmlConstants.SUBMIT);
		INPUT_BUTTONS_TYPES.add(HtmlConstants.IMAGE);
		INPUT_BUTTONS_TYPES.add(HtmlConstants.BUTTON);
		INPUT_BUTTONS_TYPES.add(HtmlConstants.RESET);
	}

	/**
	 * @webtest.parameter required="yes/no"
	 * description="The NAME attribute for the button of interest.
	 * Name has lower precedence than <em>htmlId</em>."
	 */
	public void setName(String name) {
		fName = name;
	}

	public String getName() {
		return fName;
	}

	/**
	 * Sets the index of the button to click (starting with 0) within the buttons
	 * identified with the other criteria.
	 *
	 * @param index the new value
	 * @webtest.parameter required="no"
	 * default="0"
	 * description="The index (starting with 0) of the button to click within the buttons having the specified label and/or name. Useful for instance to distinguish two buttons having the same name."
	 */
	public void setFieldIndex(final String index) {
		fFieldIndex = index;
	}

	public String getFieldIndex() {
		return fFieldIndex;
	}

	/**
	 * @webtest.parameter required="no"
	 * description="Optional X coordinate of click within an image button. If set, Y coordinate must also be set."
	 */
	public void setX(String clickPositionX) {
		fX = clickPositionX;
	}

	public String getX() {
		return fX;
	}

	/**
	 * @webtest.parameter required="no"
	 * description="Optional Y coordinate of click within an image button. If set, X coordinate must also be set."
	 */
	public void setY(String clickPositionY) {
		fY = clickPositionY;
	}

	public String getY() {
		return fY;
	}


	/**
	 * @deprecated use setFieldIndex instead
	 */

	public void setIndex(final int index) {
		LOG.warn("setIndex is deprecated - use setFieldIndex instead");
		setFieldIndex(Integer.toString(index));
	}

	/**
	 * Finds the button in the page according to the properties set on this step
	 *
	 * @param page the page to search in
	 * @return the button, <code>null</code> if not found
	 */
	protected HtmlElement findClickableElementByAttribute(final HtmlPage page) {
		HtmlElement button = null;
		// look for the button in the current form
		if (getContext().getCurrentForm() != null) {
			LOG.debug("Looking for button in current form");
			button = findButton(getContext().getCurrentForm());
		}
		// if not found look at the other forms
		if (button == null) {
			button = findButtonAllForms(page);
		}
		return button;
	}

	protected Page findTarget() throws XPathException, IOException, SAXException {
		final HtmlElement button = findClickableElement(getContext().getCurrentHtmlResponse(this));
		if (button == null) {
			throw buildNoButtonFoundException();
		}

		LOG.info("-> findTarget(by " + button.getTagName() + "): name="
				+ button.getAttribute("name") + " value="
				+ button.getAttribute("value"));

		if (isImageButton()) {
			LOG.info("-> findTarget(by " + button.getTagName() + "): name="
					+ button.getAttribute("name") + " value="
					+ button.getAttribute("value"));
			return ((HtmlInput) button).click(Integer.parseInt(getX()), Integer.parseInt(getY()));
		}
		return button.click();
	}

	/**
	 * Builds an exception with helpfull information
	 * @return the exception
	 */
	private StepFailedException buildNoButtonFoundException() {
		final StepFailedException e = new StepFailedException("No button found", this);

		final HtmlForm currentForm = getContext().getCurrentForm();
		final StringBuffer msg = new StringBuffer();
		if (currentForm != null) {
			msg.append("In current form:\n");
			msg.append(getButtonsDescription(currentForm));
		}

		final Iterator formsIterator = getContext().getCurrentHtmlResponse(this).getForms().iterator();
		while (formsIterator.hasNext()) {
			final HtmlForm form = (HtmlForm) formsIterator.next();
			if (form != currentForm)
			{
				if (msg.length() != 0)
					msg.append("\n\n");
				msg.append("In " + form + ":\n");
				msg.append(getButtonsDescription(form));
			}
		}
		
		e.addDetail("available buttons", msg.toString());
		return e;
	}

	private String getButtonsDescription(final HtmlForm _form) {
		final List buttons = new ArrayList();
		for (final Iterator iter=_form.getHtmlElementDescendants().iterator(); iter.hasNext(); ) {
		    final HtmlElement element = (HtmlElement) iter.next();
			if ((element instanceof HtmlInput) 
					&& isInputButtonType((HtmlInput) element)) {
				buttons.add(element);
			}
			else if (element instanceof HtmlButton) {
				buttons.add(element);
			}
		}
		
		if (buttons.isEmpty())
			return "none";
		else
			return buttons.toString();
	}

	protected String getLogMessageForTarget() {
		return "by clickButton with name: " + getName();
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullResponseCheck();

		paramCheck((StringUtils.isEmpty(getX()) && !StringUtils.isEmpty(getY())) || (!StringUtils.isEmpty(getX()) && StringUtils.isEmpty(getY())),
				"X and Y values must be set for click button support!");

		optionalIntegerParamCheck(getFieldIndex(), "fieldIndex", true);
		optionalIntegerParamCheck(getX(), "x", false);
		optionalIntegerParamCheck(getY(), "y", false);
	}


	/**
	 * Checks that the element is of the desired html type and has the right name and label (if needed)
	 *
	 * @param elt the button to check
	 * @return the button if ok, <code>null</code> otherwise
	 */
	HtmlElement checkFoundElement(final HtmlElement elt) throws StepFailedException {
		// check that it is a "button"
		if (!isButton(elt)) {
			throw new StepFailedException("Selected element is a " + elt.getTagName() + " tag and not a button", this);
		}

		if (hasMatchingNameOrDontCare(elt) && hasMatchingLabelOrDontCare(elt)) {
			LOG.debug("Button passes test with label and name");
			return elt;
		}
		LOG.debug("Test with name and label fails for html button: " + elt);
		return null;
	}

	/**
	 * Looks for the first button (that may be an input of type submit, image or button or a "normal" button)
	 * in the given form corresponding to the criterias
	 *
	 * @param form the form in which the button should be searched
	 * @return the button, <code>null</code> if not found
	 */
	HtmlElement findButton(final HtmlForm form) {
		LOG.debug("Looking for inputs of type submit, image or button in " + form);
		HtmlElement button = findInputButton(form);
		if (button != null) {
			return button;
		}
		LOG.debug("Looking for \"normal\" button in " + form);
		return findNormalButton(form);
	}

	private HtmlElement findButtonAllForms(final HtmlPage currentResp) {
		LOG.debug("Looking for button in all forms contained in the document");
		List forms = currentResp.getForms();
		if (forms.size() == 0) {
			LOG.warn("No forms found - page probably non-compliant - searching page anyway");
			return searchButton(currentResp);
		}
		for (final Iterator iter = forms.iterator(); iter.hasNext();) {
			HtmlElement button = findButton((HtmlForm) iter.next());
			if (button != null) {
				return button;
			}
		}
		return null;
	}

	private static boolean isButton(HtmlElement elt) {
		if (elt instanceof HtmlButton) {
			LOG.debug("It's a button, that's ok");
			return true;
		}
		if (elt instanceof HtmlInput && isInputButtonType((HtmlInput) elt)) {
			LOG.debug("It's an " + elt.getAttribute(HtmlConstants.TYPE) + " input, that's ok");
			return true;
		}
		LOG.debug("Html element is not a button");
		return false;
	}

	private static boolean isInputButtonType(final HtmlInput input) {
		return INPUT_BUTTONS_TYPES.contains(input.getTypeAttribute().toLowerCase());
	}

	private HtmlElement findInputButton(final HtmlForm form) {
		final Collection inputButtons = form.getHtmlElementsByTagName(HtmlConstants.INPUT);
		return findInputButton(inputButtons.iterator());
	}

	private HtmlElement searchButton(final HtmlPage page) {
		final Collection buttons = new ArrayList();
		final Iterator childElements = page.getHtmlElementDescendants().iterator();
		while (childElements.hasNext()) {
			final HtmlElement elt = (HtmlElement) childElements.next();
			if (isButton(elt)) {
				buttons.add(elt);
			}
		}
		HtmlElement button = findInputButton(buttons.iterator());
		if (button == null) {
			button = findNormalButton(buttons.iterator());
		}
		return button;
	}

	private HtmlElement findInputButton(final Iterator candidateIterator) {
		int indexFound = 0; // should index be across both button types? currently not
		while (candidateIterator.hasNext()) {
			final HtmlElement curElement = (HtmlElement) candidateIterator.next();
			if (!(curElement instanceof HtmlInput)) {
				continue;
			}
			final HtmlInput curInput = (HtmlInput) curElement;
			if (!isInputButtonType(curInput)) {
				continue; // not a "button"
			}
			LOG.debug("Examining button: " + curInput);
			if (hasMatchingNameOrDontCare(curInput) && hasMatchingLabelOrDontCare(curInput)) {
				if (indexFound == ConversionUtil.convertToInt(getFieldIndex(), 0)) {
					LOG.debug(curInput.getTypeAttribute() + " button found: " + curInput);
					return curInput;
				}
				++indexFound;
			}
		}
		return null;
	}

	private HtmlElement findNormalButton(final HtmlForm form) {
		return findNormalButton(form.getHtmlElementDescendants().iterator());
	}

	private HtmlElement findNormalButton(final Iterator candidateIterator) {
		int indexFound = 0; // should index be across both button types? currently not
		while (candidateIterator.hasNext()) {
			final HtmlElement curElement = (HtmlElement) candidateIterator.next();
			if (!(curElement instanceof HtmlButton)) {
				continue;
			}
			final HtmlButton curButton = (HtmlButton) curElement;
			LOG.debug("Examining button: " + curButton);
			if (hasMatchingNameOrDontCare(curButton) && hasMatchingLabelOrDontCare(curButton)) {
				if (indexFound == ConversionUtil.convertToInt(getFieldIndex(), 0)) {
					LOG.debug("Normal button found: " + curButton);
					return curButton;
				}
				++indexFound;
			}
		}
		return null;
	}

	private boolean hasMatchingNameOrDontCare(final HtmlElement curButton) {
		if (curButton instanceof HtmlInput) {
			return hasMatchingNameOrDontCare((HtmlInput) curButton);
		}
		if (curButton instanceof HtmlButton) {
			return hasMatchingNameOrDontCare((HtmlButton) curButton);
		}
		throw new IllegalArgumentException("Button is neither a HtmlInput nor a HtmlButton: " + curButton);
	}

	private boolean hasMatchingNameOrDontCare(final HtmlInput curButton) {
		return getName() == null || getName().equals(curButton.getNameAttribute());
	}

	private boolean hasMatchingNameOrDontCare(final HtmlButton curButton) {
		return getName() == null || getName().equals(curButton.getNameAttribute());
	}

	private boolean hasMatchingLabelOrDontCare(final HtmlElement curButton) {
		if (getLabel() == null)
			return true;
		else if (curButton instanceof HtmlImageInput)
		{
			return getLabel().equals(((HtmlImageInput) curButton).getAltAttribute()); 
		}
		else  
		{
			return getLabel().equals(curButton.asText());
		}
	}

	protected boolean isImageButton() {
		return !StringUtils.isEmpty(getX()) && !StringUtils.isEmpty(getY());
	}

    /**
     * Called by Ant to set the text nested between opening and closing tags.
     * @param text the text to set
     * @webtest.nested.parameter
     *    required="no"
     *    description="Alternative way to set the 'label' attribute."
     */
    public void addText(final String text) {
 	    setLabel(getProject().replaceProperties(text));
 	}

    // override to specify doc specific for clickButton
	/**
	 * @param xpath The xpath to set.
	 * @webtest.parameter required="no"
	 * description="The xpath of the button to click."
	 */
	public void setXpath(final String xpath) {
		super.setXpath(xpath);
	}
	/**
	 * @param newLabel The Label to set.
	 * @webtest.parameter required="no"
	 * description="The label of the button to click.
	 * This is the text representation for a text button or the alt attribute for an image button"
	 */
	public void setLabel(final String newLabel) {
		super.setLabel(newLabel);
	}

	/**
	 * @param htmlId The HtmlId to set.
	 * @webtest.parameter required="no"
	 * description="The html id of the button to click."
	 */
	public void setHtmlId(final String htmlId) {
		super.setHtmlId(htmlId);
	}
}
