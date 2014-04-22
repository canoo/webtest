// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.MessagingException;
import javax.mail.BodyPart;
import javax.mail.Part;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailStorePartFilename}.
 *
 * @author Paul King, ASERT
 */
public class EmailStorePartFilenameTest extends BaseEmailTestCase
{
    private static final String PROPERTY_NAME = "dummyProperty";
    private static final String MESSAGE_ID = "123";
    private static final boolean DELETE_ON_EXIT = false;
    private static final String LS = System.getProperty("line.separator");

    protected Step createStep() {
        return new EmailStorePartFilename();
    }

    public void testMandatoryParams() {
        final EmailStorePartFilename step = (EmailStorePartFilename) getStep();
        step.getContext().put("EmailConfigInfo", null);
        assertStepRejectsNullParam("property", new TestBlock() {
            public void call() throws Throwable {
                executeStep(step);
            }
        });
    }

    public void testInvalidMessageId() {
        final EmailStorePartFilename step = (EmailStorePartFilename) getStep();
        step.getContext().put("EmailConfigInfo", null);
        step.setProperty(PROPERTY_NAME);
        step.setMessageId("non-integer");
        assertErrorOnExecute(step, "invalid messageId", "Can't parse messageId parameter with value 'non-integer' as an integer.");
    }

    public void testInvalidPartIndex() {
        final EmailStorePartFilename step = (EmailStorePartFilename) getStep();
        step.getContext().put("EmailConfigInfo", null);
        step.setProperty(PROPERTY_NAME);
        step.setPartIndex("non-integer");
        assertErrorOnExecute(step, "invalid partIndex", "Can't parse partIndex parameter with value 'non-integer' as an integer.");
    }

    public void testIoException() throws Exception {
        final EmailStorePartFilename step = (EmailStorePartFilename) getStep();
        final EmailHelper helper = prepareHelper(step, 0);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().throwException(new IOException("dummyIoException"));
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error processing content: Error processing email message: dummyIoException");
    }

    public void testSimpleNoAttachments() throws Exception {
        checkSimpleAttachments("Simple Message with no attachments " + LS, null, true, 0);
    }

    public void testSimpleAttachment() throws Exception {
        checkSimpleAttachments("Message" + LS + "begin 123 abcdef.txt" + LS,
                "abcdef.txt", false, 0);
    }

    public void testSimpleAttachments1() throws Exception {
        checkSimpleAttachments("Message" + LS + "begin 123 abc.txt" + LS + "begin 345 def.txt" + LS,
                "abc.txt", false, 0);
    }

    public void testSimpleAttachments2() throws Exception {
        checkSimpleAttachments("Message" + LS + "begin 123 abc.txt" + LS + "begin 345 def.txt" + LS,
                "def.txt", false, 1);
    }

    private void checkSimpleAttachments(final String messageBody, final String expectedFilename,
                                        final boolean willFail, final int partIndex) throws Exception {
        final EmailStorePartFilename step = (EmailStorePartFilename) getStep();
        final EmailHelper helper = prepareHelper(step, partIndex);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().returnValue(messageBody);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        if (willFail) {
            assertFailOnExecute(step, "expected to fail", "No matching part found.");
        } else {
            executeStep(step);
            assertEquals(expectedFilename, step.getWebtestProperty(PROPERTY_NAME));
        }
    }

    public void testMultipart1() throws Exception {
        checkMultipart(0, 0, "PartIndex too large.", null, null);
    }

    public void testMultipart2() throws Exception {
        checkMultipart(1, 0, "No filename for inline Message Part.", Part.INLINE, null);
    }

    public void testMultipart3() throws Exception {
        checkMultipart(1, 0, null, Part.ATTACHMENT, "dummyFilename1");
    }

    public void testMultipart4() throws Exception {
        checkMultipart(2, 1, null, Part.ATTACHMENT, "dummyFilename2");
    }

    private void checkMultipart(final int partCount, final int partIndex,
                                final String failureMessage, final String disposition,
                                final String expectedFilename) throws Exception {
        final EmailStorePartFilename step = (EmailStorePartFilename) getStep();
        final EmailHelper helper = prepareHelper(step, partIndex);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        final Multipart mockMultipart = setUpMultipart(partCount, partIndex, disposition, expectedFilename);
        mockMessage.getContent();
        modify().returnValue(mockMultipart);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        if (failureMessage != null) {
            assertFailOnExecute(step, "expected to fail", failureMessage);
        } else {
            executeStep(step);
            assertEquals(expectedFilename, step.getWebtestProperty(PROPERTY_NAME));
        }
    }

    private BodyPart setUpBodyPart(final String disposition, final String filename) throws MessagingException {
        final BodyPart mockBodyPart = (BodyPart) mock(BodyPart.class, "mockBodyPart");
        mockBodyPart.getDisposition();
        modify().returnValue(disposition);
        if (disposition.equals(Part.ATTACHMENT)) {
            mockBodyPart.getFileName();
            modify().returnValue(filename);
        }
        return mockBodyPart;
    }

    private Multipart setUpMultipart(final int partCount, final int partIndex,
                                     final String disposition, final String expectedFilename) throws MessagingException {
        final Multipart mockMultipart = (Multipart) intercept(Multipart.class, "mockMultipart");
        mockMultipart.getCount();
        modify().returnValue(partCount);
        if (partCount > partIndex) {
            final BodyPart mockBodyPart = setUpBodyPart(disposition, expectedFilename);
            mockMultipart.getBodyPart(partIndex);
            modify().returnValue(mockBodyPart);
        }
        return mockMultipart;
    }

    private EmailHelper prepareHelper(final EmailStorePartFilename step, final int partIndex) {
        step.setProperty(PROPERTY_NAME);
        step.setMessageId(MESSAGE_ID);
        step.setPartIndex(String.valueOf(partIndex));
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        return helper;
    }

}
