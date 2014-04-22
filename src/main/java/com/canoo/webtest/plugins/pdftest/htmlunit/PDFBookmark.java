package com.canoo.webtest.plugins.pdftest.htmlunit;

import java.util.List;

/**
 * Represent a bookmark in a PDF document.
 * @author Marc Guillemot
 */
public interface PDFBookmark 
{
	String getTitle();	

	/**
	 * Get all children deep first
	 * @return a list of {@link PDFBookmark}
	 */
	List<? extends PDFBookmark> getAllChildren();

	/**
	 * Gets the parent bookmark
	 * @return the parent bookmark
	 */
	PDFBookmark getParent();

	/**
	 * Get the direct children of this bookmark
	 * @return a list of {@link PDFBookmark}
	 */
	List<? extends PDFBookmark> getchildren();
}
