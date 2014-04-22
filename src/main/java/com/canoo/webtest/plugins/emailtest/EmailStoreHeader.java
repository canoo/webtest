// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;

/**
 * Stores the value of a header field within a message (or within a message part).
 *
 * @author Paul King, ASERT
 * @webtest.step category="Email"
 * name="emailStoreHeader"
 * description="Stores the value of a message (or message part) header field into a property."
 */
public class EmailStoreHeader extends AbstractMessageStoreStep
{
    private static final Logger LOG = Logger.getLogger(EmailStoreHeader.class);
    private String fHeaderName;
    private String fPartIndex;

    public String getHeaderName() {
        return fHeaderName;
    }

    /**
     * Sets the name of the header of interest.
     *
     * @param headerName The header name
     * @webtest.parameter required="yes"
     * description="The name of the header of interest."
     */
    public void setHeaderName(final String headerName) {
        fHeaderName = headerName;
    }

    public String getPartIndex() {
        return fPartIndex;
    }

    /**
     * Sets the part index.
     *
     * @param partIndex The message part of interest
     * @webtest.parameter
     *   required="no"
     *   description="The index of the message part of interest. If set for a <em>Simple</em> message, will cause the step to fail. If set for a MIME <em>MultiPart</em> message will retrieve headers related to the message part instead of headers of the message itself."
     */
    public void setPartIndex(final String partIndex) {
        fPartIndex = partIndex;
    }

    /**
     * Calculate the result.
     *
     * @param message
     * @return The result
     */
    protected String performOperation(final Message message) throws MessagingException {
        if (StringUtils.isEmpty(getPartIndex())) {
            return arrayToString(message.getHeader(getHeaderName()));
        }
        final Object content;
        try {
            content = message.getContent();
        }
        catch (IOException e) {
            LOG.error("Error processing email message: ", e);
            throw new MessagingException("Error processing email message: " + e.getMessage());
        }
        if (content instanceof Multipart) {
            final Multipart mp = (Multipart) content;
            final int part = ConversionUtil.convertToInt(getPartIndex(), 0);
            if (part >= mp.getCount()) {
                throw new StepFailedException("PartIndex too large.", this);
            }
            return arrayToString(mp.getBodyPart(part).getHeader(getHeaderName()));
        }
        throw new StepFailedException("PartIndex supplied for a non-MultiPart message.", this);
    }

    private static String arrayToString(final String[] values) {
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i]);
            if (i < values.length - 1) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getHeaderName(), "headerName");
        optionalIntegerParamCheck(getPartIndex(), "partIndex", true);
    }
}