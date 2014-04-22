// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.boundary.FileBoundary;

import java.io.File;

/**
 * @author Etienne Studer
 */
public abstract class PdfTestResources
{
    public static final File DEFAULT_FILE = FileBoundary.getFile("/testDocForms.pdf", PdfTestResources.class);
    public static final File ENCRYPTED_FILE = FileBoundary.getFile("/testDocFormsSecure.pdf", PdfTestResources.class);
    public static final File UTF16_FILE = FileBoundary.getFile("/testDocUtf16.pdf", PdfTestResources.class);
}
