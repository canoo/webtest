// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Stores the number of messages into a property.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Email"
 * name="emailStoreMessageCount"
 * description="Stores the number of messages (optionally matching provided criteria) into a property."
 */
public class EmailStoreMessageCount extends AbstractSelectStep
{
    /**
     * Calculate the count
     *
     * @param folder
     * @return The extracted count value
     */
    protected String processContent(final Folder folder) throws MessagingException {
        final Message[] messages = retrieveMatchingMessages(folder);
        return String.valueOf(messages.length);
    }

}