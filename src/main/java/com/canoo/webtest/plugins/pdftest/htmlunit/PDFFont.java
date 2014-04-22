package com.canoo.webtest.plugins.pdftest.htmlunit;


/**
 * Represents a font in a PDF document.
 * @author Paul King
 * @author Marc Guillemot
 */
public interface PDFFont
{
    String getName();
    String getType();
    int getPage();
}
