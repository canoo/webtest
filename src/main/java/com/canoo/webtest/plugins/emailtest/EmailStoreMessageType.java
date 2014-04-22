// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.apache.log4j.Logger;

/**
 * Stores a string representing the message type.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Email"
 * name="emailStoreMessageType"
 * description="Stores a string representing the message type, either '<em>Simple</em>' or '<em>MultiPart</em>'."
 */
public class EmailStoreMessageType extends AbstractMessageStoreStep
{
    private static final Logger LOG = Logger.getLogger(EmailStoreMessageType.class);
    /**
     * Perform the operation.
     *
     * @param message
     */
    protected String performOperation(final Message message) throws MessagingException {
        try {
            final Object content = message.getContent();
            if (content instanceof Multipart) {
                return "MultiPart";
            } else {
                return "Simple";
            }
        } catch (IOException e) {
            LOG.error("Error processing email message: ", e);
            throw new MessagingException("Error processing email message: " + e.getMessage());
        }
    }
}