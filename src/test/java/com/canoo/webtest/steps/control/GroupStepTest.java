// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.control;

import com.canoo.webtest.steps.Step;

/**
 * @author Paul King
 * @since <pre>Jun 2005</pre>
 */
public class GroupStepTest extends BaseWrappedStepTestCase {

	protected Step createStep() {
		return new GroupStep();
	}

}
