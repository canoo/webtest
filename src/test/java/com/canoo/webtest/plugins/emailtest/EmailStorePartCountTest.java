// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.MessagingException;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailStorePartCount}.
 *
 * @author Paul King, ASERT
 */
public class EmailStorePartCountTest extends BaseEmailTestCase
{
    private static final String PROPERTY_NAME = "dummyProperty";
    private static final String MESSAGE_ID = "123";
    private static final boolean DELETE_ON_EXIT = false;
    private static final String LS = System.getProperty("line.separator");

    protected Step createStep() {
        return new EmailStorePartCount();
    }

    public void testMandatoryParams() {
        final EmailStorePartCount step = (EmailStorePartCount) getStep();
        step.getContext().put("EmailConfigInfo", null);
        assertStepRejectsNullParam("property", new TestBlock() {
            public void call() throws Throwable {
                executeStep(step);
            }
        });
    }

    public void testInvalidMessageId() {
        final EmailStorePartCount step = (EmailStorePartCount) getStep();
        step.getContext().put("EmailConfigInfo", null);
        step.setProperty(PROPERTY_NAME);
        step.setMessageId("non-integer");
        assertErrorOnExecute(step, "invalid messageId", "Can't parse messageId parameter with value 'non-integer' as an integer.");
    }

    public void testSimpleNoAttachments() throws Exception {
        checkSimpleAttachments("Simple Message with no attachments " + LS, 0);
    }

    public void testSimpleAttachment() throws Exception {
        checkSimpleAttachments("Message" + LS + "begin 123 " + LS, 1);
    }

    public void testSimpleAttachments() throws Exception {
        checkSimpleAttachments("Message" + LS + "begin 123 " + LS + "begin 345 " + LS, 2);
    }

    private void checkSimpleAttachments(final String messageBody, final int numAttachments) throws Exception {
        final EmailStorePartCount step = (EmailStorePartCount) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().returnValue(messageBody);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
        assertEquals(numAttachments, Integer.parseInt(step.getWebtestProperty(PROPERTY_NAME)));
    }

    public void testMultipart1() throws Exception {
        checkMultipart(1);
    }

    public void testMultipart2() throws Exception {
        checkMultipart(2);
    }

    private void checkMultipart(final int partCount) throws Exception {
        final EmailStorePartCount step = (EmailStorePartCount) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        final Multipart multipart = setUpMultipart(partCount);
        mockMessage.getContent();
        modify().returnValue(multipart);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
        assertEquals(partCount, Integer.parseInt(step.getWebtestProperty(PROPERTY_NAME)));
    }

    public void testIoException() throws Exception {
        final EmailStorePartCount step = (EmailStorePartCount) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().throwException(new IOException("dummyIoException"));
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error processing content: Error processing email message: dummyIoException");
    }

    private Multipart setUpMultipart(int partCount) throws MessagingException {
        Multipart mockMultipart = (Multipart) intercept(Multipart.class, "mockMultipart");
        mockMultipart.getCount();
        modify().returnValue(partCount);
        return mockMultipart;
    }

    private EmailHelper prepareHelper(final EmailStorePartCount step) {
        step.setProperty(PROPERTY_NAME);
        step.setMessageId(MESSAGE_ID);
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        return helper;
    }

}
