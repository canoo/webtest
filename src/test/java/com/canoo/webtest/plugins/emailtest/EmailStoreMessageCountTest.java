// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailStoreMessageCount}.
 *
 * @author Paul King, ASERT
 */
public class EmailStoreMessageCountTest extends BaseEmailTestCase
{
    private static final String PROPERTY_NAME = "dummyPropertyName";
    private static final String DEFAULT_FROM_ADDRESS = "dummyFrom@host";
    private static final String SELECTED_FROM_ADDRESS = "selectedFrom@host";
    private static final String DEFAULT_SUBJECT = "default dummy subject";
    private static final String SELECTED_SUBJECT = "selected subject";
    private static final String REGEX_SUBJECT = "/sel.*ject/";
    private static final String DEFAULT_REPLYTO_ADDRESS = "dummyReplyTo@host";
    private static final String SELECTED_REPLYTO_ADDRESS1 = "dummyReplyTo@host";
    private static final String SELECTED_REPLYTO_ADDRESS2 = "selectedReplyTo@host";
    private static final String SELECTED_REPLYTO_ADDRESSES =
            SELECTED_REPLYTO_ADDRESS1 + "," + SELECTED_REPLYTO_ADDRESS2;
    private static final String DEFAULT_CC_ADDRESS = "dummyCc@host";
    private static final String SELECTED_CC_ADDRESS = "selectedCc@host";

    protected Step createStep() {
        return new EmailStoreMessageCount();
    }

    public void testNoHeaders() throws Exception {
        final EmailStoreMessageCount step = (EmailStoreMessageCount) getStep();
        step.setProperty(PROPERTY_NAME);
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = false;
        final int numMessages = 4;
        setUpGetMessagesExpectations(step, helper, numMessages);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        executeStep(step);
        assertEquals(numMessages, Integer.parseInt(step.getWebtestProperty(PROPERTY_NAME)));
    }

    public void testHeaders1() throws Exception {
        final EmailStoreMessageCount step = (EmailStoreMessageCount) getStep();
        step.setProperty(PROPERTY_NAME);
        step.setFrom(SELECTED_FROM_ADDRESS);
        step.setSubject(SELECTED_SUBJECT);
        step.setReplyTo(SELECTED_REPLYTO_ADDRESSES);
        step.setCc(SELECTED_CC_ADDRESS);
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = false;
        final int numMessages = 4;
        final boolean[] fromFlags = {false, true, true, true};
        final boolean[] subjectFlags = {false, false, true, true};
        final boolean[] replyToFlags = {false, false, false, true};
        final boolean[] ccFlags = {false, false, false, true};
        final Message[] messages = setUpGetMessagesExpectations(step, helper, numMessages);
        setUpMessageHeaderExpectations(messages, fromFlags, subjectFlags,
                replyToFlags, ccFlags);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        executeStep(step);
        assertEquals(1, Integer.parseInt(step.getWebtestProperty(PROPERTY_NAME)));
    }

    public void testHeaders2() throws Exception {
        final EmailStoreMessageCount step = (EmailStoreMessageCount) getStep();
        step.setProperty(PROPERTY_NAME);
        step.setSubject(REGEX_SUBJECT);
        step.setReplyTo(SELECTED_REPLYTO_ADDRESSES);
        step.setCc(SELECTED_CC_ADDRESS);
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = false;
        final int numMessages = 4;
        final boolean[] fromFlags = {true, true, true, true};
        final boolean[] subjectFlags = {false, false, true, true};
        final boolean[] replyToFlags = {false, false, false, true};
        final boolean[] ccFlags = {false, false, false, false};
        final Message[] messages = setUpGetMessagesExpectations(step, helper, numMessages);
        setUpMessageHeaderExpectations(messages, fromFlags, subjectFlags,
                replyToFlags, ccFlags);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        executeStep(step);
        assertEquals(0, Integer.parseInt(step.getWebtestProperty(PROPERTY_NAME)));
    }

    private void setUpMessageHeaderExpectations(final Message[] messages, final boolean[] fromFlags,
                                                final boolean[] subjectFlags, final boolean[] replyToFlags, final boolean[] ccFlags
    ) throws MessagingException {
        for (int i = 0; i < messages.length; i++) {
            messages[i].getFrom();
            if (!fromFlags[i]) {
                modify().returnValue(new Address[]{new InternetAddress(DEFAULT_FROM_ADDRESS)});
            } else {
                modify().returnValue(new Address[]{new InternetAddress(SELECTED_FROM_ADDRESS)});
                messages[i].getSubject();
                if (!subjectFlags[i]) {
                    modify().returnValue(DEFAULT_SUBJECT);
                } else {
                    modify().returnValue(SELECTED_SUBJECT);
                    messages[i].getReplyTo();
                    if (!replyToFlags[i]) {
                        modify().returnValue(new Address[]{new InternetAddress(DEFAULT_REPLYTO_ADDRESS)});
                    } else {
                        modify().returnValue(new Address[]{
                                new InternetAddress(SELECTED_REPLYTO_ADDRESS1),
                                new InternetAddress(SELECTED_REPLYTO_ADDRESS2)});
                        messages[i].getRecipients(MimeMessage.RecipientType.CC);
                        if (!ccFlags[i]) {
                            modify().returnValue(new Address[]{new InternetAddress(DEFAULT_CC_ADDRESS)});
                        } else {
                            modify().returnValue(new Address[]{new InternetAddress(SELECTED_CC_ADDRESS)});
                            messages[i].getRecipients(MimeMessage.RecipientType.TO);
                            modify().returnValue(new Address[]{new InternetAddress("dummyTo@host")});
                        }
                    }
                }
            }
        }
    }

}
