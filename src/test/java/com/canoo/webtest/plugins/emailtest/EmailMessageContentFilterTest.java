package com.canoo.webtest.plugins.emailtest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;

import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailMessageContentFilter}.
 *
 * @author Paul King, ASERT
 */
public class EmailMessageContentFilterTest extends BaseEmailTestCase
{
    private static final String MESSAGE_ID = "123";
    private static final boolean DELETE_ON_EXIT = false;
    private static final String PLAIN_CONTENT_TYPE = "text/plain";
    private static final String SIMPLE_BODY = "Dummy Email Message";
    private static final String LS = System.getProperty("line.separator");
    private static final String MESSAGE_BODY = "Simple Message with one attachment" + LS +
        "begin 644 dummy.txt" + LS +
        "51'5M;7D@5&5X=\"!!='1A8VAM96YT " + LS +
        "end" + LS;
    private static final String DONT_CARE_DISPOSITION = Part.INLINE;
    private static final String DUMMY_CONTENT = "Dummy Content";

    protected Step createStep() {
        return new EmailMessageContentFilter();
    }

    public void testInvalidMessageId() {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        step.getContext().put("EmailConfigInfo", null);
        assertErrorOnExecute(step, "invalid messageId", "Can't parse messageId parameter with value 'null' as an integer.");
        step.setMessageId("non-integer");
        assertErrorOnExecute(step, "invalid messageId", "Can't parse messageId parameter with value 'non-integer' as an integer.");
    }

    public void testInvalidPartIndex() {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        step.getContext().put("EmailConfigInfo", null);
        step.setMessageId(MESSAGE_ID);
        step.setPartIndex("non-integer");
        assertErrorOnExecute(step, "invalid partIndex", "Can't parse partIndex parameter with value 'non-integer' as an integer.");
    }

    public void testNoPartIndex() throws Exception {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        final EmailHelper helper = prepareHelper(step, -1);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getInputStream();
        modify().returnValue(new ByteArrayInputStream(SIMPLE_BODY.getBytes()));
        mockMessage.getContentType();
        modify().returnValue(PLAIN_CONTENT_TYPE);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
        assertEquals(SIMPLE_BODY, step.getContext().getCurrentResponse().getWebResponse().getContentAsString());
        assertEquals(PLAIN_CONTENT_TYPE, step.getContext().getCurrentResponse().getWebResponse().getContentType());
    }

    public void testNoPartIndexIoException() throws Exception {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        final EmailHelper helper = prepareHelper(step, -1);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getInputStream();
        modify().throwException(new IOException("dummyIoException"));
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error performing operation: Error extracting message: dummyIoException");
    }

    public void testPartIndexIoException() throws Exception {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        final EmailHelper helper = prepareHelper(step, 0);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().throwException(new IOException("dummyIoException"));
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error performing operation: Error processing email message: dummyIoException");
    }

    public void testSimpleNoAttachments() throws Exception {
        checkSimpleAttachments(0, null,
                "Simple Message with no attachments " + LS,
                "Unable to find part with index 0.");
    }

    public void testSimpleBadUudecode() throws Exception {
        checkSimpleAttachments(0, null,
                "Simple Message with bad attachment" + LS +
                "begin 123 but no end" + LS,
                "Error performing operation: Error Uudecoding attachment: UUDecoder: Short Buffer.");
    }

    public void testSimpleWithAttachmentNoContentType() throws Exception {
        checkSimpleAttachments(0, null, MESSAGE_BODY,
                "Attribute 'contentType' must be supplied for simple messages.");
    }

    public void testSimpleWithPartIndexTooLarge() throws Exception {
        checkSimpleAttachments(1, PLAIN_CONTENT_TYPE, MESSAGE_BODY,
                "Unable to find part with index 1.");
    }

    public void testSimpleWithAttachment() throws Exception {
        final Step step = checkSimpleAttachments(0, PLAIN_CONTENT_TYPE, MESSAGE_BODY, null);
        assertEquals("Dummy Text Attachment", step.getContext().getCurrentResponse().getWebResponse().getContentAsString());
        assertEquals(PLAIN_CONTENT_TYPE, step.getContext().getCurrentResponse().getWebResponse().getContentType());
    }

    private EmailMessageContentFilter checkSimpleAttachments(final int partIndex, final String contentType, final String messageBody,
                                                             final String failMessage) throws Exception {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        step.setContentType(contentType);
        final EmailHelper helper = prepareHelper(step, partIndex);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().returnValue(messageBody);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        if (failMessage != null) {
            assertFailOnExecute(step, "expected to fail", failMessage);
        } else {
            executeStep(step);
        }
        return step;
    }

    public void testMultipart1() throws Exception {
        checkMultipart(0, 0, "PartIndex too large.", null, null, null);
    }

    public void testMultipart2() throws Exception {
        checkMultipart(1, 0,
            "Error performing operation: Actual contentType of 'text/plain' did not match expected contentType of 'text/html'",
            DONT_CARE_DISPOSITION, "text/html", PLAIN_CONTENT_TYPE);
    }

    public void testMultipart3() throws Exception {
        final Step step = checkMultipart(1, 0, null, Part.ATTACHMENT, null, PLAIN_CONTENT_TYPE);
        assertEquals(DUMMY_CONTENT, step.getContext().getCurrentResponse().getWebResponse().getContentAsString());
        assertEquals(PLAIN_CONTENT_TYPE, step.getContext().getCurrentResponse().getWebResponse().getContentType());
    }

    public void testMultipart4() throws Exception {
        final Step step = checkMultipart(1, 0, null, Part.INLINE, null, PLAIN_CONTENT_TYPE);
        assertEquals(DUMMY_CONTENT, step.getContext().getCurrentResponse().getWebResponse().getContentAsString());
        assertEquals(PLAIN_CONTENT_TYPE, step.getContext().getCurrentResponse().getWebResponse().getContentType());
    }

    private EmailMessageContentFilter checkMultipart(final int partCount, final int partIndex, final String failureMessage,
                                final String disposition, final String expectedContentType,
                                final String actualContentType) throws Exception {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        step.setContentType(expectedContentType);
        final EmailHelper helper = prepareHelper(step, partIndex);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        final Multipart mockMultipart = setUpMultipart(partCount, partIndex, disposition, expectedContentType, actualContentType);
        mockMessage.getContent();
        modify().returnValue(mockMultipart);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        if (failureMessage != null) {
            assertFailOnExecute(step, "expected to fail", failureMessage);
        } else {
            executeStep(step);
        }
        return step;
    }

    public void testMultipartWithIoException() throws Exception {
        final EmailMessageContentFilter step = (EmailMessageContentFilter) getStep();
        final EmailHelper helper = prepareHelper(step, 0);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        final Multipart mockMultipart = (Multipart) intercept(Multipart.class, "mockMultipart");
        mockMultipart.getCount();
        modify().throwException(new IOException("Dummy Messaging Exception"));
        mockMessage.getContent();
        modify().returnValue(mockMultipart);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error performing operation: Error extracting part: Dummy Messaging Exception");
    }

    private BodyPart setUpBodyPart(final String disposition, final String expectedContentType,
                                   final String actualContentType) throws Exception {
        final BodyPart mockBodyPart = (BodyPart) mock(BodyPart.class, "mockBodyPart");
        mockBodyPart.getContentType();
        modify().returnValue(actualContentType);
        if (expectedContentType == null || expectedContentType.equals(actualContentType)) {
            mockBodyPart.getDisposition();
            modify().returnValue(disposition);
            mockBodyPart.getFileName();
            mockBodyPart.getInputStream();
            modify().returnValue(new ByteArrayInputStream(DUMMY_CONTENT.getBytes()));
        }
        return mockBodyPart;
    }

    private Multipart setUpMultipart(final int partCount, final int partIndex,
                                     final String disposition, final String expectedContentType,
                                     final String actualContentType) throws Exception {
        final Multipart mockMultipart = (Multipart) intercept(Multipart.class, "mockMultipart");
        mockMultipart.getCount();
        modify().returnValue(partCount);
        if (partCount > partIndex) {
            final BodyPart mockBodyPart = setUpBodyPart(disposition, expectedContentType, actualContentType);
            mockMultipart.getBodyPart(partIndex);
            modify().returnValue(mockBodyPart);
        }
        return mockMultipart;
    }

    private EmailHelper prepareHelper(final EmailMessageContentFilter step, final int partIndex) {
        if (partIndex != -1) {
            step.setPartIndex(String.valueOf(partIndex));
        }
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        return helper;
    }

}
