// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.extension;


import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;


/**
 * Enables / disables javascript execution in the HtmlUnit "browser".<p>
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="enableJavaScript"
 * alias="enablejavascript"
 * description="Provides the ability to activate / deactivate <key>javascript</key> support. 
 * (Javascript support is enabled by default)"
 * @since Dec 2004
 */
public class EnableJavaScript extends Step {
    private String fEnable;

    public void doExecute() throws Exception {
        getContext().getWebClient().setJavaScriptEnabled(ConversionUtil.convertToBoolean(getEnable(), false));
    }

    /**
     * @webtest.parameter required="false"
     * default="false"
     * description="Indicates if JavaScript is enabled (true) or disabled (false)."
     */
    public void setEnable(final String enable) {
        fEnable = enable;
    }

    public String getEnable() {
        return fEnable;
    }
}
