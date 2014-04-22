// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import com.canoo.webtest.engine.Context;

/**
 * @author Paul King
 */
public interface ISingleMessageStep
{
    void setMessageId(String id);

    String getMessageId();

    Context getContext();
}
