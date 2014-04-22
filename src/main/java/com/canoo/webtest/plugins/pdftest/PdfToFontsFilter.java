// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFFont;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * Extracts all fonts from within the current PDF document.
 *
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step category="PDF"
 * name="pdfToFontsFilter"
 * description="Extracts all font information from within the current <key>pdf</key> document."
 */
public class PdfToFontsFilter extends AbstractPdfFilter
{
	protected void doFilter(final PDFPage pdfPage) 
	{
		final String content = fontsToString(pdfPage.getFonts());
        ContextHelper.defineAsCurrentResponse(getContext(), content, "text/plain", getClass());
    }

	static String fontsToString(final List _fonts)
	{
		final List strings = new ArrayList();
        for (int i = 0; i < _fonts.size(); i++) {
            final PDFFont font = (PDFFont) _fonts.get(i);
            strings.add(font.getPage() + "|" + font.getType() + "|" + font.getName());
        }
        Collections.sort(strings);

        final String lineSep = System.getProperty("line.separator");
        return StringUtils.join(strings, lineSep);
	}
}
