package com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox;

import org.apache.pdfbox.pdmodel.font.PDFont;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFFont;

/**
 * Implementation of {@link PDFFont} based on PDFBox.
 * @author Marc Guillemot
 */
public class PDFBoxPDFFont implements PDFFont {
	private final PDFont font_;
    private int page_;

    public PDFBoxPDFFont(final PDFont font, final int page) 
    {
    	if (font == null)
    		throw new NullPointerException("Font can't be null!");
    	font_ = font;
    	page_ = page;
    }

    public String getName() {
        return getWrappedFont().getBaseFont();
    }

    public String getType() {
        return getWrappedFont().getSubType();
    }

    /**
     * Gets the real font object wrapped by this instance
     * @return the wrapped font
     */
    public PDFont getWrappedFont()
    {
    	return font_;
    }

    public int getPage() {
        return page_;
    }

    public String toString() {
        return "PDFBoxPDFFont[name=" + getName() + ", type=" + getType() + ", page=" + getPage() + "]";
    }
}
