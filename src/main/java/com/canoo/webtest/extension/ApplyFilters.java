// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.Task;

import com.canoo.webtest.boundary.AntBoundary;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.interfaces.IBrowserAction;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Applies one or more filters to the current response and treats the result as if it was a new response.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Filter"
 * name="applyFilters"
 * description="Applies one or more filters to the current response and treats the result as if it was a new response."
 */
public class ApplyFilters extends AbstractProcessFiltersStep implements IBrowserAction
{
    /**
     * @webtest.parameter
     *   required="no"
     *   skip="yes"
     *   description="A shorthand: <em>save='prefixName'</em> is the same as
     *   <em>savePrefix='prefixName' saveResponse='true'</em>."
     */
    public void setSave(final String prefix) {
        // nothing as StepExecutionListener reads it from attributes
    	// but needs to be here to allow Ant to "set" it
    }

    /**
     * @webtest.parameter
     *   required="no"
     *   default="the 'savePrefix' parameter as specified in &lt;config&gt;."
     *   description="A name prefix can be specified for making a permanent copy of
     *   received responses. A unique number and the file extension (depending on the
     *   MIME-Type) will be appended. The <em>resultpath</em> attribute of the
     *   <config> element is used for determining the location of the saved result."
     */
    public void setSavePrefix(final String prefix) {
        // nothing as StepExecutionListener reads it from attributes
    	// but needs to be here to allow Ant to "set" it
    }

    /**
     * @webtest.parameter required="no"
     * description="Whether to make a permanent copy of received responses.
     * Overrides the default value set in the <config> element."
     */
    public void setSaveResponse(final String response) {
        // nothing as StepExecutionListener reads it from attributes
    	// but needs to be here to allow Ant to "set" it
    }

    public void doExecute() throws Exception {
        final List steps = getSteps();
        Context context = getContext();
        applyTableFilterIfNeeded(context);
        applyExtractionIfNeeded(context);
        if (!steps.isEmpty()) 
        {
            for (int i = 0; i < steps.size(); i++) {
                context = processFilterStep(context, (Step) steps.get(i));
            }
            final Page currentResponse = context.getCurrentResponse();
            final byte[] bytes = IOUtils.toByteArray(currentResponse.getWebResponse().getContentAsStream());
            ContextHelper.defineAsCurrentResponse(getContext(), bytes,
                    currentResponse.getWebResponse().getContentType(), "http://" + getClass().getName());
        }
    }

    //TODO: don't configure it too early and use UnknownElement!
    public void addTask(final Task newTask) {
    	super.addTask(AntBoundary.maybeConfigure(newTask));
    }

    private Context processFilterStep(final Context origContext, final Step step) {
        step.perform();
        final Context newContext = step.getContext();
        newContext.saveResponseAsCurrent(newContext.getCurrentResponse());
        newContext.getConfig().setResultpath(getContext().getConfig().getWebTestResultDir());
        return newContext;
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullResponseCheck();
    }
}