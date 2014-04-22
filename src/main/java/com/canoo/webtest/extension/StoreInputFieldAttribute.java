// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.form.AbstractSetFieldStep;
import com.canoo.webtest.steps.store.BaseStoreStep;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * StoreInputFieldAttribute
 *
 * @author  Vikram Shitole
 * @author  Paul King
 * @webtest.step
 *    category="Extension"
 *    name="storeInputFieldAttribute"
 *    description="Retrieves the attribute value of a selected input field. Can be used to check whether an input field is enabled."
 */
public class StoreInputFieldAttribute extends BaseStoreStep {
	private String fFieldName;
	private String fFieldValue;
	private String fAttributeName;
	private String fFieldIndex;
    private String fFormName;

    /**
	 * Locate all applicable html input components, check their number (size == 1) and
	 * store the value of the attribute.
	 *
	 * @throws com.canoo.webtest.engine.StepFailedException
	 *          if no applicable button was found
	 */
	public void doExecute() throws Exception {
		final List htmlInputs = findHtmlElements(getContext());
		final HtmlInput htmlInputElmt = (HtmlInput) AbstractSetFieldStep.selectField(htmlInputs, fFieldIndex, this);
		String retval = htmlInputElmt.getAttribute(fAttributeName);
		if (retval == HtmlElement.ATTRIBUTE_NOT_DEFINED) {
			retval = "false";
		} else if (retval == HtmlElement.ATTRIBUTE_VALUE_EMPTY || retval.length() == 0) {
			retval = "true";
		}
		storeProperty(retval);
	}

	/**
	 * Locate all input fields matching fFieldName and optionally fFieldValue
	 * if specified.
	 *
	 * @param context The current test context
	 * @return A list with all applicable input fields
	 */
	private List findHtmlElements(final Context context) throws Exception {
        List fields;
        if (fFormName != null) {
            HtmlForm form = FormUtil.findFormForField(context, fFormName, HtmlConstants.INPUT, null, fFieldName, this);
            fields = new ArrayList(form.getInputsByName(fFieldName));
        } else {
            fields = new ArrayList(((HtmlPage) context.getCurrentResponse()).getDocumentElement().getElementsByAttribute(HtmlConstants.INPUT, HtmlConstants.NAME, fFieldName));
        }
        // remove those not having the right value
        if (fFieldValue != null) {
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                final HtmlInput input = (HtmlInput) iter.next();
                if (!fFieldValue.equals(input.getValueAttribute())) {
                    iter.remove();
                }
            }
        }
        return fields;
	}

	/**
	 * Sets the name of the input field of interest.
	 *
	 * @param name Sets the name value.
	 * @webtest.parameter
	 * 	 required="yes"
	 *   description="The name of the input field of interest."
	 */
	public void setName(final String name) {
		fFieldName = name;
	}

	public String getName() {
		return fFieldName;
	}

	/**
	 * Sets the input field value to match against if there are multiple input fields with the same name.
	 *
	 * @param value Sets the value of the input field of interest.
	 * @webtest.parameter
	 * 	 required="no"
	 *   description="The value of the input field of interest."
	 */
	public void setValue(final String value) {
		fFieldValue = value;
	}

	public String getValue() {
		return fFieldValue;
	}

	/**
	 * Sets the name of the attribute of interest.<p>
	 *
	 * @param name Sets the name of the attribute.
	 * @webtest.parameter
	 *   required="yes"
	 *   description="The name of the attribute of interest, e.g. \"disabled\" or \"checked\"."
	 */
	public void setAttributeName(final String name) {
		fAttributeName = name;
	}

	public String getAttributeName() {
		return fAttributeName;
	}

	/**
	 * Sets the index of the attribute of interest.<p>
	 *
	 * @param index Selects the attribute of interest if more than one match other criteria (first index is 0).
	 * @webtest.parameter
	 *   required="no"
	 *   description="The index (starting at 0) of the attribute of interest."
	 */
	public void setFieldIndex(final String index) {
		fFieldIndex = index;
	}

	public String getFieldIndex() {
		return fFieldIndex;
	}

	/**
	 * Sets the Name of the Property.<p>
	 * @param name The Property Name
	 * @webtest.parameter
	 *   required="no"
	 *   description="Deprecated. Same as property."
	 */
	public void setPropertyName(final String name) {
		setProperty(name);
	}

    /**
     * Set the form name.<p>
     *
     * @param formName
     * @webtest.parameter required="no"
     * default="the last form selected using 'selectForm', otherwise searches all forms"
     * description="The name of the form containing the field of interest."
     */
    public void setFormName(String formName) {
        fFormName = formName;
    }

    public String getFormName() {
        return fFormName;
    }

    /**
	 * Verifies the parameters.<p>
	 */
	protected void verifyParameters() {
	    super.verifyParameters();
	    nullParamCheck(getProperty(), "property");
		nullParamCheck(getName(), "name");
		nullParamCheck(getAttributeName(), "attributeName");
		optionalIntegerParamCheck(getFieldIndex(), "fieldIndex", false);
		nullResponseCheck();
	}
}
