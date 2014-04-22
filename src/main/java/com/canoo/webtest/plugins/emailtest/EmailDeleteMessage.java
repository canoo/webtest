// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Deletes a message.
 *
 * @author Paul King, ASERT
 * @webtest.step
 *   category="Email"
 *   name="emailDeleteMessage"
 *   description="Deletes a message with a given <em>messageId</em>."
 */
public class EmailDeleteMessage extends AbstractMessageOperationStep
{
    public EmailDeleteMessage() {
        super(true);
    }

    /**
     * Perform the operation.
     *
     * @param message
     */
    protected void performOperation(final Message message) throws MessagingException {
        getHelper().markForDelete(message);
    }
}
