// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Message;

import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailStoreMessageId}.
 *
 * @author Paul King, ASERT
 */
public class EmailStoreMessageIdTest extends BaseEmailTestCase
{
    private static final String PROPERTY_NAME = "dummyPropertyName";
    private static final String DUMMY_MESSAGE_ID = "123";

    protected Step createStep() {
        return new EmailStoreMessageId();
    }

    public void testInvalidMessageIndex() {
        final EmailStoreMessageId step = (EmailStoreMessageId) getStep();
        step.getContext().put("EmailConfigInfo", null);
        step.setProperty(PROPERTY_NAME);
        step.setMessageIndex("non-integer");
        assertErrorOnExecute(step, "invalid messageIndex",
                "Can't parse messageIndex parameter with value 'non-integer' as an integer.");
    }

    public void testNoMatches() throws Exception {
        final EmailStoreMessageId step = (EmailStoreMessageId) getStep();
        step.setProperty(PROPERTY_NAME);
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = false;
        setUpGetMessagesExpectations(step, helper, 0);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error processing content: No messages matching criteria.");
    }

    public void testIndexTooBig() throws Exception {
        final EmailStoreMessageId step = (EmailStoreMessageId) getStep();
        step.setProperty(PROPERTY_NAME);
        step.setMessageIndex("6");
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = false;
        setUpGetMessagesExpectations(step, helper, 4);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error processing content: Invalid messageIndex '6', valid range is 0..3");
    }

    public void testIndexJustRight() throws Exception {
        final EmailStoreMessageId step = (EmailStoreMessageId) getStep();
        step.setProperty(PROPERTY_NAME);
        step.setMessageIndex("2");
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = false;
        Message[] messages = setUpGetMessagesExpectations(step, helper, 4);
        messages[2].getMessageNumber();
        modify().returnValue(Integer.parseInt(DUMMY_MESSAGE_ID));
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        executeStep(step);
        assertEquals(DUMMY_MESSAGE_ID, step.getWebtestProperty(PROPERTY_NAME));
    }

}
