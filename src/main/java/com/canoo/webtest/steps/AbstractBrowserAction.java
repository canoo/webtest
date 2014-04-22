// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import com.canoo.webtest.interfaces.IBrowserAction;

/**
 * Base class for steps performing some action(s) on the "browser". <p/> Steps
 * which cause a new response to be created should set the resultFilename.
 *
 * @author Marc Guillemot
 */
public abstract class AbstractBrowserAction extends Step implements IBrowserAction
{
    /**
	 * @webtest.parameter
	 *   required="no"
     *   skip="yes"
     *   description="A shorthand: <em>save='prefixName'</em> is the same as
     * <em>savePrefix='prefixName' saveResponse='true'</em>."
     */
    public void setSave(final String prefix) {
        // nothing as StepExecutionListener reads it from attributes
    	// but needs to be here to allow Ant to "set" it
    }

    /**
     * @webtest.parameter required="no"
     * default="the 'savePrefix' parameter as specified in &lt;config&gt;."
     * description="A name prefix can be specified for making a permanent copy of
     * received responses. A unique number and the file extension (depending on the
     * MIME-Type) will be appended. The <em>resultpath</em> attribute of the
     * <config> element is used for determining the location of the saved result."
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
}
