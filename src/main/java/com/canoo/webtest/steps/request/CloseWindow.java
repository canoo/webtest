// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.TopLevelWindow;

/**
 * Closes the current top level window.<p>
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="closeWindow"
 * description="Closes the current top level window. 
 * The content of the last previously opened window will become the current response."
 */
public class CloseWindow extends Step {

    public void doExecute() throws Exception {
        final Context context = getContext();
        
        final TopLevelWindow window = (TopLevelWindow) context.getCurrentResponse().getEnclosingWindow().getTopWindow();
        window.close();
    }

    protected void verifyParameters() {
        super.verifyParameters();
		nullResponseCheck();
    }
}
