// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.util;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * Utilities for working with forms.
 *
 * @author Paul King
 */
public class FormUtil
{
    private static final Logger LOG = Logger.getLogger(FormUtil.class);

    /**
     * Tests if the form contains the desired field.
     */
    private static boolean hasField(final HtmlForm form, final String tag, final String type, final String name) {
        List elements = form.getHtmlElementsByTagName(tag);
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            HtmlElement element = (HtmlElement) iter.next();
            if (name.equals(element.getAttribute(HtmlConstants.NAME))
                    && (type == null || type.equalsIgnoreCase(element.getAttribute(HtmlConstants.TYPE)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates if the form has a "text field" (text, password or textarea) with the given name.
     */
    public static boolean hasTextField(final HtmlForm form, final String name) {
        if (hasTextAreaField(form, name)) {
            return true;
        }
        return hasTextOrPasswordField(form, name);
    }

    private static boolean hasTextOrPasswordField(final HtmlForm form, final String name) {
        final List li = form.getInputsByName(name);
        for (final Iterator iter = li.iterator(); iter.hasNext();) {
            final HtmlInput element = (HtmlInput) iter.next();
            if (HtmlConstants.TEXT.equalsIgnoreCase(element.getTypeAttribute())
                    || HtmlConstants.PASSWORD.equalsIgnoreCase(element.getTypeAttribute())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTextAreaField(final HtmlForm form, final String name) {
        return !form.getTextAreasByName(name).isEmpty();
    }

    /**
     * Gets the form from the current response containing the desired field. If there is a current form then this form is
     * considered, else the first form containing such a field (this form is then set as the current form for further
     * calls).<br/> Note: the test is currently only performed on the name of the field.
     *
     * @param context
     * @param givenFormName
     * @param tag           the html tag corresponding to the field
     * @param type          the type of the input field (html attribute "type") if tag is "input"
     * @param name          the name of the input field (html attribute "name")
     * @param step
     * @return <code>null</code> if no form found.
     */
    public static HtmlForm findFormForField(Context context, String givenFormName, final String tag, final String type, final String name, Step step) {
        LOG.debug("Looking for form with " + tag + " field " + type + " named \"" + name + "\"");
        HtmlForm form = null;
        if (givenFormName != null) {
            HtmlForm givenForm = findFormByName(context.getCurrentHtmlResponse(step), givenFormName);
            form = checkFormIsSuitable(givenForm, tag, type, name);
        }
        if (form != null) {
            return form;
        }
        LOG.debug("No given form or given form not suitable, trying others");
        form = checkFormIsSuitable(context.getCurrentForm(), tag, type, name);
        if (form != null) {
            return form;
        }
        LOG.debug("No current form or current form not suitable, trying others");
        return searchAllFormsForSuitableOne(context, tag, type, name, step);
    }

    private static HtmlForm checkFormIsSuitable(final HtmlForm candidateForm, final String tag, final String type, final String name) {
        if (candidateForm != null && hasField(candidateForm, tag, type, name)) {
            LOG.debug("Form '" + candidateForm.getNameAttribute() + "' has suitable field, using it");
            return candidateForm;
        }
        return null;
    }

    private static HtmlForm searchAllFormsForSuitableOne(final Context context, final String tag, final String type, final String name, Step step) {
        for (final Iterator iter = context.getCurrentHtmlResponse(step).getForms().iterator(); iter.hasNext();) {
            final HtmlForm curForm = (HtmlForm) iter.next();
            if (hasField(curForm, tag, type, name)) {
                context.setCurrentForm(curForm);
                return curForm;
            }
        }
        return null;
    }

    /**
     * Gets the form from the current response containing the desired text field. If there is a current form then this form
     * is considered, else the first form containing such a field (this form is then set a the current form for further
     * calls).
     *
     * @param context
     * @param givenFormName
     * @param name          the name of the text field of interest (&lt;input type="text"...&gt; or
     *                      &lt;input type="password" ...&gt; or &lt;textarea ...&gt;...&lt;/textarea&gt;)
     * @param step
     * @return <code>null</code> if no form found.
     */
    public static HtmlForm findFormForTextField(Context context, String givenFormName, final String name, Step step) {
        LOG.debug("Looking for form with text field named \"" + name + "\"");
        HtmlForm form = null;
        if (givenFormName != null) {
            HtmlForm givenForm = findFormByName(context.getCurrentHtmlResponse(step), givenFormName);
            form = checkFormIsSuitable(givenForm, name);
        }
        if (form != null) {
            return form;
        }
        LOG.debug("No given form or given form not suitable, trying others");
        form = checkFormIsSuitable(context.getCurrentForm(), name);
        if (form != null) {
            return form;
        }
        LOG.debug("No current form or current form not suitable, trying others");
        return searchAllFormsForSuitableOne(context, name, step);
    }

    private static HtmlForm checkFormIsSuitable(HtmlForm candidateForm, final String name) {
        if (candidateForm != null && hasTextField(candidateForm, name)) {
            LOG.debug("Form '" + candidateForm.getNameAttribute() + "' has suitable text field, using it");
            return candidateForm;
        }
        return null;
    }

    private static HtmlForm searchAllFormsForSuitableOne(Context context, final String name, Step step) {
        for (final Iterator iter = context.getCurrentHtmlResponse(step).getForms().iterator(); iter.hasNext();) {
            final HtmlForm curForm = (HtmlForm) iter.next();
            if (hasTextField(curForm, name)) {
                context.setCurrentForm(curForm);
                return curForm;
            }
        }
        return null;
    }

    public static HtmlForm findFormByName(final HtmlPage currentResp, final String name) {
        LOG.debug("Looking for form named '" + name + "'");
        try {
            return currentResp.getFormByName(name);
        } catch (Exception e) {
            LOG.debug("Exception: " + e.getMessage());
        }
        return null;
    }

    public static HtmlForm findFormByIndex(final HtmlPage currentResp, final String indexStr) {
        LOG.debug("Looking for form with index '" + indexStr + "'");
        final int index = ConversionUtil.convertToInt(indexStr, 0);
        final int numForms = currentResp.getForms().size();
        if (index >= numForms) {
            LOG.info("Index value of '" + index + "' not in expected range: 0.." + (numForms - 1));
            return null;
        }
        return (HtmlForm) currentResp.getForms().get(index);
    }

}
