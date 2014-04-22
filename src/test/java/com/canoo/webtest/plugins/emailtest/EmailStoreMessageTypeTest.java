// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Folder;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailStoreMessageType}.
 *
 * @author Paul King, ASERT
 */
public class EmailStoreMessageTypeTest extends BaseEmailTestCase
{
    private static final String PROPERTY_NAME = "dummyProperty";
    private static final String MESSAGE_ID = "123";
    private static final boolean DELETE_ON_EXIT = false;

    protected Step createStep() {
        return new EmailStoreMessageType();
    }

    public void testMandatoryParams() {
        final EmailStoreMessageType step = (EmailStoreMessageType) getStep();
        step.getContext().put("EmailConfigInfo", null);
        assertStepRejectsNullParam("property", new TestBlock() {
            public void call() throws Throwable {
                executeStep(step);
            }
        });
    }

    public void testInvalidMessageId() {
        final EmailStoreMessageType step = (EmailStoreMessageType) getStep();
        step.getContext().put("EmailConfigInfo", null);
        step.setProperty(PROPERTY_NAME);
        step.setMessageId("non-integer");
        assertErrorOnExecute(step, "invalid messageId", "Can't parse messageId parameter with value 'non-integer' as an integer.");
    }

    public void testSimple() throws Exception {
        final EmailStoreMessageType step = (EmailStoreMessageType) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().returnValue("Simple message will return a string");
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
        assertEquals("Simple", step.getWebtestProperty(PROPERTY_NAME));
    }

    public void testMultipart() throws Exception {
        final EmailStoreMessageType step = (EmailStoreMessageType) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().returnValue(setUpMultipart());
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
        assertEquals("MultiPart", step.getWebtestProperty(PROPERTY_NAME));
    }

    public void testIoException() throws Exception {
        final EmailStoreMessageType step = (EmailStoreMessageType) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().throwException(new IOException("dummyIoException"));
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error processing content: Error processing email message: dummyIoException");
    }

    public void testNullMessage() throws Exception {
        final EmailStoreMessageType step = (EmailStoreMessageType) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Folder mockFolder = createMockFolder();
        step.getContext().put("EmailConfigInfo", MOCK_INFO);
        final String mockMid = "99";
        step.setMessageId(mockMid);
        helper.getInboxFolder(MOCK_INFO);
        modify().returnValue(mockFolder);
        helper.getMessage(Integer.parseInt(mockMid), mockFolder);
        modify().returnValue(null);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Could not find message with id '99' on server.");
    }

    private Multipart setUpMultipart() {
        return (Multipart) intercept(Multipart.class, "mockMultipart");
    }

    private EmailHelper prepareHelper(final EmailStoreMessageType step) {
        step.setProperty(PROPERTY_NAME);
        step.setMessageId(MESSAGE_ID);
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        return helper;
    }

}
