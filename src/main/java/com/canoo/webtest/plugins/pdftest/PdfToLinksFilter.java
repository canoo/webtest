// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFLink;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * Extracts all links from within the current PDF document.
 *
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step category="PDF"
 * name="pdfToLinksFilter"
 * description="Extracts all links from within the current <key>pdf</key> document."
 */
public class PdfToLinksFilter extends AbstractPdfFilter
{
    private static final Logger LOG = Logger.getLogger(PdfToLinksFilter.class);

    public void doFilter(final PDFPage pdfPage)
    {
        final StringBuffer buf = new StringBuffer();
        final List links = pdfPage.getLinks();
        final String lineSep = System.getProperty("line.separator");
        
        LOG.debug(links.size() + " links found");
        for (final Iterator iter = links.iterator(); iter.hasNext();) 
        {
			final PDFLink link = (PDFLink) iter.next();
            buf.append(link.getPage()).append("|");
            buf.append(link.getText()).append("|");
            buf.append(link.getHref()).append(lineSep);
        }
        
        LOG.debug("Defining current response with: " + buf.toString());
        ContextHelper.defineAsCurrentResponse(getContext(), buf.toString(), "text/plain", getClass());
    }
}
