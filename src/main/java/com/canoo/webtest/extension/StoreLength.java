// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.boundary.StreamBoundary;
import org.apache.log4j.Logger;

/**
 * Stores a digest value for the response (or part of the response) into a property.<p>
 * <p/>
 * Either ant or dynamic properties are supported. A stored digest value can
 * be used when invoking subsequent steps for verification purposes.
 * TODO: write a verifyLength step which takes a filename - populate the
 * file the first time the step is run.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Extension"
 * name="storeLength"
 * description="Stores a length value for the response (or part of the response) into a property. This is useful when you don't care about the content within a response but wish to ensure that at least the length hasn't changed from some expected value. Supports content filters such as <stepref name='lineSeparatorFilter'/> and <stepref name='replaceFilter'/> to allow you to filter out content that you are not interested in and the <stepref name='table' category='Core'/> locator step if you are interested in content within a table."
 */
public class StoreLength extends AbstractProcessContentStep
{
    private static final Logger LOG = Logger.getLogger(StoreLength.class);
    /**
     * Calculate the length value
     *
     * @param context The context for the response text to calculate the checksum for
     * @return The length value
     */
    protected String processContent(Context context) {
        byte[] bytes = StreamBoundary.tryGetBytes(context, this);
        LOG.debug("Got bytes: " + StoreDigest.hexRepresentation(bytes));
        return Integer.toString(bytes.length);
    }

}