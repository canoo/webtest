// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains mapping between mime types and preferred extension.
 * This is used when saving received content to determine file extension from the received mime type.
 * @author Marc Guillemot
 * @author Paul King
 */
public class MimeMap
{

    // Defaults - all of them are "well-known" types,

    private static final Map<String, String> DEFAULT_MAP = new HashMap<String, String>(101);
    public static final String EXCEL_MIME_TYPE = "application/vnd.ms-excel";
    public static final String UNKNOWN_BINARY_MIME_TYPE = "application/octet-stream";

    static {
        DEFAULT_MAP.put("application/java", "class");
        DEFAULT_MAP.put("application/mac-binhex40", "hqx");
        DEFAULT_MAP.put("application/octet-stream", "bin");
        DEFAULT_MAP.put("application/oda", "oda");
        DEFAULT_MAP.put("application/pdf", "pdf");
        DEFAULT_MAP.put("application/vnd.ms-excel", "xls");
        DEFAULT_MAP.put("application/excel", "xls");
        DEFAULT_MAP.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        DEFAULT_MAP.put("application/vnd.oasis.opendocument.text", "odt");
        DEFAULT_MAP.put("application/vnd.oasis.opendocument.spreadsheet", "ods");
        DEFAULT_MAP.put("application/vnd.google-earth.kml+xml", "kml");
        DEFAULT_MAP.put("application/postscript", "ps");
        DEFAULT_MAP.put("application/rtf", "rtf");
        DEFAULT_MAP.put("application/xhtml+xml", "html");
        DEFAULT_MAP.put("application/xml", "xml");
        DEFAULT_MAP.put("application/x-bcpio", "bcpio");
        DEFAULT_MAP.put("application/x-compress", "Z");
        DEFAULT_MAP.put("application/x-cpio", "cpio");
        DEFAULT_MAP.put("application/x-csh", "csh");
        DEFAULT_MAP.put("application/x-dvi", "dvi");
        DEFAULT_MAP.put("application/x-gtar", "gtar");
        DEFAULT_MAP.put("application/x-gzip", "gz");
        DEFAULT_MAP.put("application/x-hdf", "hdf");
        DEFAULT_MAP.put("application/x-javascript", "js");
        DEFAULT_MAP.put("application/x-latex", "latex");
        DEFAULT_MAP.put("application/x-mif", "mif");
        DEFAULT_MAP.put("application/x-netcdf", "cdf");
        DEFAULT_MAP.put("application/x-postscript", "ps");
        DEFAULT_MAP.put("application/x-sh", "sh");
        DEFAULT_MAP.put("application/x-shar", "shar");
        DEFAULT_MAP.put("application/x-shockwave-flash", "swf");
        DEFAULT_MAP.put("application/x-sv4cpio", "sv4cpio");
        DEFAULT_MAP.put("application/x-sv4crc", "sv4crc");
        DEFAULT_MAP.put("application/x-tar", "tar");
        DEFAULT_MAP.put("application/x-tcl", "tcl");
        DEFAULT_MAP.put("application/x-tex", "tex");
        DEFAULT_MAP.put("application/x-texinfo", "texinfo");
        DEFAULT_MAP.put("application/x-troff", "t");
        DEFAULT_MAP.put("application/x-troff-man", "man");
        DEFAULT_MAP.put("application/x-troff-me", "me");
        DEFAULT_MAP.put("application/x-ustar", "ustar");
        DEFAULT_MAP.put("application/x-wais-source", "ms");
        DEFAULT_MAP.put("application/x-x509-ca-cert", "cer");
        DEFAULT_MAP.put("application/zip", "zip");
        DEFAULT_MAP.put("audio/basic", "au");
        DEFAULT_MAP.put("audio/x-aiff", "aif");
        DEFAULT_MAP.put("audio/x-aiff", "aifc");
        DEFAULT_MAP.put("audio/x-aiff", "aiff");
        DEFAULT_MAP.put("audio/x-wav", "wav");
        DEFAULT_MAP.put("text/html", "html");
        DEFAULT_MAP.put("text/xhtml", "html");
        DEFAULT_MAP.put("text/xml", "xml");
        DEFAULT_MAP.put("text/javascript", "js");
        DEFAULT_MAP.put("image/gif", "gif");
        DEFAULT_MAP.put("image/ief", "ief");
        DEFAULT_MAP.put("image/jpeg", "jpg");
        DEFAULT_MAP.put("image/tiff", "tif");
        DEFAULT_MAP.put("image/x-cmu-raster", "ras");
        DEFAULT_MAP.put("image/x-portable-anymap", "pnm");
        DEFAULT_MAP.put("image/x-portable-bitmap", "pbm");
        DEFAULT_MAP.put("image/x-portable-graymap", "pgm");
        DEFAULT_MAP.put("image/x-portable-pixmap", "ppm");
        DEFAULT_MAP.put("image/x-rgb", "rgb");
        DEFAULT_MAP.put("image/x-xbitmap", "xbm");
        DEFAULT_MAP.put("image/x-xpixmap", "xpm");
        DEFAULT_MAP.put("image/x-xwindowdump", "xwd");
        DEFAULT_MAP.put("text/css", "css");
        // text/plain not needed (see default for text below)
        DEFAULT_MAP.put("text/richtext", "rtx");
        DEFAULT_MAP.put("text/tab-separated-values", "tsv");
        DEFAULT_MAP.put("text/x-java", "java");
        DEFAULT_MAP.put("text/x-setext", "etx");
        DEFAULT_MAP.put("video/mpeg", "mpeg");
        DEFAULT_MAP.put("video/mpeg2", "mpv2");
        DEFAULT_MAP.put("video/quicktime", "mov");
        DEFAULT_MAP.put("video/x-msvideo", "avi");
        DEFAULT_MAP.put("video/x-rad-screenplay", "avx");
        DEFAULT_MAP.put("video/x-sgi-movie", "movie");
        DEFAULT_MAP.put("x-world/x-vrml", "wrl");
    }

    // Mime Types should roughly be consistent with HtmlUnit
    public static String getExtension(final String contentType) {
        if (DEFAULT_MAP.containsKey(contentType)) {
            return (String) DEFAULT_MAP.get(contentType);
        }
        if (contentType.matches("application/.*xhtml\\+xml")) {
            return "html";
        }
        if (contentType.matches("application/.*\\+xml")) {
            return "xml";
        }
        if (contentType.startsWith("text/")) {
            return "txt";
        }
        return "unknown";
    }

    /**
     * HtmlUnit doesn't guess contentType of Excel files from the file system.
     * Should probably be moved to htmlunit.
     */
    public static String adjustMimeTypeIfNeeded(final String contentType, final String responseFileName) {
        if (UNKNOWN_BINARY_MIME_TYPE.equals(contentType)) {
            if (responseFileName != null && responseFileName.endsWith(".xls")) {
                return EXCEL_MIME_TYPE;
            }
        }
        return contentType;
    }
}
