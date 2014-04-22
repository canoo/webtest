// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.util.MapUtil;

import java.util.Map;

/**
 * MG: what to do if previous response is in a closed window?
 * @webtest.step category="Core"
 * name="previousResponse"
 * alias="previousresponse"
 * description="This step provides the ability to revert to the previously loaded response for further testing of the previous page if testing of the current response is complete."
 */
public class PreviousResponse extends AbstractBrowserAction
{
	private String fRestoredResponseUrl;

    public void doExecute() {
        nullResponseCheck();
        final Context context = getContext();
        context.restorePreviousResponse();
		fRestoredResponseUrl = context.getCurrentResponse().getUrl().toString();
	}

	/**
	 * Adds the url of the restored response to parent's call
	 * @see com.canoo.webtest.steps.Step#getParameterDictionary()
	 */
	protected void addComputedParameters(final Map map) {
		super.addComputedParameters(map);
		MapUtil.putIfNotNull(map, "->url", fRestoredResponseUrl);
	}
}