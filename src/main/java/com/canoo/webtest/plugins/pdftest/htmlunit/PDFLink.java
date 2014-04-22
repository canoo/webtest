package com.canoo.webtest.plugins.pdftest.htmlunit;

/**
 * Represents a link in a PDF document.
 * @author Paul King
 * @author Marc Guillemot
 */
public interface PDFLink {
    String getText();
    String getHref();
    int getPage();
}
