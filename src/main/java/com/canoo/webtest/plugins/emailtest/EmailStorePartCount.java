// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.apache.log4j.Logger;

/**
 * Stores the number of parts found within a message.
 *
 * @author Paul King, ASERT
 * @webtest.step
 *   category="Email"
 *   name="emailStorePartCount"
 *   description="Stores the number of parts found within a message. For old-style (non-MIME) uuencoded messages, this returns the number of uuencoded attachments. For new style MIME messages, it returns the number of MIME parts found."
 */
public class EmailStorePartCount extends AbstractMessageStoreStep
{
    private static final Logger LOG = Logger.getLogger(EmailStorePartCount.class);

    protected String performOperation(final Message message) throws MessagingException {
        final Object content;
        try {
            content = message.getContent();
        } catch (IOException e) {
            LOG.error("Error processing email message: ", e);
            throw new MessagingException("Error processing email message: " + e.getMessage());
        }
        if (content instanceof Multipart) {
            return String.valueOf(performMultipartMessageCount((Multipart) content));
        }
        return String.valueOf(performSimpleMessageCount((String) content));
    }

    private static int performMultipartMessageCount(final Multipart multipart) throws MessagingException {
        return multipart.getCount();
    }

    private static int performSimpleMessageCount(final String content) {
        // iterate over string looking for ^begin ddd$
        final String lineStr = "(^.*$)";
        final String startUuencodeStr = "begin \\d\\d\\d .*";
        final Pattern linePattern = Pattern.compile(lineStr, Pattern.MULTILINE);
        final Matcher matcher = linePattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            final String line = matcher.group(0);
            if (line.matches(startUuencodeStr)) {
                count++;
            }
        }
        return count;
    }
}
