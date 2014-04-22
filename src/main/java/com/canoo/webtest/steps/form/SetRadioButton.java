// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

import java.io.IOException;
import java.util.List;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.engine.xpath.XPathHelper;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;

/**
 * Provides the ability to check a radio button.<p>
 *
 * @author Marc Guillemot
 * @webtest.step
 *   category="Core"
 *   name="setRadioButton"
 *   alias="new_setradiobutton,setradiobutton"
 *   description="Provides the ability to check radio buttons in <key>HTML</key> forms."
 */
public class SetRadioButton extends AbstractSetNamedFieldStep
{
    protected HtmlForm findForm() {
        return FormUtil.findFormForField(getContext(), getFormName(), HtmlConstants.INPUT, HtmlConstants.RADIO, getName(), this);
    }

    /**
     * Finds the radio button with the given name and value in the form.
     *
     * @param form The form to search
     * @return the list of fields with the given name
     */
    protected List findFields(final HtmlForm form) {
        List fields = form.getByXPath(".//input[@type='radio' and @name=" + XPathHelper.quote(getName())
        		+ " and @value=" + XPathHelper.quote(getValue()) + "]");
        return fields;
    }

    /**
     * Updates a field.
     *
     * @param elt the field to update
     */
    protected void setField(HtmlElement elt) throws IOException {
        if (!(elt instanceof HtmlRadioButtonInput)) {
            throw new StepFailedException("Found " + elt.getTagName() +
                    " when looking for radio button", this);
        }
        ((HtmlRadioButtonInput) elt).click();
    }

    /**
     * Set the value.
     *
     * @param value
     * @webtest.parameter
     *   required="yes/no"
     *   description="The value to use to find the desired radio-button. 
     *   Must be specified if <em>name</em> is specified."
     */
    public void setValue(final String value) {
        super.setValue(value);
    }

    /**
     * Verifies the parameters.<p>
     */
    protected void verifyParameters() {
        super.verifyParameters();
        if (getHtmlId() == null && getXpath() == null && getForLabel() == null)
        {
        	nullParamCheck(getValue(), "value");
        }
        else 
        {
        	paramCheck(getValue() != null, "Can't specify attribute value when htmlid, xpath or forLabel is specified");
        }
    }
}
