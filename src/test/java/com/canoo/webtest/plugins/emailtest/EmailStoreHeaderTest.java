// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.BodyPart;

import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailStoreHeader}.
 *
 * @author Paul King, ASERT
 */
public class EmailStoreHeaderTest extends BaseEmailTestCase
{
    private static final int PART_INDEX = 0;
    private static final String HEADER_NAME = "dummyHeaderName";
    private static final String PROPERTY_NAME = "dummyProperty";

    protected Step createStep() {
        return new EmailStoreHeader();
    }

    public void testMandatoryParams() {
        final EmailStoreHeader step = (EmailStoreHeader) getStep();
        step.getContext().put("EmailConfigInfo", null);
        assertStepRejectsNullParam("property", new TestBlock() {
            public void call() throws Throwable {
                executeStep(step);
            }
        });
        step.setProperty(PROPERTY_NAME);
        assertStepRejectsNullParam("headerName", new TestBlock() {
            public void call() throws Throwable {
                executeStep(step);
            }
        });
    }

    public void testInvalidPartIndex() {
        final EmailStoreHeader step = (EmailStoreHeader) getStep();
        step.getContext().put("EmailConfigInfo", null);
        step.setProperty(PROPERTY_NAME);
        step.setHeaderName(HEADER_NAME);
        step.setPartIndex("dummyPartIndex");
        assertErrorOnExecute(step, "invalid partIndex", "Can't parse partIndex parameter with value 'dummyPartIndex' as an integer.");
    }

    public void testNoPartIndex() throws Exception {
        final EmailStoreHeader step = (EmailStoreHeader) getStep();
        step.setProperty(PROPERTY_NAME);
        step.setHeaderName(HEADER_NAME);
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        final boolean deleteOnExit = false;
        final Message mockMessage = setUpGetMessageExpectations(step, helper, deleteOnExit);
        mockMessage.getHeader(HEADER_NAME);
        modify().returnValue(new String[]{"dummyHeaderValue"});
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        executeStep(step);
        assertEquals("dummyHeaderValue", step.getWebtestProperty(PROPERTY_NAME));
    }

    public void testHasPartIndexSimpleMessage() throws Exception {
        final EmailStoreHeader step = (EmailStoreHeader) getStep();
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        final boolean deleteOnExit = false;
        final Message mockMessage = preparePartIndexMessage(step, helper, deleteOnExit);
        mockMessage.getContent();
        modify().returnValue("dummy simple message will be a string");
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        assertFailOnExecute(step, "partIndex not for simple message",
                "PartIndex supplied for a non-MultiPart message.");
    }

    public void testHasPartIndexIoException() throws Exception {
        final EmailStoreHeader step = (EmailStoreHeader) getStep();
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        final boolean deleteOnExit = false;
        final Message mockMessage = preparePartIndexMessage(step, helper, deleteOnExit);
        mockMessage.getContent();
        modify().throwException(new IOException("dummyIoException"));
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        assertFailOnExecute(step, "fails if message exception",
                "Error processing content: Error processing email message: dummyIoException");
    }

    public void testHasPartIndexTooLarge() throws Exception {
        final EmailStoreHeader step = (EmailStoreHeader) getStep();
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        final boolean deleteOnExit = false;
        final Message mockMessage = preparePartIndexMessage(step, helper, deleteOnExit);
        final Multipart mockMultipart = setUpMultipart(-1);
        mockMessage.getContent();
        modify().returnValue(mockMultipart);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        assertFailOnExecute(step, "fails if partIndex too large", "PartIndex too large.");
    }

    public void testHasPartIndexMultiPart() throws Exception {
        final EmailStoreHeader step = (EmailStoreHeader) getStep();
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        final boolean deleteOnExit = false;
        final Message mockMessage = preparePartIndexMessage(step, helper, deleteOnExit);
        final BodyPart mockBodyPart = (BodyPart) mock(BodyPart.class, "mockBodyPart");
        mockBodyPart.getHeader(HEADER_NAME);
        modify().returnValue(new String[]{"dummyHeaderValue1", "dummyHeaderValue2"});
        final Multipart mockMultipart = setUpMultipart(2);
        mockMultipart.getBodyPart(PART_INDEX);
        modify().returnValue(mockBodyPart);
        mockMessage.getContent();
        modify().returnValue(mockMultipart);
        setUpMessageOperationFinaliseExpectations(helper, deleteOnExit);
        startVerification();
        executeStep(step);
        assertEquals("dummyHeaderValue1, dummyHeaderValue2", step.getWebtestProperty(PROPERTY_NAME));
    }

    private Multipart setUpMultipart(final int partCount) throws MessagingException {
        final Multipart mockMultipart = (Multipart) mock(Multipart.class, "mockMultipart");
        mockMultipart.getCount();
        modify().returnValue(partCount);
        return mockMultipart;
    }

    private Message preparePartIndexMessage(final EmailStoreHeader step, final EmailHelper helper,
                                            final boolean deleteOnExit) throws MessagingException {
        step.setHelper(helper);
        step.setProperty(PROPERTY_NAME);
        final String partIndex = String.valueOf(PART_INDEX);
        step.setPartIndex(partIndex);
        step.setHeaderName(HEADER_NAME);
        return setUpGetMessageExpectations(step, helper, deleteOnExit);
    }

}
