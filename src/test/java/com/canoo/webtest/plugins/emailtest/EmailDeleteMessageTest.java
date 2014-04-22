// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Message;
import javax.mail.Folder;
import javax.mail.MessagingException;

import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailDeleteMessage}.
 *
 * @author Paul King, ASERT
 */
public class EmailDeleteMessageTest extends BaseEmailTestCase
{
    protected Step createStep() {
        return new EmailDeleteMessage();
    }

    public void testMandatoryParams() {
        final Step step = getStep();
        step.getContext().put("EmailConfigInfo", null);
        assertErrorOnExecute(step, "messageId not set",
                "Can't parse messageId parameter with value 'null' as an integer.");
    }

    public void testCallsHelper() throws Exception {
        final EmailDeleteMessage step = (EmailDeleteMessage) getStep();
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = true;
        final Message mockMessage = setUpGetMessageExpectations(step, helper, deleteOnExit);
        helper.markForDelete(mockMessage);
        modify().args(is.instanceOf(Message.class));
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        executeStep(step);
    }

    public void testNullMessage() throws Exception {
        final EmailDeleteMessage step = (EmailDeleteMessage) getStep();
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = true;
        final Folder mockFolder = createMockFolder();
        step.getContext().put("EmailConfigInfo", MOCK_INFO);
        final String mockMid = "99";
        step.setMessageId(mockMid);
        helper.getInboxFolder(MOCK_INFO);
        modify().returnValue(mockFolder);
        helper.getMessage(Integer.parseInt(mockMid), mockFolder);
        modify().returnValue(null);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        assertFailOnExecute(step, "expected to fail", "Could not retrieve message.");
    }

    public void testMessageException() throws Exception {
        final EmailDeleteMessage step = (EmailDeleteMessage) getStep();
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = true;
        final Folder mockFolder = createMockFolder();
        step.getContext().put("EmailConfigInfo", MOCK_INFO);
        final String mockMid = "99";
        step.setMessageId(mockMid);
        helper.getInboxFolder(MOCK_INFO);
        modify().returnValue(mockFolder);
        helper.getMessage(Integer.parseInt(mockMid), mockFolder);
        modify().throwException(new MessagingException("Dummy messaging exception"));
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        assertFailOnExecute(step, "expected to fail", "Error performing operation: Dummy messaging exception");
    }

}
