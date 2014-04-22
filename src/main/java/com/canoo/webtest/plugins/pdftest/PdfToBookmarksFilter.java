// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.Iterator;

import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFBookmark;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * Extracts all bookmarks from within the current PDF document.
 * 
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step category="PDF" name="pdfToBookmarksFilter"
 *               description="Extracts all bookmarks from within the current
 *               <key>pdf</key> document."
 */
public class PdfToBookmarksFilter extends AbstractPdfFilter {

	protected void doFilter(final PDFPage pdfPage) {
		final String bookmarks = extractBookmarkAsString(pdfPage);
		ContextHelper.defineAsCurrentResponse(getContext(), bookmarks,
				"text/plain", getClass());
	}

	private String extractBookmarkAsString(final PDFPage pdfPage) {
		StringBuffer result = new StringBuffer();
		for (final Iterator iter = pdfPage.getBookmarks().iterator(); iter
				.hasNext();) {
			final PDFBookmark bookmark = (PDFBookmark) iter.next();
			if (bookmark.getParent() == null)
				extractBookmarkAsString(result, bookmark, "");
		}
		return result.toString();
	}

	private static void extractBookmarkAsString(final StringBuffer result,
			final PDFBookmark bookmark, final String indentation) {
		result.append(indentation);
		result.append(bookmark.getTitle());
		result.append("\n");
		for (final Iterator iter = bookmark.getchildren().iterator(); iter
				.hasNext();) {
			final PDFBookmark child = (PDFBookmark) iter.next();
			extractBookmarkAsString(result, child, indentation + "    ");
		}
	}
}
