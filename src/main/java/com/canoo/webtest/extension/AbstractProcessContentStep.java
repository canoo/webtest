// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.List;

import org.apache.tools.ant.Task;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.WebClientContext;
import com.canoo.webtest.interfaces.IComputeValue;

/**
 * Abstract class used for processing content.
 */
public abstract class AbstractProcessContentStep extends AbstractProcessFiltersStep implements IComputeValue {
    private String fPropertyName;
    private String fPropertyType;
    private String fComputedValue;

    /**
     * Sets the target property type.
     *
     * @param type The Property type
     * @webtest.parameter required="no"
     * description="The target property type. Either \"ant\" or \"dynamic\"."
     * default="the \"defaultPropertyType\" as specified in the \"config\" element is used."
     */
    public void setPropertyType(final String type) {
        fPropertyType = type;
    }

    public String getPropertyType() {
        return fPropertyType;
    }

    /**
     * Sets the target property name.
     *
     * @param name The Property Name
     * @webtest.parameter
     *   required="yes"
     *   description="The target property name."
     */
    public void setProperty(final String name) {
        fPropertyName = name;
    }

    public String getProperty() {
        return fPropertyName;
    }

    public void doExecute() {
        final List steps = getSteps();
        final Context context = getContext();
        final WebClientContext.StoredResponses origResponses = context.getResponses();
        // TODO: just make table like a normal filter, i.e. add to list of steps to execute
        applyTableFilterIfNeeded(context);
        applyExtractionIfNeeded(context);
        for (int i = 0; i < steps.size(); i++) {
            final Task step = (Task) steps.get(i);
            executeContainedStep(step);
//            context = step.getContext();
        }
//        setWebtestProperty(fPropertyName, processContent(context), fPropertyType);
    	fComputedValue = processContent(getContext());
        setWebtestProperty(getProperty(), fComputedValue, getPropertyType());
        getContext().restoreResponses(origResponses);
    }

    protected abstract String processContent(Context text);

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getProperty(), "property");
        nullResponseCheck();
    }
    
	public String getComputedValue() {
		return fComputedValue;
	}
}
