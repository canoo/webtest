// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;

import sun.misc.UUDecoder;

/**
 * Returns the content associated with a message (or message Part).
 *
 * @author Paul King, ASERT
 * @webtest.step category="Email"
 * name="emailMessageContentFilter"
 * description="Returns the content associated with a message (or message Part) as the current response."
 */
public class EmailMessageContentFilter extends AbstractEmailFilter
{
    private static final Logger LOG = Logger.getLogger(EmailMessageContentFilter.class);

    private String fContentType;
    private String fPartIndex;
    private String fFilename;

    public String getContentType() {
        return fContentType;
    }

    /**
     * Sets the contentType.
     *
     * @param contentType The contentType of the message/part of interest
     * @webtest.parameter required="no"
     * description="The contentType. The contentType to use for a <em>Simple</em> message with uuencoded attachments. For MIME MultiPart messages, the contentType if supplied is checked against the contentType found."
     */
    public void setContentType(final String contentType) {
        fContentType = contentType;
    }

    public String getPartIndex() {
        return fPartIndex;
    }

    /**
     * Sets the part index.
     *
     * @param partIndex The index of the part of interest
     * @webtest.parameter required="no"
     * description="The part index."
     */
    public void setPartIndex(final String partIndex) {
        fPartIndex = partIndex;
    }

    protected void filterContent(final Message message) throws MessagingException {
        if (StringUtils.isEmpty(getPartIndex())) {
            try {
                defineAsCurrentResponse(IOUtils.toString(message.getInputStream()), message.getContentType());
                return;
            } catch (IOException e) {
                throw new MessagingException("Error extracting message: " + e.getMessage());
            }
        }
        final int partIndex = ConversionUtil.convertToInt(getPartIndex(), 0);
        try {
            final Object content = message.getContent();
            if (content instanceof Multipart) {
                extractMultiPartMessage((Multipart) content, partIndex);
                return;
            }
            extractSimpleMessage((String) content, partIndex);
        } catch (IOException e) {
            LOG.error("Error processing email message: ", e);
            throw new MessagingException("Error processing email message: " + e.getMessage());
        }
    }

    private void extractMultiPartMessage(final Multipart parts, final int partIndex) throws MessagingException {
        try {
            if (partIndex >= parts.getCount()) {
                throw new StepFailedException("PartIndex too large.", this);
            }
            final BodyPart part = parts.getBodyPart(partIndex);
            final String contentType = part.getContentType();
            if (!StringUtils.isEmpty(getContentType()) && !contentType.equals(getContentType())) {
                throw new MessagingException("Actual contentType of '" + contentType +
                        "' did not match expected contentType of '" + fContentType + "'");
            }
            final String disp = part.getDisposition();
            final String filename = part.getFileName();
            final InputStream inputStream = part.getInputStream();
            if (Part.ATTACHMENT.equals(disp)) {
                fFilename = filename;
            } else {
                fFilename = getClass().getName();
            }
            ContextHelper.defineAsCurrentResponse(getContext(), IOUtils.toString(inputStream),
                    contentType, "http://" + fFilename);
        } catch (IOException e) {
            throw new MessagingException("Error extracting part: " + e.getMessage());
        }
    }

    private void extractSimpleMessage(final String content, final int partIndex) throws MessagingException {
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(getRawBytes(content, partIndex));
        final byte[] data;
        try {
            final UUDecoder uudc = new UUDecoder();
            data = uudc.decodeBuffer(byteStream);
        } catch (IOException e) {
            throw new MessagingException("Error Uudecoding attachment: " + e.getMessage());
        }
        if (StringUtils.isEmpty(fContentType)) {
            throw new StepFailedException("Attribute 'contentType' must be supplied for simple messages.", this);
        }
        defineAsCurrentResponse(data, getContentType());
    }

    private byte[] getRawBytes(final String content, final int partIndex) throws MessagingException {
        // iterate over string looking for ^begin ddd$
        final String lineStr = "(^.*$)";
        final String startUuencodeStr = "begin \\d\\d\\d .*";
        final String endUuencodeStr = "^end.*";
        final Pattern linePattern = Pattern.compile(lineStr, Pattern.MULTILINE);
        final Matcher matcher = linePattern.matcher(content);
        boolean extracting = false;
        int count = 0;
        final StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            final String line = matcher.group(0);
            if (extracting) {
                if (line.matches(endUuencodeStr)) {
                    buf.append(" \n").append(line).append('\n');
                    extracting = false;
                    break;
                } else {
                    buf.append(line).append('\n');
                }
            } else if (line.matches(startUuencodeStr)) {
                if (count++ == partIndex) {
                    extracting = true;
                    buf.append(line).append('\n');
                    final int lastSpace = line.lastIndexOf(" ");
                    fFilename = line.substring(lastSpace + 1);
                }
            }
        }
        if (buf.length() == 0) {
            throw new StepFailedException("Unable to find part with index " + partIndex + ".");
        }
        LOG.debug("buf=" + buf.toString());
        return buf.toString().getBytes();
    }

    protected void verifyParameters() {
        super.verifyParameters();
        optionalIntegerParamCheck(getPartIndex(), "partIndex", true);
    }
}
