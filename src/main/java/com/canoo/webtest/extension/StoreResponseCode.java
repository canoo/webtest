// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.steps.store.BaseStoreStep;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Stores the HTTP Response Status Code into a property.<p>
 * <p/>
 * Either <em>ant</em> or <em>dynamic</em> properties are supported.
 * The property can be checked subsequently with <em>verifyProperty</em>.
 *
 * @author <a href="mailto:paulk at asert dot com dot au">Paul King</a>
 * @webtest.step category="Extension"
 * name="storeResponseCode"
 * description="Provides the ability to store the <key>HTTP</key> Response Code value for later checking."
 */
public class StoreResponseCode extends BaseStoreStep {
    /**
     * Sets the Name of the Property.<p>
     * @param name The Property Name
     * @webtest.parameter required="yes"
     * description="The name of the property in which to store the Response Code value."
     */
    public void setProperty(final String name) {
        super.setProperty(name);
    }

    public void doExecute() {
        final WebResponse response = getContext().getCurrentResponse().getWebResponse();
        storeProperty(Integer.toString(response.getStatusCode()));
    }

    /**
     * Verifies the parameters.<p>
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getProperty(), "property");
        nullResponseCheck();
	}
}