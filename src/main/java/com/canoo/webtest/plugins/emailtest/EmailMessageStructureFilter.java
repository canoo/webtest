// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;
import java.util.Enumeration;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Header;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * Returns the message structure.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Email"
 * name="emailMessageStructureFilter"
 * description="Returns the structure (expressed in XML) associated with a particular message as the current response."
 */
public class EmailMessageStructureFilter extends AbstractEmailFilter
{
    private static final Logger LOG = Logger.getLogger(EmailMessageStructureFilter.class);
    private static final String LS = System.getProperty("line.separator");
    private String fHeaders;
    private String[] fTokenizedHeaders;

    public String getHeaders() {
        return fHeaders;
    }

    /**
     * @webtest.parameter
     *   required="no"
     *   default="no headers"
     *   description="A comma or space seperated list of headers to include in the structure description."
     */
    public void setHeaders(final String headers) {
        fHeaders = headers;
    }

    protected void filterContent(final Message message) throws MessagingException {
        if (!StringUtils.isEmpty(getHeaders())) {
            prepareHeaders();
        }
        try {
            final Object content = message.getContent();
            if (content instanceof Multipart) {
                filterMultiPartMessage(content, message);
                return;
            }
            filterSimpleMessage((String) content, message);
        } catch (IOException e) {
            LOG.error("Error processing email message: ", e);
            throw new MessagingException("Error processing email message: " + e.getMessage());
        }
    }

    private void prepareHeaders() {
        final StringTokenizer tokens = new StringTokenizer(getHeaders(), " ,");
        fTokenizedHeaders = new String[tokens.countTokens()];
        for (int i = 0; i < fTokenizedHeaders.length; i++) {
            fTokenizedHeaders[i] = tokens.nextToken();
        }
    }

    private void filterMultiPartMessage(final Object content, final Message message) throws MessagingException {
        final Multipart parts = (Multipart) content;
        final StringBuffer buf = new StringBuffer();
        buf.append("<message type=\"MIME\" contentType=\"");
        buf.append(extractBaseContentType(message.getContentType())).append("\">").append(LS);
        processHeaders(buf, message);
        int count = parts.getCount();
        for (int i = 0; i < count; i++) {
            buf.append(processMessagePart(parts.getBodyPart(i)));
        }
        buf.append("</message>");
        defineAsCurrentResponse(buf.toString().getBytes(), "text/xml");
    }

    private void processHeaders(final StringBuffer buf, final Message message) throws MessagingException {
        if (fTokenizedHeaders != null) {
            final Enumeration allHeaders = message.getAllHeaders();
            while (allHeaders.hasMoreElements()) {
                final Header header = (Header) allHeaders.nextElement();
                if (headerIsRequired(header)) {
                    buf.append(processHeader(header));
                }
            }
        }
    }

    private boolean headerIsRequired(final Header header) {
        for (int i = 0; i < fTokenizedHeaders.length; i++) {
            if (fTokenizedHeaders[i].equalsIgnoreCase(header.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String processHeader(final Header h) {
        return "    <header name=\"" + h.getName() + "\" value=\"" + h.getValue() + "\"/>" + LS;
    }

    private void filterSimpleMessage(final String content, final Message message) throws MessagingException {
        final StringBuffer buf = new StringBuffer("<message type=\"Simple\" contentType=\"");
        buf.append(extractBaseContentType(message.getContentType())).append("\">").append(LS);
        processHeaders(buf, message);
        appendUuencodedAttachments(buf, content);
        buf.append("</message>");
        defineAsCurrentResponse(buf.toString(), "text/xml");
    }

    private static void appendUuencodedAttachments(final StringBuffer buf, final String content) {
        // iterate over string looking for ^begin ddd$
        final String lineStr = "(^.*$)";
        final String startUuencodeStr = "begin \\d\\d\\d .*";
        final Pattern linePattern = Pattern.compile(lineStr, Pattern.MULTILINE);
        final Matcher matcher = linePattern.matcher(content);
        while (matcher.find()) {
            final String line = matcher.group(0);
            if (line.matches(startUuencodeStr)) {
                final int lastSpace = line.lastIndexOf(" ");
                final String filename = line.substring(lastSpace + 1);
                buf.append("    <part type=\"uuencoded\" filename=\"");
                buf.append(filename).append("\"/>").append(LS);
            }
        }
    }

    private static String processMessagePart(final Part part) throws MessagingException {
        final String disp = part.getDisposition();
        final String contentType = part.getContentType();
        if (Part.ATTACHMENT.equals(disp)) {
            return "    <part type=\"attachment\" filename=\"" + part.getFileName() + "\" contentType=\"" + extractBaseContentType(contentType) + "\"/>" + LS;
        }
        return "    <part type=\"inline\" contentType=\"" + extractBaseContentType(contentType) + "\"/>" + LS;
    }

    private static String extractBaseContentType(final String orig) {
        final int colonStart = orig.indexOf(";");
        if (colonStart == -1) {
            return orig;
        } else {
            return orig.substring(0, colonStart);
        }
    }

}
