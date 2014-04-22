// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.extension.StoreElementAttribute;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.form.AbstractSetFieldStep;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

/**
 * @author Marc Guillemot
 * @author Paul King
 * @author Denis N. Antonioli
 */
public abstract class AbstractVerifyFormStep extends Step {
	private static final Logger LOG = Logger.getLogger(AbstractVerifyFormStep.class);

	private String fName;
	private String fValue;
	private String fFormName;
	private String fFieldIndex;
	private String fHtmlId;
	private String fXPath;

	/**
	 * @param name
	 * @webtest.parameter
	 * 	 required="yes"
	 * description="The xpath of the input field of interest. One of <em>name</em>, <em>htmlId</em> or <em>xpath</em> is required."
	 */
	public void setName(final String name) {
		fName = name;
	}

	public String getName() {
		return fName;
	}


	/**
	 * @param value
	 * @webtest.parameter
	 * 	 required="no"
	 *   description="The value of the input field of interest."
	 */
	public void setValue(final String value) {
		fValue = value;
	}

	public String getValue() {
		return fValue;
	}


	/**
	 * @param formName
	 * @webtest.parameter
	 *   required="no"
	 *   description="The name of the form containing the field of interest."
	 */
	public void setFormName(final String formName) {
		fFormName = formName;
	}

	public String getFormName() {
		return fFormName;
	}


	/**
	 * @param index
	 * @webtest.parameter
	 *   required="no"
	 *   description="The index (starting at 0) of the field of interest (if more than one)."
	 */
	public void setFieldIndex(final String index) {
		fFieldIndex = index;
	}

	public String getFieldIndex() {
		return fFieldIndex;
	}

	/**
	 * Set the xpath.
	 *
	 * @param xpath
	 * @webtest.parameter required="yes/no"
	 * description="The xpath of the input field of interest. One of <em>name</em>, <em>htmlId</em> or <em>xpath</em> is required."
	 */
	public void setXpath(final String xpath) {
		fXPath = xpath;
	}

	public String getXpath() {
		return fXPath;
	}

	/**
	 * Set the html id.
	 *
	 * @param htmlId
	 * @webtest.parameter required="yes/no"
	 * description="The id of the input field of interest. One of <em>name</em>, <em>htmlId</em> or <em>xpath</em> is required."
	 */
	public void setHtmlId(final String htmlId) {
		fHtmlId = htmlId;
	}

	public String getHtmlId() {
		return fHtmlId;
	}

	public void doExecute() throws IOException {
		if (getName() != null) {
			final HtmlForm form = findForm();
			if (form == null) {
				throw new StepFailedException("No suitable form found having field named \"" + getName() + "\"", this);
			}
			verifyField(AbstractSetFieldStep.selectField(findFields(form), getFieldIndex(), this));
		} 
		else {
			verifyField(StoreElementAttribute.findElement(getContext().getCurrentResponse(), getHtmlId(), getXpath(), LOG, this));
		}
	}

	/**
	 * Finds the relevant form.
	 *
	 */
	protected abstract HtmlForm findForm();

	/**
	 * Finds then verifies the field of interest.
	 * @param form
	 */
	protected abstract List findFields(final HtmlForm form);

	/**
	 * Verifies a field according to the step.
	 * It is up to the step's implementation to decide how to verify the step.
	 *
	 * @param field The field to verify.
	 */
	protected abstract void verifyField(final HtmlElement field) throws IOException;

	protected void verifyParameters() {
		super.verifyParameters();
		nullResponseCheck();

		int count = 0;
		if (getXpath() != null) {
			count++;
		}
		if (getName() != null) {
			count++;
		}
		if (getHtmlId() != null) {
			count++;
		}
		paramCheck(count == 0, AbstractSetFieldStep.MESSAGE_ARGUMENT_MISSING);
		paramCheck(count > 1, AbstractSetFieldStep.MESSAGE_ARGUMENT_REDUNDANT);
		if (getName() == null) {
			paramCheck(getFieldIndex() != null, "The attribute 'fieldIndex' is only valid with the attribute 'name'.");
		} 
		else {
			optionalIntegerParamCheck(getFieldIndex(), "fieldIndex", false);
		}
	}

	protected boolean isValueNull() {
		return getValue() == null;
	}
	
	public boolean isPerformingAction() {
		return false;
	}
}
