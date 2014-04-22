// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.FormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * Provides the ability to select a form as the current form for setting fields in this form
 * in subsequent steps.
 *
 * @author Marc Guillemot
 * @webtest.step
 *   category="Core"
 *   name="selectForm"
 *   alias="new_selectform,selectform"
 *   description="This step can be used to specify a form in the document as the <em>current form</em>. The <em>current form</em> is given precedence when locating the form to use in form-related steps."
 */
public final class SelectForm extends Step {
    private static final Logger LOG = Logger.getLogger(SelectForm.class);
    private String fIndex;
    private String fName;

    /**
     * Select a form as the current one.
     *
     * @throws com.canoo.webtest.engine.StepExecutionException if no form is found
     */
    public void doExecute() throws Exception {
        nullResponseCheck();
        final Context context = getContext();
        final HtmlPage currentResp = (HtmlPage) context.getCurrentResponse();
        LOG.debug("Selecting current form in response from " + currentResp.getUrl());
        HtmlForm form;
        if (fName != null) {
            form = FormUtil.findFormByName(currentResp, fName);
        } else {
            form = FormUtil.findFormByIndex(currentResp, fIndex);
        }
        if (form == null) {
            throw new StepFailedException("Form not found in doc", this);
        }
        context.setCurrentForm(form);
    }

    /**
     * Set the index.
     *
     * @param index
     * @webtest.parameter
     *   required="yes/no"
     *   description="The <em>index</em> of the form to select (starting at 0). Either <em>name</em> or <em>index</em> is required."
     */
    public void setIndex(final String index) {
        fIndex = index;
    }

    public String getIndex() {
        return fIndex;
    }

    /**
     * Set the name.
     *
     * @param name
     * @webtest.parameter
     *   required="yes/no"
     *   description="The <em>name</em> of the form to select (i.e. the value of the NAME attribute in <form name='foo' ... >). Either <em>name</em> or <em>index</em> is required."
     */
    public void setName(final String name) {
        fName = name;
    }

    public String getName() {
        return fName;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        paramCheck(StringUtils.isEmpty(fName) && StringUtils.isEmpty(fIndex), "Either \"name\" or \"index\"  must be set");
        paramCheck(!StringUtils.isEmpty(fName) && !StringUtils.isEmpty(fIndex), "Only one of \"name\" and \"index\"  must be set");
        optionalIntegerParamCheck(fIndex, "index", false); // if exists, must be integer
    }

    /**
     * Called by Ant to set the text nested between opening and closing tags.
     * @param text the text to set
     * @webtest.nested.parameter
     *    required="no"
     *    description="Alternative way to set the 'name' attribute."
     */
    public void addText(final String text) {
 		setName(text);
 	}
}
