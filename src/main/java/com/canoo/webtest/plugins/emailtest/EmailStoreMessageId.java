// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.util.ConversionUtil;

/**
 * Stores the message's id into a property.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Email"
 * name="emailStoreMessageId"
 * description="Stores the message id (optionally matching specific criteria) of a particular message."
 */
public class EmailStoreMessageId extends AbstractSelectStep
{
    private static final Logger LOG = Logger.getLogger(EmailStoreMessageId.class);
    private String fMessageIndex;

    public String getMessageIndex() {
        return fMessageIndex;
    }

    /**
     * Sets the message index.
     *
     * @param index The message index
     * @webtest.parameter required="no"
     * default="0, the first message"
     * description="The messageIndex if more than one message match."
     */
    public void setMessageIndex(final String index) {
        fMessageIndex = index;
    }

    /**
     * Calculate the message id
     *
     * @param folder
     * @return The extracted count value
     */
    protected String processContent(final Folder folder) throws MessagingException {
        final Message[] messages = retrieveMatchingMessages(folder);
        if (StringUtils.isEmpty(fMessageIndex)) {
            LOG.info("Multiple matching messages found, using the first.");
        }
        if (messages.length == 0) {
            throw new MessagingException("No messages matching criteria.");
        }
        final int messageIndex = ConversionUtil.convertToInt(getMessageIndex(), 0);
        if (messageIndex >= messages.length) {
            throw new MessagingException("Invalid messageIndex '" + getMessageIndex() +
                    "', valid range is 0.." + (messages.length - 1));
        }
        return String.valueOf(messages[messageIndex].getMessageNumber());
    }

    protected void verifyParameters() {
        super.verifyParameters();
        optionalIntegerParamCheck(getMessageIndex(), "messageIndex", true);
    }
}