// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.IStringVerifier;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.request.TargetHelper;
import com.canoo.webtest.util.ConversionUtil;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

/**
 * Selects one or many elements of a select field ( &lt;select name="foo"&gt;...
 * &lt;/select&gt;)
 *
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="setSelectField"
 * alias="new_setselectfield,setselectfield"
 * description="Provides the ability to update select fields in <key>HTML</key> forms."
 */
public class SetSelectField extends AbstractSetNamedFieldStep {
	private static final Logger LOG = Logger.getLogger(SetSelectField.class);
	static final String MESSAGE_MISSING_OPTION_IDENTIFIER = "Either \"value\", \"text\" or \"optionIndex\" is required to identify option in select identified by \"htmlId\" or \"xpath\"!";
    static final String AT_MOST_ONE_VALUE_TEXT_OPTIONINDEX = "At most one of \"value\", \"text\" or \"optionIndex\" can be set!";

    private boolean fIsMultiSelect, fIsRegex;
    private String fMultiSelect;
    private String fRegex;
    private String fText;
    private String fOptionIndex;
    private String fUserName;
    private String fPassword;
    private String fSavePrefix;
    private String fSaveResponse;
    private final TargetHelper fTargetHelper = new TargetHelper(this);

    /**
     * Gets the value of the regex attribute. The value is only available after the parameter has been validated.
     * @return <code>true</code> if {@link #getText()} is a regular expression
     */
    public boolean isRegex() {
        return fIsRegex;
    }

	/**
	 * Gets the value of the multiSelect attribute. The value is only available after the parameter has been validated.
	 * @return <code>true</code> if multiple selection is allowed
	 */
	public boolean isMultiSelect() {
		return fIsMultiSelect;
	}

	/**
	 * @webtest.parameter required="false"
	 * default="false"
	 * description="Specifies whether multiple selections are allowed. Unless set to true, every setselect overrides the value of preceding calls."
	 */
	public void setMultiselect(final String multiSelect) {
		fMultiSelect = multiSelect;
	}

	public String getMultiselect() {
		return fMultiSelect;
	}

	/**
	 * @webtest.parameter required="no"
	 * default="false"
	 * description="Specifies whether the option text represents a regular expression."
	 */
	public void setRegex(final String regex) {
		fRegex = regex;
	}

	public String getRegex() {
		return fRegex;
	}

	/**
	 * @webtest.parameter required="yes/no"
	 * description="The text of the option to select (i.e. the text nested in the option tag. One of <em>text</em>, <em>value</em> or <em>optionIndex</em> is required."
	 */
	public void setText(final String text) {
		fText = text;
	}

	public String getText() {
		return fText;
	}

	/**
	 * @param index The new index value
	 * @webtest.parameter required="yes/no"
	 * description="The index of the option to select (i.e. the position of the option in the select starting with 0). One of <em>text</em>, <em>value</em> or <em>optionIndex</em> is required."
	 */
	public void setOptionIndex(final String index) {
		fOptionIndex = index; // option index (do we eventually need both of these?)
	}

	public String getOptionIndex() {
		return fOptionIndex;
	}

	/**
	 * @param userName
	 * @webtest.parameter required="no"
	 * description="A username that can be provided for pages that require basic authentication. Only needed if setting the select field invokes <key>javascript</key> and causes the page to move to a secure page. Required if <em>password</em> is specified."
	 */
	public void setUserName(String userName) {
		fUserName = userName;
	}

	public String getUserName() {
		return fUserName;
	}

	/**
	 * @param password
	 * @webtest.parameter required="no"
	 * description="A password that can be provided for pages that require basic authentication. Required if <em>userName</em> is specified."
	 */
	public void setPassword(String password) {
		fPassword = password;
	}

	public String getPassword() {
		return fPassword;
	}

	/**
	 * @webtest.parameter required="no"
	 * default="the 'savePrefix' parameter as specified in &lt;config&gt;."
	 * description="A name prefix can be specified for making a permanent copy of any received responses. Only needed if setting the select field invokes <key>javascript</key> which causes the browser to move to another page."
	 */
	public void setSavePrefix(String prefix) {
		fSavePrefix = prefix;
	}

	public String getSavePrefix() {
		return fSavePrefix;
	}

	/**
	 * @webtest.parameter required="no"
	 * description="Whether to make a permanent copy of any received responses. Overrides the default value set in the <config> element. Only needed if setting the select field invokes <key>javascript</key> which causes the browser to move to another page."
	 */
	public void setSaveResponse(String response) {
		fSaveResponse = response;
	}

	public String getSaveResponse() {
		return fSaveResponse;
	}

	public String getSave() {
		return null;
	}

	/**
	 * @param index
	 * @deprecated use setOptionIndex instead
	 */
	public void setIndex(final String index) {
		LOG.warn("setIndex is deprecated - use setOptionIndex instead");
		setOptionIndex(index);
	}

	/**
	 * Set the value
	 *
	 * @param value
	 * @webtest.parameter required="yes/no"
	 * description="The value of the option to select (i.e. the value of the \"value\" attribute of a select element). One of <em>text</em>, <em>value</em> or <em>optionIndex</em> is required."
	 */
	public void setValue(final String value) {
		super.setValue(value);
	}

	protected HtmlForm findForm() {
		return FormUtil.findFormForField(getContext(), getFormName(), HtmlConstants.SELECT, null, getName(), this);
	}

	/**
	 * Finds all relevant fields with the given name in the form.
	 *
	 * @param form The form to search
	 * @return The list of fields with the given name
	 */
	protected List findFields(final HtmlForm form) {
		return form.getSelectsByName(getName());
	}

	protected void setField(final HtmlElement elt) {
		final HtmlSelect select;
		final HtmlOption option;
		if (elt instanceof HtmlOption)
		{
			option = (HtmlOption) elt;
			select = (HtmlSelect) option.getEnclosingElement(HtmlConstants.SELECT); // TODO: can be simplified with next htmlunit build
		}
		else if (elt instanceof HtmlSelect)
		{
			select = (HtmlSelect) elt;
			// if htmlId or xpath specified, we know now first that text, value or optionIndex is needed 
			if (getText() == null && getOptionIndex() == null && getValue() == null)
				throw new StepExecutionException(MESSAGE_MISSING_OPTION_IDENTIFIER, this);
			option = findMatchingOption(select);
		}
		else
		{
			throw new StepFailedException("Found " + elt.getTagName() +
				" when looking for select", this);
		}

		if (select.isMultipleSelectEnabled() && !fIsMultiSelect) {
			deselectOtherOptions(select, option);
		}
		updateOption(select, option);
	}

	void updateOption(final HtmlSelect select, final HtmlOption option) {
		if (option == null) {
			throw new StepFailedException("No option found matching criteria in select " + select);
		}
		fTargetHelper.setUsername(getUserName());
		fTargetHelper.setPassword(getPassword());
		maybeTarget(option.getPage(), select, option);
	}

	protected void maybeTarget(final Page page, final HtmlSelect select, final HtmlOption option) {
		LOG.debug("Selected option: " + option);
		select.setSelectedAttribute(option, true);
	}

	private static void deselectOtherOptions(final HtmlSelect select, final HtmlOption option) {
		for (final Iterator iter = select.getOptions().iterator(); iter.hasNext();) {
			final HtmlOption curOpt = (HtmlOption) iter.next();
			if (curOpt != option && curOpt.isSelected()) {
				curOpt.setSelected(false);
			}
		}
	}

	/**
	 * Selects the option specified by value, text or index and returns it.<p>
	 *
	 * @param select the <select> containing the option
	 * @return the selected option
	 */
	HtmlOption findMatchingOption(final HtmlSelect select) throws StepExecutionException {
		LOG.debug("Searching for the right option in " + select);
		if (getValue() != null) {
			LOG.debug("Searching option with value: " + getValue());
			try {
				return select.getOptionByValue(getValue());
			}
			catch (final ElementNotFoundException enfe) {
				LOG.debug(enfe.getMessage());
				return null;
			}
		}
		else if (getText() != null) {
			LOG.debug("Searching option with text: " + getText());
			return getOptionForText(select, getText());
		}
		else
		{
			LOG.debug("Searching option with index: " + getOptionIndex());
			return (HtmlOption) select.getOptions().get(ConversionUtil.convertToInt(getOptionIndex(), 0));
		}
	}

	/**
	 * Search in the select the first option element that has the given
	 * text <p/>
	 *
	 * @param select the select in which to search
	 * @param text   The text representing a particular value
	 * @return The option element corresponding to the specified text
	 */
	private HtmlOption getOptionForText(final HtmlSelect select, final String text) {
		final IStringVerifier verifier = getVerifier(fIsRegex);
		for (final Iterator iter = select.getOptions().iterator(); iter.hasNext();) {
			final HtmlOption option = (HtmlOption) iter.next();
			LOG.debug("Examining option: " + option);
			if (verifier.verifyStrings(text, option.asText())) {
				LOG.debug("Found option by text: " + option);
				return option;
			}
		}
		throw new StepFailedException("No option element found with text \"" + text + "\"", this);
	}

	protected void verifyParameters() {
		super.verifyParameters();

		int iNbNotNull = 0;
		if (!isValueNull()) {
			++iNbNotNull;
		}
		if (getText() != null) {
			++iNbNotNull;
		}
		if (getOptionIndex() != null) {
			integerParamCheck(getOptionIndex(), "optionIndex", false);
			++iNbNotNull;
		}
		final boolean bXPathOrId = getXpath() != null || getHtmlId() != null;
		
		paramCheck(iNbNotNull > 1, AT_MOST_ONE_VALUE_TEXT_OPTIONINDEX);
		paramCheck(!bXPathOrId  && iNbNotNull == 0,
                "One of \"xpath\", \"htmlId\", \"value\", \"text\" or \"optionIndex\" is required!");
		fIsMultiSelect = ConversionUtil.convertToBoolean(getMultiselect(), false);
		fIsRegex = ConversionUtil.convertToBoolean(getRegex(), false);
	}
}
