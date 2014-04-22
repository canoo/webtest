// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;

/**
 * Stores the filename of a message part.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Email"
 * name="emailStorePartFilename"
 * description="Stores the filename of a message part. Fails if the message contains no parts or if the partIndex is invalid or if the part is an inline part with no filename."
 */
public class EmailStorePartFilename extends AbstractMessageStoreStep
{
    private static final Logger LOG = Logger.getLogger(EmailStorePartFilename.class);
    private String fPartIndex;

    public String getPartIndex() {
        return fPartIndex;
    }

    /**
     * Sets the part index.
     *
     * @param partIndex The message part of interest
     * @webtest.parameter
     *   required="no"
     *   default="0, the first part"
     *   description="The index of the message part of interest. If set for a <em>Simple</em> message, will search for a UU-encoded attachment within the message."
     */
    public void setPartIndex(final String partIndex) {
        fPartIndex = partIndex;
    }

    /**
     * Calculate the result.
     *
     * @param message
     * @return The result
     */
    protected String performOperation(final Message message) throws MessagingException {
        final Object content;
        try {
            content = message.getContent();
        }
        catch (IOException e) {
            LOG.error("Error processing email message: ", e);
            throw new MessagingException("Error processing email message: " + e.getMessage());
        }

        final int part = ConversionUtil.convertToInt(getPartIndex(), 0);
        if (content instanceof Multipart) {
            final Multipart mp = (Multipart) content;
            if (part >= mp.getCount()) {
                throw new StepFailedException("PartIndex too large.", this);
            }
            final BodyPart bodyPart = mp.getBodyPart(part);
            final String disp = bodyPart.getDisposition();
            if (Part.ATTACHMENT.equals(disp)) {
                return bodyPart.getFileName();
            }
            throw new StepFailedException("No filename for inline Message Part.", this);
        }
        return getSimpleMessageFilename((String) content, part);
    }

    private String getSimpleMessageFilename(final String content, final int part) {
        // iterate over string looking for ^begin ddd$
        final String lineStr = "(^.*$)";
        final String startUuencodeStr = "begin \\d\\d\\d .*";
        final Pattern linePattern = Pattern.compile(lineStr, Pattern.MULTILINE);
        final Matcher matcher = linePattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            final String line = matcher.group(0).trim();
            if (line.matches(startUuencodeStr)) {
                if (count == part) {
                    final int lastSpace = line.lastIndexOf(" ");
                    return line.substring(lastSpace + 1);
                }
                count++;
            }
        }
        throw new StepFailedException("No matching part found.", this);
    }

    protected void verifyParameters() {
        super.verifyParameters();
        optionalIntegerParamCheck(getPartIndex(), "partIndex", true);
    }
}