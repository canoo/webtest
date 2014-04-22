// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract class used by email steps needing to select one or more messages.
 *
 * @author Paul King
 */
public abstract class AbstractSelectStep extends AbstractStoreStep
{
    private String fSubject;
    private String fFrom;
    private String fTo;
    private String fCc;
    private String fReplyTo;

    /**
     * Sets the Subject field.
     *
     * @param value The Subject field
     * @webtest.parameter required="no"
     * description="The email Subject header field."
     */
    public void setSubject(final String value) {
        fSubject = value;
    }

    public String getSubject() {
        return fSubject;
    }

    /**
     * Sets the From field.
     *
     * @param value The From field
     * @webtest.parameter required="no"
     * description="The email From header field."
     */
    public void setFrom(final String value) {
        fFrom = value;
    }

    public String getFrom() {
        return fFrom;
    }

    /**
     * Sets the To field.
     *
     * @param value The To field
     * @webtest.parameter required="no"
     * description="The email To header field."
     */
    public void setTo(final String value) {
        fTo = value;
    }

    public String getTo() {
        return fTo;
    }

    /**
     * Sets the Cc field.
     *
     * @param value The Cc field
     * @webtest.parameter required="no"
     * description="The email Cc header field."
     */
    public void setCc(final String value) {
        fCc = value;
    }

    public String getCc() {
        return fCc;
    }

    /**
     * Sets the ReplyTo field.
     *
     * @param value The ReplyTo field
     * @webtest.parameter required="no"
     * description="The email ReplyTo header field."
     */
    public void setReplyTo(final String value) {
        fReplyTo = value;
    }

    public String getReplyTo() {
        return fReplyTo;
    }

    protected Message[] retrieveMatchingMessages(final Folder folder) throws MessagingException {
        final Message[] messages = getHelper().getMessages(folder);
        // For efficiency
        if (noMatchCriteria()) {
            return messages;
        }
        final List result = new ArrayList();
        for (int message = 0; message < messages.length; message++) {
            if (messageMatches(messages[message])) {
                result.add(messages[message]);
            }
        }
        return (Message[]) result.toArray(new Message[]{});
    }

    private boolean noMatchCriteria() {
        return StringUtils.isEmpty(getSubject()) && StringUtils.isEmpty(getFrom()) && StringUtils.isEmpty(getTo())
                && StringUtils.isEmpty(getCc()) && StringUtils.isEmpty(getReplyTo());
    }

    boolean messageMatches(final Message message) throws MessagingException {
        if (!doMatch(getFrom(), message.getFrom()[0].toString())) {
            return false;
        }
        if (!doMatch(getSubject(), message.getSubject())) {
            return false;
        }
        if (!doMatchMultiple(getReplyTo(), message.getReplyTo())) {
            return false;
        }
        if (!doMatchMultiple(getCc(), message.getRecipients(MimeMessage.RecipientType.CC))) {
            return false;
        }
        return doMatchMultiple(getTo(), message.getRecipients(MimeMessage.RecipientType.TO));
    }

    static boolean doMatch(final String expected, String actual) {
    	actual = StringUtils.defaultString(actual, ""); // catch null

    	// semantics are: if no expectation then match
        if (StringUtils.isEmpty(expected)) {
            return true;
        }

        if (isRegexMatch(expected)) {
            return getVerifier(true).verifyStrings(expected.substring(1, expected.length() - 1), actual);
        }
        return getVerifier(false).verifyStrings(expected, actual);
    }

    static boolean doMatchMultiple(final String expected, final Address[] actuals) {
        // semantics are: if no expectation then match
        if (StringUtils.isEmpty(expected)) {
            return true;
        }
        else if (actuals == null) {
        	return false;
        }

        final StringTokenizer expectedTokens = new StringTokenizer(expected, ",");
        while (expectedTokens.hasMoreTokens()) {
            final String expectedToken = expectedTokens.nextToken().trim();
            boolean hasMatched = false;
            for (int i = 0; i < actuals.length; i++) {
                final Address actual = actuals[i];
                hasMatched = doMatch(expectedToken, actual.toString());
                if (hasMatched) {
                    break;
                }
            }
            if (!hasMatched) {
                return false;
            }
        }
        return true;
    }

    protected static boolean isRegexMatch(final String expected) {
        return expected.startsWith("/") && expected.endsWith("/") && expected.length() > 1;
    }
}

