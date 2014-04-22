// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;

/**
 * Abstract class used by email steps that store a property related to a single message.
 *
 * @author Paul King
 */
public abstract class AbstractMessageStoreStep extends AbstractStoreStep implements ISingleMessageStep
{
    private String fMessageId;

    /**
     * Sets the message id.
     *
     * @param id The id of the message of interest
     * @webtest.parameter
     *   required="yes"
     *   description="The message id."
     */
    public void setMessageId(final String id) {
        fMessageId = id;
    }

    public String getMessageId() {
        return fMessageId;
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        optionalIntegerParamCheck(fMessageId, "messageId", true);
    }

    protected String processContent(final Folder folder) throws MessagingException {
        final int id = ConversionUtil.convertToInt(getMessageId(), 0);
        final Message message = getHelper().getMessage(id, folder);
        if (message == null) {
            throw new StepFailedException("Could not find message with id '" + id + "' on server.", this);
        }
        return performOperation(message);
    }

    protected abstract String performOperation(Message message) throws MessagingException;
}
