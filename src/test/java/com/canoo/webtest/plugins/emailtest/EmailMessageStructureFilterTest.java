package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Enumeration;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Header;

import com.canoo.webtest.steps.Step;

/**
 * Test for {@link EmailMessageStructureFilter}.
 *
 * @author Paul King, ASERT
 */
public class EmailMessageStructureFilterTest extends BaseEmailTestCase
{
    private static final boolean DELETE_ON_EXIT = false;
    private static final String LS = System.getProperty("line.separator");
    private static final String PLAIN_CONTENT_TYPE = "text/plain";
    private static final String XML_CONTENT_TYPE = "text/xml";

    private static final String BODY_1 = "Simple Message Body";
    private static final String BODY_2 = "Simple Message with one attachment" + LS +
        "begin 644 dummy.txt" + LS +
        "51'5M;7D@5&5X=\"!!='1A8VAM96YT " + LS +
        "end" + LS;
    private static final String STRUCTURE_1 = "<message type=\"Simple\" contentType=\"text/plain\">" + LS +
            "</message>";
    private static final String STRUCTURE_2 = "<message type=\"Simple\" contentType=\"text/plain\">" + LS +
            "    <part type=\"uuencoded\" filename=\"dummy.txt\"/>" + LS +
            "</message>";
    private static final String STRUCTURE_3 = "<message type=\"MIME\" contentType=\"text/plain\">" + LS +
            "</message>";
    private static final String STRUCTURE_4 = "<message type=\"MIME\" contentType=\"text/plain\">" + LS +
            "    <part type=\"attachment\" filename=\"dummyFilename0\" contentType=\"text/xml\"/>" + LS +
            "    <part type=\"inline\" contentType=\"text/plain\"/>" + LS +
            "</message>";
    private static final String STRUCTURE_5 = "<message type=\"Simple\" contentType=\"text/plain\">" + LS +
            "    <header name=\"subject\" value=\"dummyHeaderValue0\"/>" + LS +
            "    <header name=\"from\" value=\"dummyHeaderValue1\"/>" + LS +
            "</message>";

    protected Step createStep() {
        return new EmailMessageStructureFilter();
    }

    public void testInvalidMessageId() {
        final EmailMessageStructureFilter step = (EmailMessageStructureFilter) getStep();
        step.getContext().put("EmailConfigInfo", null);
        assertErrorOnExecute(step, "invalid messageId", "Can't parse messageId parameter with value 'null' as an integer.");
        step.setMessageId("non-integer");
        assertErrorOnExecute(step, "invalid messageId", "Can't parse messageId parameter with value 'non-integer' as an integer.");
    }

    public void testNoHeaderSimpleMessage1() throws Exception {
        checkNoHeaderSimpleMessage(BODY_1, STRUCTURE_1, PLAIN_CONTENT_TYPE, null);
    }

    public void testNoHeaderSimpleMessage2() throws Exception {
        checkNoHeaderSimpleMessage(BODY_2, STRUCTURE_2, "text/plain; charset='US-ASCII'", null);
    }

    public void testNoHeaderSimpleMessage3() throws Exception {
        checkNoHeaderSimpleMessage(BODY_1, STRUCTURE_5, PLAIN_CONTENT_TYPE, "subject,from");
    }

    private void checkNoHeaderSimpleMessage(final String body, final String structure,
                                            final String contentType, final String headers) throws Exception {
        final EmailMessageStructureFilter step = (EmailMessageStructureFilter) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().returnValue(body);
        mockMessage.getContentType();
        modify().returnValue(contentType);
        if (headers != null) {
            step.setHeaders(headers);
            setUpHeaders(mockMessage, headers);
        }
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
        assertEquals(structure, step.getContext().getCurrentResponse().getWebResponse().getContentAsString());
        assertEquals(XML_CONTENT_TYPE, step.getContext().getCurrentResponse().getWebResponse().getContentType());
    }

    private void setUpHeaders(final Message mockMessage, final String headerStr) throws MessagingException {
        final StringTokenizer tokens = new StringTokenizer(headerStr, ", ");
        final Header[] headers = new Header[tokens.countTokens() + 1];
        for (int i = 0; i < headers.length - 1; i++) {
            headers[i] = new Header(tokens.nextToken(), "dummyHeaderValue" + i);
        }
        headers[headers.length - 1] = new Header("foo", "bar"); // add unused header
        final Enumeration headerEnum = new Enumeration() {
            private int fCount;
            public boolean hasMoreElements() {
                return fCount < headers.length;
            }
            public Object nextElement() {
                return headers[fCount++];
            }
        };
        mockMessage.getAllHeaders();
        modify().returnValue(headerEnum);
    }

    public void testIoException() throws Exception {
        final EmailMessageStructureFilter step = (EmailMessageStructureFilter) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().throwException(new IOException("dummyIoException"));
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        assertFailOnExecute(step, "expected to fail",
                "Error performing operation: Error processing email message: dummyIoException");
    }

    public void testSaveParams() throws Exception {
        final EmailMessageStructureFilter step = (EmailMessageStructureFilter) getStep();
        final EmailHelper helper = prepareHelper(step);
        step.setSaveResponse("MyResponse");
        step.setSavePrefix("MyPrefix");
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        mockMessage.getContent();
        modify().returnValue("dummy");
        mockMessage.getContentType();
        modify().returnValue("dummy");
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
    }

    public void testMultipart1() throws Exception {
        checkMultipart(0, STRUCTURE_3);
    }

    public void testMultipart2() throws Exception {
        checkMultipart(2, STRUCTURE_4);
    }

    private void checkMultipart(final int partCount, final String expectedStructure) throws Exception {
        final EmailMessageStructureFilter step = (EmailMessageStructureFilter) getStep();
        final EmailHelper helper = prepareHelper(step);
        final Message mockMessage = setUpGetMessageExpectations(step, helper, DELETE_ON_EXIT);
        final Multipart mockMultipart = setUpMultipart(partCount);
        mockMessage.getContent();
        modify().returnValue(mockMultipart);
        mockMessage.getContentType();
        modify().returnValue(PLAIN_CONTENT_TYPE);
        setUpMessageOperationFinaliseExpectations(helper, DELETE_ON_EXIT);
        startVerification();
        executeStep(step);
        assertEquals(expectedStructure, step.getContext().getCurrentResponse().getWebResponse().getContentAsString());
        assertEquals(XML_CONTENT_TYPE, step.getContext().getCurrentResponse().getWebResponse().getContentType());
    }

    private BodyPart setUpBodyPart(final String disposition, final String contentType,
                                   final String filename, final String id) throws Exception {
        final BodyPart mockBodyPart = (BodyPart) intercept(BodyPart.class, id);
        mockBodyPart.getDisposition();
        modify().returnValue(disposition);
        mockBodyPart.getContentType();
        modify().returnValue(contentType);
        if (disposition.equals(Part.ATTACHMENT)) {
            mockBodyPart.getFileName();
            modify().returnValue(filename);
        }
        return mockBodyPart;
    }

    private static final String[] DISP = {Part.ATTACHMENT, Part.INLINE};
    private static final String[] TYPE = {XML_CONTENT_TYPE, PLAIN_CONTENT_TYPE};

    private Multipart setUpMultipart(final int partCount) throws Exception {
        final Multipart mockMultipart = (Multipart) intercept(Multipart.class, "mockMultipart");
        mockMultipart.getCount();
        modify().returnValue(partCount);
        for (int i = 0; i < partCount; i++) {
            final Part part = setUpBodyPart(DISP[i % 2], TYPE[i % 2], "dummyFilename" + i, "mockBodyPart" + i);
            mockMultipart.getBodyPart(i);
            modify().returnValue(part);
        }
        return mockMultipart;
    }

    private EmailHelper prepareHelper(final EmailMessageStructureFilter step) {
        final EmailHelper helper = (EmailHelper) mock(EmailHelper.class, "helper");
        step.setHelper(helper);
        return helper;
    }

}
