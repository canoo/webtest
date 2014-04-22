// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps;

import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.interfaces.IContentFilter;

/**
 * Helper class for filters.
 * @author Paul King
 * @author Marc Guillemot
 */
public abstract class AbstractFilter extends Step implements IContentFilter
{
    private static final String DUMMY_FILTER_URLSTR = "http://dummyFilterUrl";

    /**
     * Place the content as the current response
     * @param text
     * @param contentType
     * @throws Exception
     */
    protected void defineAsCurrentResponse(final String text, final String contentType) throws Exception {
        ContextHelper.defineAsCurrentResponse(getContext(), text, contentType, DUMMY_FILTER_URLSTR);
    }
}
