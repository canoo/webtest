// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.store;


import com.canoo.webtest.engine.StepFailedException;
import com.gargoylesoftware.htmlunit.WebResponse;


/**
 * Stores a header value (from the Http Response) into a property.<p>
 * <p/>
 * Either <em>ant</em> or <em>dynamic</em> properties are supported.
 * The property can be checked subsequently with <em>verifyProperty</em>.
 *
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="storeHeader"
 * description="Provides the ability to store an <key>HTTP</key> Header value for later checking"
 */
public class StoreHeader extends BaseStoreStep {
    private String fHeaderName;

    /**
     * Sets the Name of the Header of interest.<p>
     *
     * @param name The Header Name
     * @webtest.parameter required="yes"
     * description="The name of the Http Response Header of interest.
     * If the property name is not specified, the header name is used as key to store the value found."
     */
    public void setName(final String name) {
        fHeaderName = name;
    }

    public String getName() {
        return fHeaderName;
    }

    public void doExecute() {
        final WebResponse response = getContext().getCurrentResponse().getWebResponse();
        final String headerValue = response.getResponseHeaderValue(fHeaderName);

        if (headerValue == null) {
            throw new StepFailedException("Header \"" + fHeaderName + "\" not set!", this);
        }
        storeProperty(headerValue, getName());
    }

    /**
     * Verifies the parameters.<p>
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(fHeaderName, "name");
        nullResponseCheck();
	}
}