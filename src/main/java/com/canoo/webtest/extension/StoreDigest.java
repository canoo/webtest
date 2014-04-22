// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.boundary.StreamBoundary;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Stores a digest value for the response (or part of the response) into a property.<p>
 * <p/>
 * Either ant or dynamic properties are supported. A stored digest value can
 * be used when invoking subsequent steps for verification purposes.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Extension"
 * name="storeDigest"
 * description="Stores a digest value for the response (or part of the response) into a property. This is useful when you don't care what the value within a response is but you want to ensure that the generated content hasn't changed. If any single character in the response changes, so will the digest. Supports content filters such as <stepref name='lineSeparatorFilter'/> and <stepref name='replaceFilter'/> to allow you to filter out changes that you don't care about  and the <stepref name='table' category='Core'/> locator step if you are interested in content within a table."
 */
public class StoreDigest extends AbstractProcessContentStep
{
    private String fAlgorithm;

    /**
     * Calculate the digest value
     *
     * @param context The context for the response text to calculate the checksum for
     * @return The digest value
     */
    protected String processContent(final Context context) {
        final MessageDigest md = tryGetDigest();
        md.update(StreamBoundary.tryGetBytes(context, this));
        final byte[] digest = md.digest();
        return hexRepresentation(digest);
    }

    private MessageDigest tryGetDigest() {
        try {
            return MessageDigest.getInstance(getType() == null ? "SHA-1" : getType());
        } 
        catch (final NoSuchAlgorithmException e) {
            throw new StepFailedException("No such message digest algorithm '" + getType() + "' available.", this);
        }
    }

    public static String hexRepresentation(final byte[] digest) {
        final StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            final byte b = digest[i];
            hexString.append(Integer.toHexString(0xFF & b));
            hexString.append(" ");
        }
        return hexString.toString().trim();
    }

    /**
     * Sets the type of message digest algorithm to use.<p>
     *
     * @param value message digest algorithm to use
     * @webtest.parameter required="no" default="SHA-1"
     * description="The type of message digest algorithm to use, e.g. 'MD5' or 'SHA-1'."
     */
    public void setType(final String value) {
        fAlgorithm = value;
    }

    public String getType() {
        return fAlgorithm;
    }
}