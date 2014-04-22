package com.canoo.webtest.steps;


/**
 * @author Denis N. Antonioli
 */

public class AbstractBrowserActionTest extends StepTest {

	protected Step createStep() {
		return new AbstractBrowserAction(){
			public void doExecute() throws Exception {
				// noop
			}
        };
	}

}