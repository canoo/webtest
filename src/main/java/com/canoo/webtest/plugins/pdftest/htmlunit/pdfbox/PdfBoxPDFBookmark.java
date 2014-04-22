package com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFBookmark;

/**
 * Implementation of {@link PDFBookmark} based on 
 * <a href="http://www.pdfbox.org/">PDFBox</a>. 
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfBoxPDFBookmark implements PDFBookmark
{
	private final PDOutlineItem nativeBookmark_;
	private final PdfBoxPDFBookmark parent_;
	private final List<PdfBoxPDFBookmark> children_;
	
	public PdfBoxPDFBookmark(final PDOutlineItem nativeBookmark, final PdfBoxPDFBookmark parent)
	{
		nativeBookmark_ = nativeBookmark;
		parent_ = parent;
		children_ = readChildren();
	}

	protected List<PdfBoxPDFBookmark> readChildren() {
		final List<PdfBoxPDFBookmark> children = new ArrayList<PdfBoxPDFBookmark>();
    	PDOutlineItem nativeChild = nativeBookmark_.getFirstChild();
        while (nativeChild != null) {
        	children.add(new PdfBoxPDFBookmark(nativeChild, this));
            nativeChild = nativeChild.getNextSibling();
        }

		return children;
	}

	public String getTitle() 
	{
		return nativeBookmark_.getTitle();
	}
	
	public PDFBookmark getParent()
	{
		return parent_;
	}

	public List<PdfBoxPDFBookmark> getchildren()
	{
		return children_;
	}

	/**
	 * Get all children deep first
	 * @return a list of {@link PDFBookmark}
	 */
	public List<PdfBoxPDFBookmark> getAllChildren() 
	{
		final List<PdfBoxPDFBookmark> results = new ArrayList<PdfBoxPDFBookmark>();
		for (final Iterator iter = getchildren().iterator(); iter.hasNext();) {
			final PdfBoxPDFBookmark child = (PdfBoxPDFBookmark) iter.next();
			results.add(child);
			results.addAll(child.getAllChildren());
		}
		return results;
	}
}
